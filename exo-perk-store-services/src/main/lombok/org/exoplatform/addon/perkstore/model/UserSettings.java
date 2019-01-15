package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import org.exoplatform.addon.perkstore.service.utils.Utils;

import lombok.Data;

@Data
public class UserSettings implements Serializable, Cloneable {

  private static final long serialVersionUID = 4866936020505632226L;

  private String            cometdChannel    = Utils.COMETD_CHANNEL;

  private String            cometdToken;

  private String            cometdContext;

  private boolean           isAdministrator;

  private boolean           canAddProduct;

  @Override
  @SuppressWarnings("all")
  protected Object clone() throws CloneNotSupportedException {
    return new UserSettings();
  }

}
