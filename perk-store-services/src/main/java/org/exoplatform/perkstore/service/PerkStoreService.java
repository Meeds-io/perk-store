/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.perkstore.service;

import static org.exoplatform.perkstore.model.constant.PerkStoreError.*;
import static org.exoplatform.perkstore.model.constant.ProductOrderModificationType.*;
import static org.exoplatform.perkstore.model.constant.ProductOrderStatus.*;
import static org.exoplatform.perkstore.model.constant.ProductOrderTransactionStatus.*;
import static org.exoplatform.perkstore.service.utils.Utils.*;
import static org.exoplatform.perkstore.statistic.StatisticUtils.OPERATION;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.perkstore.exception.PerkStoreException;
import org.exoplatform.perkstore.model.*;
import org.exoplatform.perkstore.model.constant.*;
import org.exoplatform.perkstore.statistic.*;
import org.exoplatform.perkstore.storage.PerkStoreStorage;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.ws.frameworks.json.impl.JsonException;

/**
 * A service to manage perkstore entities
 */
public class PerkStoreService implements ExoPerkStoreStatisticService, Startable {

  private static final Log          LOG                                  = ExoLogger.getLogger(PerkStoreService.class);

  private static final String       USERNAME_IS_MANDATORY_ERROR          = "Username is mandatory";

  private static final String       STATISTIC_OPERATION_SAVE_PRODUCT     = "product_save";

  private static final String       STATISTIC_OPERATION_SAVE_ORDER       = "order_save";

  private static final String       STATISTIC_OPERATION_CREATE_PRODUCT   = "create_product";

  private static final String       STATISTIC_OPERATION_MODIFY_PRODUCT   = "modify_product";

  private static final String       STATISTIC_OPERATION_CREATE_ORDER     = "create_order";

  private static final String       STATISTIC_OPERATION_CHANGE_STATUS    = "change_order_status";

  private static final String       STATISTIC_OPERATION_PAY_ORDER        = "pay_order";

  private static final String       STATISTIC_OPERATION_DELIVER_ORDER    = "deliver_order";

  private static final String       STATISTIC_OPERATION_REFUND_ORDER     = "refund_order";

  private static final String       STATISTIC_OPERATION_REFUND_PAY_ORDER = "refund_pay_order";

  private PerkStoreWebSocketService webSocketService;

  private PerkStoreStorage          perkStoreStorage;

  private SettingService            settingService;

  private ListenerService           listenerService;

  private GlobalSettings            storedGlobalSettings;

  private PortalContainer           container;

  public PerkStoreService(PerkStoreWebSocketService webSocketService,
                          PerkStoreStorage perkStoreStorage,
                          SettingService settingService,
                          PortalContainer container) {
    this.perkStoreStorage = perkStoreStorage;
    this.webSocketService = webSocketService;
    this.settingService = settingService;
    this.container = container;
  }

