package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import groovy.transform.ToString;
import lombok.Data;

@Data
@ToString
public class UserProductData implements Serializable, Cloneable {
  private static final long serialVersionUID = -7144496703478026420L;

  private String            username;

  private boolean           canEdit;

  private boolean           canOrder;

  private double            purchasedInCurrentPeriod;

  private double            totalPurchased;

  @SuppressWarnings("all")
  public Object clone() {
    return new UserProductData();
  }
}
