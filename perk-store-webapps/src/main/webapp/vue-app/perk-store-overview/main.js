import './initComponents.js';
import * as extensions from './extensions.js';

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

//getting language of user
const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.addon.PerkStore-${lang}.json`;

const appId = 'PerkStoreOverviewApp';


export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    new Vue({
      template: `<perk-store-overview id="${appId}" />`,
      i18n,
      vuetify,
    }).$mount(`#${appId}`);

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