  @Override
  public void start() {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      this.storedGlobalSettings = loadGlobalSettings();
    } catch (Exception e) {
      LOG.error("Error loading perk store global settings", e);
    } finally {
      RequestLifeCycle.end();
    }
  }

  @Override
  public void stop() {
    // Nothing to shutdown
  }

  public void saveGlobalSettings(GlobalSettings settings, String username) throws Exception {
    if (!isPerkStoreManager(username)) {
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
    settings.setManagersProfiles(null);
    settings.setProductCreationPermissionsProfiles(null);

    getSettingService().set(PERKSTORE_CONTEXT,
                            PERKSTORE_SCOPE,
                            SETTINGS_KEY_NAME,
                            SettingValue.create(transformToString(settings)));
    this.storedGlobalSettings = null;

    getListenerService().broadcast(SETTINGS_MODIFIED_EVENT, this, getGlobalSettings());
  }

  public GlobalSettings getGlobalSettings() {
    if (this.storedGlobalSettings == null) {
      this.storedGlobalSettings = loadGlobalSettings();
    }
    return this.storedGlobalSettings.clone(); // NOSONAR
  }

  public GlobalSettings getGlobalSettings(String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("username is null");
    }

    GlobalSettings globalSettings = getGlobalSettings();
    if (!canAccessApplication(globalSettings, username)) {
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

  @ExoPerkStoreStatistic(local = true, service = "perkstore", operation = STATISTIC_OPERATION_SAVE_PRODUCT)
  public Product saveProduct(Product product, String username) throws Exception {
    if (product == null) {
      throw new IllegalArgumentException("Product is mandatory");
    }
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username is null");
    }

    boolean isNew = product.getId() == 0;

    // Make sure to store only allowed fields to change
    Product productToStore = null;
    if (isNew) {
      checkCanAddProduct(username);
      productToStore = new Product();
    } else {
      productToStore = perkStoreStorage.getProductById(product.getId());
      if (productToStore == null) {
        throw new PerkStoreException(PRODUCT_NOT_EXISTS, product.getId());
      }
      if (!canEditProduct(productToStore, username)) {
        throw new PerkStoreException(PRODUCT_MODIFICATION_DENIED, username, productToStore.getTitle());
      }
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
    productToStore.setAllowFraction(product.isAllowFraction());
    productToStore.setPrice(product.getPrice());
    productToStore.setMaxOrdersPerUser(product.getMaxOrdersPerUser());
    productToStore.setImageFiles(product.getImageFiles());
    if (product.getOrderPeriodicity() != null) {
      productToStore.setOrderPeriodicity(product.getOrderPeriodicity().trim());
    } else {
      productToStore.setOrderPeriodicity(null);
    }

    productToStore.setUnlimited(product.isUnlimited());
    if (product.isUnlimited()) {
      productToStore.setTotalSupply(0);
    } else {
      productToStore.setTotalSupply(product.getTotalSupply());
    }

    product = perkStoreStorage.saveProduct(productToStore, username);

    getListenerService().broadcast(PRODUCT_CREATE_OR_MODIFY_EVENT, product, isNew);
    return product;
  }

  public List<Product> getProducts(boolean available, String username) throws Exception {
    List<Product> products = perkStoreStorage.getAllProducts();
    if (products == null || products.isEmpty() || !canAccessApplication(getGlobalSettings(), username)) {
      return Collections.emptyList();
    }
    boolean isPerkstoreManager = isPerkStoreManager(username);
    Iterator<Product> productsIterator = products.iterator();
    while (productsIterator.hasNext()) {
      Product product = productsIterator.next();
      boolean canEdit = isPerkstoreManager || canEditProduct(product, username);

      if (canEdit || (canViewProduct(product, username, isPerkstoreManager) && product.isEnabled())) {
        computeProductFields(username, product, canEdit);
      } else {
        productsIterator.remove();
      }
    }
    if (available) {
      products = products.stream()
                         .filter(product -> product.isEnabled() && (product.isUnlimited() || product.getTotalSupply() > product.getPurchased()))
                         .collect(Collectors.toList());
    }
    return products;
  }

  public List<ProductOrder> getOrders(OrderFilter filter, String username) throws Exception {
    if (filter == null) {
      throw new IllegalArgumentException("Filter is mandatory");
    }
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("username is mandatory");
    }

    if (!canAccessApplication(getGlobalSettings(), username)) {
      throw new PerkStoreException(GLOBAL_SETTINGS_ACCESS_DENIED, username);
    } else {
      if (filter.getProductId() > 0) {
        Product product = getProductById(filter.getProductId());
        if (product == null) {
          throw new PerkStoreException(PRODUCT_NOT_EXISTS, filter.getProductId());
        }
        if (!canViewProduct(product, username, isPerkStoreManager(username))) {
          throw new PerkStoreException(PRODUCT_ACCESS_DENIED, product.getTitle(), username);
        }
      }
    }
    List<ProductOrder> orders = null;
    long selectedOrderId = filter.getSelectedOrderId();
    if (selectedOrderId > 0) {
      // One single order is selected
      ProductOrder order = getOrderById(selectedOrderId);
      if (order == null
          || (!StringUtils.equals(order.getSender().getId(), username) && !canEditProduct(order.getProductId(), username))) {
        throw new PerkStoreException(ORDER_ACCESS_DENIED, selectedOrderId, username);
      } else {
        return Collections.singletonList(order);
      }
    } else if (filter.getProductId() == 0) {
      // If no product is chosen, then display my orders, even for a manager
      orders = perkStoreStorage.getOrders(username, filter);
    } else if (!filter.isCurrentUserOrders() && canEditProduct(filter.getProductId(), username)) {
      // If manager, display all orders of the product
      orders = perkStoreStorage.getOrders(null, filter);
    } else {
      // If display orders of current user on the product
      orders = perkStoreStorage.getOrders(username, filter);
    }
    if (orders != null && !orders.isEmpty()) {
      orders.stream().forEach(order -> computeOrderFields(null, order));
    }
    return orders;
  }

  public Long countOrders(OrderFilter filter, String username) {
    if (filter == null) {
      throw new IllegalArgumentException("Filter is mandatory");
    }
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("username is mandatory");
    }
    return perkStoreStorage.countOrders(username, filter);
  }

  public void checkCanCreateOrder(ProductOrder order, String username) throws PerkStoreException {
    if (order == null) {
      throw new IllegalArgumentException("Order is mandatory");
    }
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username is mandatory");
    }
    Product product = getProductById(order.getProductId());
    if (product == null) {
      throw new PerkStoreException(PRODUCT_NOT_EXISTS, order.getProductId());
    }
    if (!product.isEnabled()) {
      throw new PerkStoreException(PRODUCT_IS_DISABLED, product.getTitle());
    }
    if (order.getId() != 0) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, product.getTitle());
    }

    if (!isUserMemberOf(username, getGlobalSettings().getAccessPermissionsProfiles())
        || !isUserMemberOf(username, product.getAccessPermissions())) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, product.getTitle());
    }

    if (StringUtils.isBlank(order.getTransactionHash())) {
      throw new PerkStoreException(ORDER_CREATION_EMPTY_TX);
    }

    checkTransactionHashNotExists(product, order, username);

    order.setSender(toProfile(USER_ACCOUNT_TYPE, username));
    if (order.getStatus() != null) {
      throw new PerkStoreException(ORDER_CREATION_STATUS_DENIED);
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

  @ExoPerkStoreStatistic(local = true, service = "perkstore", operation = STATISTIC_OPERATION_CREATE_ORDER)
  public ProductOrder createOrder(ProductOrder order, String username) throws Exception {
    checkCanCreateOrder(order, username);

    Product product = getProductById(order.getProductId());

    double quantity = order.getQuantity();
    if (!product.isAllowFraction()) {
      quantity = (int) quantity;
      order.setQuantity(quantity);
    }

    Profile sender = toProfile(USER_ACCOUNT_TYPE, username);
    if (sender == null) {
      throw new IllegalStateException("Social identity not found for user " + username);
    }
    order.setSender(sender);

    // Create new instance to avoid injecting values from front end
    ProductOrder productOrder = new ProductOrder();
    if (quantity <= 0) {
      throw new IllegalStateException("Wrong order quantity: " + quantity);
    }
    productOrder.setQuantity(quantity);
    productOrder.setProductId(order.getProductId());
    double amount = quantity * product.getPrice();
    productOrder.setAmount(amount);
    productOrder.setSender(sender);

    if (order.getReceiver() != null && order.getReceiver().getTechnicalId() != product.getReceiverMarchand().getTechnicalId()) {
      productOrder.setError(PerkStoreError.ORDER_FRAUD_WRONG_RECEIVER);
      LOG.warn("Transaction receiver '{}' sent by '{}' to buy product '{}' is wrong. It must be '{}'",
               order.getReceiver(),
               username,
               product.getTitle(),
               product.getReceiverMarchand());
    }

    productOrder.setReceiver(product.getReceiverMarchand());
    productOrder.setTransactionHash(formatTransactionHash(order.getTransactionHash()));
    productOrder.setTransactionStatus(StringUtils.isBlank(order.getTransactionHash()) ? NONE.name() : PENDING.name());
    productOrder.setRefundTransactionStatus(NONE.name());
    productOrder.setRemainingQuantityToProcess(quantity);
    if (productOrder.getError() != null) {
      productOrder.setStatus(FRAUD.name());
      productOrder.setRemainingQuantityToProcess(0);
    } else if (StringUtils.isBlank(order.getTransactionHash())) {
      productOrder.setStatus(CANCELED.name());
    } else {
      productOrder.setStatus(ORDERED.name());
    }

    // Always compute it because it's store and MUST be consistent all the time
    computeRemainingQuantity(productOrder, productOrder.getDeliveredQuantity(), productOrder.getRefundedQuantity());
    productOrder = perkStoreStorage.saveOrder(productOrder);

    if (productOrder.getError() != null) {
      addFraudStatistic(sender.getTechnicalId(), productOrder.getTransactionHash(), productOrder);
    }

    computeOrderFields(product, productOrder);

    getListenerService().broadcast(ORDER_CREATE_OR_MODIFY_EVENT,
                                   product,
                                   new ProductOrderModification(toProfile(USER_ACCOUNT_TYPE, username), NEW, null, productOrder));
    return productOrder;
  }

  @ExoPerkStoreStatistic(local = true, service = "perkstore", operation = STATISTIC_OPERATION_SAVE_ORDER)
  public ProductOrder saveOrder(ProductOrder order,
                                ProductOrderModificationType modificationType,
                                String username,
                                boolean checkUsername) throws Exception {
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

    if (checkUsername && !canEditProduct(order.getProductId(), username)) {
      throw new PerkStoreException(ORDER_MODIFICATION_DENIED, username, order.getProductId());
    }

    // Create new instance to avoid injecting annoying values
    ProductOrder orderToUpdate = getOrderById(orderId);
    if (orderToUpdate == null) {
      throw new PerkStoreException(ORDER_NOT_EXISTS, orderId);
    }

    ProductOrderModification productOrderModification = new ProductOrderModification(toProfile(USER_ACCOUNT_TYPE, username),
                                                                                     modificationType,
                                                                                     orderToUpdate.clone(),
                                                                                     orderToUpdate);
    double deliveredQuantity = orderToUpdate.getDeliveredQuantity();
    double refundedQuantity = orderToUpdate.getRefundedQuantity();

    boolean broadcastOrderEvent = true;
    switch (modificationType) {
    case STATUS:
      if (checkUsername && StringUtils.isBlank(username)) {
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
      if (deliveredQuantity < 0) {
        throw new IllegalStateException("Delivered quantity can't be negative");
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
      if (refundedQuantity < 0) {
        throw new IllegalStateException("Refunded quantity can't be negative");
      }
      if (order.getRefundedAmount() < 0) {
        throw new IllegalStateException("Refunded quantity can't be negative");
      }
      checkTransactionRefundHashNotExists(product, order, username);

      orderToUpdate.setRefundTransactionHash(formatTransactionHash(order.getRefundTransactionHash()));
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
      orderToUpdate.setError(order.getError());
      break;
    case REFUND_TX_STATUS:
      // DO NOT CHANGE THIS line location !!!
      broadcastOrderEvent = !StringUtils.equals(order.getRefundTransactionStatus(), orderToUpdate.getRefundTransactionStatus());

      computeOrderDeliverStatus(orderToUpdate, refundedQuantity, deliveredQuantity);
      orderToUpdate.setRefundTransactionStatus(order.getRefundTransactionStatus());
      orderToUpdate.setError(order.getError());
      break;
    default:
      throw new UnsupportedOperationException("Order modification type '" + modificationType + "' is not supported");
    }

    if (orderToUpdate.getError() != null) {
      orderToUpdate.setStatus(FRAUD.name());
    }

    // Always compute it because it's store and MUST be consistent all the time
    computeRemainingQuantity(orderToUpdate, orderToUpdate.getDeliveredQuantity(), orderToUpdate.getRefundedQuantity());
    orderToUpdate = perkStoreStorage.saveOrder(orderToUpdate);

    if (broadcastOrderEvent) {
      computeOrderFields(product, orderToUpdate);

      getListenerService().broadcast(ORDER_CREATE_OR_MODIFY_EVENT, product, productOrderModification);
    }

    return orderToUpdate;
  }

  public void saveOrderTransactionStatus(Map<String, Object> transactionDetails) throws Exception {
    if (transactionDetails == null) {
      throw new IllegalArgumentException("Transaction detail is mandatory");
    }
    // Add some verifications
    String hash = (String) transactionDetails.get("hash");
    if (StringUtils.isBlank(hash)) {
      throw new IllegalArgumentException("Transaction hash is empty");
    }

    boolean succeeded = (boolean) transactionDetails.get("status");

    ProductOrderModificationType modificationType = null;
    ProductOrder order = perkStoreStorage.findOrderByTransactionHash(hash);
    if (order == null) {
      order = perkStoreStorage.findOrderByRefundTransactionHash(hash);
      if (order == null || !StringUtils.equalsIgnoreCase(order.getRefundTransactionHash(), hash)) {
        // Nor order was found with hash corresponding to payment or refund
        // Transaction
        return;
      } else {
        order.setRefundTransactionStatus(succeeded ? SUCCESS.name() : FAILED.name());
        modificationType = REFUND_TX_STATUS;

        LOG.debug("Set Refund order {} with transaction hash {} as scceeded={}", order.getId(), hash, succeeded);
      }
    } else if (StringUtils.equalsIgnoreCase(order.getTransactionHash(), hash)) {
      order.setTransactionStatus(succeeded ? SUCCESS.name() : FAILED.name());
      modificationType = TX_STATUS;

      LOG.debug("Set Order {} with transaction hash {} as scceeded={}", order.getId(), hash, succeeded);
    }

    String contractAddress = (String) transactionDetails.get("contractAddress");
    String contractMethodName = (String) transactionDetails.get("contractMethodName");
    double contractAmount = (double) transactionDetails.get("contractAmount");
    long fromIdentityId = (long) transactionDetails.get("from");
    long toIdentityId = (long) transactionDetails.get("to");
    long issuerId = (long) transactionDetails.get("issuerId");

    // Do some coherence checks on mined transaction
    if (succeeded) {
      Profile sender = modificationType == TX_STATUS ? order.getSender() : order.getReceiver();
      Profile receiver = modificationType == TX_STATUS ? order.getReceiver() : order.getSender();

      if (StringUtils.isBlank(contractAddress)) {
        order.setError(PerkStoreError.ORDER_FRAUD_NOT_TOKEN_TRANSACTION);
      } else if (!StringUtils.equals("transfer", contractMethodName)) {
        order.setError(PerkStoreError.ORDER_FRAUD_WRONG_TOKEN_TRANSFER_METHOD);
      } else if (sender != null && sender.getTechnicalId() != fromIdentityId) {
        order.setError(PerkStoreError.ORDER_FRAUD_WRONG_SENDER);
      } else if (receiver != null && receiver.getTechnicalId() != toIdentityId) {
        order.setError(PerkStoreError.ORDER_FRAUD_WRONG_RECEIVER);
      } else if (modificationType == TX_STATUS && Math.abs(contractAmount - order.getAmount()) > 0.001) {
        order.setError(PerkStoreError.ORDER_FRAUD_WRONG_AMOUNT);
      }

      if (order.getError() != null) {
        if (modificationType == TX_STATUS) {
          order.setTransactionStatus(FAILED.name());
        } else {
          order.setRefundTransactionStatus(FAILED.name());
        }

        addFraudStatistic(issuerId, hash, order);
      }
    }

    saveOrder(order, modificationType, null, false);
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
      throw new PerkStoreException(PRODUCT_NOT_EXISTS, productId);
    } else if (canViewProduct(product, username, isPerkStoreManager(username))) {
      computeProductFields(username, product, canEditProduct(product, username));
    } else {
      product = new Product();
      product.setId(productId);
    }
    return product;
  }

  public Product getProductById(long productId) {
    return perkStoreStorage.getProductById(productId);
  }

  public FileDetail getFileDetail(long productId, long imageId, boolean retrieveData, String username) throws Exception {
    if (!canViewProduct(getProductById(productId), username, isPerkStoreManager(username))) {
      return null;
    }
    return perkStoreStorage.getFileDetail(productId, imageId, retrieveData);
  }

  public boolean isPerkStoreManager(String username) throws Exception {
    if (isUserAdmin(username)) {
      return true;
    }

    GlobalSettings globalSettings = getGlobalSettings();
    if (globalSettings != null && globalSettings.getManagers() != null && !globalSettings.getManagers().isEmpty()) {
      return hasPermission(username, globalSettings.getManagers());
    }
    return false;
  }

  public void replaceTransactions(String oldHash, String newHash) {
    perkStoreStorage.replaceTransactions(oldHash, newHash);
  }

  @Override
  public Map<String, Object> getStatisticParameters(String operation, Object result, Object... methodArgs) {
    Map<String, Object> parameters = new HashMap<>();
    String issuer = null;
    if (StringUtils.equals(STATISTIC_OPERATION_SAVE_PRODUCT, operation)) {
      Product savedProduct = (Product) result;
      if (savedProduct == null) {
        return null;
      }

      Product product = (Product) methodArgs[0];
      operation = product.getId() <= 0 ? STATISTIC_OPERATION_CREATE_PRODUCT : STATISTIC_OPERATION_MODIFY_PRODUCT;
      parameters.put(OPERATION, operation);
      parameters.put("product_id", savedProduct.getId());
      parameters.put("total_supply", savedProduct.getTotalSupply());
      parameters.put("marchand_identity_id", savedProduct.getReceiverMarchand().getTechnicalId());
      parameters.put("price", savedProduct.getPrice());
      parameters.put("enabled", savedProduct.isEnabled());

      issuer = (String) methodArgs[methodArgs.length - 1];
    } else if (StringUtils.equals(STATISTIC_OPERATION_CREATE_ORDER, operation)) {
      ProductOrder savedOrder = (ProductOrder) result;
      if (savedOrder == null) {
        return null;
      }

      parameters.put("order_id", savedOrder.getId());
      parameters.put("product_id", savedOrder.getProductId());
      parameters.put("buyer_identity_id", savedOrder.getSender().getTechnicalId());
      parameters.put("marchand_identity_id", savedOrder.getReceiver().getTechnicalId());
      parameters.put("order_quantity", savedOrder.getQuantity());
      parameters.put("order_amount", savedOrder.getAmount());
      parameters.put("transaction_hash", savedOrder.getTransactionHash());

      issuer = (String) methodArgs[methodArgs.length - 1];
    } else if (StringUtils.equals(STATISTIC_OPERATION_SAVE_ORDER, operation)) {
      ProductOrder savedOrder = (ProductOrder) result;
      if (savedOrder == null) {
        return null;
      }

      ProductOrderModificationType modificationType = (ProductOrderModificationType) methodArgs[1];
      switch (modificationType) {
      case NEW:
        return null;
      case STATUS:
        parameters.put(OPERATION, STATISTIC_OPERATION_CHANGE_STATUS);

        parameters.put("order_id", savedOrder.getId());
        parameters.put("product_id", savedOrder.getProductId());
        parameters.put("order_status", savedOrder.getStatus());
        break;
      case TX_STATUS:
        parameters.put(OPERATION, STATISTIC_OPERATION_PAY_ORDER);

        parameters.put("order_id", savedOrder.getId());
        parameters.put("product_id", savedOrder.getProductId());
        parameters.put("buyer_identity_id", savedOrder.getSender().getTechnicalId());
        parameters.put("marchand_identity_id", savedOrder.getReceiver().getTechnicalId());
        parameters.put("transaction_hash", savedOrder.getTransactionHash());
        parameters.put("transaction_status", savedOrder.getTransactionStatus());
        parameters.put("order_status", savedOrder.getStatus());
        break;
      case DELIVERED_QUANTITY:
        parameters.put(OPERATION, STATISTIC_OPERATION_DELIVER_ORDER);

        parameters.put("order_id", savedOrder.getId());
        parameters.put("product_id", savedOrder.getProductId());
        parameters.put("buyer_identity_id", savedOrder.getSender().getTechnicalId());
        parameters.put("order_quantity", savedOrder.getQuantity());
        parameters.put("delivered_order_quantity", savedOrder.getDeliveredQuantity());
        parameters.put("refunded_order_quantity", savedOrder.getRefundedQuantity());
        parameters.put("remaining_order_quantity", savedOrder.getRemainingQuantityToProcess());
        parameters.put("order_status", savedOrder.getStatus());
        break;
      case REFUNDED_QUANTITY:
        parameters.put(OPERATION, STATISTIC_OPERATION_REFUND_ORDER);

        parameters.put("order_id", savedOrder.getId());
        parameters.put("product_id", savedOrder.getProductId());
        parameters.put("buyer_identity_id", savedOrder.getSender().getTechnicalId());
        parameters.put("marchand_identity_id", savedOrder.getReceiver().getTechnicalId());
        parameters.put("order_quantity", savedOrder.getQuantity());
        parameters.put("delivered_order_quantity", savedOrder.getDeliveredQuantity());
        parameters.put("refunded_order_quantity", savedOrder.getRefundedQuantity());
        parameters.put("remaining_order_quantity", savedOrder.getRemainingQuantityToProcess());
        parameters.put("order_refund_amount", savedOrder.getRefundedAmount());
        parameters.put("refund_transaction_hash", savedOrder.getRefundTransactionHash());
        parameters.put("order_status", savedOrder.getStatus());
        break;
      case REFUND_TX_STATUS:
        parameters.put(OPERATION, STATISTIC_OPERATION_REFUND_PAY_ORDER);

        parameters.put("order_id", savedOrder.getId());
        parameters.put("product_id", savedOrder.getProductId());
        parameters.put("buyer_identity_id", savedOrder.getSender().getTechnicalId());
        parameters.put("marchand_identity_id", savedOrder.getReceiver().getTechnicalId());
        parameters.put("refund_transaction_hash", savedOrder.getRefundTransactionHash());
        parameters.put("refund_transaction_status", savedOrder.getRefundTransactionStatus());
        parameters.put("order_status", savedOrder.getStatus());
        break;
      default:
        break;
      }

      issuer = (String) methodArgs[methodArgs.length - 2];
    } else {
      LOG.warn("Statistic operation type '{}' not handled", operation);
      return null;
    }

    if (StringUtils.isNotBlank(issuer)) {
      Identity identity = getIdentityByTypeAndId(OrganizationIdentityProvider.NAME, issuer);
      if (identity == null) {
        LOG.debug("Can't find identity with remote id: {}" + issuer);
      } else {
        parameters.put("user_social_id", identity.getId());
      }
    }
    return parameters;
  }

  private GlobalSettings loadGlobalSettings() {
    SettingValue<?> globalSettingsValue = getSettingService().get(PERKSTORE_CONTEXT, PERKSTORE_SCOPE, SETTINGS_KEY_NAME);
    if (globalSettingsValue == null || StringUtils.isBlank(globalSettingsValue.getValue().toString())) {
      return new GlobalSettings();
    } else {
      GlobalSettings globalSettings;
      try {
        globalSettings = fromString(GlobalSettings.class, globalSettingsValue.getValue().toString());
      } catch (JsonException e) {
        throw new IllegalStateException("Can't read perkstore settings", e);
      }
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

  private void computeRemainingQuantity(ProductOrder persistedOrder,
                                        double deliveredQuantity,
                                        double refundedQuantity) throws PerkStoreException {
    if (StringUtils.equalsIgnoreCase(persistedOrder.getStatus(), CANCELED.name())
        || StringUtils.equalsIgnoreCase(persistedOrder.getStatus(), ERROR.name())
        || StringUtils.equalsIgnoreCase(persistedOrder.getStatus(), FRAUD.name())) {
      persistedOrder.setRemainingQuantityToProcess(0);
    } else {
      double remainingQuantityToProcess = persistedOrder.getQuantity() - refundedQuantity - deliveredQuantity;
      if (remainingQuantityToProcess < 0) {
        throw new PerkStoreException(ORDER_MODIFICATION_QUANTITY_INVALID_REMAINING,
                                     remainingQuantityToProcess,
                                     persistedOrder.getId());
      }
      persistedOrder.setRemainingQuantityToProcess(remainingQuantityToProcess);
    }
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

    userData.setNotProcessedOrders(perkStoreStorage.countRemainingOrdersToProcess(identityId, productId));

    if (userData.isCanEdit()) {
      product.setNotProcessedOrders(perkStoreStorage.countRemainingOrdersToProcess(productId));
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

  private void checkTransactionHashNotExists(Product product, ProductOrder order, String username) throws PerkStoreException {
    String transactionHash = order.getTransactionHash();
    if (StringUtils.isNotBlank(transactionHash)) {
      ProductOrder orderWithSameTransactionHash = perkStoreStorage.findOrderByTransactionHash(transactionHash);
      if (orderWithSameTransactionHash != null) {
        LOG.warn(username + " is attempting to recreate an order with the same transaction hash twice "
            + transactionHash);
        throw new PerkStoreException(PerkStoreError.ORDER_CREATION_DENIED,
                                     username,
                                     product.getTitle());
      }
    }
  }

  private void checkTransactionRefundHashNotExists(Product product,
                                                   ProductOrder order,
                                                   String username) throws PerkStoreException {
    String transactionHash = order.getRefundTransactionHash();
    if (StringUtils.isNotBlank(transactionHash)) {
      ProductOrder orderWithSameRefundTransactionHash = perkStoreStorage.findOrderByRefundTransactionHash(transactionHash);
      if (orderWithSameRefundTransactionHash != null && orderWithSameRefundTransactionHash.getId() != order.getId()) {
        LOG.warn(username + " is attempting to refund an order with another order refund transaction hash "
            + transactionHash);
        throw new PerkStoreException(PerkStoreError.ORDER_CREATION_DENIED,
                                     username,
                                     product.getTitle());
      }
    }
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
      throw new PerkStoreException(PRODUCT_CREATION_DENIED, username);
    }
  }

  private boolean canAccessApplication(GlobalSettings globalSettings, String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      return false;
    }

    Identity userIdentity = getIdentityByTypeAndId(USER_ACCOUNT_TYPE, username);
    if (userIdentity == null) {
      return false;
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

    return isUserAdmin(username) || (hasPermission(username, globalSettings.getProductCreationPermissions())
        && hasPermission(username, globalSettings.getAccessPermissions()));
  }

  private boolean canEditProduct(long productId, String username) throws Exception {
    if (StringUtils.isBlank(username)) {
      return false;
    }

    Product product = getProductById(productId);
    return canEditProduct(product, username);
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

    if (isPerkStoreManager(username)) {
      return true;
    }

    List<Profile> marchands = product.getMarchands();
    if (marchands == null || marchands.isEmpty()) {
      return false;
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

    if (product == null) {
      return false;
    }

    List<Profile> accessPermissions = product.getAccessPermissions();
    if (accessPermissions == null || accessPermissions.isEmpty()) {
      return true;
    }

    return isUserMemberOf(username, accessPermissions);
  }

  private void addFraudStatistic(long issuerId, String hash, ProductOrder order) {
    if (order.getError() != null) {
      Map<String, Object> statisticParameters = new HashMap<>();
      statisticParameters.put(StatisticUtils.LOCAL_SERVICE, "perkstore");
      statisticParameters.put(StatisticUtils.OPERATION, "order_fraud");
      statisticParameters.put("user_social_id", issuerId);
      statisticParameters.put("order_id", order.getId());
      statisticParameters.put("product_id", order.getProductId());
      if (StringUtils.isNotBlank(hash)) {
        statisticParameters.put("fraud_transaction_hash", hash);
      }
      if (StringUtils.isNotBlank(order.getTransactionHash())) {
        statisticParameters.put("transaction_hash", order.getTransactionHash());
      }
      if (StringUtils.isNotBlank(order.getRefundTransactionHash())) {
        statisticParameters.put("refund_transaction_hash", order.getRefundTransactionHash());
      }
      statisticParameters.put("error_code", order.getError().getCode());
      statisticParameters.put("error_code_msg", order.getError().getErrorCode());
      StatisticUtils.addStatisticEntry(statisticParameters);
    }
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
