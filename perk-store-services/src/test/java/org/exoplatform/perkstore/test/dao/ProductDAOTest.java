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

import org.exoplatform.perkstore.dao.PerkStoreProductDAO;
import org.exoplatform.perkstore.entity.ProductEntity;
import org.exoplatform.perkstore.test.BasePerkStoreTest;

public class ProductDAOTest extends BasePerkStoreTest {

  /**
   * Check that service is instantiated and functional
   */
  @Test
  public void testServiceInstantiated() {
    PerkStoreProductDAO productDAO = getService(PerkStoreProductDAO.class);
    assertNotNull(productDAO);
  }

  @Test
  public void testDeleteAll() {
    PerkStoreProductDAO productDAO = getService(PerkStoreProductDAO.class);
    try {
      productDAO.deleteAll();
      fail("shouldn't be able to delete all entities");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testDeleteAllEntities() {
    PerkStoreProductDAO productDAO = getService(PerkStoreProductDAO.class);
    try {
      productDAO.deleteAll(Collections.emptyList());
      fail("shouldn't be able to delete all entities");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testGetAllProducts() {
    PerkStoreProductDAO productDAO = getService(PerkStoreProductDAO.class);
    List<ProductEntity> products = productDAO.getAllProducts();

    assertNotNull(products);
    assertEquals(0, products.size());

    newProduct();

    products = productDAO.getAllProducts();

    assertEquals(1, products.size());
  }

}
