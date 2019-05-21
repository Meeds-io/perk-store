<template>
  <v-dialog
    v-model="dialog"
    content-class="uiPopup with-overflow"
    class="buyProductModal"
    width="300px"
    max-width="100vw"
    persistent
    @keydown.esc="close">
    <v-card class="elevation-12">
      <div class="popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="close">
        </a>
        <span class="PopupTitle popupTitle ellipsis">
          Buy {{ product && product.title }}
        </span>
      </div>
      <buy-form
        ref="buyForm"
        :product="product"
        :symbol="symbol"
        :need-password="needPassword"
        :wallet-loading="walletLoading"
        :wallet-enabled="walletEnabled"
        :opened="dialog"
        @ordered="dialog = false;$emit('ordered', $event)"
        @close="close" />
    </v-card>
  </v-dialog>
</template>

<script>
import BuyForm from './BuyForm.vue';

export default {
  components: {
    BuyForm,
  },
  props: {
    product: {
      type: Object,
      default: function() {
        return {};
      },
    },
    symbol: {
      type: String,
      default: function() {
        return null;
      },
    },
    needPassword: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    walletEnabled: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    walletLoading: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
  },
  data() {
    return {
      dialog: false,
    };
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.$refs.buyForm.init();
      } else {
        this.$emit('closed');
      }
    },
  },
  methods: {
    open() {
      this.dialog = true;
    },
    close() {
      if(!this.loading) {
        this.dialog = false;
      }
    },
  },
};
</script>
