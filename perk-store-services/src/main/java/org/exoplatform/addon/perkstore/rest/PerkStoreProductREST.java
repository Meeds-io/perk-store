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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.FileDetail;
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

    String currentUserId = getCurrentUserId();
    try {
      perkStoreService.saveProduct(product, currentUserId);
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Saving product", currentUserId, product);
    } catch (Exception e) {
      LOG.error("Error saving product", e);
      return Response.status(500).build();
    }
    return Response.ok().build();
  }

  /**
   * Get product details
   * 
   * @param productId
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("get")
  @RolesAllowed("users")
  public Response getProduct(@QueryParam("productId") String productId, @QueryParam("username") String username) {
    if (StringUtils.isBlank(productId)) {
      LOG.warn("Bad request sent to server with empty productId");
      return Response.status(400).build();
    }
    String currentUserId = getCurrentUserId();
    try {
      if (StringUtils.equals(currentUserId, username)) {
        Product product = perkStoreService.getProductById(Long.parseLong(productId), currentUserId);
        return Response.ok(product).build();
      } else {
        LOG.warn("User '{}' is attempting to access user '{}' data", currentUserId, username);
        return Response.status(403).build();
      }
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Getting product details", currentUserId, productId);
    } catch (Exception e) {
      LOG.error("Error getting product", e);
      return Response.status(500).build();
    }
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
    String currentUserId = getCurrentUserId();
    try {
      List<Product> allProducts = perkStoreService.getProducts(currentUserId);
      return Response.ok(allProducts).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Getting products list", currentUserId, null);
    } catch (Exception e) {
      LOG.error("Error getting products list", e);
      return Response.status(500).build();
    }
  }

  /**
   * Get product illustration
   * 
   * @param request
   * @param productId
   * @param imageId
   * @return
   */
  @GET
  @Path("{productId}/{imageId}")
  @RolesAllowed("users")
  public Response getProductImage(@Context Request request,
                                  @PathParam("productId") long productId,
                                  @PathParam("imageId") long imageId) {
    String currentUserId = getCurrentUserId();
    try {
      FileDetail fileDetail = perkStoreService.getFileDetail(productId, imageId, false, currentUserId);
      if (fileDetail == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      //
      long lastUpdated = fileDetail.getLastUpdated();
      EntityTag eTag = new EntityTag(String.valueOf(lastUpdated));
      //
      Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
      if (builder == null) {
        fileDetail = perkStoreService.getFileDetail(productId, imageId, true, currentUserId);
        InputStream stream = new ByteArrayInputStream(fileDetail.getData());
        builder = Response.ok(stream, "image/png");
        builder.tag(eTag);
      }
      CacheControl cc = new CacheControl();
      cc.setMaxAge(86400);
      builder.cacheControl(cc);
      return builder.cacheControl(cc).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Getting products list", currentUserId, null);
    } catch (Exception e) {
      LOG.warn("Error getting image {} on product {}", imageId, productId, e);
      return Response.serverError().build();
    }
  }

}
