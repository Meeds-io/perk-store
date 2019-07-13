package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import org.exoplatform.addon.perkstore.service.utils.Utils;

import groovy.transform.ToString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class UserSettings extends PerkStoreCloneable implements Serializable {

  private static final long serialVersionUID = 4866936020505632226L;

  private String            cometdChannel    = Utils.COMETD_CHANNEL;

  @Exclude
  private String            cometdToken;

  private String            cometdContext;

  private boolean           isAdministrator;

  private boolean           canAddProduct;

  @Override
  public UserSettings clone() { // NOSONAR
    return new UserSettings();
  }

}
