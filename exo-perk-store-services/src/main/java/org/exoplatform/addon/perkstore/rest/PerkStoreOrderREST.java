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

import static org.exoplatform.addon.perkstore.model.constant.ProductOrderModificationType.*;
import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.util.Arrays;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.OrderFilter;
import org.exoplatform.addon.perkstore.model.ProductOrder;
import org.exoplatform.addon.perkstore.model.constant.ProductOrderModificationType;
import org.exoplatform.addon.perkstore.service.PerkStoreService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * This class provide a REST endpoint to retrieve detailed information about
 * perk store order
 */
@Path("/perkstore/api/order")
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

  /**
   * List orders of a product
   * 
   * @param filter
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("list")
  @RolesAllowed("users")
  public Response listOrders(OrderFilter filter) {
    if (filter == null) {
      LOG.warn("Bad request sent to server with empty filter");
      return Response.status(400).build();
    }
    try {
      List<ProductOrder> orders = perkStoreService.getOrders(filter, getCurrentUserId());
      return Response.ok(orders).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Error listing orders");
    } catch (Exception e) {
      LOG.error("Error listing orders", e);
      return Response.status(500).build();
    }
  }

  /**
   * Create order
   * 
   * @param order
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("save")
  @RolesAllowed("users")
  public Response createOrder(ProductOrder order) {
    if (order == null) {
      LOG.warn("Bad request sent to server with empty order to create");
      return Response.status(400).build();
    }
    try {
      perkStoreService.createOrder(order, getCurrentUserId());
      return Response.ok().build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Error creating new order");
    } catch (Exception e) {
      LOG.error("Error creating order", e);
      return Response.status(500).build();
    }
  }

  /**
   * Save order simulation
   * 
   * @param order
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("saveSimulate")
  @RolesAllowed("users")
  public Response saveOrderSimulate(ProductOrder order) {
    if (order == null) {
      LOG.warn("Bad request sent to server with empty order");
      return Response.status(400).build();
    }
    try {
      order.setTransactionHash(FAKE_TRANSACTION_HASH);
      perkStoreService.checkCanOrder(order, getCurrentUserId());
      return Response.ok().build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Error simulating order save");
    } catch (Exception e) {
      LOG.error("Error simulating order save", e);
      return Response.status(500).build();
    }
  }

  /**
   * Save order status
   * 
   * @param order
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("saveStatus")
  @RolesAllowed("users")
  public Response saveOrder(ProductOrder order, @QueryParam("modificationType") String modificationType) {
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
    try {
      perkStoreService.saveOrder(order, orderModificationType, getCurrentUserId(), true);
      return Response.ok(perkStoreService.getOrderById(order.getId())).build();
    } catch (PerkStoreException e) {
      return computeErrorResponse(LOG, e, "Error saving order status");
    } catch (Exception e) {
      LOG.error("Error saving order status", e);
      return Response.status(500).build();
    }
  }

}
