import {throwErrorFromServerCall} from './PerkStoreError.js';

export function getProductList() {
  return fetch('/portal/rest/perkstore/api/product/list', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error getting products list');
    }
  });
}

export function getProduct(productId) {
  return fetch(`/portal/rest/perkstore/api/product/get?productId=${productId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error getting product with id', productId);
    }
  });
}

export function saveProduct(product) {
  return fetch('/portal/rest/perkstore/api/product/save', {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(product),
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      return throwErrorFromServerCall(resp, 'Error saving product');
    }
  });
}
