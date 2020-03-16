import PerkStoreApp from './components/PerkStoreApp.vue';

Vue.use(Vuetify);

const vuetify = new Vuetify({
  dark: true,
  iconfont: 'mdi',
});

const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.addon.PerkStore-${lang}.json`;

exoi18n.loadLanguageAsync(lang, url).then(i18n => {
  new Vue({
    render: (h) => h(PerkStoreApp),
    i18n,
    vuetify,
  }).$mount('#PerkStoreApp');
});
