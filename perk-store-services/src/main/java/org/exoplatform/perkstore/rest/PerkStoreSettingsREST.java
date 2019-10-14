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
package org.exoplatform.perkstore.rest;

import static org.exoplatform.perkstore.service.utils.Utils.computeErrorResponse;
import static org.exoplatform.perkstore.service.utils.Utils.getCurrentUserId;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.perkstore.exception.PerkStoreException;
import org.exoplatform.perkstore.model.GlobalSettings;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.annotations.*;

@Path("/perkstore/api/settings")
@Api(value = "/perkstore/api/settings", description = "Manages perk store application settings") // NOSONAR
@RolesAllowed("users")
public class PerkStoreSettingsREST implements ResourceContainer {
  private static final Log LOG = ExoLogger.getLogger(PerkStoreSettingsREST.class);

  private PerkStoreService perkStoreService;

  public PerkStoreSettingsREST(PerkStoreService perkStoreService) {
    this.perkStoreService = perkStoreService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Retrieves perk store settings with user settings", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "returns global having user settings object")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getSettings() {
    String currentUserId = getCurrentUserId();
    try {
      return Response.ok(perkStoreService.getGlobalSettings(currentUserId)).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Retrieving global settings", currentUserId, null);
    } catch (Exception e) {
      LOG.error("Error when retrieving global settings", e);
      return Response.status(500).build();
    }
  }

  @Path("save")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Saves perk store global settings", httpMethod = "POST", response = Response.class, produces = "application/json", notes = "returns empty response")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response saveSettings(@ApiParam(value = "Global settings to save", required = true) GlobalSettings settings) throws Exception {
    if (settings == null) {
      return Response.status(400).build();
    }
    String currentUserId = getCurrentUserId();
    if (!perkStoreService.isPerkStoreManager(currentUserId)) {
      return Response.status(403).build();
    }
    try {
      perkStoreService.saveGlobalSettings(settings, currentUserId);
      return Response.noContent().build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Saving global settings", currentUserId, null);
    } catch (Exception e) {
      LOG.error("Error while saving global settings", e);
      return Response.status(500).build();
    }
  }

}
