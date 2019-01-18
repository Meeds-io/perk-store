package org.exoplatform.addon.perkstore.service;

import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.addon.perkstore.dao.PerkStoreOrderDAO;
import org.exoplatform.addon.perkstore.dao.PerkStoreProductDAO;
import org.exoplatform.addon.perkstore.entity.ProductEntity;
import org.exoplatform.addon.perkstore.entity.ProductOrderEntity;
import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.addon.perkstore.model.constant.PerkStoreError;
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
    List<ProductEntity> productEntities = productDAO.findAll();
    // Used to enable cache usage
    return productEntities.stream().map(productEntity -> getProductById(productEntity.getId())).collect(Collectors.toList());
  }

  public double countOrderedQuantity(long id) {
    if (id == 0) {
      return 0;
    }
    return orderDAO.countOrderedQuantityByProductId(id);
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
    return orderDAO.countUserTotalPurchasedQuantity(productId, identityId);
  }

  public double countUserPurchasedQuantityInPeriod(long productId, long identityId, long startDate, long endDate) {
    if (productId == 0) {
      return 0;
    }
    return orderDAO.countUserPurchasedQuantityInPeriod(productId, identityId, startDate, endDate);
  }

  public List<ProductOrder> getOrders(String username, OrderFilter filter) throws PerkStoreException {
    long selectedOrderId = filter.getSelectedOrderId();
    if (selectedOrderId > 0) {
      ProductOrder order = getOrderById(selectedOrderId);
      if (order == null) {
        return Collections.emptyList();
      } else if (StringUtils.isBlank(username)) {
        return Collections.singletonList(order);
      } else if (StringUtils.equals(order.getSender().getId(), username)) {
        return Collections.singletonList(order);
      } else {
        throw new PerkStoreException(PerkStoreError.ORDER_ACCESS_DENIED, order.getId(), username);
      }
    } else {
      if (filter.getLimit() == 0) {
        filter.setLimit(DEFAULT_QUERY_LIMIT);
      }
      List<ProductOrderEntity> entities = orderDAO.getOrders(username, filter);
      return entities.stream()
                     .map(orderEntity -> getOrderById(orderEntity.getId()))
                     .collect(Collectors.toList());
    }
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
