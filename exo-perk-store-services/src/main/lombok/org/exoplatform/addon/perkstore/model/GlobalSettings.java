package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;
import java.util.List;

import groovy.transform.ToString;
import lombok.Data;

@Data
@ToString
public class GlobalSettings implements Serializable, Cloneable {

  private static final long serialVersionUID = 6313043752170656574L;

  // Attributes for storage
  private List<Long>        productCreationPermissions;

  private List<Long>        accessPermissions;

  private List<Long>        managers;

  private String            symbol;

  // Computed attributes for display
  private List<Profile>     productCreationPermissionsProfiles;

  private List<Profile>     accessPermissionsProfiles;

  private List<Profile>     managersProfiles;

  // Computed
  private UserSettings      userSettings     = new UserSettings();

  @SuppressWarnings("all")
  public GlobalSettings clone() {
    try {
      return (GlobalSettings) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Error while cloning object");
    }
  }

}
