package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import org.exoplatform.addon.perkstore.model.constant.ProductOrderModificationType;

import lombok.Data;

@Data
public class ProductOrder implements Serializable {
  private static final long            serialVersionUID = 1315929554209305549L;

  private long                         id;

  private long                         productId;

  private String                       transactionHash;

  private String                       refundTransactionHash;

  private double                       quantity;

  private double                       amount;

  private double                       refundedAmount;

  private Profile                      sender;

  private Profile                      receiver;

  private double                       deliveredQuantity;

  private double                       refundedQuantity;

  private double                       remainingQuantityToProcess;

  private long                         createdDate;

  private long                         deliveredDate;

  private long                         refundedDate;

  private String                       status;

  private String                       transactionStatus;

  private String                       refundTransactionStatus;

  // Processed
  private String                       productTitle;

  // Not stored, used in notification only to identity modifier for listeners
  private Profile                      lastModifier;

  // Not stored, used in notification only to identity modification type for
  // listeners
  private ProductOrderModificationType modificationType;
}
