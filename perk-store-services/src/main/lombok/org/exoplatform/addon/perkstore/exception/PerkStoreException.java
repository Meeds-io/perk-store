package org.exoplatform.addon.perkstore.exception;

import static org.exoplatform.addon.perkstore.service.utils.Utils.getI18NMessage;

import java.io.Serializable;
import java.util.MissingResourceException;

import org.exoplatform.addon.perkstore.model.constant.PerkStoreError;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class PerkStoreException extends Exception {

  private static final Log     LOG              = ExoLogger.getLogger(PerkStoreException.class);

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
    return getMessage(null, true);
  }

  public String getMessage(String message, boolean includeSuffix) {
    try {
      return this.errorType.computeErrorMessage(message, !includeSuffix, this.parameters);
    } catch (Exception e) {
      LOG.error("      -------- DEVELOPMENT BUG: Error composing exception message", e);
      return this.errorType.getErrorCode();
    }
  }

  @Override
  public String getLocalizedMessage() {
    String errorCode = null;
    try {
      errorCode = this.errorType.getErrorCode();
      String message = getI18NMessage("perkstore.error." + errorCode);
      return this.getMessage(message, false);
    } catch (IllegalStateException | MissingResourceException e) {
      LOG.warn("Error getting message for code: {}", errorCode, e);
      return this.getMessage();
    } catch (Exception e) {
      return this.getMessage();
    }
  }

  public PerkStoreError getErrorType() {
    return errorType;
  }
}
