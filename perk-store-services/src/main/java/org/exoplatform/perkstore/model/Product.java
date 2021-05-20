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
import java.util.List;
import java.util.Set;

import groovy.transform.ToString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class Product extends PerkStoreCloneable implements Serializable {
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
    return (Product) super.clone();
  }
}
