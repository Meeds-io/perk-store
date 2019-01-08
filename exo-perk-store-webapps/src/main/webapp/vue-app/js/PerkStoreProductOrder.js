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

export function saveOrderStatus(orderId, status, delivered, refunded) {
  return fetch('/portal/rest/perkstore/api/order/saveStatus', {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: $.param({
      orderId: Number(orderId),
      status: status,
      delivered: Number(delivered),
      refunded: Number(refunded),
    }),
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error saving order status', orderId, status);
    }
  });
}
