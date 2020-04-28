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
package org.exoplatform.perkstore.exception;

import static org.exoplatform.perkstore.service.utils.Utils.getI18NMessage;

import java.io.Serializable;
import java.util.MissingResourceException;

import org.exoplatform.perkstore.model.constant.PerkStoreError;
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
      LOG.debug("Error getting message for code: {}", errorCode, e);
      return this.getMessage();
    }
  }

  public PerkStoreError getErrorType() {
    return errorType;
  }
}
