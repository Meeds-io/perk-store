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

import static org.exoplatform.addon.perkstore.model.PerkStoreError.*;
import static org.exoplatform.addon.perkstore.model.ProductOrderStatus.*;
import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.ws.frameworks.json.impl.JsonException;

/**
 * A service to manage perkstore entities
 */
public class PerkStoreService implements Startable {
  private static final Log LOG = ExoLogger.getLogger(PerkStoreService.class);

  private PerkStoreStorage perkStoreStorage;

  private SettingService   settingService;

  private GlobalSettings   storedGlobalSettings;

  public PerkStoreService(PerkStoreStorage perkStoreStorage) {
    this.perkStoreStorage = perkStoreStorage;
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

  public void saveGlobalSettings(GlobalSettings settings, String username) throws Exception {
    GlobalSettings globalSettings = getGlobalSettings();
    if (globalSettings == null || globalSettings.getAccessPermissions() == null && !isUserAdmin(username)) {
      throw new PerkStoreException(GLOBAL_SETTINGS_MODIFICATION_DENIED, username);
    }
    getSettingService().set(PERKSTORE_CONTEXT,
                            PERKSTORE_SCOPE,
                            SETTINGS_KEY_NAME,
                            SettingValue.create(transformToString(settings)));
    this.storedGlobalSettings = null;
  }

  public GlobalSettings getGlobalSettings(String username) throws Exception {
    GlobalSettings globalSettings = getGlobalSettings();
    if (globalSettings == null) {
      globalSettings = new GlobalSettings();
    } else if (!canAccessApplication(globalSettings, username)) {
      throw new PerkStoreException(GLOBAL_SETTINGS_ACCESS_DENIED, username);
    }
    globalSettings.setCanAddProduct(canAddProduct(username));
    globalSettings.setAdministrator(isUserAdmin(username));
    if (!globalSettings.isAdministrator()) {
      // Delete useless information for normal user
      globalSettings.setAccessPermissions(null);
      globalSettings.setProductCreationPermissions(null);
    }
    return globalSettings;
  }

  public void saveProduct(String username, Product product) throws Exception {
    if (product.getId() == 0) {
      checkCanAddProduct(username);
    }

    if (!canEditProduct(product, username)) {
      throw new PerkStoreException(PRODUCT_MODIFICATION_DENIED, username, product.getTitle());
    }

    // Make sure to store only allowed fields to change
    Product productToStore = null;
    if (product.getId() == 0) {
      productToStore = new Product();
    } else {
      productToStore = perkStoreStorage.getProductById(product.getId());
    }

    productToStore.setTitle(product.getTitle());
    productToStore.setIllustrationURL(product.getIllustrationURL());
    productToStore.setDescription(product.getDescription());
    productToStore.setReceiverMarchand(product.getReceiverMarchand());
    productToStore.setAccessPermissions(product.getAccessPermissions());
    productToStore.setMarchands(product.getMarchands());
    productToStore.setEnabled(product.isEnabled());
    productToStore.setUnlimited(product.isUnlimited());
    productToStore.setPrice(product.getPrice());
    productToStore.setMaxOrdersPerUser(product.getMaxOrdersPerUser());
    productToStore.setOrderPeriodicity(product.getOrderPeriodicity());
    productToStore.setTotalSupply(product.getTotalSupply());

    perkStoreStorage.saveProduct(username, productToStore);
  }

  public List<Product> getProducts(String username) throws Exception {
    List<Product> products = perkStoreStorage.getAllProducts();
    if (products == null || products.isEmpty()) {
      return Collections.emptyList();
    }
    boolean canAddProduct = canAddProduct(username);
    Iterator<Product> productsIterator = products.iterator();
    while (productsIterator.hasNext()) {
      Product product = productsIterator.next();
      if (canViewProduct(product, username, canAddProduct)) {
        computeProductFields(username, product);
      } else {
        productsIterator.remove();
      }
    }
    return products;
  }

  public List<ProductOrder> getOrders(String username, OrderFilter filter) throws Exception {
    if (filter == null) {
      throw new IllegalArgumentException("Filter is mandatory");
    }
    if (StringUtils.isBlank(username)) {
      String currentUserId = getCurrentUserId();
      if (StringUtils.isBlank(username) || !canAddProduct(currentUserId)) {
        throw new IllegalAccessException(currentUserId + " is attempting to access orders list with filter: " + filter);
      }
    }
    if (canEditProduct(filter.getProductId(), username)) {
      return perkStoreStorage.getOrders(null, filter);
    } else {
      return perkStoreStorage.getOrders(username, filter);
    }
  }

  public void checkCanOrder(String username, ProductOrder order) throws PerkStoreException {
    if (order == null) {
      throw new IllegalArgumentException("Order is mandatory");
    }
    if (order.getId() != 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }
    if (order.getProductId() == 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }
    Product product = getProduct(order.getProductId());

    order.setSender(toProfile(USER_ACCOUNT_TYPE, username));

    checkOrderCoherence(username, product, order);
  }

  public void createOrder(String username, ProductOrder order) throws PerkStoreException {
    if (order == null) {
      throw new IllegalArgumentException("Order is null");
    }
    if (order.getId() != 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }
    if (order.getProductId() == 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }

    Product product = getProduct(order.getProductId());

    Profile sender = toProfile(USER_ACCOUNT_TYPE, username);
    order.setSender(sender);

    checkOrderCoherence(username, product, order);

    // Create new instance to avoid injecting values from front end
    ProductOrder productOrder = new ProductOrder();
    productOrder.setProductId(order.getProductId());
    productOrder.setAmount(order.getQuantity() * product.getPrice());
    productOrder.setReceiver(order.getReceiver());
    productOrder.setSender(sender);
    productOrder.setTransactionHash(order.getTransactionHash());
    productOrder.setQuantity(order.getQuantity());
    productOrder.setStatus(ORDERED.name());
    productOrder.setRemainingQuantityToProcess(order.getQuantity());
    productOrder.setCreatedDate(System.currentTimeMillis());

    perkStoreStorage.saveOrder(productOrder);
  }

  public void saveOrderPaymentStatus(String hash, boolean transactionSuccess) throws PerkStoreException {
    if (StringUtils.isBlank(hash)) {
      throw new IllegalArgumentException("Transaction hash is mandatory");
    }
    ProductOrder order = perkStoreStorage.findOrderByTransactionHash(hash);
    if (order == null) {
      // Transaction doesn't correspond to a known product order
      return;
    }

    if (transactionSuccess) {
      order.setStatus(PAYED.name());
    } else {
      order.setStatus(ERROR.name());
      order.setError("Transaction failed");
    }

    perkStoreStorage.saveOrder(order);
  }

  public ProductOrder getOrderById(long orderId) {
    return perkStoreStorage.getOrder(orderId);
  }

  public void saveOrderStatus(String username, ProductOrder order) throws Exception {
    if (order == null) {
      throw new IllegalArgumentException("Order is null");
    }
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username is null");
    }
    if (order.getProductId() == 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }

    long orderId = order.getId();
    if (orderId == 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }

    if (!canEditProduct(order.getProductId(), username)) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }

