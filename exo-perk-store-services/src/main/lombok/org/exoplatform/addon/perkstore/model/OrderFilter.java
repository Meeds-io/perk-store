package org.exoplatform.addon.perkstore.model;

import java.util.Date;

import lombok.Data;

@Data
public class OrderFilter {
  private long    productId;

  private int     limit;

  private boolean notProcessed;

  private boolean searchInDates;

  private Date    selectedDate;

  private boolean ordered;

  private boolean payed;

  private boolean error;

  private boolean delivered;

  private boolean refunded;
}
