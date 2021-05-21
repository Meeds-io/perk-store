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
