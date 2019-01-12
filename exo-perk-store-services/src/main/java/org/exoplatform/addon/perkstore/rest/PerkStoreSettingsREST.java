/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.addon.perkstore.rest;

import static org.exoplatform.addon.perkstore.service.utils.Utils.computeErrorResponse;
import static org.exoplatform.addon.perkstore.service.utils.Utils.getCurrentUserId;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.GlobalSettings;
import org.exoplatform.addon.perkstore.service.PerkStoreService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * This class provide a REST endpoint to manage global settings
 */
@Path("/perkstore/api/settings")
@RolesAllowed("users")
public class PerkStoreSettingsREST implements ResourceContainer {
  private static final Log LOG = ExoLogger.getLogger(PerkStoreSettingsREST.class);

  private PerkStoreService perkStoreService;

  public PerkStoreSettingsREST(PerkStoreService perkStoreService) {
    this.perkStoreService = perkStoreService;
  }

  /**
   * @return global settings of Perk store application
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response getSettings() {
    try {
      return Response.ok(perkStoreService.getGlobalSettings(getCurrentUserId())).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Error when retrieving global settings");
    } catch (Exception e) {
      LOG.error("Error when retrieving global settings", e);
      return Response.status(500).build();
    }
  }

  /**
   * Save global settings of Perk store application
   * 
   * @param settings
   * @return
   */
  @Path("save")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  public Response saveSettings(GlobalSettings settings) {
    try {
      perkStoreService.saveGlobalSettings(settings, getCurrentUserId());
      return Response.ok().build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Error while saving global settings");
    } catch (Exception e) {
      LOG.error("Error while saving global settings", e);
      return Response.status(500).build();
    }
  }

}
