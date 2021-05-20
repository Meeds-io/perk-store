/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
