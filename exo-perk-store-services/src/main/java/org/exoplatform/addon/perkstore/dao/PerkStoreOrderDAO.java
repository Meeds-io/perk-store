package org.exoplatform.addon.perkstore.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.addon.perkstore.entity.ProductOrderEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class PerkStoreOrderDAO extends GenericDAOJPAImpl<ProductOrderEntity, Long> {
  private static final String PRODUCT_ID_PARAMETER = "productId";

  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(ProductOrderEntity entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(List<ProductOrderEntity> entities) {
    throw new UnsupportedOperationException();
  }

  public double countOrderedQuantityByProductId(long id) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countOrderedQuantityByProductId", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, id);
    return query.getSingleResult();
  }

  public long countRemainingOrdersByProductId(long id) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("Order.countRemainingOrdersByProductId", Long.class);
    query.setParameter(PRODUCT_ID_PARAMETER, id);
    return query.getSingleResult();
  }

  public double countUserTotalPurchasedQuantity(long productId, long identityId) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserTotalPurchasedQuantity", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter("sender", identityId);
    return query.getSingleResult();
  }

  public double countUserPurchasedQuantityInPeriod(long productId, long identityId, long startDate, long endDate) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserPurchasedQuantityInPeriod", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter("sender", identityId);
    query.setParameter("from", startDate);
    query.setParameter("to", endDate);
    return query.getSingleResult();
  }

}
