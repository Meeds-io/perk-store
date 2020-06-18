<template>
  <div class="searchCard searchPerkStoreProductCard">
    <product-detail
      :product="result"
      :symbol="symbol"
      :caroussel-height="164"
      :card-height="224"
      hide-buttons
      hide-pending
      hide-elevation
      class="border-box-sizing pa-0 box-shadow border-radius"
      @product-details="openProductDetail" />
  </div>
</template>

<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    result: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    symbol: null,
  }),
  created() {
    this.symbol = window.walletSettings && window.walletSettings.contractDetail && window.walletSettings.contractDetail.symbol;
    if (!this.symbol) {
      document.addEventListener('exo-wallet-init-result', (result) => {
        this.symbol = result && result.detail && result.detail.symbol;
      });
    }
  },
  methods: {
    openProductDetail() {
      window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore?productId=${this.result.id}`;
    }
  },
}
</script>