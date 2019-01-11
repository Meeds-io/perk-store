package org.exoplatform.addon.perkstore.model;

import lombok.Data;

@Data
public class OrderFilter {
  private long    productId;

  private int     limit;

  private boolean notProcessed;

  private boolean searchInDates;

  private long    selectedDate;

  private boolean ordered;

  private boolean canceled;

  private boolean payed;

  private boolean error;

  private boolean delivered;

  private boolean refunded;
}