    // Create new instance to avoid injecting annoying values
    ProductOrder productOrder = perkStoreStorage.getOrder(orderId);
    if (productOrder == null) {
      throw new PerkStoreException(ORDER_NOT_EXISTS, username, orderId);
    }

    double deliveredQuantity = order.getDeliveredQuantity();
    double refundedQuantity = order.getRefundedQuantity();
    String status = order.getStatus();

    ProductOrderStatus oldStatus = ProductOrderStatus.valueOf(productOrder.getStatus());
    ProductOrderStatus newStatus = ProductOrderStatus.valueOf(status);

    productOrder.setStatus(status);
    productOrder.setDeliveredQuantity(deliveredQuantity);
    productOrder.setRefundedQuantity(refundedQuantity);
    double remainingQuantityToProcess = productOrder.getQuantity() - refundedQuantity
        - deliveredQuantity;
    if (remainingQuantityToProcess < 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_QUANTITY_INVALID_REMAINING,
                                   remainingQuantityToProcess,
                                   productOrder.getId());
    }
    productOrder.setRemainingQuantityToProcess(remainingQuantityToProcess);
    if (oldStatus != newStatus) {
      if ((oldStatus == DELIVERED || deliveredQuantity > 0) && productOrder.getDeliveredDate() == 0) {
        productOrder.setDeliveredDate(System.currentTimeMillis());
      } else if ((oldStatus == REFUNDED || refundedQuantity > 0)
          && productOrder.getRefundedDate() == 0) {
        productOrder.setRefundedDate(System.currentTimeMillis());
      }
    }
    if (deliveredQuantity == 0) {
      productOrder.setDeliveredDate(0);
    }
    if (refundedQuantity == 0) {
      productOrder.setRefundedDate(0);
    }
    perkStoreStorage.saveOrder(productOrder);
  }

  private GlobalSettings loadGlobalSettings() throws JsonException {
    SettingValue<?> globalSettingsValue = getSettingService().get(PERKSTORE_CONTEXT, PERKSTORE_SCOPE, SETTINGS_KEY_NAME);
    if (globalSettingsValue == null || StringUtils.isBlank(globalSettingsValue.getValue().toString())) {
      return new GlobalSettings();
    } else {
      return fromString(GlobalSettings.class, globalSettingsValue.getValue().toString());
    }
  }

  private void computeProductFields(String username, Product product) throws Exception {
    product.setCanEdit(StringUtils.isNotBlank(username) && canEditProduct(product, username));
    product.setCanOrder(StringUtils.isNotBlank(username) && canViewProduct(product, username, false));
    long productId = product.getId();

    product.setPurchased(perkStoreStorage.countOrderedQuantity(productId));
    product.setNotProcessedOrders(perkStoreStorage.countRemainingOrdersToProcess(productId));

    // Retrieve the following fields for not marchand only
    if (product.getReceiverMarchand() != null && !StringUtils.equals(product.getReceiverMarchand().getId(), username)) {

      UserOrders userOrders = new UserOrders();
      product.setUserOrders(userOrders);
      Identity identity = getIdentityByTypeAndId(USER_ACCOUNT_TYPE, username);
      long identityId = Long.parseLong(identity.getId());
      userOrders.setTotalPuchased(perkStoreStorage.countUserTotalPurchasedQuantity(productId, identityId));

      double purchasedQuantityInPeriod = countPurchasedQuantityInCurrentPeriod(product, identityId);
      userOrders.setPurchasedInCurrentPeriod(purchasedQuantityInPeriod);
    }
  }

  private double countPurchasedQuantityInCurrentPeriod(Product product, long identityId) {
    if (StringUtils.isBlank(product.getOrderPeriodicity())) {
      return 0;
    }
    ProductOrderPeriodType periodType = ProductOrderPeriodType.valueOf(product.getOrderPeriodicity().toUpperCase());
    ProductOrderPeriod period = periodType.getPeriodOfTime(LocalDateTime.now());
    return perkStoreStorage.countUserPurchasedQuantityInPeriod(product.getId(),
                                                               identityId,
                                                               period.getStartDate(),
                                                               period.getEndDate());
  }

  private void checkOrderCoherence(String username, Product product, ProductOrder order) throws PerkStoreException {
    if (StringUtils.isBlank(username)) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }
    if (order.getId() != 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }
    if (order.getProductId() == 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }
    if (product == null) {
      throw new PerkStoreException(PRODUCT_NOT_EXISTS, order.getProductId());
    }
    if (order.getStatus() != null) {
      throw new PerkStoreException(ORDER_CREATION_STATUS_DENIED);
    }
    if (StringUtils.isBlank(order.getTransactionHash())) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_TX);
    }
    if (order.getQuantity() <= 0) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_QUANTITY);
    }
    if (order.getReceiver() == null) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_RECEIVER);
    }
    if (order.getSender() == null) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_SENDER);
    }

    if (!canViewProduct(product, username, false)) {
      throw new PerkStoreException(ORDER_CREATION_DENIED, username, product.getTitle());
    }

    checkOrderQuantity(product, order);
  }

  private void checkOrderQuantity(Product product, ProductOrder productOrder) throws PerkStoreException {
    double quantity = productOrder.getQuantity();
    long productId = productOrder.getProductId();

    Profile sender = productOrder.getSender();
    String username = sender.getId();
    long identityId = sender.getTechnicalId();

    // check availability
    if (!product.isUnlimited()) {
      double orderedQuantity = perkStoreStorage.countOrderedQuantity(productId);
      double totalSupply = product.getTotalSupply();
      if ((orderedQuantity + quantity) > totalSupply) {
        throw new PerkStoreException(ORDER_CREATION_QUANTITY_EXCEEDS_SUPPLY, username);
      }
    }

    // check max user orders
    double maxOrdersPerUser = product.getMaxOrdersPerUser();
    if (maxOrdersPerUser > 0) {
      double purchasedQuantity = 0;
      if (product.getOrderPeriodicity() != null) {
        // user purchased orders per period
        purchasedQuantity = countPurchasedQuantityInCurrentPeriod(product, identityId);
      } else {
        // user purchased orders all time
        purchasedQuantity = perkStoreStorage.countUserTotalPurchasedQuantity(productId, identityId);
      }

      if ((purchasedQuantity + quantity) > maxOrdersPerUser) {
        throw new PerkStoreException(ORDER_CREATION_QUANTITY_EXCEEDS_ALLOWED, username);
      }
    }
  }

  private Product getProduct(long productId) {
    return perkStoreStorage.getProductById(productId);
  }

  private void checkCanAddProduct(String username) throws Exception {
    if (!canAddProduct(username)) {
      throw new PerkStoreException(PerkStoreError.PRODUCT_CREATION_DENIED, username);
    }
  }

  private boolean canAccessApplication(GlobalSettings globalSettings, String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      return false;
    }

    if (globalSettings == null) {
      return true;
    }

    List<String> accessPermissions = globalSettings.getAccessPermissions();

    if (accessPermissions == null) {
      // No permissions check to do
      return true;
    }

    return hasPermission(username, accessPermissions);
  }

  private boolean canAddProduct(String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      return false;
    }

    GlobalSettings globalSettings = getGlobalSettings();
    if (globalSettings == null) {
      return true;
    }

    List<String> productCreationPermissions = globalSettings.getProductCreationPermissions();
    List<String> accessPermissions = globalSettings.getAccessPermissions();

    if (productCreationPermissions == null && accessPermissions == null) {
      // No permissions check to do
      return true;
    }

    return hasPermission(username, productCreationPermissions) && hasPermission(username, accessPermissions);
  }

  private boolean canEditProduct(long productId, String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      return false;
    }

    Product product = getProduct(productId);
    return canEditProduct(product, username);
  }

  private boolean canEditProduct(Product product, String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      return false;
    }

    if (product.getId() == 0) {
      return true;
    }

    List<Profile> marchands = product.getMarchands();
    if (marchands == null || marchands.isEmpty()) {
      return canAddProduct(username);
    }

    return isUserMemberOf(username, marchands);
  }

  private boolean canViewProduct(Product product, String username, boolean canAddProduct) {
    if (canAddProduct) {
      return true;
    }

    if (StringUtils.isBlank(username)) {
      return false;
    }

    List<Profile> accessPermissions = product.getAccessPermissions();
    if (accessPermissions == null || accessPermissions.isEmpty()) {
      return true;
    }

    return isUserMemberOf(username, accessPermissions);
  }

  private GlobalSettings getGlobalSettings() throws JsonException {
    if (this.storedGlobalSettings == null) {
      this.storedGlobalSettings = loadGlobalSettings();
    }
    return this.storedGlobalSettings.clone();
  }

  private SettingService getSettingService() {
    if (settingService == null) {
      settingService = CommonsUtils.getService(SettingService.class);
    }
    return settingService;
  }

}
