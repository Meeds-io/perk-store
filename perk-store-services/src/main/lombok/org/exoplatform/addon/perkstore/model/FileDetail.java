package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;

@Data
public class FileDetail implements Serializable {

  private static final long serialVersionUID = 3118275951807957785L;

  private long              id;

  private String            uploadId;

  @Exclude
  private String            name;

  @Exclude
  private long              size;

  @Exclude
  private String            src;

  @Exclude
  private byte[]            data;

  @Exclude
  private long              lastUpdated;

}
