package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserOrders implements Serializable {
  private static final long serialVersionUID = -7144496703478026420L;

  private double            purchasedInCurrentPeriod;

  private double            totalOrders;

}
