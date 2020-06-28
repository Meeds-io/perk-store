package org.exoplatform.perkstore.storage.cached;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.perkstore.dao.PerkStoreOrderDAO;
import org.exoplatform.perkstore.dao.PerkStoreProductDAO;
import org.exoplatform.perkstore.exception.PerkStoreException;
import org.exoplatform.perkstore.model.Product;
import org.exoplatform.perkstore.model.ProductOrder;
import org.exoplatform.perkstore.service.utils.Utils;
import org.exoplatform.perkstore.storage.PerkStoreStorage;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

public class PerkStoreCachedStorage extends PerkStoreStorage {

  private static final int                                    ORDER_ID_CONTEXT       = 0;

  private static final int                                    ORDER_TX_HASH_CONTEXT  = 1;

  private static final int                                    REFUND_TX_HASH_CONTEXT = 2;

  private FutureExoCache<Long, Product, Object>               productFutureCache     = null;

  private FutureExoCache<Serializable, ProductOrder, Integer> orderFutureCache       = null;

  public PerkStoreCachedStorage(PerkStoreProductDAO perkStoreProductDAO,
                                PerkStoreOrderDAO perkStoreOrderDAO,
                                CacheService cacheService) {
    super(perkStoreProductDAO, perkStoreOrderDAO);

    // Product cache
    ExoCache<Long, Product> productCache = cacheService.getCacheInstance("perkstore.product");
    Loader<Long, Product, Object> productLoader = new Loader<Long, Product, Object>() {
      @Override
      public Product retrieve(Object context, Long productId) throws Exception {
        return PerkStoreCachedStorage.super.getProductById(productId);
      }
    };
    this.productFutureCache = new FutureExoCache<>(productLoader, productCache);

    // Product order cache
    ExoCache<Serializable, ProductOrder> orderCache = cacheService.getCacheInstance("perkstore.order");
    Loader<Serializable, ProductOrder, Integer> orderLoader = new Loader<Serializable, ProductOrder, Integer>() {
      @Override
      public ProductOrder retrieve(Integer context, Serializable key) throws Exception {
        if (context == ORDER_ID_CONTEXT) {
          return PerkStoreCachedStorage.super.getOrderById((Long) key);
        } else if (context == ORDER_TX_HASH_CONTEXT) {
          return PerkStoreCachedStorage.super.findOrderByTransactionHash((String) key);
        } else if (context == REFUND_TX_HASH_CONTEXT) {
          return PerkStoreCachedStorage.super.findOrderByRefundTransactionHash((String) key);
        } else {
          throw new IllegalStateException("Unkown context id " + context);
        }
      }
    };
    this.orderFutureCache = new FutureExoCache<>(orderLoader, orderCache);
  }

  @Override
  public ProductOrder getOrderById(long orderId) {
    ProductOrder order = this.orderFutureCache.get(ORDER_ID_CONTEXT, orderId);
    return cloneOrder(order);
  }

  @Override
  public ProductOrder findOrderByTransactionHash(String hash) {
    ProductOrder order = this.orderFutureCache.get(ORDER_TX_HASH_CONTEXT, hash);
    return cloneOrder(order);
  }

  @Override
  public ProductOrder findOrderByRefundTransactionHash(String hash) {
    ProductOrder order = this.orderFutureCache.get(REFUND_TX_HASH_CONTEXT, hash);
    return cloneOrder(order);
  }

  @Override
  public Product getProductById(long productId) {
    Product product = this.productFutureCache.get(null, productId);
    if (product == null) {
      return null;
    } else {
      product = product.clone();
      // To refresh user and space display names
      Utils.refreshProfile(product.getReceiverMarchand());
      return product;
    }
  }

  @Override
  public ProductOrder saveOrder(ProductOrder order) throws PerkStoreException {
    long orderId = order.getId();
    long productId = order.getProductId();

    try {
      return super.saveOrder(order);
    } finally {
      this.orderFutureCache.remove(orderId);
      this.productFutureCache.remove(productId);
      if (StringUtils.isNotBlank(order.getTransactionHash())) {
        this.orderFutureCache.remove(order.getTransactionHash());
      }
      if (StringUtils.isNotBlank(order.getRefundTransactionHash())) {
        this.orderFutureCache.remove(order.getRefundTransactionHash());
      }
    }
  }

  @Override
  public ProductOrder replaceTransactions(String oldHash, String newHash) {
    ProductOrder order = super.replaceTransactions(oldHash, newHash);
    if (order != null) {
      this.orderFutureCache.remove(order.getId());
      this.productFutureCache.remove(order.getProductId());
      if (StringUtils.isNotBlank(order.getTransactionHash())) {
        this.orderFutureCache.remove(order.getTransactionHash());
      }
      if (StringUtils.isNotBlank(order.getRefundTransactionHash())) {
        this.orderFutureCache.remove(order.getRefundTransactionHash());
      }
    }
    return order;
  }

  @Override
  public Product saveProduct(Product product, String username) throws PerkStoreException {
    long productId = product.getId();
    try {
      return super.saveProduct(product, username);
    } finally {
      this.productFutureCache.remove(productId);
    }
  }

  private ProductOrder cloneOrder(ProductOrder order) {
    if (order == null) {
      return null;
    } else {
      order = order.clone();
      // To refresh user and space display names
      Utils.refreshProfile(order.getSender());
      Utils.refreshProfile(order.getReceiver());
      return order;
    }
  }

  public void clearCache() {
    orderFutureCache.clear();
    productFutureCache.clear();
  }
}
