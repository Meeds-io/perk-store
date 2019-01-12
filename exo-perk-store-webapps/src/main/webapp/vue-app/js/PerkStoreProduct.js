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
    if (!resp || !resp.ok) {
      return throwErrorFromServerCall(resp, 'Error saving product');
    }
  });
}
