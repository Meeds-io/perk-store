import './initComponents.js';

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
