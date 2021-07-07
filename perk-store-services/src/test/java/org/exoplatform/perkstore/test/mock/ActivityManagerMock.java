/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.perkstore.test.mock;

import java.util.List;

import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;

@SuppressWarnings("all")
public class ActivityManagerMock implements ActivityManager {

  public void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity activity) {

  }

  public void saveActivityNoReturn(ExoSocialActivity activity) {

  }

  public void saveActivity(Identity streamOwner, String type, String title) {

  }

  public ExoSocialActivity getActivity(String activityId) {

    return null;
  }

  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) {

    return null;
  }

  public void updateActivity(ExoSocialActivity activity) {

  }

  public void deleteActivity(ExoSocialActivity activity) {

  }

  public void deleteActivity(String activityId) {

  }

  public void saveComment(ExoSocialActivity activity, ExoSocialActivity newComment) {

  }

  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity) {

    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity, boolean loadSubComments) {

    return null;
  }

  public void deleteComment(String activityId, String commentId) {

  }

  public void deleteComment(ExoSocialActivity activity, ExoSocialActivity comment) {

  }

  public void saveLike(ExoSocialActivity activity, Identity identity) {

  }

  public void deleteLike(ExoSocialActivity activity, Identity identity) {

  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity identity) {

    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity ownerIdentity, Identity viewerIdentity) {

    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfConnectionsWithListAccess(Identity identity) {

    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfSpaceWithListAccess(Identity spaceIdentity) {

    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpacesWithListAccess(Identity identity) {

    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivityFeedWithListAccess(Identity identity) {

    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity poster) {

    return null;
  }

  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, String... activityTypes) {

    return null;
  }

  public void addProcessor(ActivityProcessor activityProcessor) {

  }

  public void addProcessorPlugin(BaseActivityProcessorPlugin activityProcessorPlugin) {

  }

  public void addActivityEventListener(ActivityListenerPlugin activityListenerPlugin) {

  }

  public ExoSocialActivity saveActivity(Identity streamOwner, ExoSocialActivity activity) {

    return null;
  }

  public ExoSocialActivity saveActivity(ExoSocialActivity activity) {

    return null;
  }

  public List<ExoSocialActivity> getActivities(Identity identity) throws ActivityStorageException {

    return null;
  }

  public List<ExoSocialActivity> getActivities(Identity identity, long start, long limit) throws ActivityStorageException {

    return null;
  }

  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity) throws ActivityStorageException {

    return null;
  }

  public List<ExoSocialActivity> getActivitiesOfUserSpaces(Identity ownerIdentity) {

    return null;
  }

  public List<ExoSocialActivity> getActivityFeed(Identity identity) throws ActivityStorageException {

    return null;
  }

  public void removeLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException {

  }

  public List<ExoSocialActivity> getComments(ExoSocialActivity activity) throws ActivityStorageException {

    return null;
  }

  public ExoSocialActivity recordActivity(Identity owner, String type, String title) throws ActivityStorageException {

    return null;
  }

  public ExoSocialActivity recordActivity(Identity owner, ExoSocialActivity activity) throws Exception {

    return null;
  }

  public ExoSocialActivity recordActivity(Identity owner,
                                          String type,
                                          String title,
                                          String body) throws ActivityStorageException {

    return null;
  }

  public int getActivitiesCount(Identity owner) throws ActivityStorageException {

    return 0;
  }

  public RealtimeListAccess<ExoSocialActivity> getAllActivitiesWithListAccess() {

    return null;
  }

  public List<ExoSocialActivity> getSubComments(ExoSocialActivity comment) {

    return null;
  }

  public int getMaxUploadSize() {

    return 0;
  }

  public List<ExoSocialActivity> getActivities(List<String> activityIdList) {

    return null;
  }

  public boolean isActivityEditable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer) {

    return false;
  }

}
