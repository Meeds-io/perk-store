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

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.Product;
import org.exoplatform.addon.perkstore.service.PerkStoreService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * This class provide a REST endpoint to retrieve detailed information about
 * perk store product
 */
@Path("/perkstore/api/product")
@RolesAllowed("users")
public class PerkStoreProductREST implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(PerkStoreProductREST.class);

  private PerkStoreService perkStoreService;

  public PerkStoreProductREST(PerkStoreService perkStoreService) {
    this.perkStoreService = perkStoreService;
  }

  /**
   * Save product
   * 
   * @param product
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("save")
  @RolesAllowed("users")
  public Response saveProduct(Product product) {
    if (product == null) {
      LOG.warn("Bad request sent to server with empty product");
      return Response.status(400).build();
    }
    try {
      perkStoreService.saveProduct(getCurrentUserId(), product);
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Error saving product");
    } catch (Exception e) {
      LOG.error("Error saving product", e);
      return Response.status(500).build();
    }
    return Response.ok().build();
  }

  /**
   * Retrieve products
   * 
   * @return
   */
  @Path("list")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response listProducts() {
    try {
      List<Product> allProducts = perkStoreService.getProducts(getCurrentUserId());
      return Response.ok(allProducts).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Error getting products list");
    } catch (Exception e) {
      LOG.error("Error getting products list", e);
      return Response.status(500).build();
    }
  }

}
