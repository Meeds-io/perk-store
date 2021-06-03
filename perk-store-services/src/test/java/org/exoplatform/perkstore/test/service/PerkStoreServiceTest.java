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
package org.exoplatform.perkstore.test.service;

import static org.exoplatform.perkstore.model.constant.ProductOrderStatus.FRAUD;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.*;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.perkstore.exception.PerkStoreException;
import org.exoplatform.perkstore.model.*;
import org.exoplatform.perkstore.model.constant.*;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.perkstore.service.utils.Utils;
import org.exoplatform.perkstore.test.BasePerkStoreTest;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

public class PerkStoreServiceTest extends BasePerkStoreTest {

  /**
   * Check that service is instantiated and functional
   */
  @Test
  public void testServiceInstantiated() {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    assertNotNull(perkStoreService);
  }

  @Test
  public void testGetGlobalSettings() throws Exception { // NOSONAR
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    GlobalSettings globalSettings = perkStoreService.getGlobalSettings();
    assertNotNull(globalSettings);
    checkBasicOperations(globalSettings);

    assertTrue(globalSettings.getAccessPermissions() == null || globalSettings.getAccessPermissions().isEmpty());
    assertTrue(globalSettings.getManagers() == null || globalSettings.getManagers().isEmpty());
    assertTrue(globalSettings.getProductCreationPermissions() == null
        || globalSettings.getProductCreationPermissions().isEmpty());
    assertTrue(globalSettings.getAccessPermissionsProfiles() == null || globalSettings.getAccessPermissionsProfiles().isEmpty());
    assertTrue(globalSettings.getManagersProfiles() == null || globalSettings.getManagersProfiles().isEmpty());
    assertTrue(globalSettings.getProductCreationPermissionsProfiles() == null
        || globalSettings.getProductCreationPermissionsProfiles().isEmpty());
    assertNull(globalSettings.getSymbol());
    assertNull(globalSettings.getUserSettings());

    GlobalSettings globalSettingsTmp = perkStoreService.getGlobalSettings();
    assertNotNull(globalSettingsTmp);
    assertEquals(globalSettingsTmp, globalSettings);
    assertEquals(globalSettingsTmp.toString(), globalSettings.toString());
    assertFalse(globalSettingsTmp == globalSettings);
  }

