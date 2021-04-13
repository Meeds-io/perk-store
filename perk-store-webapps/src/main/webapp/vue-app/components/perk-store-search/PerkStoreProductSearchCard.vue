<template>
  <v-card 
    class="d-flex flex-column border-radius box-shadow" 
    flat
    min-height="227">
    <div class="d-flex flex-grow-1 pa-0">
      <v-img
        v-if="hasImages"
        :src="imageSrc"
        aspect-ratio="1"
        min-height="70px"
        min-width="70px"
        height="100%"
        width="100%"
        max-height="153px"
        max-width="224px"
        class="primary--text" />
      <v-icon
        v-else
        class="primary--text ma-auto"
        size="70">
        fa-images
      </v-icon>
    </div>
    <v-list class="light-grey-background flex-grow-0 border-top-color no-border-radius pa-0">
      <v-list-item :href="productLink" class="px-0 pt-1 pb-2">
        <v-list-item-icon class="mx-0 my-auto">
          <span class="uiIconHandbag tertiary--text ps-1 pe-2 display-1"></span>
        </v-list-item-icon>
        <v-list-item-content>
          <v-list-item-title :title="result.title">
            {{ result.title }}
          </v-list-item-title>
          <v-list-item-subtitle>
            {{ result.price }}
            <span class="tertiary-color">{{ symbol }}</span>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-card>
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
  computed: {
    productLink() {
      return this.result && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore?productId=${this.result.id}`;
    },
    hasImages() {
      return this.result && this.result.imageFiles && this.result.imageFiles.length;
    },
    imageSrc() {
      return this.hasImages && this.result.imageFiles[0].src;
    },
  },
  created() {
    this.symbol = window.walletSettings && window.walletSettings.contractDetail && window.walletSettings.contractDetail.symbol;
    document.addEventListener('exo-wallet-init-result', (result) => {
      this.symbol = result && result.detail && result.detail.symbol;
    });
  },
}
</script>