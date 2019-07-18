package org.exoplatform.addon.perkstore.test.dao;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.exoplatform.addon.perkstore.dao.PerkStoreProductDAO;
import org.exoplatform.addon.perkstore.entity.ProductEntity;
import org.exoplatform.addon.perkstore.test.BasePerkStoreTest;

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
