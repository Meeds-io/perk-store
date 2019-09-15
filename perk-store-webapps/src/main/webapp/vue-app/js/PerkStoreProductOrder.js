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
