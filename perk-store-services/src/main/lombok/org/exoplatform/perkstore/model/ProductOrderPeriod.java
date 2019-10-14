package org.exoplatform.perkstore.model;

import java.io.Serializable;

import groovy.transform.ToString;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderPeriod extends PerkStoreCloneable implements Serializable {

  private static final long serialVersionUID = -9185889239496825058L;

  private long              startDate;

  private long              endDate;

  @Override
  public Object clone() { // NOSONAR
    return super.clone();
  }
}
