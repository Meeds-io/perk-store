package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import groovy.transform.ToString;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Data
@ToString
public class Product implements Serializable, Cloneable {
  private static final long serialVersionUID = 3182323147042158001L;

  private long              id;

  @Exclude
  private String            title;

  @Exclude
  private String            description;

  @Exclude
  private String            illustrationURL;

  @Exclude
  private Set<FileDetail>   imageFiles;

  @Exclude
  private boolean           enabled;

  @Exclude
  private boolean           unlimited;

  @Exclude
  private boolean           allowFraction;

  @Exclude
  private double            totalSupply;

  @Exclude
  private double            price;

  @Exclude
  private Profile           receiverMarchand;

  @Exclude
  private List<Profile>     marchands;

  @Exclude
  private List<Profile>     accessPermissions;

  @Exclude
  private String            orderPeriodicity;

  @Exclude
  private String            orderPeriodicityLabel;

  @Exclude
  private Profile           creator;

  @Exclude
  private Profile           lastModifier;

  @Exclude
  private double            maxOrdersPerUser;

  @Exclude
  private long              createdDate;

  @Exclude
  private long              lastModifiedDate;

  // Computed
  @Exclude
  private long              notProcessedOrders;

  // Computed
  @Exclude
  private double            purchased;

  // Computed
  @Exclude
  private UserProductData   userData         = new UserProductData();

  @SuppressWarnings("all")
  public Product clone() {
    try {
      return (Product) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Error while cloning Product object");
    }
  }
}
