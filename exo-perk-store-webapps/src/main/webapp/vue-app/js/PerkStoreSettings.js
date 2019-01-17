import {initCometd} from './PerkStoreWebSocket.js';
import {throwErrorFromServerCall} from './PerkStoreError.js';

export function initSettings() {
  return getSettings().then((settings) => {
    settings = window.perkStoreSettings = Object.assign({}, settings);
    initCometd(settings);
    return settings;
  });
}

export function getSettings() {
  return fetch(`/portal/rest/perkstore/api/settings`, {credentials: 'include'}).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      return throwErrorFromServerCall(resp, 'Error getting settings');
    }
  });
}

export function saveSettings(settings) {
  if (settings) {
    return fetch(`/portal/rest/perkstore/api/settings/save`, {
      credentials: 'include',
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(settings),
    }).then((resp) => {
      if (!resp || !resp.ok) {
        return throwErrorFromServerCall(resp, 'Error saving settings');
      }
    });
  } else {
    throw new Error('Error saving empty settings', settings);
  }
}

export function getDefaultOrderFilter() {
  return {
    ordered: true,
    canceled: true,
    error: true,
    paid: true,
    partial: true,
    delivered: true,
    refunded: true,
  };
}

export function getOrderFilter() {
  const filter = localStorage.getItem('exo-perkstore-order-filter');
  if (filter) {
    return JSON.parse(filter);
  } else {
    return getDefaultOrderFilter();
  }
}

export function saveOrderFilter(filter) {
  localStorage.setItem('exo-perkstore-order-filter', JSON.stringify(filter));
}
