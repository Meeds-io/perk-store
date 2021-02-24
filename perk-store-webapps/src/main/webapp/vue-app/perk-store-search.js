import PerkStoreProductSearchCard from './components/perk-store-search/PerkStoreProductSearchCard.vue';
import ProductDetail from './components/perk-store/ProductDetail.vue';

Vue.component('perk-store-product-search-card', PerkStoreProductSearchCard);
Vue.component('product-detail', ProductDetail);

export function formatSearchResult(results, term) {
  if (results && results.length) {
    results = results.filter(product => (product.title && product.title.toLowerCase().indexOf(term.toLowerCase()) >= 0) || (product.description && product.description.toLowerCase().indexOf(term.toLowerCase()) >= 0));
  }
  return results;
}

$(document).ready(() => {
  if (window.walletAddonInstalled) {
    document.dispatchEvent(new CustomEvent('exo-wallet-init'));
  }
});
