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
package org.exoplatform.perkstore.test.dao;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.exoplatform.perkstore.dao.PerkStoreOrderDAO;
import org.exoplatform.perkstore.entity.ProductEntity;
import org.exoplatform.perkstore.entity.ProductOrderEntity;
import org.exoplatform.perkstore.model.OrderFilter;
import org.exoplatform.perkstore.model.constant.ProductOrderStatus;
import org.exoplatform.perkstore.test.BasePerkStoreTest;

public class ProductOrderDAOTest extends BasePerkStoreTest {

  /**
   * Check that service is instantiated and functional
   */
  @Test
  public void testServiceInstantiated() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);
    assertNotNull(orderDAO);
  }

  @Test
  public void testDeleteAll() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);
    try {
      orderDAO.deleteAll();
      fail("shouldn't be able to delete all entities");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testDeleteAllEntities() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);
    try {
      orderDAO.deleteAll(Collections.emptyList());
      fail("shouldn't be able to delete all entities");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testCountOrders() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    Long count = orderDAO.count();
    assertEquals(0, count, 0);
  }

  @Test
  public void testCountOrderedQuantityByProductId() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    double count = orderDAO.countOrderedQuantityByProductId(1);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countOrderedQuantityByProductId(productEntity.getId());
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrder(productEntity);

    count = orderDAO.countOrderedQuantityByProductId(productEntity.getId());
    assertEquals(orderEntity.getQuantity(), count, 0);
  }

  @Test
  public void testCountOrderedQuantityByProductIdAndStatus() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    double count = orderDAO.countOrderedQuantityByProductIdAndStatus(1l, ProductOrderStatus.ORDERED);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countOrderedQuantityByProductIdAndStatus(1l, ProductOrderStatus.ORDERED);
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrder(productEntity);

    count = orderDAO.countOrderedQuantityByProductIdAndStatus(productEntity.getId(), orderEntity.getStatus());
    assertEquals(orderEntity.getQuantity(), count, 0);
  }

  @Test
  public void testCountRefundedQuantityByProductId() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    double count = orderDAO.countRefundedQuantityByProductId(1l);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countRefundedQuantityByProductId(1l);
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrderInstance(productEntity);
    int refundedQuantity = 1;
    orderEntity.setRefundedQuantity(refundedQuantity);
    orderEntity = orderDAO.create(orderEntity);
    entitiesToClean.add(0, orderEntity);

    count = orderDAO.countRefundedQuantityByProductId(productEntity.getId());
    assertEquals(refundedQuantity, count, 0);
  }

  @Test
  public void testCountRemainingOrdersToProcessByProductId() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    double count = orderDAO.countRemainingOrdersToProcessByProductId(1l);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countRemainingOrdersToProcessByProductId(1l);
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrderInstance(productEntity);
    int remainingQuantity = 1;
    orderEntity.setRemainingQuantity(remainingQuantity);
    orderEntity = orderDAO.create(orderEntity);
    entitiesToClean.add(0, orderEntity);

    count = orderDAO.countRemainingOrdersToProcessByProductId(productEntity.getId());
    assertEquals(remainingQuantity, count, 0);
  }

  @Test
  public void testCountRemainingOrdersByIdentityIdAndProductId() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    double count = orderDAO.countRemainingOrdersByIdentityIdAndProductId(1l, 1l);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countRemainingOrdersByIdentityIdAndProductId(1l, 1l);
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrderInstance(productEntity);
    int remainingQuantity = 1;
    orderEntity.setRemainingQuantity(remainingQuantity);
    orderEntity = orderDAO.create(orderEntity);
    entitiesToClean.add(0, orderEntity);

    count = orderDAO.countRemainingOrdersByIdentityIdAndProductId(orderEntity.getSenderId(), productEntity.getId());
    assertEquals(remainingQuantity, count, 0);

    count = orderDAO.countRemainingOrdersByIdentityIdAndProductId(20l, productEntity.getId());
    assertEquals(0, count, 0);
  }

  @Test
  public void testCountUserTotalPurchasedQuantity() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    double count = orderDAO.countUserTotalPurchasedQuantity(1l, 1l);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countUserTotalPurchasedQuantity(1l, 1l);
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrder(productEntity);

    count = orderDAO.countUserTotalPurchasedQuantity(productEntity.getId(), orderEntity.getSenderId());
    assertEquals(orderEntity.getQuantity(), count, 0);

    count = orderDAO.countUserTotalPurchasedQuantity(productEntity.getId(), 20l);
    assertEquals(0, count, 0);
  }

  @Test
  public void testFindOrderByTransactionHash() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    ProductOrderEntity orderToFind = orderDAO.findOrderByTransactionHash(generateRandomHash());
    assertNull(orderToFind);

    ProductEntity productEntity = newProduct();
    ProductOrderEntity orderEntity = newOrder(productEntity);

    orderToFind = orderDAO.findOrderByTransactionHash(orderEntity.getTransactionHash());
    assertNotNull(orderToFind);

    orderToFind = orderDAO.findOrderByTransactionHash(generateRandomHash());
    assertNull(orderToFind);
  }

  @Test
  public void findOrderByRefundTransactionHash() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    ProductOrderEntity orderToFind = orderDAO.findOrderByRefundTransactionHash(generateRandomHash());
    assertNull(orderToFind);

    ProductEntity productEntity = newProduct();
    ProductOrderEntity orderEntity = newOrderInstance(productEntity);
    orderEntity.setRefundTransactionHash(generateRandomHash());
    orderEntity = orderDAO.create(orderEntity);
    entitiesToClean.add(0, orderEntity);

    orderToFind = orderDAO.findOrderByRefundTransactionHash(orderEntity.getRefundTransactionHash());
    assertNotNull(orderToFind);

    orderToFind = orderDAO.findOrderByRefundTransactionHash(generateRandomHash());
    assertNull(orderToFind);
  }

  @Test
  public void testGetOrders() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    OrderFilter filter = new OrderFilter();
    filter.setLimit(10);

    List<ProductOrderEntity> orders = orderDAO.getOrders(null, filter);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    ProductEntity productEntity = newProduct();

    orders = orderDAO.getOrders(null, filter);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    ProductOrderEntity orderEntity = newOrder(productEntity);

    orders = orderDAO.getOrders(null, filter);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    orders = orderDAO.getOrders(null, filter);
    filter.setOrdered(true);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    orders = orderDAO.getOrders(null, filter);
    filter.setNotProcessed(true);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    orders = orderDAO.getOrders(null, filter);
    filter.setProductId(productEntity.getId());
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    orders = orderDAO.getOrders(null, filter);
    filter.setSelectedOrderId(orderEntity.getId());
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    orders = orderDAO.getOrders(null, filter);
    filter.setSearchInDates(true);
    filter.setSelectedDate(System.currentTimeMillis());
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    String username = "root" + orderEntity.getSenderId();

    orders = orderDAO.getOrders(username, filter);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    // Test with not existing identity
    orders = orderDAO.getOrders("test", filter);
    assertNotNull(orders);
    assertEquals(1, orders.size(), 0);

    orders = orderDAO.getOrders("root30", filter);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    OrderFilter filterTmp = filter.clone();
    filterTmp.setSelectedOrderId(200l);
    orders = orderDAO.getOrders(null, filterTmp);
    assertNotNull(orders);
    // Selected order id is processed in Service layer
    assertEquals(1, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setProductId(200l);
    orders = orderDAO.getOrders(null, filterTmp);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setDelivered(true);
    orders = orderDAO.getOrders(null, filterTmp);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setCanceled(true);
    orders = orderDAO.getOrders(null, filterTmp);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setPaid(true);
    orders = orderDAO.getOrders(null, filterTmp);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setPartial(true);
    orders = orderDAO.getOrders(null, filterTmp);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setError(true);
    orders = orderDAO.getOrders(null, filterTmp);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setNotProcessed(false);
    filterTmp.setOrdered(false);
    filterTmp.setRefunded(true);
    orders = orderDAO.getOrders(null, filterTmp);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    filterTmp = filter.clone();
    filterTmp.setSearchInDates(true);
    filterTmp.setSelectedDate(System.currentTimeMillis() - 86400000l);
    orders = orderDAO.getOrders(null, filterTmp);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);
  }

  @Test
  public void testCountUserTotalRefundedQuantity() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    double count = orderDAO.countUserTotalRefundedQuantity(1l, 1l);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countUserTotalRefundedQuantity(1l, 1l);
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrderInstance(productEntity);
    int refundedQuantity = 1;
    orderEntity.setRefundedQuantity(refundedQuantity);
    orderEntity = orderDAO.create(orderEntity);
    entitiesToClean.add(0, orderEntity);

    count = orderDAO.countUserTotalRefundedQuantity(productEntity.getId(), orderEntity.getSenderId());
    assertEquals(refundedQuantity, count, 0);

    count = orderDAO.countUserTotalRefundedQuantity(productEntity.getId(), 20l);
    assertEquals(0, count, 0);
  }

  @Test
  public void testCountUserPurchasedQuantityInPeriod() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    long startTime = System.currentTimeMillis() - 1000;

    double count = orderDAO.countUserPurchasedQuantityInPeriod(1l, 1l, startTime, startTime + 2000);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countUserPurchasedQuantityInPeriod(productEntity.getId(), 1l, startTime, startTime + 2000);
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrder(productEntity);

    long endTime = System.currentTimeMillis() + 1000;

    count = orderDAO.countUserPurchasedQuantityInPeriod(productEntity.getId(), orderEntity.getSenderId(), startTime, endTime);
    assertEquals(orderEntity.getQuantity(), count, 0);

    count = orderDAO.countUserPurchasedQuantityInPeriod(productEntity.getId(), 200l, startTime, endTime);
    assertEquals(0, count, 0);

    count = orderDAO.countUserPurchasedQuantityInPeriod(3000l, orderEntity.getSenderId(), startTime, endTime);
    assertEquals(0, count, 0);

    count = orderDAO.countUserPurchasedQuantityInPeriod(productEntity.getId(), orderEntity.getSenderId(), endTime, endTime);
    assertEquals(0, count, 0);

    count = orderDAO.countUserPurchasedQuantityInPeriod(productEntity.getId(), orderEntity.getSenderId(), startTime, startTime);
    assertEquals(0, count, 0);
  }

  @Test
  public void testCountUserOrderedQuantityByStatusInPeriod() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    long startTime = System.currentTimeMillis() - 1000;

    double count = orderDAO.countUserOrderedQuantityByStatusInPeriod(1l,
                                                                     1l,
                                                                     startTime,
                                                                     startTime + 2000,
                                                                     ProductOrderStatus.ORDERED);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countUserOrderedQuantityByStatusInPeriod(productEntity.getId(),
                                                              1l,
                                                              startTime,
                                                              startTime + 2000,
                                                              ProductOrderStatus.ORDERED);
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrder(productEntity);

    long endTime = System.currentTimeMillis() + 1000;

    count = orderDAO.countUserOrderedQuantityByStatusInPeriod(productEntity.getId(),
                                                              orderEntity.getSenderId(),
                                                              startTime,
                                                              endTime,
                                                              orderEntity.getStatus());
    assertEquals(orderEntity.getQuantity(), count, 0);

    count = orderDAO.countUserOrderedQuantityByStatusInPeriod(productEntity.getId(),
                                                              200l,
                                                              startTime,
                                                              endTime,
                                                              orderEntity.getStatus());
    assertEquals(0, count, 0);

    count = orderDAO.countUserOrderedQuantityByStatusInPeriod(3000l,
                                                              orderEntity.getSenderId(),
                                                              startTime,
                                                              endTime,
                                                              orderEntity.getStatus());
    assertEquals(0, count, 0);

    count = orderDAO.countUserOrderedQuantityByStatusInPeriod(productEntity.getId(),
                                                              orderEntity.getSenderId(),
                                                              endTime,
                                                              endTime,
                                                              orderEntity.getStatus());
    assertEquals(0, count, 0);

    count = orderDAO.countUserOrderedQuantityByStatusInPeriod(productEntity.getId(),
                                                              orderEntity.getSenderId(),
                                                              startTime,
                                                              startTime,
                                                              orderEntity.getStatus());
    assertEquals(0, count, 0);

    count = orderDAO.countUserOrderedQuantityByStatusInPeriod(productEntity.getId(),
                                                              orderEntity.getSenderId(),
                                                              startTime,
                                                              endTime,
                                                              ProductOrderStatus.DELIVERED);
    assertEquals(0, count, 0);
  }

  @Test
  public void testCountUserRefundedQuantityInPeriod() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    long startTime = System.currentTimeMillis() - 1000;

    double count = orderDAO.countUserRefundedQuantityInPeriod(1l, 1l, startTime, startTime + 2000);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countUserRefundedQuantityInPeriod(productEntity.getId(), 1l, startTime, startTime + 2000);
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrderInstance(productEntity);
    int refundedQuantity = 1;
    orderEntity.setRefundedQuantity(refundedQuantity);
    orderEntity = orderDAO.create(orderEntity);
    entitiesToClean.add(0, orderEntity);

    long endTime = System.currentTimeMillis() + 1000;

    count = orderDAO.countUserRefundedQuantityInPeriod(productEntity.getId(), orderEntity.getSenderId(), startTime, endTime);
    assertEquals(orderEntity.getQuantity(), count, 0);

    count = orderDAO.countUserRefundedQuantityInPeriod(productEntity.getId(), 200l, startTime, endTime);
    assertEquals(0, count, 0);

    count = orderDAO.countUserRefundedQuantityInPeriod(3000l, orderEntity.getSenderId(), startTime, endTime);
    assertEquals(0, count, 0);

    count = orderDAO.countUserRefundedQuantityInPeriod(productEntity.getId(), orderEntity.getSenderId(), endTime, endTime);
    assertEquals(0, count, 0);

    count = orderDAO.countUserRefundedQuantityInPeriod(productEntity.getId(), orderEntity.getSenderId(), startTime, startTime);
    assertEquals(0, count, 0);
  }

  @Test
  public void testCountUserTotalOrderedQuantityByStatus() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);

    double count = orderDAO.countUserTotalOrderedQuantityByStatus(1l, 1l, ProductOrderStatus.ORDERED);
    assertEquals(0, count, 0);

    ProductEntity productEntity = newProduct();

    count = orderDAO.countUserTotalOrderedQuantityByStatus(productEntity.getId(), 1l, ProductOrderStatus.ORDERED);
    assertEquals(0, count, 0);

    ProductOrderEntity orderEntity = newOrderInstance(productEntity);
    int refundedQuantity = 1;
    orderEntity.setRefundedQuantity(refundedQuantity);
    orderEntity = orderDAO.create(orderEntity);
    entitiesToClean.add(0, orderEntity);

    count = orderDAO.countUserTotalOrderedQuantityByStatus(productEntity.getId(),
                                                           orderEntity.getSenderId(),
                                                           orderEntity.getStatus());
    assertEquals(refundedQuantity, count, 0);

    count = orderDAO.countUserTotalOrderedQuantityByStatus(productEntity.getId(),
                                                           orderEntity.getSenderId(),
                                                           ProductOrderStatus.CANCELED);
    assertEquals(0, count, 0);

    count = orderDAO.countUserTotalOrderedQuantityByStatus(productEntity.getId(), 20l, orderEntity.getStatus());
    assertEquals(0, count, 0);

    count = orderDAO.countUserTotalOrderedQuantityByStatus(3000l, orderEntity.getSenderId(), orderEntity.getStatus());
    assertEquals(0, count, 0);
  }
  @Test
  public void testGetOrdersByIdentity() {
    PerkStoreOrderDAO orderDAO = getService(PerkStoreOrderDAO.class);
    List<ProductOrderEntity> orders = orderDAO.getOrdersByIdentity(0L);
    assertNotNull(orders);
    assertEquals(0, orders.size(), 0);

    ProductEntity productEntity = newProduct();
    ProductOrderEntity orderEntity = newOrder(productEntity);
    List<ProductOrderEntity> savedOrderEntity = orderDAO.getOrdersByIdentity(1L);
    assertNotNull(savedOrderEntity);
    assertEquals(1, savedOrderEntity.size(), 0);
    assertEquals(1L, savedOrderEntity.get(0).getReceiverId(), 0);


    savedOrderEntity = orderDAO.getOrdersByIdentity(2L);
    assertNotNull(savedOrderEntity);
    assertEquals(1, savedOrderEntity.size(), 0);
    assertEquals(2L, savedOrderEntity.get(0).getSenderId(), 0);
  }

}
