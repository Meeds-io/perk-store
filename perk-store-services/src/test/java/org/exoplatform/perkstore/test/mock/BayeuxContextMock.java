package org.exoplatform.perkstore.test.mock;

import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import org.exoplatform.services.jcr.RepositoryService;

@SuppressWarnings("all")
public class BayeuxContextMock extends EXoContinuationBayeux {

  public BayeuxContextMock() {
    super();
  }

  @Override
  public void dispose() {
  }

}
