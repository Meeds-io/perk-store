package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class FileDetail extends PerkStoreCloneable implements Serializable {

  private static final long serialVersionUID = 3118275951807957785L;

  public FileDetail(long id) {
    this.id = id;
  }

  private long   id;

  private String uploadId;

  @Exclude
  private String name;

  @Exclude
  private long   size;

  @Exclude
  private String src;

  @Exclude
  private byte[] data;

  @Exclude
  private long   lastUpdated;

}
