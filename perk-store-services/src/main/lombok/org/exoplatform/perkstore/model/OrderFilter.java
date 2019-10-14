package org.exoplatform.perkstore.model;

import java.io.Serializable;

import lombok.*;

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

  private boolean           currentUserOrders;

  private boolean           ordered;

  private boolean           canceled;

  private boolean           partial;

  private boolean           paid;

  private boolean           error;

  private boolean           delivered;

  private boolean           refunded;

  private boolean           fraud;

  @Override
  public OrderFilter clone() { // NOSONAR
    return (OrderFilter) super.clone();
  }
}
