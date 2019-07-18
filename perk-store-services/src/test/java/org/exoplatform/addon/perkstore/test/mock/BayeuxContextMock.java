package org.exoplatform.addon.perkstore.test.mock;

import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import org.exoplatform.services.jcr.RepositoryService;

@SuppressWarnings("all")
public class BayeuxContextMock extends EXoContinuationBayeux {

  public BayeuxContextMock(RepositoryService repoService) {
    super(repoService);
  }

  @Override
  public void dispose() {
  }

}
