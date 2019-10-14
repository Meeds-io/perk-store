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

import static org.exoplatform.perkstore.model.constant.ProductOrderModificationType.*;
import static org.exoplatform.perkstore.service.utils.Utils.*;

import java.util.Arrays;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.perkstore.exception.PerkStoreException;
import org.exoplatform.perkstore.model.OrderFilter;
import org.exoplatform.perkstore.model.ProductOrder;
import org.exoplatform.perkstore.model.constant.ProductOrderModificationType;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.annotations.*;

@Path("/perkstore/api/order")
@Api(value = "/perkstore/api/order", description = "Manages perk store product orders") // NOSONAR
@RolesAllowed("users")
public class PerkStoreOrderREST implements ResourceContainer {

  private static final Log                                LOG                         =
                                                              ExoLogger.getLogger(PerkStoreOrderREST.class);

  private static final List<ProductOrderModificationType> ALLOWED_ORDER_MODIFICATIONS = Arrays.asList(DELIVERED_QUANTITY,
                                                                                                      REFUNDED_QUANTITY,
                                                                                                      STATUS);

  private PerkStoreService                                perkStoreService;

  public PerkStoreOrderREST(PerkStoreService perkStoreService) {
    this.perkStoreService = perkStoreService;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("list")
  @RolesAllowed("users")
  @ApiOperation(value = "Retrieves the list of orders for current user using a filter", httpMethod = "POST", response = Response.class, consumes = "application/json", produces = "application/json", notes = "returns a list of orders")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response listOrders(@ApiParam(value = "OrderFilter object with search conditions", required = true) OrderFilter filter) {
    if (filter == null) {
      LOG.warn("Bad request sent to server with empty filter");
      return Response.status(400).build();
    }
    String currentUserId = getCurrentUserId();
    try {
      List<ProductOrder> orders = perkStoreService.getOrders(filter, currentUserId);
      return Response.ok(orders).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Listing orders", currentUserId, filter);
    } catch (Exception e) {
      LOG.error("Error listing orders", e);
      return Response.status(500).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("save")
  @RolesAllowed("users")
  @ApiOperation(value = "Creates a new order for current user on a selected product", httpMethod = "POST", response = Response.class, consumes = "application/json", produces = "application/json", notes = "returns created order")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response createOrder(@ApiParam(value = "ProductOrder object", required = true) ProductOrder order) {
    if (order == null) {
      LOG.warn("Bad request sent to server with empty order to create");
      return Response.status(400).build();
    }
    String currentUserId = getCurrentUserId();
    try {
      order = perkStoreService.createOrder(order, currentUserId);
      return Response.ok(order).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Creating new order", currentUserId, order);
    } catch (Exception e) {
      LOG.error("Error creating order", e);
      return Response.status(500).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("saveSimulate")
  @RolesAllowed("users")
  @ApiOperation(value = "Check that order can be saved with given attributes", httpMethod = "POST", response = Response.class, consumes = "application/json", produces = "application/json", notes = "returns no content")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response saveOrderSimulate(@ApiParam(value = "ProductOrder object", required = true) ProductOrder order) {
    if (order == null) {
      LOG.warn("Bad request sent to server with empty order");
      return Response.status(400).build();
    }
    String currentUserId = getCurrentUserId();
    try {
      order.setTransactionHash(FAKE_TRANSACTION_HASH);
      perkStoreService.checkCanCreateOrder(order, currentUserId);
      return Response.noContent().build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Simulating order creation", currentUserId, order);
    } catch (Exception e) {
      LOG.error("Error simulating order save", e);
      return Response.status(500).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("saveStatus")
  @RolesAllowed("users")
  @ApiOperation(value = "Modifies an existing order by specifying the type of modification (order attribute to change)", httpMethod = "POST", response = Response.class, consumes = "application/json", produces = "application/json", notes = "returns modified order")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 403, message = "Unauthorized operation"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response saveOrder(@ApiParam(value = "ProductOrder object", required = true) ProductOrder order,
                            @ApiParam(value = "ProductOrderModificationType enum name", required = true) @QueryParam("modificationType") String modificationType) {
    if (order == null) {
      LOG.warn("Bad request sent to server with empty order");
      return Response.status(400).build();
    }
    if (StringUtils.isBlank(modificationType)) {
      LOG.warn("Bad request sent to server with empty order modification type");
      return Response.status(400).build();
    }
    ProductOrderModificationType orderModificationType = ProductOrderModificationType.valueOf(modificationType.toUpperCase());
    if (!ALLOWED_ORDER_MODIFICATIONS.contains(orderModificationType)) {
      LOG.warn("Bad request sent to server with denied order modification type");
      return Response.status(403).build();
    }
    String currentUserId = getCurrentUserId();
    try {
      perkStoreService.saveOrder(order, orderModificationType, currentUserId, true);
      return Response.ok(perkStoreService.getOrderById(order.getId())).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Saving order status", currentUserId, order);
    } catch (Exception e) {
      LOG.error("Error saving order status", e);
      return Response.status(500).build();
    }
  }

}