  @Test
  public void testGetGlobalSettingsForUser() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);

    try {
      perkStoreService.getGlobalSettings(null);
      fail("Shouldn't be able to get global settings for a null user");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    GlobalSettings userGlobalSettings = perkStoreService.getGlobalSettings(USERNAME);

    assertNotNull(userGlobalSettings);

    assertNull(userGlobalSettings.getAccessPermissions());
    assertNull(userGlobalSettings.getManagers());
    assertNull(userGlobalSettings.getAccessPermissionsProfiles());
    assertNull(userGlobalSettings.getManagersProfiles());
    assertNull(userGlobalSettings.getProductCreationPermissions());
    assertNull(userGlobalSettings.getProductCreationPermissionsProfiles());
    assertNull(userGlobalSettings.getSymbol());
    assertNotNull(userGlobalSettings.getUserSettings());

    GlobalSettings userGlobalSettingsTmp = perkStoreService.getGlobalSettings(USERNAME);
    assertNotNull(userGlobalSettingsTmp);
    assertEquals(userGlobalSettingsTmp, userGlobalSettings);
    assertEquals(userGlobalSettingsTmp.toString(), userGlobalSettings.toString());
    assertFalse(userGlobalSettingsTmp == userGlobalSettings);
    checkBasicOperations(userGlobalSettingsTmp);

    assertNotNull(userGlobalSettingsTmp.getUserSettings());
    assertEquals(userGlobalSettingsTmp.getUserSettings(), userGlobalSettings.getUserSettings());
    assertEquals(userGlobalSettingsTmp.getUserSettings().hashCode(), userGlobalSettings.getUserSettings().hashCode());
    assertFalse(userGlobalSettingsTmp.getUserSettings() == userGlobalSettings.getUserSettings());

    assertNotNull(userGlobalSettingsTmp.getUserSettings().getCometdChannel());
    assertNotNull(userGlobalSettingsTmp.getUserSettings().getCometdContext());
    assertNotNull(userGlobalSettingsTmp.getUserSettings().getCometdToken());
    assertFalse(userGlobalSettingsTmp.getUserSettings().isAdministrator());
    assertTrue(userGlobalSettingsTmp.getUserSettings().isCanAddProduct());
    GlobalSettings defaultSettings = perkStoreService.getGlobalSettings();
    GlobalSettings newSettings = new GlobalSettings();
    newSettings.setAccessPermissionsProfiles(Arrays.asList(Utils.toProfile(3l)));
    perkStoreService.saveGlobalSettings(newSettings, USERNAME_ADMIN);
    try {
      perkStoreService.getGlobalSettings(USERNAME);
      fail("User not allowed to access application");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.GLOBAL_SETTINGS_ACCESS_DENIED, e.getErrorType());
    } finally {
      perkStoreService.saveGlobalSettings(defaultSettings.clone(), USERNAME_ADMIN);
    }
  }

  @Test
  public void testSaveGlobalSettings() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    try {
      perkStoreService.saveGlobalSettings(new GlobalSettings(), USERNAME);
      fail("User not member of rewarding administrators group should,'t be able to modify settings");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.GLOBAL_SETTINGS_MODIFICATION_DENIED, e.getErrorType());
    }

    GlobalSettings defaultSettings = perkStoreService.getGlobalSettings();

    GlobalSettings newSettings = new GlobalSettings();
    perkStoreService.saveGlobalSettings(newSettings, USERNAME_ADMIN);
    GlobalSettings globalSettingsTmp = perkStoreService.getGlobalSettings();
    assertNotNull(globalSettingsTmp);
    assertEquals(newSettings, globalSettingsTmp);

    List<Profile> managers = Arrays.asList(new Profile(2l));
    List<Profile> accessPermission = Arrays.asList(new Profile(1l));
    List<Profile> creationPermission = Arrays.asList(new Profile(3l));
    String symbol = "symbol";

    newSettings.setAccessPermissionsProfiles(accessPermission);
    newSettings.setManagersProfiles(managers);
    newSettings.setProductCreationPermissionsProfiles(creationPermission);
    newSettings.setSymbol(symbol);
    perkStoreService.saveGlobalSettings(newSettings, USERNAME_ADMIN);
    try {
      globalSettingsTmp = perkStoreService.getGlobalSettings();
      assertNotNull(globalSettingsTmp);
      assertEquals(newSettings, globalSettingsTmp);

      assertNotNull(globalSettingsTmp.getAccessPermissionsProfiles());
      assertEquals(1, globalSettingsTmp.getAccessPermissionsProfiles().size());
      assertNotNull(globalSettingsTmp.getManagersProfiles());
      assertEquals(1, globalSettingsTmp.getManagersProfiles().size());
      assertNotNull(globalSettingsTmp.getProductCreationPermissionsProfiles());
      assertEquals(1, globalSettingsTmp.getProductCreationPermissionsProfiles().size());
      assertNotNull(globalSettingsTmp.getSymbol());
      assertEquals(symbol, globalSettingsTmp.getSymbol());
    } finally {
      perkStoreService.saveGlobalSettings(defaultSettings.clone(), USERNAME_ADMIN);
    }
  }

  @Test
  public void testIsPerkStoreManager() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    assertTrue(perkStoreService.isPerkStoreManager(USERNAME_ADMIN));
    assertFalse(perkStoreService.isPerkStoreManager(USERNAME));

    GlobalSettings defaultSettings = perkStoreService.getGlobalSettings();
    GlobalSettings newSettings = new GlobalSettings();
    newSettings.setManagersProfiles(Arrays.asList(Utils.toProfile(1l)));
    perkStoreService.saveGlobalSettings(newSettings, USERNAME_ADMIN);
    try {
      assertTrue(perkStoreService.isPerkStoreManager(USERNAME));
    } finally {
      perkStoreService.saveGlobalSettings(defaultSettings, USERNAME_ADMIN);
    }
  }

  @Test
  public void testSaveProduct() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    try {
      perkStoreService.saveProduct(null, USERNAME_ADMIN);
      fail("product should be mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      perkStoreService.saveProduct(new Product(), null);
      fail("username should be mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      perkStoreService.saveProduct(new Product(), USERNAME_ADMIN);
      fail("Some product attributes are required when saving a new product");
    } catch (Exception e) {
      // Expected
    }

    Product product = new Product();
    Product savedProduct = newProduct(perkStoreService, product);

    assertNotNull(savedProduct);
    assertTrue(savedProduct.getId() > 0);
    product.setId(savedProduct.getId());
    assertTrue(savedProduct.getImageFiles() == null || savedProduct.getImageFiles().isEmpty());
    assertEquals(product, savedProduct);
    checkBasicOperations(savedProduct);

    assertEquals(product.getTitle(), savedProduct.getTitle());
    assertEquals(product.getIllustrationURL(), savedProduct.getIllustrationURL());
    assertEquals(product.getDescription(), savedProduct.getDescription());
    assertEquals(product.getAccessPermissions(), savedProduct.getAccessPermissions());
    assertEquals(product.getMarchands(), savedProduct.getMarchands());
    assertEquals(product.isEnabled(), savedProduct.isEnabled());
    assertEquals(product.isAllowFraction(), savedProduct.isAllowFraction());
    assertEquals(product.getPrice(), savedProduct.getPrice(), 0);
    assertEquals(product.getMaxOrdersPerUser(), savedProduct.getMaxOrdersPerUser(), 0);
    assertNotNull(savedProduct.getOrderPeriodicity());
    assertEquals(product.getOrderPeriodicity().toLowerCase(), savedProduct.getOrderPeriodicity().toLowerCase());
    assertEquals(product.isUnlimited(), savedProduct.isUnlimited());
    assertEquals(product.getTotalSupply(), savedProduct.getTotalSupply(), 0);

    assertEquals(0, savedProduct.getNotProcessedOrders(), 0);
    assertEquals(0, savedProduct.getPurchased(), 0);

    savedProduct.setNotProcessedOrders(10);
    savedProduct.setPurchased(10);
    savedProduct = perkStoreService.saveProduct(product, USERNAME);
    assertEquals("NotProcessedOrders should be computed and not saved", 0, savedProduct.getNotProcessedOrders(), 0);
    assertEquals("Purchased should be computed and not saved", 0, savedProduct.getPurchased(), 0);

    product.setImageFiles(new HashSet<>(Arrays.asList(new FileDetail(100l))));
    savedProduct = perkStoreService.saveProduct(product, USERNAME);
    assertEquals("Fake image detail id should have been deleted when saving", 0, savedProduct.getImageFiles().size());

    long savedProductId = savedProduct.getId();
    savedProduct.setId(200l);
    try {
      perkStoreService.saveProduct(savedProduct, USERNAME);
      fail("shouldn't be able to update non existing product");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.PRODUCT_NOT_EXISTS, e.getErrorType());
    }

    savedProduct.setId(savedProductId);
    try {
      perkStoreService.saveProduct(savedProduct, "root2");
      fail("shouldn't be able to update product");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.PRODUCT_MODIFICATION_DENIED, e.getErrorType());
    }

    savedProduct.setDescription(null);
    savedProduct.setIllustrationURL(null);
    savedProduct.setOrderPeriodicity(null);
    savedProduct = perkStoreService.saveProduct(savedProduct, USERNAME);
    assertNotNull(savedProduct);
    assertNull(savedProduct.getDescription());
    assertNull(savedProduct.getIllustrationURL());
    assertNull(savedProduct.getIllustrationURL());
    assertNull(savedProduct.getOrderPeriodicity());
    assertNull(savedProduct.getOrderPeriodicityLabel());
  }

  @Test
  public void testGetProducts() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    List<Product> products = perkStoreService.getProducts(false, USERNAME);
    assertNotNull(products);
    assertEquals(0, products.size());

    newProduct(perkStoreService, new Product());
    products = perkStoreService.getProducts(false, USERNAME);
    assertNotNull(products);
    assertEquals(1, products.size());

    Product savedProduct = newProduct(perkStoreService, new Product());
    products = perkStoreService.getProducts(false,"root2");
    assertNotNull(products);
    assertEquals(2, products.size());

    savedProduct.setAccessPermissions(Arrays.asList(Utils.toProfile(1l)));
    perkStoreService.saveProduct(savedProduct, USERNAME);
    products = perkStoreService.getProducts(false, "root2");
    assertNotNull(products);
    assertEquals(1, products.size());

    products = perkStoreService.getProducts(false, USERNAME_ADMIN);
    assertNotNull(products);
    assertEquals(2, products.size());
  }
  
  @Test
  public void testGetAvailableProducts() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);

    Product savedProduct = newProduct(perkStoreService, new Product());
    Product savedProduct1 = newProduct(perkStoreService, new Product());
    Product savedProduct2 = newProduct(perkStoreService, new Product());
    Product savedProduct3 = newProduct(perkStoreService, new Product());

    List<Product> products = perkStoreService.getProducts(false, USERNAME);
    assertNotNull(products);
    assertEquals(4, products.size());
    
    savedProduct2.setEnabled(false);
    savedProduct3.setEnabled(false);
    perkStoreService.saveProduct(savedProduct2, USERNAME);
    perkStoreService.saveProduct(savedProduct3, USERNAME);


    products = perkStoreService.getProducts(true, USERNAME);
    assertNotNull(products);
    assertEquals(2, products.size());
  }

  @Test
  public void testGetProductById() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    Product product = perkStoreService.getProductById(0);
    assertNull(product);

    Product savedProduct = newProduct(perkStoreService, new Product());
    assertNotNull(savedProduct);

    Product productById = perkStoreService.getProductById(savedProduct.getId());
    assertNotNull(productById);

    productById = perkStoreService.getProductById(savedProduct.getId());
    assertNotNull(productById);
  }

  @Test
  public void testGetProductByIdAndUser() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    Product product = null;
    try {
      product = perkStoreService.getProductById(0, USERNAME);
      fail("Should throw an exception with error message");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.PRODUCT_NOT_EXISTS, e.getErrorType());
    }
    assertNull(product);

    Product savedProduct = newProduct(perkStoreService, new Product());
    assertNotNull(savedProduct);

    Product productById = perkStoreService.getProductById(savedProduct.getId(), USERNAME);
    assertNotNull(productById);

    productById = perkStoreService.getProductById(savedProduct.getId(), USERNAME);
    assertNotNull(productById);
    assertNotNull(productById.getUserData());
    checkBasicOperations(productById.getUserData());
  }

  @Test
  public void testCheckCanCreateOrder() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);

    try {
      perkStoreService.checkCanCreateOrder(null, USERNAME_ADMIN);
      fail("order should be mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    try {
      perkStoreService.checkCanCreateOrder(new ProductOrder(), null);
      fail("username should be mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    try {
      perkStoreService.checkCanCreateOrder(new ProductOrder(), USERNAME_ADMIN);
      fail("productId should be mandatory");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.PRODUCT_NOT_EXISTS, e.getErrorType());
    }

    try {
      ProductOrder order = new ProductOrder();
      order.setProductId(200l);
      perkStoreService.checkCanCreateOrder(new ProductOrder(), USERNAME_ADMIN);
      fail("productId should exists");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.PRODUCT_NOT_EXISTS, e.getErrorType());
    }

    Product savedProduct = newProduct(perkStoreService, new Product());

    GlobalSettings defaultSettings = perkStoreService.getGlobalSettings();
    GlobalSettings newSettings = new GlobalSettings();
    newSettings.setAccessPermissionsProfiles(Arrays.asList(Utils.toProfile(2l)));
    perkStoreService.saveGlobalSettings(newSettings, USERNAME_ADMIN);
    try {
      try {
        ProductOrder order = newOrderInstance(savedProduct);
        perkStoreService.checkCanCreateOrder(order, USERNAME);
        fail("Shouldn't be able to create a new order when not member of access permission profiles for application");
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.ORDER_MODIFICATION_DENIED, e.getErrorType());
      }

      newSettings.setAccessPermissionsProfiles(Arrays.asList(Utils.toProfile(1l)));
      perkStoreService.saveGlobalSettings(newSettings, USERNAME_ADMIN);
      try {
        ProductOrder order = newOrderInstance(savedProduct);
        perkStoreService.checkCanCreateOrder(order, USERNAME);
        fail("Shouldn't be able to create a new order on inaccessible product");
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.ORDER_MODIFICATION_DENIED, e.getErrorType());
      }

      savedProduct.setAccessPermissions(Arrays.asList(Utils.toProfile(1l)));
      perkStoreService.saveProduct(savedProduct, USERNAME);

      ProductOrder savedOrder = newOrder(savedProduct);

      try {
        ProductOrder order = newOrderInstance(savedProduct);
        perkStoreService.checkCanCreateOrder(order, USERNAME);
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.ORDER_CREATION_QUANTITY_EXCEEDS_ALLOWED, e.getErrorType());
      }

      savedProduct.setTotalSupply(2);
      perkStoreService.saveProduct(savedProduct, USERNAME);
      try {
        ProductOrder order = newOrderInstance(savedProduct);
        perkStoreService.checkCanCreateOrder(order, USERNAME);
        fail("shouldn't be able to order again");
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.ORDER_CREATION_QUANTITY_EXCEEDS_ALLOWED, e.getErrorType());
      }

      savedProduct.setMaxOrdersPerUser(2);
      perkStoreService.saveProduct(savedProduct, USERNAME);

      savedProduct = perkStoreService.getProductById(savedProduct.getId());

      try {
        ProductOrder order = newOrderInstance(savedProduct);
        order.setTransactionHash(savedOrder.getTransactionHash());
        perkStoreService.checkCanCreateOrder(order, USERNAME);
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.ORDER_CREATION_DENIED, e.getErrorType());
      }

      ProductOrder order = newOrderInstance(savedProduct);
      perkStoreService.checkCanCreateOrder(order, USERNAME);

      try {
        ProductOrder newOrder = newOrderInstance(savedProduct);
        newOrder.setId(500);
        perkStoreService.checkCanCreateOrder(newOrder, USERNAME_ADMIN);
        fail("productId should exists");
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.ORDER_MODIFICATION_DENIED, e.getErrorType());
      }

      savedProduct.setEnabled(false);
      savedProduct = perkStoreService.saveProduct(savedProduct, USERNAME);
      try {
        ProductOrder newOrder = newOrderInstance(savedProduct);
        perkStoreService.checkCanCreateOrder(newOrder, USERNAME_ADMIN);
        fail("productId should exists");
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.PRODUCT_IS_DISABLED, e.getErrorType());
      }
    } finally {
      perkStoreService.saveGlobalSettings(defaultSettings, USERNAME_ADMIN);
    }
  }

  @Test
  public void testCreateOrder() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    Product savedProduct = newProduct(perkStoreService, new Product());

    GlobalSettings defaultSettings = perkStoreService.getGlobalSettings();
    GlobalSettings newSettings = new GlobalSettings();
    newSettings.setAccessPermissionsProfiles(Arrays.asList(Utils.toProfile(2l)));
    perkStoreService.saveGlobalSettings(newSettings, USERNAME_ADMIN);
    try {
      ProductOrder savedOrder = null;
      try {
        savedOrder = newOrder(savedProduct);
        fail("Shouldn't be able to create a new order when not member of access permission profiles for application");
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.ORDER_MODIFICATION_DENIED, e.getErrorType());
      }
      assertNull(savedOrder);

      newSettings.setAccessPermissionsProfiles(Arrays.asList(Utils.toProfile(1l)));
      perkStoreService.saveGlobalSettings(newSettings, USERNAME_ADMIN);
      try {
        savedOrder = newOrder(savedProduct);
        fail("Shouldn't be able to create a new order on inaccessible product");
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.ORDER_MODIFICATION_DENIED, e.getErrorType());
      }
      assertNull(savedOrder);

      savedProduct.setAccessPermissions(Arrays.asList(Utils.toProfile(1l)));
      perkStoreService.saveProduct(savedProduct, USERNAME);

      savedOrder = newOrder(savedProduct);
      assertNotNull(savedOrder);
      assertNull(savedOrder.getError());

      try {
        savedOrder = newOrder(savedProduct);
        fail("shouldn't be able to order again");
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.ORDER_CREATION_QUANTITY_EXCEEDS_ALLOWED, e.getErrorType());
      }

      savedProduct.setTotalSupply(2);
      perkStoreService.saveProduct(savedProduct, USERNAME);
      try {
        savedOrder = newOrder(savedProduct);
        fail("shouldn't be able to order again");
      } catch (PerkStoreException e) {
        assertNotNull(e.getErrorType());
        assertNotNull(e.getLocalizedMessage());
        assertEquals(PerkStoreError.ORDER_CREATION_QUANTITY_EXCEEDS_ALLOWED, e.getErrorType());
      }

      savedProduct.setMaxOrdersPerUser(2);
      perkStoreService.saveProduct(savedProduct, USERNAME);
      newOrder(savedProduct);
    } finally {
      perkStoreService.saveGlobalSettings(defaultSettings, USERNAME_ADMIN);
    }
  }

  @Test
  public void testReplaceOrderHash() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    Product product = newProduct(perkStoreService, new Product());
    product.setAccessPermissions(null);
    product = perkStoreService.saveProduct(product, USERNAME);
    ProductOrder order = newOrder(product);
    assertNotNull(order);
    assertNull(order.getError());

    String newHash = generateRandomHash();
    perkStoreService.replaceTransactions(order.getTransactionHash(), newHash);
    ProductOrder replacedOrder = perkStoreService.getOrderById(order.getId());
    assertEquals(newHash, replacedOrder.getTransactionHash());
  }

  @Test
  public void testCreateOrderWithWrongReceiverFraud() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    Product product = newProductInstance(new Product());
    product.setAccessPermissions(null);
    Product savedProduct = perkStoreService.saveProduct(product, USERNAME);
    entitiesToClean.add(0, savedProduct);

    ProductOrder productOrder = newOrderInstance(savedProduct);
    productOrder.setReceiver(Utils.toProfile(3l));
    ProductOrder savedOrder = perkStoreService.createOrder(productOrder, USERNAME);
    entitiesToClean.add(0, savedOrder);

    assertNotNull(savedOrder);
    assertEquals(ProductOrderStatus.FRAUD.name(), savedOrder.getStatus());
    assertEquals(PerkStoreError.ORDER_FRAUD_WRONG_RECEIVER, savedOrder.getError());
  }

  @Test
  public void testSaveOrder() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);

    try {
      perkStoreService.saveOrder(null, ProductOrderModificationType.NEW, USERNAME, true);
      fail("Shouldn't be able to save a null order");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      perkStoreService.saveOrder(new ProductOrder(), null, USERNAME, true);
      fail("Shouldn't be able to save a null modificationType");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      perkStoreService.saveOrder(new ProductOrder(), ProductOrderModificationType.NEW, USERNAME, true);
      fail("Shouldn't be able to save an order without an associated product");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.ORDER_CREATION_EMPTY_PRODUCT, e.getErrorType());
    }
    try {
      ProductOrder order = new ProductOrder();
      order.setProductId(1l);
      perkStoreService.saveOrder(order, ProductOrderModificationType.NEW, USERNAME, true);
      fail("Shouldn't be able to save an order with non existing product");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.ORDER_CREATION_EMPTY_PRODUCT, e.getErrorType());
    }

    Product savedProduct = newProductInstance(new Product());
    savedProduct.setAccessPermissions(Arrays.asList(Utils.toProfile(1l)));
    savedProduct = perkStoreService.saveProduct(savedProduct, USERNAME);
    entitiesToClean.add(savedProduct);

    try {
      ProductOrder order = new ProductOrder();
      order.setProductId(savedProduct.getId());
      perkStoreService.saveOrder(order, ProductOrderModificationType.NEW, USERNAME, true);
      fail("Shouldn't be able to save a non existing order");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.ORDER_NOT_EXISTS, e.getErrorType());
    }

    ProductOrder savedOrder = newOrder(savedProduct);
    checkBasicOperations(savedOrder);
    try {
      perkStoreService.saveOrder(savedOrder, ProductOrderModificationType.NEW, "root3", true);
      fail("Shouldn't be able to modify order when not manager of product neither on perk store");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.ORDER_MODIFICATION_DENIED, e.getErrorType());
    }

  }

  @Test
  public void testGetOrderById() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    ProductOrder orderById = perkStoreService.getOrderById(0);
    assertNull(orderById);

    Product savedProduct = newProductInstance(new Product());
    savedProduct.setAccessPermissions(Arrays.asList(Utils.toProfile(1l)));
    savedProduct = perkStoreService.saveProduct(savedProduct, USERNAME);
    entitiesToClean.add(savedProduct);

    ProductOrder savedOrder = newOrder(savedProduct);
    checkBasicOperations(savedOrder);

    orderById = perkStoreService.getOrderById(savedOrder.getId());
    assertNotNull(orderById);
    checkBasicOperations(savedOrder);

    assertEquals(savedOrder, orderById);
  }

  @Test
  public void testGetOrders() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);

    OrderFilter filter = new OrderFilter();
    checkBasicOperations(filter);

    filter.setLimit(10);
    checkBasicOperations(filter);

    try {
      perkStoreService.getOrders(null, USERNAME);
      fail("filter shouldn't be null");
    } catch (IllegalArgumentException e1) {
      // Expected
    }
    try {
      perkStoreService.getOrders(filter, null);
      fail("username shouldn't be null");
    } catch (IllegalArgumentException e1) {
      // Expected
    }

    List<ProductOrder> orders = perkStoreService.getOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    Product savedProduct = newProductInstance(new Product());
    savedProduct.setAccessPermissions(Arrays.asList(Utils.toProfile(1l)));
    savedProduct = perkStoreService.saveProduct(savedProduct, USERNAME);
    entitiesToClean.add(savedProduct);

    orders = perkStoreService.getOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    ProductOrder savedOrder = newOrder(savedProduct);

    orders = perkStoreService.getOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    filter.setMyOrders(true);
    filter.setOrdersType(ProductOrderType.ALL);
    orders = perkStoreService.getOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    filter.setOrdersType(ProductOrderType.RECEIVED);
    orders = perkStoreService.getOrders(filter, "root10");
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    filter.setOrdersType(ProductOrderType.SEND);
    orders = perkStoreService.getOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);
    filter.setMyOrders(false);
    filter.setOrdersType(null);

    orders = perkStoreService.getOrders(filter, USERNAME);
    filter.setOrdered(true);
    checkBasicOperations(filter);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    orders = perkStoreService.getOrders(filter, USERNAME);
    filter.setNotProcessed(true);
    checkBasicOperations(filter);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    orders = perkStoreService.getOrders(filter, USERNAME);
    filter.setProductId(savedProduct.getId());
    checkBasicOperations(filter);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    orders = perkStoreService.getOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    try {
      filter.setSelectedOrderId(savedOrder.getId());
      checkBasicOperations(filter);
      perkStoreService.getOrders(filter, "root5");
      fail("User 'root5' shouldn't be able to access product");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.PRODUCT_ACCESS_DENIED, e.getErrorType());
    }

    savedProduct.setAccessPermissions(null);
    perkStoreService.saveProduct(savedProduct, USERNAME);
    try {
      filter.setSelectedOrderId(savedOrder.getId());
      checkBasicOperations(filter);
      perkStoreService.getOrders(filter, "root5");
      fail("User 'root5' shouldn't be able to access order of 'user1'");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.ORDER_ACCESS_DENIED, e.getErrorType());
    }

    orders = perkStoreService.getOrders(filter, USERNAME);
    filter.setSearchInDates(true);
    filter.setSelectedDate(System.currentTimeMillis());
    checkBasicOperations(filter);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    filter.setSelectedOrderId(0);
    orders = perkStoreService.getOrders(filter, USERNAME);
    filter.setSearchInDates(true);
    filter.setSelectedDate(System.currentTimeMillis());
    checkBasicOperations(filter);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    String username = savedOrder.getSender().getId();

    orders = perkStoreService.getOrders(filter, username);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    RequestLifeCycle.end();
    RequestLifeCycle.begin(container);

    savedProduct = perkStoreService.getProductById(savedProduct.getId(), username);
    checkBasicOperations(savedProduct.getUserData());
    assertEquals(1, savedProduct.getPurchased(), 0);
    assertEquals(1, savedProduct.getUserData().getPurchasedInCurrentPeriod(), 0);
    assertEquals(1, savedProduct.getUserData().getTotalPurchased(), 0);
    assertEquals(username, savedProduct.getUserData().getUsername());
    assertTrue(savedProduct.getUserData().isCanOrder());
    assertTrue(savedProduct.getUserData().isCanEdit());

    // Test with not existing identity
    try {
      perkStoreService.getOrders(filter, "test-non-existing");
      fail("Non existing user shouldn't be able to access application");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.GLOBAL_SETTINGS_ACCESS_DENIED, e.getErrorType());
    }

    orders = perkStoreService.getOrders(filter, "root30");
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    OrderFilter filterTmp = filter.clone();
    filterTmp.setSelectedOrderId(200l);
    try {
      perkStoreService.getOrders(filterTmp, USERNAME);
      fail("User 'root1' shouldn't be able to access not his order");
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.ORDER_ACCESS_DENIED, e.getErrorType());
    }

    filterTmp = filter.clone();
    filterTmp.setProductId(200l);
    try {
      perkStoreService.getOrders(filterTmp, USERNAME);
    } catch (PerkStoreException e) {
      assertNotNull(e.getErrorType());
      assertNotNull(e.getLocalizedMessage());
      assertEquals(PerkStoreError.PRODUCT_NOT_EXISTS, e.getErrorType());
    }

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setDelivered(true);
    orders = perkStoreService.getOrders(filterTmp, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setCanceled(true);
    orders = perkStoreService.getOrders(filterTmp, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setPaid(true);
    orders = perkStoreService.getOrders(filterTmp, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setPartial(true);
    orders = perkStoreService.getOrders(filterTmp, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setError(true);
    orders = perkStoreService.getOrders(filterTmp, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setRefunded(true);
    orders = perkStoreService.getOrders(filterTmp, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setFraud(true);
    orders = perkStoreService.getOrders(filterTmp, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setSearchInDates(true);
    filterTmp.setSelectedDate(System.currentTimeMillis() - 86400000l);
    orders = perkStoreService.getOrders(filterTmp, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);
  }
  
  @Test
  public void testCountOrders() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    
    OrderFilter filter = new OrderFilter();
    checkBasicOperations(filter);
    
    filter.setLimit(10);
    filter.setNotProcessed(true);
    checkBasicOperations(filter);
    
    try {
      perkStoreService.countOrders(null, USERNAME);
      fail("filter shouldn't be null");
    } catch (IllegalArgumentException e1) {
      // Expected
    }
    try {
      perkStoreService.countOrders(filter, null);
      fail("username shouldn't be null");
    } catch (IllegalArgumentException e1) {
      // Expected
    }
    Long orders = perkStoreService.countOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders, 0);
    
    Product[] products = new Product[5];
    
    for (int i = 0; i < 5; i++) {
      products[i] = newProductInstance(new Product());
      products[i].setAccessPermissions(Arrays.asList(Utils.toProfile(1l)));
      products[i] = perkStoreService.saveProduct(products[i], USERNAME_ADMIN);
      entitiesToClean.add(products[i]);
      newOrder(products[i]);
    }
    
    orders = perkStoreService.countOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(5, orders.intValue());
    }
    
  @Test
  public void testGetOrdersWithFraud() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);

    OrderFilter filter = new OrderFilter();
    filter.setLimit(10);

    Product savedProduct = newProductInstance(new Product());
    savedProduct.setAccessPermissions(Arrays.asList(Utils.toProfile(1l)));
    savedProduct = perkStoreService.saveProduct(savedProduct, USERNAME);
    entitiesToClean.add(savedProduct);

    ProductOrder savedOrder = newOrder(savedProduct);

    filter.setFraud(true);
    List<ProductOrder> orders = perkStoreService.getOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    savedOrder.setStatus(FRAUD.name());
    perkStoreService.saveOrder(savedOrder, ProductOrderModificationType.STATUS, USERNAME, false);

    orders = perkStoreService.getOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    filter.setFraud(true);
    orders = perkStoreService.getOrders(filter, USERNAME);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    filter.setFraud(false);
    filter.setNotProcessed(true);
    orders = perkStoreService.getOrders(filter, USERNAME);
    assertEquals(0, orders.size(), 0);

    filter.setNotProcessed(false);
    filter.setOrdered(true);
    orders = perkStoreService.getOrders(filter, USERNAME);
    assertEquals(0, orders.size(), 0);

  }

  @Test
  public void testSaveOrderTransactionStatus() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);

    try {
      perkStoreService.saveOrderTransactionStatus(null);
      fail("transaction hash shoud be mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    try {
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("hash", generateRandomHash());
      parameters.put("status", true);

      perkStoreService.saveOrderTransactionStatus(parameters);
    } catch (Exception e) {
      fail("Shouldn't fail when no order corresponds to mined hash");
    }

    Product savedProduct = newProduct(perkStoreService, new Product());
    savedProduct.setAccessPermissions(Arrays.asList(Utils.toProfile(1l)));
    savedProduct.setMaxOrdersPerUser(2);
    savedProduct.setTotalSupply(2);
    perkStoreService.saveProduct(savedProduct, USERNAME);

    ProductOrder savedOrder = newOrder(savedProduct);

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("hash", savedOrder.getTransactionHash());
    parameters.put("from", savedOrder.getSender().getTechnicalId());
    parameters.put("to", savedOrder.getReceiver().getTechnicalId());
    parameters.put("contractAddress", "contractAddress");
    parameters.put("contractMethodName", "transfer");
    parameters.put("contractAmount", savedOrder.getAmount());
    parameters.put("issuerId", savedOrder.getSender().getTechnicalId());
    parameters.put("status", false);

    perkStoreService.saveOrderTransactionStatus(parameters);
    savedOrder = perkStoreService.getOrderById(savedOrder.getId());
    assertEquals(ProductOrderStatus.ERROR.name(), savedOrder.getStatus());
    assertNull(savedOrder.getError());

    parameters.put("status", true);
    perkStoreService.saveOrderTransactionStatus(parameters);
    savedOrder = perkStoreService.getOrderById(savedOrder.getId());
    assertEquals(ProductOrderStatus.PAID.name(), savedOrder.getStatus());
    assertNull(savedOrder.getError());

    savedOrder.setRefundTransactionHash(generateRandomHash());
    savedOrder.setRefundedQuantity(1);
    perkStoreService.saveOrder(savedOrder, ProductOrderModificationType.REFUNDED_QUANTITY, USERNAME, true);
    savedOrder = perkStoreService.getOrderById(savedOrder.getId());
    assertEquals(1, savedOrder.getRefundedQuantity(), 0);
    assertEquals(ProductOrderStatus.REFUNDED.name(), savedOrder.getStatus());
    assertEquals(ProductOrderTransactionStatus.PENDING.name(), savedOrder.getRefundTransactionStatus());
    assertNull(savedOrder.getError());

    parameters.put("hash", savedOrder.getRefundTransactionHash());
    parameters.put("to", savedOrder.getSender().getTechnicalId());
    parameters.put("from", savedOrder.getReceiver().getTechnicalId());
    parameters.put("contractAddress", "contractAddress");
    parameters.put("contractMethodName", "transfer");
    parameters.put("contractAmount", savedOrder.getAmount());
    parameters.put("issuerId", savedOrder.getReceiver().getTechnicalId());
    parameters.put("status", false);
    perkStoreService.saveOrderTransactionStatus(parameters);
    savedOrder = perkStoreService.getOrderById(savedOrder.getId());
    assertEquals(ProductOrderTransactionStatus.FAILED.name(), savedOrder.getRefundTransactionStatus());
    assertNull(savedOrder.getError());

    parameters.put("status", true);
    perkStoreService.saveOrderTransactionStatus(parameters);
    savedOrder = perkStoreService.getOrderById(savedOrder.getId());
    assertEquals(ProductOrderTransactionStatus.SUCCESS.name(), savedOrder.getRefundTransactionStatus());
    assertNull(savedOrder.getError());
  }

  @Test
  public void testSaveOrderTransactionStatusWithEmptyTokenAddressFraud() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    Product product = newProductInstance(new Product());
    product.setAccessPermissions(null);
    Product savedProduct = perkStoreService.saveProduct(product, USERNAME);
    entitiesToClean.add(0, savedProduct);

    ProductOrder savedOrder = newOrder(savedProduct);
    assertNull(savedOrder.getError());

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("hash", savedOrder.getTransactionHash());
    parameters.put("from", savedOrder.getSender().getTechnicalId());
    parameters.put("to", savedOrder.getReceiver().getTechnicalId());
    parameters.put("contractMethodName", "transfer");
    parameters.put("contractAmount", savedOrder.getAmount());
    parameters.put("issuerId", savedOrder.getSender().getTechnicalId());
    parameters.put("status", true);
    perkStoreService.saveOrderTransactionStatus(parameters);

    savedOrder = perkStoreService.getOrderById(savedOrder.getId());
    assertNotNull(savedOrder);
    assertEquals(ProductOrderStatus.FRAUD.name(), savedOrder.getStatus());
    assertEquals(PerkStoreError.ORDER_FRAUD_NOT_TOKEN_TRANSACTION, savedOrder.getError());
  }

  @Test
  public void testSaveOrderTransactionStatusWithWrongContractMethodFraud() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    Product product = newProductInstance(new Product());
    product.setAccessPermissions(null);
    Product savedProduct = perkStoreService.saveProduct(product, USERNAME);
    entitiesToClean.add(0, savedProduct);

    ProductOrder savedOrder = newOrder(savedProduct);
    assertNull(savedOrder.getError());

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("hash", savedOrder.getTransactionHash());
    parameters.put("from", savedOrder.getSender().getTechnicalId());
    parameters.put("to", savedOrder.getReceiver().getTechnicalId());
    parameters.put("contractAddress", "contractAddress");
    parameters.put("contractMethodName", "reward");
    parameters.put("contractAmount", savedOrder.getAmount());
    parameters.put("issuerId", savedOrder.getSender().getTechnicalId());
    parameters.put("status", true);
    perkStoreService.saveOrderTransactionStatus(parameters);

    savedOrder = perkStoreService.getOrderById(savedOrder.getId());
    assertNotNull(savedOrder);
    assertEquals(ProductOrderStatus.FRAUD.name(), savedOrder.getStatus());
    assertEquals(PerkStoreError.ORDER_FRAUD_WRONG_TOKEN_TRANSFER_METHOD, savedOrder.getError());
  }

  @Test
  public void testSaveOrderTransactionStatusWithWrongSenderFraud() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    Product product = newProductInstance(new Product());
    product.setAccessPermissions(null);
    Product savedProduct = perkStoreService.saveProduct(product, USERNAME);
    entitiesToClean.add(0, savedProduct);

    ProductOrder savedOrder = newOrder(savedProduct);
    assertNull(savedOrder.getError());

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("hash", savedOrder.getTransactionHash());
    parameters.put("from", 9l);
    parameters.put("to", savedOrder.getReceiver().getTechnicalId());
    parameters.put("contractAddress", "contractAddress");
    parameters.put("contractMethodName", "transfer");
    parameters.put("contractAmount", savedOrder.getAmount());
    parameters.put("issuerId", savedOrder.getSender().getTechnicalId());
    parameters.put("status", true);
    perkStoreService.saveOrderTransactionStatus(parameters);

    savedOrder = perkStoreService.getOrderById(savedOrder.getId());
    assertNotNull(savedOrder);
    assertEquals(ProductOrderStatus.FRAUD.name(), savedOrder.getStatus());
    assertEquals(PerkStoreError.ORDER_FRAUD_WRONG_SENDER, savedOrder.getError());
  }

  @Test
  public void testSaveOrderTransactionStatusWithWrongReceiverFraud() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    Product product = newProductInstance(new Product());
    product.setAccessPermissions(null);
    Product savedProduct = perkStoreService.saveProduct(product, USERNAME);
    entitiesToClean.add(0, savedProduct);

    ProductOrder savedOrder = newOrder(savedProduct);
    assertNull(savedOrder.getError());

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("hash", savedOrder.getTransactionHash());
    parameters.put("from", savedOrder.getSender().getTechnicalId());
    parameters.put("to", 8l);
    parameters.put("contractAddress", "contractAddress");
    parameters.put("contractMethodName", "transfer");
    parameters.put("contractAmount", savedOrder.getAmount());
    parameters.put("issuerId", savedOrder.getSender().getTechnicalId());
    parameters.put("status", true);
    perkStoreService.saveOrderTransactionStatus(parameters);

    savedOrder = perkStoreService.getOrderById(savedOrder.getId());
    assertNotNull(savedOrder);
    assertEquals(ProductOrderStatus.FRAUD.name(), savedOrder.getStatus());
    assertEquals(PerkStoreError.ORDER_FRAUD_WRONG_RECEIVER, savedOrder.getError());
  }

  @Test
  public void testSaveOrderTransactionStatusWithWrongAmountFraud() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);
    Product product = newProductInstance(new Product());
    product.setAccessPermissions(null);
    Product savedProduct = perkStoreService.saveProduct(product, USERNAME);
    entitiesToClean.add(0, savedProduct);

    ProductOrder savedOrder = newOrder(savedProduct);
    assertNull(savedOrder.getError());

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("hash", savedOrder.getTransactionHash());
    parameters.put("from", savedOrder.getSender().getTechnicalId());
    parameters.put("to", savedOrder.getReceiver().getTechnicalId());
    parameters.put("contractAddress", "contractAddress");
    parameters.put("contractMethodName", "transfer");
    parameters.put("contractAmount", savedProduct.getPrice() * savedOrder.getQuantity() * 2);
    parameters.put("issuerId", savedOrder.getSender().getTechnicalId());
    parameters.put("status", true);
    perkStoreService.saveOrderTransactionStatus(parameters);

    savedOrder = perkStoreService.getOrderById(savedOrder.getId());
    assertNotNull(savedOrder);
    assertEquals(ProductOrderStatus.FRAUD.name(), savedOrder.getStatus());
    assertEquals(PerkStoreError.ORDER_FRAUD_WRONG_AMOUNT, savedOrder.getError());
  }

  @Test
  public void testGetFileDetail() throws Exception {
    PerkStoreService perkStoreService = getService(PerkStoreService.class);

    ConfigurationManager configurationManager = getService(ConfigurationManager.class);
    URL url = configurationManager.getResource("classpath:/conf/configuration.properties");
    assertNotNull(url);

    UploadService uploadService = Mockito.mock(UploadService.class);
    Mockito.when(uploadService.getUploadResource(Mockito.any())).thenAnswer(new Answer<UploadResource>() {
      @Override
      public UploadResource answer(InvocationOnMock invocation) throws Throwable {
        String uploadId = invocation.getArgumentAt(0, String.class);
        UploadResource upResource = new UploadResource(uploadId);
        upResource.setFileName(uploadId);
        upResource.setMimeType("image/png");
        upResource.setStoreLocation(url.getFile());
        upResource.setEstimatedSize(0);
        return upResource;
      }
    });
    container.registerComponentInstance(UploadService.class, uploadService);
    try {
      Product savedProduct = newProduct(perkStoreService, new Product());
      FileDetail fileDetail = perkStoreService.getFileDetail(savedProduct.getId(), 1l, true, USERNAME);
      assertNull(fileDetail);

      fileDetail = new FileDetail();
      checkBasicOperations(fileDetail);
      fileDetail.setUploadId(generateRandomHash());
      HashSet<FileDetail> imageFiles = new HashSet<>();
      imageFiles.add(fileDetail);
      savedProduct.setImageFiles(imageFiles);
      savedProduct = perkStoreService.saveProduct(savedProduct, USERNAME);

      assertNotNull(savedProduct);
      assertNotNull(savedProduct.getImageFiles());
      assertEquals(1, savedProduct.getImageFiles().size());
      checkBasicOperations(savedProduct.getImageFiles().iterator().next());

      savedProduct.setImageFiles(Collections.emptySet());
      savedProduct = perkStoreService.saveProduct(savedProduct, USERNAME);
      assertNotNull(savedProduct);
      assertTrue(savedProduct.getImageFiles() == null || savedProduct.getImageFiles().isEmpty());
    } finally {
      container.unregisterComponent(UploadService.class);
    }
  }
}
