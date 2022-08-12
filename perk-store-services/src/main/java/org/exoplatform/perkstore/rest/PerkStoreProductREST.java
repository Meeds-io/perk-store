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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.common.http.HTTPStatus;
import org.json.JSONObject;

import org.exoplatform.perkstore.exception.PerkStoreException;
import org.exoplatform.perkstore.model.FileDetail;
import org.exoplatform.perkstore.model.Product;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;


@Path("/perkstore/api/product")
@Tag(name = "/perkstore/api/product", description = "Manages perk store products")
@RolesAllowed("users")
public class PerkStoreProductREST implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(PerkStoreProductREST.class);

  private PerkStoreService perkStoreService;

  public PerkStoreProductREST(PerkStoreService perkStoreService) {
    this.perkStoreService = perkStoreService;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Creates or modifies a product", method = "POST", description = "Creates or modifies a product and returns the saved product")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response saveProduct(@RequestBody(description = "Product object", required = true) Product product) {
    if (product == null) {
      LOG.warn("Bad request sent to server with empty product");
      return Response.status(400).build();
    }

    String currentUserId = getCurrentUserId();
    try {
      product = perkStoreService.saveProduct(product, currentUserId);
      product = perkStoreService.getProductById(product.getId(), currentUserId);
      return Response.ok(product).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Saving product", currentUserId, product);
    } catch (Exception e) {
      LOG.error("Error saving product", e);
      return Response.status(500).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{productId}")
  @RolesAllowed("users")
  @Operation(
          summary = "Retrieves a product by its id",
          method = "GET",
          description = "Retrieves a product by its id and returns the selected product if exists")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getProduct(@Parameter(description = "Product technical id", required = true) @PathParam("productId") long productId) {
    if (productId == 0) {
      LOG.warn("Bad request sent to server with empty productId");
      return Response.status(400).build();
    }
    String currentUserId = getCurrentUserId();
    try {
      Product product = perkStoreService.getProductById(productId, currentUserId  );
      return Response.ok(product).build();
    } catch (Exception e) {
      LOG.error("Error getting product", e);
      return Response.status(500).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
          summary = "Get the list of product accessible by current user",
          method = "GET",
          description = "returns list of products")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response listProducts(@Parameter(description = "Returning only the available products or all products") @Schema(defaultValue = "false") @QueryParam("available") boolean available,
                               @Parameter(description = "Returning the number of Products or not") @Schema(defaultValue = "false") @QueryParam("returnSize") boolean returnSize) {
    String currentUserId = getCurrentUserId();
    try {
      List<Product> allProducts = perkStoreService.getProducts(available, currentUserId);
      
      if (returnSize) {
        JSONObject size = new JSONObject();
        size.put("size", allProducts.size());
        return Response.ok(size.toString()).build();
      } else {
        return Response.ok(allProducts).build();
      }
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Getting products list", currentUserId, null);
    } catch (Exception e) {
      LOG.error("Error getting products list", e);
      return Response.status(500).build();
    }
  }

  @GET
  @Path("{productId}/image/{imageId}")
  @RolesAllowed("users")
  @Operation(
          summary = "Get product image by its id",
          method = "GET",
          description = "returns image content of a product by its id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "304", description = "Image not modified"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getProductImage(@Context Request request,
                                  @Parameter(description = "Product technical id", required = true) @PathParam("productId") long productId,
                                  @Parameter(description = "Image file technical id", required = true) @PathParam("imageId") long imageId) {
    String currentUserId = getCurrentUserId();
    try {
      FileDetail fileDetail = perkStoreService.getFileDetail(productId, imageId, false, currentUserId);
      if (fileDetail == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      long lastUpdated = fileDetail.getLastUpdated();
      EntityTag eTag = new EntityTag(String.valueOf(lastUpdated));
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

  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Path("delete/{productId}")
  @RolesAllowed("users")
  @Operation(
          summary = "Retrieves a product by its id",
          method = "GET",
          description = "returns selected product if exists")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "403", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response deleteProduct(@Parameter(description = "Product technical id", required = true)
  @PathParam("productId")
  long productId) {
    if (productId <= 0) {
      LOG.warn("Bad request sent to server with empty productId");
      return Response.status(HTTPStatus.BAD_REQUEST).build();
    }
    String currentUserId = getCurrentUserId();
    try {
      perkStoreService.deleteProduct(productId, currentUserId);
      return Response.ok().build();
    } catch (IllegalArgumentException e) {
      return Response.status(HTTPStatus.BAD_REQUEST).build();
    } catch (IllegalAccessException e) {
      return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (PerkStoreException e) {
      return Response.status(HTTPStatus.NOT_FOUND).build();
    } catch (Exception e) {
      LOG.error("Error deleting product", e);
      return Response.status(500).build();
    }
  }
}
