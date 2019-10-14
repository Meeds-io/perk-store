package org.exoplatform.perkstore.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.perkstore.entity.ProductEntity;

public class PerkStoreProductDAO extends GenericDAOJPAImpl<ProductEntity, Long> {
  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(List<ProductEntity> entities) {
    throw new UnsupportedOperationException();
  }

  public List<ProductEntity> getAllProducts() {
    TypedQuery<ProductEntity> query = getEntityManager().createNamedQuery("Product.getAllProducts", ProductEntity.class);
    return query.getResultList();
  }

}
