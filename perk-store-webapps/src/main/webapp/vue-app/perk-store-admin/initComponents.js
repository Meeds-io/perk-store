import PerkStoreAdmin from './components/PerkStoreAdmin.vue';
import AutoComplete from './components/AutoComplete.vue';

const components = {
  'perk-store-admin': PerkStoreAdmin,
  'perk-store-auto-complete': AutoComplete,
};

for (const key in components) {
  Vue.component(key, components[key]);
}