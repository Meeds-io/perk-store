package org.exoplatform.addon.perkstore.dao;

import java.util.List;

import org.exoplatform.addon.perkstore.entity.ProductEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class PerkStoreProductDAO extends GenericDAOJPAImpl<ProductEntity, Long> {
  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(ProductEntity entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(List<ProductEntity> entities) {
    throw new UnsupportedOperationException();
  }

}
