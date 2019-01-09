package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Product implements Serializable {
  private static final long serialVersionUID = 3182323147042158001L;

  private long              id;

  private String            title;

  private String            description;

  private String            illustrationURL;

  private boolean           enabled;

  private boolean           unlimited;

  private double            totalSupply;

  private double            price;

  private Profile           receiverMarchand;

  private List<Profile>     marchands;

  private String            orderPeriodicity;

  private String            orderPeriodicityLabel;

  private String            creator;

  private String            lastModifier;

  private long              maxOrdersPerUser;

  private long              createdDate;

  private long              lastModifiedDate;

  private double            purchased;

  private boolean           canEdit;

  private long              notProcessedOrders;

  // Processed
  private UserOrders        userOrders;

}
