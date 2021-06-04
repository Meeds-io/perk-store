package org.exoplatform.perkstore.model.constant;

public enum ProductOrderType {
  ALL("ALL","all"),
  SENT("SENT","sent"),
  RECEIVED("RECEIVED","received");
  private String name;

  private String label;

  private ProductOrderType(String name, String label) {
    this.name = name;
    this.label = label;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }
}
