package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class ProductOrder implements Serializable {
  private static final long serialVersionUID = 1315929554209305549L;

  private long              id;

  private String            transactionHash;

  private double            quantity;

  private double            amount;

  private Profile           sender;

  private Profile           receiver;

  private double            deliveredQuantity;

  private double            refundedQuantity;

  private double            remainingQuantityToProcess;

  private long              createdDate;

  private long              deliveredDate;

  private long              refundedDate;

  private String            error;

  private String            status;
}
