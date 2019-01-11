package org.exoplatform.addon.perkstore.exception;

import java.io.Serializable;

import org.exoplatform.addon.perkstore.model.PerkStoreError;

public class PerkStoreException extends Exception {

  private static final long    serialVersionUID = -5925338937598951745L;

  private final PerkStoreError errorType;

  private final Serializable[] parameters;

  public PerkStoreException(PerkStoreError errorType, Serializable... parameters) {
    if (errorType == null) {
      throw new IllegalArgumentException("Error details is mandatory");
    }
    errorType.checkValidParametersCount(parameters);

    this.errorType = errorType;
    this.parameters = parameters;
  }

  @Override
  public String getMessage() {
    return this.errorType.getMessage(this.parameters);
  }

  public PerkStoreError getErrorType() {
    return errorType;
  }
}
