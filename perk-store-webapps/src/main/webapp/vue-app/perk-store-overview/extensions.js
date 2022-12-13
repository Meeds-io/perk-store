export function registerPerkStoreOverview() {
  extensionRegistry.registerComponent('my-rewards-perkstore-overview', 'my-rewards-perkstore-item', {
    id: 'perkstore-rewards-overview',
    vueComponent: Vue.options.components['perk-store-overview'],
    rank: 10,
  });
}