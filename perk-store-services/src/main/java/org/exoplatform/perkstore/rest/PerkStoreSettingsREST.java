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
package org.exoplatform.perkstore.rest;

import static org.exoplatform.perkstore.service.utils.Utils.computeErrorResponse;
import static org.exoplatform.perkstore.service.utils.Utils.getCurrentUserId;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.perkstore.exception.PerkStoreException;
import org.exoplatform.perkstore.model.GlobalSettings;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;


@Path("/perkstore/api/settings")
@Tag(name = "/perkstore/api/settings", description = "Manages perk store application settings")
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
  @Operation(
          summary = "Retrieves perk store settings with user settings",
          method = "GET",
          description = "returns global having user settings object")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
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
  @Operation(
          summary = "Saves perk store global settings",
          method = "POST",
          description = "Saves perk store global settings and returns an empty response")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response saveSettings(@RequestBody(description = "Global settings to save", required = true) GlobalSettings settings) throws Exception {
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
