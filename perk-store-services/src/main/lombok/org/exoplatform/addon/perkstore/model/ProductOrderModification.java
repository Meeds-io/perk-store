package org.exoplatform.addon.perkstore.model;

import java.io.Serializable;

import org.exoplatform.addon.perkstore.model.constant.ProductOrderModificationType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductOrderModification implements Serializable {

  private static final long            serialVersionUID = -3261362964414058401L;

  private Profile                      lastModifier;

  private ProductOrderModificationType modificationType;

  private ProductOrder                 oldValue;

  private ProductOrder                 newValue;
}
