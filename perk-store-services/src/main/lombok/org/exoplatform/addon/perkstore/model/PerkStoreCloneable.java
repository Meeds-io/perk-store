package org.exoplatform.addon.perkstore.model;

public abstract class PerkStoreCloneable implements Cloneable {

  public Object clone() { // NOSONAR
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Error while cloning object");
    }
  }

}
