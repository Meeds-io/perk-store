package org.exoplatform.addon.perkstore.service;

import org.picocontainer.Startable;

import org.exoplatform.addon.perkstore.dao.PerkStoreOrderDAO;
import org.exoplatform.addon.perkstore.dao.PerkStoreProductDAO;
import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.Product;
import org.exoplatform.addon.perkstore.model.ProductOrder;
import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

public class PerkStoreCachedStorage extends PerkStoreStorage implements Startable {

  private CacheService                               cacheService;

  private FutureExoCache<Long, Product, Object>      productFutureCache = null;

  private FutureExoCache<Long, ProductOrder, Object> orderFutureCache   = null;

  public PerkStoreCachedStorage(PerkStoreProductDAO perkStoreProductDAO, PerkStoreOrderDAO perkStoreOrderDAO) {
    super(perkStoreProductDAO, perkStoreOrderDAO);
  }

  @Override
  public void start() {
    ExoCache<Long, Product> productCache = getCacheService().getCacheInstance("perkstore.product");
    ExoCache<Long, ProductOrder> orderCache = getCacheService().getCacheInstance("perkstore.order");

    Loader<Long, Product, Object> productLoader = new Loader<Long, Product, Object>() {
      @Override
      public Product retrieve(Object context, Long productId) throws Exception {
        return PerkStoreCachedStorage.super.getProductById(productId);
      }
    };
    this.productFutureCache = new FutureExoCache<>(productLoader, productCache);
    Loader<Long, ProductOrder, Object> orderLoader = new Loader<Long, ProductOrder, Object>() {
      @Override
      public ProductOrder retrieve(Object context, Long orderId) throws Exception {
        return PerkStoreCachedStorage.super.getOrderById(orderId);
      }
    };
    this.orderFutureCache = new FutureExoCache<>(orderLoader, orderCache);
  }

  @Override
  public void stop() {
    // Not task whan stopping
  }

  @Override
  public ProductOrder getOrderById(long orderId) {
    ProductOrder order = this.orderFutureCache.get(null, orderId);
    return order == null ? null : order.clone();
  }

  @Override
  public Product getProductById(long productId) {
    Product product = this.productFutureCache.get(null, productId);
    return product == null ? null : product.clone();
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
    }
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

  private CacheService getCacheService() {
    if (cacheService == null) {
      cacheService = CommonsUtils.getService(CacheService.class);
    }
    return cacheService;
  }
}
