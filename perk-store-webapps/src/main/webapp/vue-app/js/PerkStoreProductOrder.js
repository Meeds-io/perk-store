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
import {throwErrorFromServerCall} from './PerkStoreError.js';

const DEFAULT_DECIMALS = 3;

export function toFixed(value, decimals) {
  if (!decimals) {
    decimals = DEFAULT_DECIMALS;
  }
  try {
    return Number.parseFloat(value).toFixed(decimals).replace(/(\..*[1-9])0+$/, '$1').replace(/\.0*$/, '');
  } catch (e) {
    console.error('Error parsing value ', value, ' same value will be retruned', e);
    return value;
  }
}

export function getOrderList(productId, filter, selectedOrderId, currentUserOrders, limit) {
  if (!productId) {
    productId = 0;
  }

  filter = Object.assign(filter || {}, {
    productId: productId,
    currentUserOrders: currentUserOrders || false,
    selectedOrderId: selectedOrderId,
    limit: limit ? limit : 0,
  });

  return fetch('/portal/rest/perkstore/api/order/list', {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(filter),
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      return throwErrorFromServerCall(resp, `Error getting orders of product id : ${productId}`);
    }
  });
}

export function createOrder(order, simulate) {
  return fetch(`/portal/rest/perkstore/api/order/save${simulate ? 'Simulate' : ''}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(order),
  }).then((resp) => {
    if (!resp || !resp.ok) {
      return throwErrorFromServerCall(resp, 'Error saving order');
    }
  });
}

export function saveOrderStatus(order, modificationType) {
  delete order.modificationType;
  return fetch(`/portal/rest/perkstore/api/order/saveStatus?modificationType=${modificationType}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(order),
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      return throwErrorFromServerCall(resp, 'Error saving order status');
    }
  });
}
