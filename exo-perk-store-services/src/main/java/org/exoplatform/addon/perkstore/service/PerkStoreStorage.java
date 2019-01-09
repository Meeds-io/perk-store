package org.exoplatform.addon.perkstore.service;

import static org.exoplatform.addon.perkstore.service.utils.Utils.fromEntity;
import static org.exoplatform.addon.perkstore.service.utils.Utils.toEntity;

import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.addon.perkstore.dao.PerkStoreOrderDAO;
import org.exoplatform.addon.perkstore.dao.PerkStoreProductDAO;
import org.exoplatform.addon.perkstore.entity.ProductEntity;
import org.exoplatform.addon.perkstore.model.Product;
import org.exoplatform.addon.perkstore.service.utils.Utils;

public class PerkStoreStorage {
  private PerkStoreProductDAO productDAO;

  private PerkStoreOrderDAO   orderDAO;

  public PerkStoreStorage(PerkStoreProductDAO perkStoreProductDAO, PerkStoreOrderDAO perkStoreOrderDAO) {
    this.productDAO = perkStoreProductDAO;
    this.orderDAO = perkStoreOrderDAO;
  }

  public Product getProductById(long productId) {
    ProductEntity entity = productDAO.find(productId);
    if (entity == null) {
      return null;
    } else {
      return fromEntity(entity);
    }
  }

  public Product saveProduct(String currentUserId, Product product) {
    if (product == null) {
      throw new IllegalArgumentException();
    }

    ProductEntity entity = toEntity(product);
    if (product.getId() == 0) {
      entity.setCreator(currentUserId);
      entity.setCreatedDate(System.currentTimeMillis());
      entity = productDAO.create(entity);
    } else {
      entity.setLastModifier(currentUserId);
      entity.setLastModifiedDate(System.currentTimeMillis());
      entity = productDAO.update(entity);
    }

    return fromEntity(entity);
  }

  public List<Product> getAllProducts() {
    List<ProductEntity> productEntities = productDAO.findAll();
    return productEntities.stream().map(Utils::fromEntity).collect(Collectors.toList());
  }

  public double countOrderedQuantity(long id) {
    if (id == 0) {
      return 0;
    }
    return orderDAO.countOrderedQuantityByProductId(id);
  }

  public long countRemainingOrders(long id) {
    if (id == 0) {
      return 0;
    }
    return orderDAO.countRemainingOrdersByProductId(id);
  }

  public double countUserTotalPurchasedQuantity(long productId, long identityId) {
    if (productId == 0) {
      return 0;
    }
    return orderDAO.countUserTotalPurchasedQuantity(productId, identityId);
  }

  public double countUserPurchasedQuantityInPeriod(long productId, long identityId, long startDate, long endDate) {
    if (productId == 0) {
      return 0;
    }
    return orderDAO.countUserPurchasedQuantityInPeriod(productId, identityId, startDate, endDate);
  }

}
