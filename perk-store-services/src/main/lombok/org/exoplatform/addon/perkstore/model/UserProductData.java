package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import groovy.transform.ToString;
import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
public class UserProductData extends PerkStoreCloneable implements Serializable {
  private static final long serialVersionUID = -7144496703478026420L;

  public UserProductData(String username) {
    this.username = username;
  }

  private String  username;

  @Exclude
  private boolean canEdit;

  @Exclude
  private boolean canOrder;

  @Exclude
  private double  purchasedInCurrentPeriod;

  @Exclude
  private double  totalPurchased;

  @Exclude
  private double  notProcessedOrders;

  @SuppressWarnings("all")
  public Object clone() {
    return new UserProductData(username);
  }
}
