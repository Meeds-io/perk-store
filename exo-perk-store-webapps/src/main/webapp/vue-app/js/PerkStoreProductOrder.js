import {throwErrorFromServerCall} from './PerkStoreError.js';

export function getOrderList(productId, filter, limit) {
  if (!productId) {
    return Promise.resolve([]);
  }

  filter = Object.assign({}, filter, {productId: productId, limit: limit ? limit : 0});

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

export function saveOrder(order, simulate) {
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

export function saveOrderStatus(orderId, productId, status, delivered, refunded) {
  return fetch('/portal/rest/perkstore/api/order/saveStatus', {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      id: Number(orderId),
      productId: Number(productId),
      status: status,
      deliveredQuantity: Number(delivered),
      refundedQuantity: Number(refunded),
    }),
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      return throwErrorFromServerCall(resp, 'Error saving order status');
    }
  });
}
