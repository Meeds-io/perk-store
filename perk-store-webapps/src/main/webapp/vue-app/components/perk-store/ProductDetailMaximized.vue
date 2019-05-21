<template>
  <v-card>
    <v-card-actions class="pa-3">
      <div class="headline">{{ product.title }}</div>
      <v-spacer />
      <!-- 
      <v-icon>star_border</v-icon>
      <v-icon>star_border</v-icon>
      <v-icon>star_border</v-icon>
      <v-icon>star_border</v-icon>
      <v-icon>star_border</v-icon>
       -->
    </v-card-actions>
    <v-divider light />
    <v-layout wrap>
      <v-flex
        v-if="product.imageFiles && product.imageFiles.length"
        md5
        xs12>
        <image-attachment-selector
          :images="product.imageFiles" />
      </v-flex>
      <v-flex
        :class="product.imageFiles && product.imageFiles.length ? 'md7': md12"
        xs12>
        <product-detail-content
          :product="product"
          :symbol="symbol"
          :max-orders-reached="maxOrdersReached"
          :available="available"
          hover />
        <v-divider />
        <v-card-title class="productDetailRightParent pt-0">
          <div v-if="ordered" class="alert alert-success v-content">
            <i class="uiIconSuccess"></i>
            Your order has been submitted successfully. <a href="javascript:void(0);" @click="ordered = false">Close</a>
          </div>
          <buy-form
            v-else
            ref="buyForm"
            :product="product"
            :symbol="symbol"
            :need-password="needPassword"
            :wallet-loading="walletLoading"
            :wallet-enabled="walletEnabled"
            opened
            integrated-form
            class="productBuyForm"
            @ordered="ordered =true; $emit('ordered', $event)" />
          <v-divider vertical />
          <div class="pl-2 pr-1 pt-2 pb-1 productDescription">{{ product.description }}</div>
        </v-card-title>
      </v-flex>
    </v-layout>
  </v-card>
</template>

<script>
import ProductDetailContent from './ProductDetailContent.vue';
import ImageAttachmentSelector from './ImageAttachmentSelector.vue';
import BuyForm from './BuyForm.vue';

export default {
  components: {
    ProductDetailContent,
    ImageAttachmentSelector,
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
        return '';
      },
    },
    needPassword: {
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
    walletEnabled: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
  },
  data: () => ({
    ordered: false,
  }),
  computed: {
    productLink() {
      return (this.product && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore?productId=${this.product.id}`) || '#';
    },
    userData() {
      return (this.product && this.product.userData) || {};
    },
    ordersListBtnClass() {
      if(!this.product) {
        return '';
      }
      let paddingIndex = 0;
      if(this.userData.canEdit) {
        paddingIndex++;
      }
      if(this.displayBuyButton) {
        paddingIndex++;
      }
      return `left-pa${paddingIndex}`;
    },
    editBtnClass() {
      if(this.displayBuyButton) {
        return 'left-pa1';
      }
      return '';
    },
    displayBuyButton() {
      return this.product && this.product.enabled && this.userData.canOrder && this.product.receiverMarchand && this.product.receiverMarchand.type && this.product.receiverMarchand.id && (this.product.receiverMarchand.type !== 'user' || this.product.receiverMarchand.id !== eXo.env.portal.userName);
    },
    disabledBuy() {
      return (!this.product.unlimited && this.available <= 0) || this.maxOrdersReached;
    },
    purchasedPercentageLabel() {
      return `${Number(this.purchasedPercentage).toFixed(0)}%`;
    },
    purchasedPercentage() {
      return !this.product.unlimited ? this.product.totalSupply ? ((this.product.purchased * 100) /this.product.totalSupply) : 100 : 0;
    },
    maxOrdersCurrentPeriodReached() {
      return this.userData && this.product.orderPeriodicity && this.userData.purchasedInCurrentPeriod && this.userData.purchasedInCurrentPeriod >= this.product.maxOrdersPerUser;
    },
    maxOrdersAllTimeReached() {
      return this.userData && !this.product.orderPeriodicity && this.userData.totalPurchased && this.userData.totalPurchased >= this.product.maxOrdersPerUser;
    },
    maxOrdersReached() {
      return this.product.maxOrdersPerUser && this.userData && (this.maxOrdersCurrentPeriodReached || this.maxOrdersAllTimeReached);
    },
    available() {
      if(this.product.unlimited) {
        return 10000;
      } else {
        const available = this.product.totalSupply - this.product.purchased;
        return available > 0 ? available : 0;
      }
    },
  },
  watch: {
    product() {
      this.ordered = false;
    }
  },
  methods: {
    openProductDetail(event) {
      event.stopPropagation();
      event.preventDefault();

      this.$emit('product-details', this.product);
    },
    displayBuyModal() {
      if (!this.disabledBuy && this.walletEnabled) {
        this.$emit('buy', this.product);
      }
    }
  }
}
</script>