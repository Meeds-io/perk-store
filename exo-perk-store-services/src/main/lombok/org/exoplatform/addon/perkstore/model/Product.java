package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Product implements Serializable, Cloneable {
  private static final long serialVersionUID = 3182323147042158001L;

  private long              id;

  private String            title;

  private String            description;

  private String            illustrationURL;

  private boolean           enabled;

  private boolean           unlimited;

  private boolean           allowFraction;

  private double            totalSupply;

  private double            price;

  private Profile           receiverMarchand;

  private List<Profile>     marchands;

  private List<Profile>     accessPermissions;

  private String            orderPeriodicity;

  private String            orderPeriodicityLabel;

  private Profile           creator;

  private Profile           lastModifier;

  private double            maxOrdersPerUser;

  private long              createdDate;

  private long              lastModifiedDate;

  // Computed
  private long              notProcessedOrders;

  // Computed
  private double            purchased;

  // Computed
  private UserProductData   userData         = new UserProductData();

  @SuppressWarnings("all")
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Error while cloning object");
    }
  }
}
