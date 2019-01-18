import './../css/main.less';

import PerkStoreApp from './components/PerkStoreApp.vue';

Vue.use(Vuetify);

const vueInstance = new Vue({
  el: '#PerkStoreApp',
  render: (h) => h(PerkStoreApp),
});
