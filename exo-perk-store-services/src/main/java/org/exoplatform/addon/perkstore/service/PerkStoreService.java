/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.addon.perkstore.service;

import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.ws.frameworks.json.impl.JsonException;

/**
 * A service to manage kudos
 */
public class PerkStoreService implements Startable {
  private static final Log LOG = ExoLogger.getLogger(PerkStoreService.class);

  private IdentityManager  identityManager;

  private SpaceService     spaceService;

  private ListenerService  listenerService;

  private PerkStoreStorage perkStoreStorage;

  private SettingService   settingService;

  private GlobalSettings   storedGlobalSettings;

  public PerkStoreService(PerkStoreStorage perkStoreStorage,
                          SettingService settingService,
                          SpaceService spaceService,
                          IdentityManager identityManager,
                          ListenerService listenerService) {
    this.perkStoreStorage = perkStoreStorage;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.settingService = settingService;
    this.listenerService = listenerService;
  }

  @Override
  public void start() {
    try {
      this.storedGlobalSettings = loadGlobalSettings();
    } catch (JsonException e) {
      LOG.error("Error when loading global settings", e);
    }
  }

  @Override
  public void stop() {
    // Nothing to shutdown
  }

  public void saveGlobalSettings(GlobalSettings settings) {
    settingService.set(PERKSTORE_CONTEXT, PERKSTORE_SCOPE, SETTINGS_KEY_NAME, SettingValue.create(transformToString(settings)));
    this.storedGlobalSettings = null;
  }

  public GlobalSettings getGlobalSettings() throws JsonException {
    if (this.storedGlobalSettings == null) {
      this.storedGlobalSettings = loadGlobalSettings();
    }
    return this.storedGlobalSettings;
  }

  private GlobalSettings loadGlobalSettings() throws JsonException {
    SettingValue<?> globalSettingsValue = settingService.get(PERKSTORE_CONTEXT, PERKSTORE_SCOPE, SETTINGS_KEY_NAME);
    if (globalSettingsValue == null || StringUtils.isBlank(globalSettingsValue.getValue().toString())) {
      return new GlobalSettings();
    } else {
      return fromString(GlobalSettings.class, globalSettingsValue.getValue().toString());
    }
  }

  public Product getProductById(long productId) {
    return perkStoreStorage.getProductById(productId);
  }

  public boolean canEdit(long productId, String currentUsername) {
    if (productId == 0) {
      return true;
    }
    Product product = getProductById(productId);
    return canEdit(product, currentUsername);
  }

  public boolean canEdit(Product product, String currentUsername) {
    if (product.getId() == 0) {
      return true;
    }

    List<Profile> marchands = product.getMarchands();
    for (Profile profile : marchands) {
      if (profile == null) {
        continue;
      }

      if (USER_ACCOUNT_TYPE.equals(profile.getType())) {
        if (profile.getId().equals(currentUsername)) {
          return true;
        }
      } else {
        Space space = getSpace(profile.getId());
        if (space == null) {
          LOG.warn("Can't check identity permission on space '{}' because the space wasn't found", profile.getId());
        } else if (spaceService.isMember(space, currentUsername)) {
          return true;
        }
      }
    }
    return false;
  }

  public void saveProduct(String currentUserId, Product product) throws IllegalAccessException {
    if (!canEdit(product, getCurrentUserId()) || !canEdit(product.getId(), getCurrentUserId())) {
      throw new IllegalAccessException("User " + currentUserId + " isn't alowed to modify product " + product.getTitle());
    }

    perkStoreStorage.saveProduct(currentUserId, product);
  }

  public List<Product> getAllProducts(String currentUserId) {
    List<Product> products = perkStoreStorage.getAllProducts();
    for (Product product : products) {
      computeFields(currentUserId, product);
    }
    return products;
  }

  public double countPurchasedQuantityInCurrentPeriod(Product product, long identityId) {
    double purchasedQuantityInPeriod = 0;
    if (StringUtils.isNotBlank(product.getOrderPeriodicity())) {
      ProductOrderPeriodType periodType = ProductOrderPeriodType.valueOf(product.getOrderPeriodicity());
      ProductOrderPeriod period = periodType.getPeriodOfTime(LocalDateTime.now());
      purchasedQuantityInPeriod = perkStoreStorage.countUserPurchasedQuantityInPeriod(product.getId(),
                                                                                      identityId,
                                                                                      period.getStartDate(),
                                                                                      period.getEndDate());
    }
    return purchasedQuantityInPeriod;
  }

  private void computeFields(String currentUserId, Product product) {
    product.setCanEdit(StringUtils.isNotBlank(currentUserId) && canEdit(product, currentUserId));
    // Retrieve the following fields for not marchand only
    if (product.getReceiverMarchand() != null && !StringUtils.equals(product.getReceiverMarchand().getId(), currentUserId)) {
      long productId = product.getId();
      product.setPurchased(perkStoreStorage.countOrderedQuantity(productId));
      product.setNotProcessedOrders(perkStoreStorage.countRemainingOrders(productId));

      UserOrders userOrders = new UserOrders();
      product.setUserOrders(userOrders);
      Identity identity = getIdentityByTypeAndId(USER_ACCOUNT_TYPE, currentUserId);
      long identityId = Long.parseLong(identity.getId());
      userOrders.setTotalOrders(perkStoreStorage.countUserTotalPurchasedQuantity(productId, identityId));

      double purchasedQuantityInPeriod = countPurchasedQuantityInCurrentPeriod(product, identityId);
      userOrders.setPurchasedInCurrentPeriod(purchasedQuantityInPeriod);
    }
  }

}
