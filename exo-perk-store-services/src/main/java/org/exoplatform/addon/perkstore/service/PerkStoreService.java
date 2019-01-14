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
import org.exoplatform.services.listener.ListenerService;
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

  private ListenerService  listenerService;

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
    if (globalSettings == null || globalSettings.getAccessPermissions() == null && !isPerkStoreManager(username)) {
      throw new PerkStoreException(GLOBAL_SETTINGS_MODIFICATION_DENIED, username);
    }

    List<Profile> permissionsProfiles = settings.getAccessPermissionsProfiles();
    List<Long> permissions = new ArrayList<>();
    settings.setAccessPermissions(permissions);

    addIdentityIdsFromProfiles(permissionsProfiles, permissions);

    permissionsProfiles = settings.getManagersProfiles();
    permissions = new ArrayList<>();
    settings.setManagers(permissions);

    addIdentityIdsFromProfiles(permissionsProfiles, permissions);

    permissionsProfiles = settings.getProductCreationPermissionsProfiles();
    permissions = new ArrayList<>();
    settings.setProductCreationPermissions(permissions);

    addIdentityIdsFromProfiles(permissionsProfiles, permissions);

    getSettingService().set(PERKSTORE_CONTEXT,
                            PERKSTORE_SCOPE,
                            SETTINGS_KEY_NAME,
                            SettingValue.create(transformToString(settings)));
    this.storedGlobalSettings = null;
  }

  public GlobalSettings getGlobalSettings() throws JsonException {
    if (this.storedGlobalSettings == null) {
      this.storedGlobalSettings = loadGlobalSettings();
      if (this.storedGlobalSettings == null) {
        this.storedGlobalSettings = new GlobalSettings();
      }
    }
    return this.storedGlobalSettings.clone();
  }

  public GlobalSettings getGlobalSettings(String username) throws Exception {
    GlobalSettings globalSettings = getGlobalSettings();
    if (globalSettings == null) {
      globalSettings = new GlobalSettings();
    } else if (!canAccessApplication(globalSettings, username)) {
      throw new PerkStoreException(GLOBAL_SETTINGS_ACCESS_DENIED, username);
    }

    globalSettings.setCanAddProduct(canAddProduct(username));
    globalSettings.setAdministrator(isPerkStoreManager(username));

    // Delete useless information for normal user
    if (!globalSettings.isAdministrator()) {
      globalSettings.setManagers(null);
      globalSettings.setAccessPermissions(null);
      globalSettings.setProductCreationPermissions(null);
      globalSettings.setManagersProfiles(null);
      globalSettings.setAccessPermissionsProfiles(null);
      globalSettings.setProductCreationPermissionsProfiles(null);
    }
    return globalSettings;
  }

  public void saveProduct(Product product, String username) throws Exception {
    if (product == null) {
      throw new IllegalArgumentException("Product is mandatory");
    }
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username is mandatory");
    }

    boolean isNew = product.getId() == 0;
    if (isNew) {
      checkCanAddProduct(username);
    }

    if (!canEditProduct(product, username)) {
      throw new PerkStoreException(PRODUCT_MODIFICATION_DENIED, username, product.getTitle());
    }

    // Make sure to store only allowed fields to change
    Product productToStore = null;
    if (isNew) {
      productToStore = new Product();
    } else {
      productToStore = perkStoreStorage.getProductById(product.getId());
    }

    productToStore.setTitle(product.getTitle().trim());
    if (product.getIllustrationURL() != null) {
      productToStore.setIllustrationURL(product.getIllustrationURL().trim());
    } else {
      productToStore.setIllustrationURL(null);
    }
    if (product.getDescription() != null) {
      productToStore.setDescription(product.getDescription().trim());
    } else {
      productToStore.setDescription(null);
    }
    productToStore.setReceiverMarchand(product.getReceiverMarchand());
    productToStore.setAccessPermissions(product.getAccessPermissions());
    productToStore.setMarchands(product.getMarchands());
    productToStore.setEnabled(product.isEnabled());
    productToStore.setUnlimited(product.isUnlimited());
    productToStore.setAllowFraction(product.isAllowFraction());
    productToStore.setPrice(product.getPrice());
    productToStore.setMaxOrdersPerUser(product.getMaxOrdersPerUser());
    if (product.getOrderPeriodicity() != null) {
      productToStore.setOrderPeriodicity(product.getOrderPeriodicity().trim());
    } else {
      productToStore.setOrderPeriodicity(null);
    }
    productToStore.setTotalSupply(product.getTotalSupply());

    product = perkStoreStorage.saveProduct(productToStore, username);

    getListenerService().broadcast(PRODUCT_CREATE_OR_MODIFY_EVENT, product, isNew);
  }

  public List<Product> getProducts(String username) throws Exception {
    List<Product> products = perkStoreStorage.getAllProducts();
    if (products == null || products.isEmpty()) {
      return Collections.emptyList();
    }
    boolean isPerkstoreManager = isPerkStoreManager(username);
    Iterator<Product> productsIterator = products.iterator();
    while (productsIterator.hasNext()) {
      Product product = productsIterator.next();
      if (canViewProduct(product, username, isPerkstoreManager)) {
        computeProductFields(username, product);
      } else {
        productsIterator.remove();
      }
    }
    return products;
  }

  public List<ProductOrder> getOrders(OrderFilter filter, String username) throws Exception {
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

  public void checkCanOrder(ProductOrder order, String username) throws PerkStoreException {
    if (order == null) {
      throw new IllegalArgumentException("Order is mandatory");
    }
    if (order.getId() != 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }
    if (order.getProductId() == 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }
    Product product = getProductById(order.getProductId());

    order.setSender(toProfile(USER_ACCOUNT_TYPE, username));

    checkOrderCoherence(username, product, order);
  }

  public void createOrder(ProductOrder order, String username) throws Exception {
    if (order == null) {
      throw new IllegalArgumentException("Order is null");
    }
    if (order.getId() != 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }
    if (order.getProductId() == 0) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_PRODUCT);
    }

    Product product = getProductById(order.getProductId());
    if (product == null) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_PRODUCT);
    }

    double quantity = order.getQuantity();
    if (!product.isAllowFraction()) {
      quantity = (int) quantity;
      order.setQuantity(quantity);
    }

    Profile sender = toProfile(USER_ACCOUNT_TYPE, username);
    if (sender != null) {
      order.setSender(sender);
    }

    checkOrderCoherence(username, product, order);

    // Create new instance to avoid injecting values from front end
    ProductOrder productOrder = new ProductOrder();
    productOrder.setProductId(order.getProductId());
    productOrder.setAmount(quantity * product.getPrice());
    productOrder.setReceiver(order.getReceiver());
    productOrder.setSender(sender);
    productOrder.setTransactionHash(order.getTransactionHash());
    productOrder.setQuantity(quantity);
    productOrder.setStatus(ORDERED.name());
    productOrder.setRemainingQuantityToProcess(quantity);

    productOrder = perkStoreStorage.saveOrder(productOrder);

    getListenerService().broadcast(PRODUCT_PURCHASED_EVENT, product, productOrder);
  }

  public void saveOrderPaymentStatus(String hash, boolean transactionSuccess) throws Exception {
    if (StringUtils.isBlank(hash)) {
      throw new IllegalArgumentException("Transaction hash is mandatory");
    }
    ProductOrder order = perkStoreStorage.findOrderByTransactionHash(hash);
    if (order == null) {
      // Transaction doesn't correspond to a known product order
      return;
    }

    if (order.getProductId() == 0) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_PRODUCT);
    }

    Product product = getProductById(order.getProductId());
    if (product == null) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_PRODUCT);
    }

    boolean statusChanged = false;
    String oldStatus = order.getStatus();

    if (transactionSuccess) {
      if (StringUtils.equals(ORDERED.name(), oldStatus) || StringUtils.equals(CANCELED.name(), oldStatus)
          || StringUtils.equals(ERROR.name(), oldStatus)) {
        order.setStatus(PAYED.name());
        statusChanged = true;
      }
    } else {
      order.setStatus(ERROR.name());
      order.setError("Transaction failed");
      statusChanged = true;
    }

    if (statusChanged) {
      order = perkStoreStorage.saveOrder(order);
    }

    // Broadcast transaction finished event is different from save Order condition
    if (!oldStatus.equals(order.getStatus())) {
      getListenerService().broadcast(ORDER_PAYED_EVENT, product, order);
    }

  }

  public void saveOrderStatus(ProductOrder order, String username) throws Exception {
    if (order == null) {
      throw new IllegalArgumentException("Order is null");
    }
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username is null");
    }

    if (order.getProductId() == 0) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_PRODUCT);
    }

    Product product = getProductById(order.getProductId());
    if (product == null) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_PRODUCT);
    }
    long orderId = order.getId();
    if (orderId == 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }

    if (!canEditProduct(order.getProductId(), username)) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }

    // Create new instance to avoid injecting annoying values
    ProductOrder persistedOrder = perkStoreStorage.getOrder(orderId);
    if (persistedOrder == null) {
      throw new PerkStoreException(ORDER_NOT_EXISTS, username, orderId);
    }

    persistedOrder.setStatus(order.getStatus());
    setOrderQuantities(persistedOrder, order);
    setOrderDates(persistedOrder, order);

    persistedOrder = perkStoreStorage.saveOrder(persistedOrder);

    persistedOrder.setLastModifier(toProfile(USER_ACCOUNT_TYPE, username));
    getListenerService().broadcast(ORDER_MODIFIED_EVENT, product, persistedOrder);
  }

  public ProductOrder getOrderById(long orderId) {
    return perkStoreStorage.getOrder(orderId);
  }

  public Product getProductById(long productId) {
    return perkStoreStorage.getProductById(productId);
  }

  private GlobalSettings loadGlobalSettings() throws JsonException {
    SettingValue<?> globalSettingsValue = getSettingService().get(PERKSTORE_CONTEXT, PERKSTORE_SCOPE, SETTINGS_KEY_NAME);
    if (globalSettingsValue == null || StringUtils.isBlank(globalSettingsValue.getValue().toString())) {
      return new GlobalSettings();
    } else {
      GlobalSettings globalSettings = fromString(GlobalSettings.class, globalSettingsValue.getValue().toString());
      if (globalSettings != null) {
        List<Long> accessPermissions = globalSettings.getAccessPermissions();
        if (accessPermissions != null && !accessPermissions.isEmpty()) {
          globalSettings.setAccessPermissionsProfiles(new ArrayList<>());
          for (Long identityId : accessPermissions) {
            globalSettings.getAccessPermissionsProfiles().add(toProfile(identityId));
          }
        }
        List<Long> managers = globalSettings.getManagers();
        if (managers != null && !managers.isEmpty()) {
          globalSettings.setManagersProfiles(new ArrayList<>());
          for (Long identityId : managers) {
            globalSettings.getManagersProfiles().add(toProfile(identityId));
          }
        }
        List<Long> productCreationPermissions = globalSettings.getProductCreationPermissions();
        if (productCreationPermissions != null && !productCreationPermissions.isEmpty()) {
          globalSettings.setProductCreationPermissionsProfiles(new ArrayList<>());
          for (Long identityId : productCreationPermissions) {
            globalSettings.getProductCreationPermissionsProfiles().add(toProfile(identityId));
          }
        }
      }
      return globalSettings;
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

  private void checkCanAddProduct(String username) throws Exception {
    if (!canAddProduct(username)) {
      throw new PerkStoreException(PerkStoreError.PRODUCT_CREATION_DENIED, username);
    }
  }

  private boolean isPerkStoreManager(String username) throws Exception {
    if (isUserAdmin(username)) {
      return true;
    }

    GlobalSettings globalSettings = getGlobalSettings();
    if (globalSettings != null && globalSettings.getManagers() != null
        && !globalSettings.getManagers().isEmpty()) {
      return hasPermission(username, globalSettings.getManagers());
    }
    return false;
  }

  private boolean canAccessApplication(GlobalSettings globalSettings, String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      return false;
    }

    if (globalSettings == null) {
      return true;
    }

    if (isPerkStoreManager(username)) {
      return true;
    }

    return hasPermission(username, globalSettings.getAccessPermissions());
  }

  private boolean canAddProduct(String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      return false;
    }

    GlobalSettings globalSettings = getGlobalSettings();
    if (globalSettings == null) {
      return true;
    }

    return hasPermission(username, globalSettings.getProductCreationPermissions())
        && hasPermission(username, globalSettings.getAccessPermissions());
  }

  private boolean canEditProduct(long productId, String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      return false;
    }
    if (isUserAdmin(username)) {
      return true;
    }

    return canEditProduct(getProductById(productId), username);
  }

  private boolean canEditProduct(Product product, String username) throws Exception {
    if (product == null) {
      throw new IllegalArgumentException("Product is mandatory");
    }

    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username is mandatory");
    }

    if (product.getId() == 0) {
      return canAddProduct(username);
    }

    Profile creator = product.getCreator();
    if (StringUtils.equals(username, creator.getId())) {
      return true;
    }

    List<Profile> marchands = product.getMarchands();
    if (marchands == null || marchands.isEmpty()) {
      return canAddProduct(username);
    }

    return isUserMemberOf(username, marchands);
  }

  private boolean canViewProduct(Product product, String username, boolean isPerkStoreManager) {
    if (isPerkStoreManager) {
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

  private void setOrderDates(ProductOrder persistedOrder, ProductOrder order) {
    ProductOrderStatus oldStatus = ProductOrderStatus.valueOf(persistedOrder.getStatus());
    ProductOrderStatus newStatus = ProductOrderStatus.valueOf(order.getStatus());
    if (oldStatus != newStatus) {
      if (persistedOrder.getDeliveredDate() == 0 && (oldStatus == DELIVERED || order.getDeliveredQuantity() > 0)) {
        persistedOrder.setDeliveredDate(System.currentTimeMillis());
      }
      if (persistedOrder.getRefundedDate() == 0 && (oldStatus == REFUNDED || order.getRefundedQuantity() > 0)) {
        persistedOrder.setRefundedDate(System.currentTimeMillis());
      }
    }
    if (order.getDeliveredQuantity() == 0) {
      persistedOrder.setDeliveredDate(0);
    }
    if (order.getRefundedQuantity() == 0) {
      persistedOrder.setRefundedDate(0);
    }
  }

  private void setOrderQuantities(ProductOrder persistedOrder, ProductOrder order) throws PerkStoreException {
    double deliveredQuantity = order.getDeliveredQuantity();
    double refundedQuantity = order.getRefundedQuantity();
    persistedOrder.setDeliveredQuantity(deliveredQuantity);
    persistedOrder.setRefundedQuantity(refundedQuantity);
    double remainingQuantityToProcess = persistedOrder.getQuantity() - refundedQuantity
        - deliveredQuantity;
    if (remainingQuantityToProcess < 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_QUANTITY_INVALID_REMAINING,
                                   remainingQuantityToProcess,
                                   persistedOrder.getId());
    }
    persistedOrder.setRemainingQuantityToProcess(remainingQuantityToProcess);
  }

  private SettingService getSettingService() {
    if (settingService == null) {
      settingService = CommonsUtils.getService(SettingService.class);
    }
    return settingService;
  }

  private ListenerService getListenerService() {
    if (listenerService == null) {
      listenerService = CommonsUtils.getService(ListenerService.class);
    }
    return listenerService;
  }
}
