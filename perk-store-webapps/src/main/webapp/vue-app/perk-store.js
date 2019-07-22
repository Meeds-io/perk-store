import './../css/main.less';

import PerkStoreApp from './components/PerkStoreApp.vue';

Vue.use(Vuetify);

$(document).ready(() => {
  const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || 'en';
  const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.addon.PerkStore-${lang}.json`;

  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    new Vue({
      el: '#PerkStoreApp',
      render: (h) => h(PerkStoreApp),
      i18n,
    })
  });
});
