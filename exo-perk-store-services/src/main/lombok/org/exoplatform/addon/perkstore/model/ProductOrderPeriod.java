package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import groovy.transform.ToString;
import lombok.Data;

@Data
@ToString
public class ProductOrderPeriod implements Serializable {

  private static final long serialVersionUID = -9185889239496825058L;

  private long              startDate;

  private long              endDate;

}
