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

import lombok.*;
import org.exoplatform.perkstore.model.constant.ProductOrderType;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilter extends PerkStoreCloneable implements Serializable {
  private static final long serialVersionUID = -5111358721086199308L;

  private long              productId;

  private int               limit;

  private boolean           notProcessed;

  private boolean           searchInDates;

  private long              selectedDate;

  private long              selectedOrderId;

  private boolean           ordered;

  private boolean           canceled;

  private boolean           partial;

  private boolean           paid;

  private boolean           error;

  private boolean           delivered;

  private boolean           refunded;

  private boolean           fraud;

  private boolean           myOrders;

  private ProductOrderType ordersType;


  @Override
  public OrderFilter clone() { // NOSONAR
    return (OrderFilter) super.clone();
  }
}
