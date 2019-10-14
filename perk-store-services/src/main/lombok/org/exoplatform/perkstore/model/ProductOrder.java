package org.exoplatform.perkstore.model;

import java.io.Serializable;

import org.exoplatform.perkstore.model.constant.PerkStoreError;

import groovy.transform.ToString;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class ProductOrder extends PerkStoreCloneable implements Serializable {
  private static final long serialVersionUID = 1315929554209305549L;

  private long              id;

  private long              productId;

  private String            transactionHash;

  private String            refundTransactionHash;

  private double            quantity;

  private double            amount;

  private double            refundedAmount;

  private Profile           sender;

  private Profile           receiver;

  private double            deliveredQuantity;

  private double            refundedQuantity;

  private double            remainingQuantityToProcess;

  private long              createdDate;

  private long              deliveredDate;

  private long              refundedDate;

  private String            status;

  private String            transactionStatus;

  private String            refundTransactionStatus;

  private PerkStoreError    error;

  // Processed
  private String            productTitle;

  @SuppressWarnings("all")
  @Override
  public ProductOrder clone() {
    return (ProductOrder) super.clone();
  }
}
