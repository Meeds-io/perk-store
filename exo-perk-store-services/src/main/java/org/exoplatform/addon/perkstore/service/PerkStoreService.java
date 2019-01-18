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

import static org.exoplatform.addon.perkstore.model.constant.PerkStoreError.*;
import static org.exoplatform.addon.perkstore.model.constant.ProductOrderModificationType.*;
import static org.exoplatform.addon.perkstore.model.constant.ProductOrderStatus.*;
import static org.exoplatform.addon.perkstore.model.constant.ProductOrderTransactionStatus.*;
import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.addon.perkstore.model.constant.*;
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

  private static final String       USERNAME_IS_MANDATORY_ERROR = "Username is mandatory";

  private static final Log          LOG                         = ExoLogger.getLogger(PerkStoreService.class);

  private PerkStoreWebSocketService webSocketService;

  private PerkStoreStorage          perkStoreStorage;

  private SettingService            settingService;

  private ListenerService           listenerService;

  private GlobalSettings            storedGlobalSettings;

  public PerkStoreService(PerkStoreWebSocketService webSocketService, PerkStoreStorage perkStoreStorage) {
    this.perkStoreStorage = perkStoreStorage;
    this.webSocketService = webSocketService;
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

    // Delete useless data for storage
    settings.setUserSettings(null);
    settings.setAccessPermissionsProfiles(null);
    settings.setManagers(null);
    settings.setProductCreationPermissionsProfiles(null);

    getSettingService().set(PERKSTORE_CONTEXT,
                            PERKSTORE_SCOPE,
                            SETTINGS_KEY_NAME,
                            SettingValue.create(transformToString(settings)));
    this.storedGlobalSettings = null;

    try {
      getListenerService().broadcast(SETTINGS_MODIFIED_EVENT, this, getGlobalSettings());
    } catch (Exception e) {
      LOG.warn("Error while braodcasting event {}", SETTINGS_MODIFIED_EVENT, e);
    }
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
    if (StringUtils.isBlank(username)) {
      throw new IllegalStateException("username is null");
    }

    GlobalSettings globalSettings = getGlobalSettings();
    if (globalSettings == null) {
      globalSettings = new GlobalSettings();
    } else if (!canAccessApplication(globalSettings, username)) {
      throw new PerkStoreException(GLOBAL_SETTINGS_ACCESS_DENIED, username);
    }

    UserSettings userSettings = globalSettings.getUserSettings();
    if (userSettings == null) {
      userSettings = new UserSettings();
      globalSettings.setUserSettings(userSettings);
    }

    userSettings.setCometdToken(webSocketService.getUserToken(username));
    userSettings.setCometdContext(webSocketService.getCometdContextName());
    userSettings.setCanAddProduct(canAddProduct(username));
    userSettings.setAdministrator(isPerkStoreManager(username));

    // Delete useless information for normal user
    if (!userSettings.isAdministrator()) {
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
      throw new IllegalArgumentException("Username is null");
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
      boolean canEdit = canEditProduct(product, username);

      if (canViewProduct(product, username, isPerkstoreManager)
          && (product.isEnabled() || canEdit)) {
        computeProductFields(username, product, canEdit);
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
      if (StringUtils.isBlank(currentUserId) || !canAddProduct(currentUserId)) {
        throw new IllegalAccessException(currentUserId + " is attempting to access orders list with filter: " + filter);
      }
    }
    List<ProductOrder> orders = null;
    if (filter.getProductId() == 0 && StringUtils.isBlank(username)) {
      throw new IllegalAccessException("No user nor product is chosen to display commands");
    } else if (filter.getProductId() == 0) {
      orders = perkStoreStorage.getOrders(username, filter);
    } else if (canEditProduct(filter.getProductId(), username)) {
      orders = perkStoreStorage.getOrders(null, filter);
    } else {
      orders = perkStoreStorage.getOrders(username, filter);
    }
    if (orders != null && !orders.isEmpty()) {
      orders.stream().forEach(order -> computeOrderFields(null, order));
    }
    return orders;
  }

  public void checkCanOrder(ProductOrder order, String username) throws PerkStoreException {
    if (order == null) {
      throw new IllegalArgumentException("Order is mandatory");
    }
    if (order.getProductId() == 0) {
      throw new PerkStoreException(PRODUCT_NOT_EXISTS, order.getProductId());
    }
    Product product = getProductById(order.getProductId());
    if (product == null) {
      throw new PerkStoreException(PRODUCT_NOT_EXISTS, order.getProductId());
    }
    if (order.getId() != 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, product.getTitle());
    }

    order.setSender(toProfile(USER_ACCOUNT_TYPE, username));
    checkOrderCoherence(username, product, order);
  }

  public void createOrder(ProductOrder order, String username) throws Exception {
    checkCanOrder(order, username);

    Product product = getProductById(order.getProductId());

    double quantity = order.getQuantity();
    if (!product.isAllowFraction()) {
      quantity = (int) quantity;
      order.setQuantity(quantity);
    }

    Profile sender = toProfile(USER_ACCOUNT_TYPE, username);
    if (sender != null) {
      order.setSender(sender);
    }

    // Create new instance to avoid injecting values from front end
    ProductOrder productOrder = new ProductOrder();
    productOrder.setProductId(order.getProductId());
    productOrder.setAmount(quantity * product.getPrice());
    productOrder.setReceiver(order.getReceiver());
    productOrder.setSender(sender);
    productOrder.setTransactionHash(order.getTransactionHash());
    productOrder.setTransactionStatus(StringUtils.isBlank(order.getTransactionHash()) ? NONE.name() : PENDING.name());
    productOrder.setRefundTransactionStatus(NONE.name());
    productOrder.setQuantity(quantity);
    productOrder.setStatus(ORDERED.name());
    productOrder.setRemainingQuantityToProcess(quantity);

    productOrder = perkStoreStorage.saveOrder(productOrder);

    productOrder.setModificationType(NEW);
    computeOrderFields(product, productOrder);

    getListenerService().broadcast(ORDER_CREATE_OR_MODIFY_EVENT, product, productOrder);
  }

  public ProductOrder saveOrder(ProductOrder order,
                                ProductOrderModificationType modificationType,
                                String username) throws Exception {
    if (order == null) {
      throw new IllegalArgumentException("Order is null");
    }
    if (modificationType == null) {
      throw new IllegalArgumentException("Order modification type is null");
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
      throw new PerkStoreException(ORDER_NOT_EXISTS, orderId);
    }

    if (!canEditProduct(order.getProductId(), username)) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }

    // Create new instance to avoid injecting annoying values
    ProductOrder orderToUpdate = getOrderById(orderId);
    if (orderToUpdate == null) {
      throw new PerkStoreException(ORDER_NOT_EXISTS, orderId);
    }

    double deliveredQuantity = orderToUpdate.getDeliveredQuantity();
    double refundedQuantity = orderToUpdate.getRefundedQuantity();

    boolean broadcastOrderEvent = true;
    switch (modificationType) {
    case STATUS:
      if (StringUtils.isBlank(username)) {
        throw new IllegalArgumentException(USERNAME_IS_MANDATORY_ERROR);
      }
      orderToUpdate.setStatus(order.getStatus());
      break;
    case DELIVERED_QUANTITY:
      // get fresh value from method parameter
      deliveredQuantity = order.getDeliveredQuantity();

      if (StringUtils.isBlank(username)) {
        throw new IllegalArgumentException(USERNAME_IS_MANDATORY_ERROR);
      }
      orderToUpdate.setDeliveredQuantity(deliveredQuantity);
      if (deliveredQuantity == 0) {
        orderToUpdate.setDeliveredDate(0);
      } else if (orderToUpdate.getDeliveredDate() == 0) {
        orderToUpdate.setDeliveredDate(System.currentTimeMillis());
      }
      computeOrderDeliverStatus(orderToUpdate, refundedQuantity, deliveredQuantity);
      break;
    case REFUNDED_QUANTITY:
      // get fresh value from method parameter
      refundedQuantity = order.getRefundedQuantity();

      if (StringUtils.isBlank(username)) {
        throw new IllegalArgumentException(USERNAME_IS_MANDATORY_ERROR);
      }
      orderToUpdate.setRefundTransactionHash(order.getRefundTransactionHash());
      orderToUpdate.setRefundTransactionStatus(PENDING.name());
      orderToUpdate.setRefundedQuantity(refundedQuantity);
      orderToUpdate.setRefundedAmount(order.getRefundedAmount());
      if (refundedQuantity == 0) {
        orderToUpdate.setRefundedDate(0);
      } else if (orderToUpdate.getRefundedDate() == 0) {
        orderToUpdate.setRefundedDate(System.currentTimeMillis());
      }
      computeOrderDeliverStatus(orderToUpdate, refundedQuantity, deliveredQuantity);
      break;
    case TX_STATUS:
      // DO NOT CHANGE THIS line location !!!
      broadcastOrderEvent = !StringUtils.equals(order.getTransactionStatus(), orderToUpdate.getTransactionStatus());

      computeOrderPaymentStatus(orderToUpdate, StringUtils.equals(SUCCESS.name(), order.getTransactionStatus()));
      orderToUpdate.setTransactionStatus(order.getTransactionStatus());
      break;
    case REFUND_TX_STATUS:
      // DO NOT CHANGE THIS line location !!!
      broadcastOrderEvent = !StringUtils.equals(order.getRefundTransactionStatus(), orderToUpdate.getRefundTransactionStatus());

      computeOrderDeliverStatus(orderToUpdate, refundedQuantity, deliveredQuantity);
      orderToUpdate.setRefundTransactionStatus(order.getRefundTransactionStatus());
      break;
    default:
      throw new UnsupportedOperationException("Order modification type is not supported");
    }

    // Always compute it because it's store and MUST be consistent all the time
    computeRemainingQuantity(orderToUpdate, orderToUpdate.getDeliveredQuantity(), orderToUpdate.getRefundedQuantity());

    orderToUpdate = perkStoreStorage.saveOrder(orderToUpdate);

    if (broadcastOrderEvent) {
      orderToUpdate.setLastModifier(toProfile(USER_ACCOUNT_TYPE, username));
      orderToUpdate.setModificationType(modificationType);

      computeOrderFields(product, orderToUpdate);

      getListenerService().broadcast(ORDER_CREATE_OR_MODIFY_EVENT, product, orderToUpdate);
    }

    return orderToUpdate;
  }

  public void saveOrderTransactionStatus(String hash, boolean transactionSuccess) throws Exception {
    if (StringUtils.isBlank(hash)) {
      throw new IllegalArgumentException("Transaction hash is mandatory");
    }

    ProductOrderModificationType modificationType = null;
    ProductOrder order = perkStoreStorage.findOrderByTransactionHash(hash);
    if (order == null) {
      order = perkStoreStorage.findOrderByRefundTransactionHash(hash);
      if (order == null) {
        // Nor order was found with hash corresponding to payment or refund
        // Transaction
        return;
      } else {
        order.setRefundTransactionStatus(transactionSuccess ? SUCCESS.name() : FAILED.name());
        modificationType = REFUND_TX_STATUS;
      }
    } else {
      order.setTransactionStatus(transactionSuccess ? SUCCESS.name() : FAILED.name());
      modificationType = TX_STATUS;
    }
    saveOrder(order, modificationType, null);
  }

  public ProductOrder getOrderById(long orderId) {
    return computeOrderFields(null, perkStoreStorage.getOrderById(orderId));
  }

  public Product getProductById(long productId, String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("username is madatory");
    }
    Product product = getProductById(productId);
    if (product == null) {
      throw new PerkStoreException(PerkStoreError.PRODUCT_NOT_EXISTS, productId);
    }
    if (!canViewProduct(product, username, isPerkStoreManager(username))) {
      return null;
    }
    computeProductFields(username, product, canEditProduct(product, username));
    return product;
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

  private ProductOrder computeOrderFields(Product product, ProductOrder order) {
    if (order == null) {
      return null;
    }
    if (product == null) {
      product = getProductById(order.getProductId());
    }
    if (product == null) {
      return order;
    }
    order.setProductTitle(product.getTitle());
    return order;
  }

  private void computeOrderDeliverStatus(ProductOrder order, double refundedQuantity, double deliveredQuantity) {
    if (order.getQuantity() == refundedQuantity) {
      order.setStatus(REFUNDED.name());
    } else if (order.getQuantity() == refundedQuantity + deliveredQuantity) {
      order.setStatus(DELIVERED.name());
    } else {
      order.setStatus(PARTIAL.name());
    }
  }

  private void computeRemainingQuantity(ProductOrder persistedOrder,
                                        double deliveredQuantity,
                                        double refundedQuantity) throws PerkStoreException {
    double remainingQuantityToProcess = persistedOrder.getQuantity() - refundedQuantity - deliveredQuantity;
    if (remainingQuantityToProcess < 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_QUANTITY_INVALID_REMAINING,
                                   remainingQuantityToProcess,
                                   persistedOrder.getId());
    }
    persistedOrder.setRemainingQuantityToProcess(remainingQuantityToProcess);
  }

  private void computeProductFields(String username, Product product, boolean canEdit) {
    long productId = product.getId();

    product.setPurchased(perkStoreStorage.countOrderedQuantity(productId));

    UserProductData userData = new UserProductData();
    product.setUserData(userData);

    userData.setUsername(username);
    userData.setCanEdit(canEdit);
    userData.setCanOrder(StringUtils.isNotBlank(username) && canViewProduct(product, username, false));

    long identityId = 0;
    if (StringUtils.isNotBlank(username)) {
      Identity currentUserIdentity = getIdentityByTypeAndId(USER_ACCOUNT_TYPE, username);
      identityId = Long.parseLong(currentUserIdentity.getId());
    }

    // Retrieve the following fields for not marchand only
    if (product.getReceiverMarchand() != null && !StringUtils.equals(product.getReceiverMarchand().getId(), username)) {
      userData.setTotalPurchased(perkStoreStorage.countUserTotalPurchasedQuantity(productId, identityId));

      double purchasedQuantityInPeriod = countPurchasedQuantityInCurrentPeriod(product, identityId);
      userData.setPurchasedInCurrentPeriod(purchasedQuantityInPeriod);
    }

    if (userData.isCanEdit()) {
      product.setNotProcessedOrders(perkStoreStorage.countRemainingOrdersToProcess(productId));
    } else if (identityId > 0) {
      product.setNotProcessedOrders(perkStoreStorage.countRemainingOrdersToProcess(identityId, productId));
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

  private void computeOrderPaymentStatus(ProductOrder persistedOrder, boolean transactionSuccess) {
    if (transactionSuccess) {
      // Change status of order to PAID only if the the status was ERROR,
      // CANCELED, ORDERED
      String oldStatus = persistedOrder.getStatus();
      if (StringUtils.equals(ORDERED.name(), oldStatus) || StringUtils.equals(CANCELED.name(), oldStatus)
          || StringUtils.equals(ERROR.name(), oldStatus)) {
        persistedOrder.setStatus(PAID.name());
      }
    } else {
      persistedOrder.setStatus(ERROR.name());
    }
  }

  private void checkOrderCoherence(String username, Product product, ProductOrder order) throws PerkStoreException {
    if (StringUtils.isBlank(username)) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }
    if (order.getId() != 0) {
      throw new PerkStoreException(ORDER_NOT_EXISTS, order.getId());
    }
    if (product == null || order.getProductId() == 0) {
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
    if (globalSettings != null && globalSettings.getManagers() != null && !globalSettings.getManagers().isEmpty()) {
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

    return isUserAdmin(username) || (hasPermission(username, globalSettings.getProductCreationPermissions())
        && hasPermission(username, globalSettings.getAccessPermissions()));
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
      return false;
    }

    if (product.getId() == 0) {
      return canAddProduct(username);
    }

    if (isUserAdmin(username)) {
      return true;
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
