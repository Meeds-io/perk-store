export function getOrderList(productId) {
  if (!productId) {
    return Promise.resolve([]);
  }

  return fetch(`/portal/rest/perkstore/api/order/list?productId=${productId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error getting orders of product id', productId);
    }
  });
}

export function saveOrder(order) {
  return fetch('/portal/rest/perkstore/api/order/save', {
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
      throw new Error('Error saving order', order);
    }
  });
}

export function saveOrderStatus(orderId, status, toRefund, refunded) {
  return fetch('/portal/rest/perkstore/api/order/saveStatus', {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: $.param({
      orderId: Number(orderId),
      status: status,
      toRefund: toRefund,
      refunded: refunded,
    }),
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error saving order status', orderId, status);
    }
  });
}
