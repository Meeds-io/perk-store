package org.exoplatform.addon.perkstore.storage;

import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.addon.perkstore.dao.PerkStoreOrderDAO;
import org.exoplatform.addon.perkstore.dao.PerkStoreProductDAO;
import org.exoplatform.addon.perkstore.entity.ProductEntity;
import org.exoplatform.addon.perkstore.entity.ProductOrderEntity;
import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.addon.perkstore.model.constant.PerkStoreError;
import org.exoplatform.addon.perkstore.model.constant.ProductOrderStatus;
import org.exoplatform.social.core.identity.model.Identity;

public class PerkStoreStorage {
  private static final int    DEFAULT_QUERY_LIMIT = 100;

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

  public Product saveProduct(Product product, String username) throws PerkStoreException {
    if (product == null) {
      throw new IllegalArgumentException("product argument is null");
    }

    ProductEntity entity = toEntity(product);
    if (product.getId() == 0) {
      Identity identity = getIdentityByTypeAndId(USER_ACCOUNT_TYPE, username);
      if (identity == null) {
        throw new PerkStoreException(PerkStoreError.PRODUCT_CREATION_DENIED, username);
      }
      entity.setCreator(Long.parseLong(identity.getId()));
      entity.setCreatedDate(System.currentTimeMillis());
      entity.setId(null);
      entity = productDAO.create(entity);
    } else {
      Identity identity = getIdentityByTypeAndId(USER_ACCOUNT_TYPE, username);
      if (identity == null) {
        throw new PerkStoreException(PerkStoreError.PRODUCT_MODIFICATION_DENIED, username, product.getTitle());
      }
      entity.setLastModifier(Long.parseLong(identity.getId()));
      entity.setLastModifiedDate(System.currentTimeMillis());
      entity = productDAO.update(entity);
    }

    return fromEntity(entity);
  }

  public List<Product> getAllProducts() {
    List<ProductEntity> productEntities = productDAO.getAllProducts();
    // Used to enable cache usage
    return productEntities.stream().map(productEntity -> getProductById(productEntity.getId())).collect(Collectors.toList());
  }

  public double countOrderedQuantity(long productId) {
    if (productId == 0) {
      return 0;
    }
    double countOrderedQuantities = orderDAO.countOrderedQuantityByProductId(productId);
    if (countOrderedQuantities > 0) {
      return countOrderedQuantities - orderDAO.countRefundedQuantityByProductId(productId)
          - orderDAO.countOrderedQuantityByProductIdAndStatus(productId, ProductOrderStatus.CANCELED);
    } else {
      return 0;
    }
  }

  public long countRemainingOrdersToProcess(long id) {
    if (id == 0) {
      return 0;
    }
    return orderDAO.countRemainingOrdersToProcessByProductId(id);
  }

  public long countRemainingOrdersToProcess(long identityId, long productId) {
    if (identityId == 0 || productId == 0) {
      return 0;
    }
    return orderDAO.countRemainingOrdersByIdentityIdAndProductId(identityId, productId);
  }

  public double countUserTotalPurchasedQuantity(long productId, long identityId) {
    if (productId == 0) {
      return 0;
    }
    double userTotalPurchasedQuantity = orderDAO.countUserTotalPurchasedQuantity(productId, identityId);
    if (userTotalPurchasedQuantity > 0) {
      return userTotalPurchasedQuantity - orderDAO.countUserTotalRefundedQuantity(productId, identityId)
          - orderDAO.countUserTotalOrderedQuantityByStatus(productId, identityId, ProductOrderStatus.CANCELED);
    } else {
      return 0;
    }
  }

  public double countUserPurchasedQuantityInPeriod(long productId, long identityId, long startDate, long endDate) {
    if (productId == 0) {
      return 0;
    }
    double userTotalPurchasedQuantity = orderDAO.countUserPurchasedQuantityInPeriod(productId, identityId, startDate, endDate);
    if (userTotalPurchasedQuantity > 0) {
      return userTotalPurchasedQuantity - orderDAO.countUserRefundedQuantityInPeriod(productId, identityId, startDate, endDate)
          - orderDAO.countUserOrderedQuantityByStatusInPeriod(productId,
                                                              identityId,
                                                              startDate,
                                                              endDate,
                                                              ProductOrderStatus.CANCELED);
    } else {
      return 0;
    }
  }

  public List<ProductOrder> getOrders(String username, OrderFilter filter) {
    if (filter.getLimit() == 0) {
      filter.setLimit(DEFAULT_QUERY_LIMIT);
    }
    List<ProductOrderEntity> entities = orderDAO.getOrders(username, filter);
    return entities.stream().map(orderEntity -> getOrderById(orderEntity.getId())).collect(Collectors.toList());
  }

  public ProductOrder getOrderById(long orderId) {
    ProductOrderEntity orderEntity = orderDAO.find(orderId);
    return orderEntity == null ? null : fromEntity(orderEntity);
  }

  public ProductOrder saveOrder(ProductOrder order) throws PerkStoreException {
    if (order == null) {
      throw new IllegalArgumentException("order argument is null");
    }

    long productId = order.getProductId();
    ProductEntity productEntity = null;
    if (productId != 0) {
      productEntity = productDAO.find(productId);
    }
    if (productEntity == null) {
      throw new PerkStoreException(PerkStoreError.PRODUCT_NOT_EXISTS, productId);
    }

    ProductOrderEntity entity = toEntity(productEntity, order);
    if (order.getId() == 0) {
      entity.setCreatedDate(System.currentTimeMillis());
      entity.setId(null);
      entity = orderDAO.create(entity);
    } else {
      entity = orderDAO.update(entity);
    }

    return fromEntity(entity);
  }

  public ProductOrder findOrderByTransactionHash(String hash) {
    ProductOrderEntity orderEntity = orderDAO.findOrderByTransactionHash(hash);
    return orderEntity == null ? null : fromEntity(orderEntity);
  }

  public ProductOrder findOrderByRefundTransactionHash(String hash) {
    ProductOrderEntity orderEntity = orderDAO.findOrderByRefundTransactionHash(hash);
    return orderEntity == null ? null : fromEntity(orderEntity);
  }

}
