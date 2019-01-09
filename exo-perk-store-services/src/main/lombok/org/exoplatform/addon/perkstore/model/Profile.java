package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class Profile implements Serializable {

  private static final long serialVersionUID = 3842109328846936552L;

  private String            type;

  private String            id;

  private long              technicalId;

  private String            displayName;

}
