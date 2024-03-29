/**
 *
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2022 Meeds Association contact@meeds.io
 *
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
 *
 */
import './initComponents.js';
import * as extensions from './extensions.js';

const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

//getting language of user
const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.addon.PerkStore-${lang}.json`;

const appId = 'PerkStoreOverviewApp';


export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    Vue.createApp({
      template: `<perk-store-overview id="${appId}" />`,
      i18n,
      vuetify,
    }, `#${appId}`, 'Perks Overview');

    // get overrided components if exists
    extensions.registerPerkStoreOverview();
    if (extensionRegistry) {
      const components = extensionRegistry.loadComponents('PerkStoreOverview');
      if (components && components.length > 0) {
        components.forEach(cmp => {
          Vue.component(cmp.componentName, cmp.componentOptions);
        });
      }
    }
  });
}