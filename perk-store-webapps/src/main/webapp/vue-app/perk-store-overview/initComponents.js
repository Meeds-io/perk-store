import PerkStoreOverview from './components/PerkStoreOverview.vue';
import ProductDetail from '../components/perk-store/ProductDetail.vue';

const components = {
  'perk-store-overview': PerkStoreOverview,
  'perk-store-product-detail': ProductDetail,
};

for (const key in components) {
  Vue.component(key, components[key]);
}