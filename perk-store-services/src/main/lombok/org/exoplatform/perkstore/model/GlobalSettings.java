package org.exoplatform.perkstore.model;

import java.io.Serializable;
import java.util.List;

import groovy.transform.ToString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class GlobalSettings extends PerkStoreCloneable implements Serializable {

  private static final long serialVersionUID = 6313043752170656574L;

  // Attributes for storage
  private List<Long>        productCreationPermissions;

  private List<Long>        accessPermissions;

  private List<Long>        managers;

  private String            symbol;

  // Computed attributes for display
  // and input fields to store settings
  @Exclude
  private List<Profile>     productCreationPermissionsProfiles;

  @Exclude
  private List<Profile>     accessPermissionsProfiles;

  @Exclude
  private List<Profile>     managersProfiles;

  // Computed
  private UserSettings      userSettings     = null;

  @SuppressWarnings("all")
  public GlobalSettings clone() {
    return (GlobalSettings) super.clone();
  }

}
