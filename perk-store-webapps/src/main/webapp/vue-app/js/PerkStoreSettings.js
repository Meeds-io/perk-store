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
    fraud: true,
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

export function getProductFilter() {
  const filter = localStorage.getItem('exo-perkstore-product-filter');
  if (filter) {
    return JSON.parse(filter);
  } else {
    return {
      disabled: false,
      soldOut: false,
    };
  }
}

export function storeProductFilter(filter) {
  localStorage.setItem('exo-perkstore-product-filter', JSON.stringify(filter));
}

export function formatDateTime(dateString) {
  if (!dateString) {
    return '-';
  }
  const date = new Date(dateString);
  return `${date.toLocaleDateString(eXo.env.portal.language, {year: 'numeric', month: 'long', day: 'numeric'})} - ${date.toLocaleTimeString()}`;
}

export function formatDate(dateString) {
  if (!dateString) {
    return '-';
  }
  const date = new Date(dateString);
  return `${date.toLocaleDateString(eXo.env.portal.language, {year: 'numeric', month: 'long', day: 'numeric'})}`;
}
