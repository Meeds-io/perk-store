<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-card>
    <v-layout wrap class="pt-3">
      <v-flex
        md5
        xs12>
        <image-attachment-selector :images="product.imageFiles" />
      </v-flex>
      <v-flex
        md7
        xs12>
        <product-detail-content
          :product="product"
          :symbol="symbol"
          :max-orders-reached="maxOrdersReached"
          :available="available"
          integrated />
        <v-card-title class="productDetailRightParent pt-0 px-0">
          <div v-if="ordered" class="alert alert-success v-content subtitle-2 mx-10">
            <i class="uiIconSuccess"></i>
            {{ $t('exoplatform.perkstore.info.orderSubmittedSuccessfully') }}.
            <a
              class="no-wrap"
              href="javascript:void(0);"
              @click="ordered = false">
              {{ $t('exoplatform.perkstore.button.close') }}
            </a>
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
            integrated
            class="productBuyForm"
            @close="$emit('close')"
            @ordered="ordered =true; $emit('ordered', $event)" />
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