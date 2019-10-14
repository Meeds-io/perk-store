package org.exoplatform.perkstore.model;

import java.io.Serializable;

import groovy.transform.ToString;
import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends PerkStoreCloneable implements Serializable {

  private static final long serialVersionUID = 3842109328846936552L;

  public Profile(long technicalId) {
    this.technicalId = technicalId;
  }

  public Profile(String id, String type) {
    this.id = id;
    this.type = type;
  }

  private String type;

  private String id;

  private long   technicalId;

  private long   spaceId;

  @Exclude
  private String spaceURLId;

  @Exclude
  private String displayName;

  @SuppressWarnings("all")
  @Override
  public Object clone() {
    return super.clone();
  }
}
