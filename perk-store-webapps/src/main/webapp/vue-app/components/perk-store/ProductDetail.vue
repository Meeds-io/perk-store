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
  <v-flex class="productDetailContent">
    <v-hover>
      <v-card
        slot-scope="{ hover }"
        :class="hideElevation && 'productDetailContentCard' || `elevation-${hover ? 9 : 3} productDetailContentCard`"
        :max-width="cardHeight">
        <v-carousel
          :show-arrows="false"
          :interval="3000"
          :height="carousselHeight"
          hide-delimiters
          cycle
          class="carousselParent clickable"
          @click="openProductDetail">
          <template v-if="product.imageFiles">
            <v-carousel-item
              v-for="(imageFile,i) in product.imageFiles"
              :key="i"
              :src="imageFile.src"
              max="300"
              class="carousselImage" />
            <v-btn
              absolute
              color="white"
              class="productCardPrice"
              fab
              left
              small
              top>
              {{ symbol }} {{ product.price }}
            </v-btn>
          </template>
          <v-expand-transition>
            <div
              v-if="hover || !product || !product.enabled || !product.imageFiles || !product.imageFiles.length || !available || maxOrdersReached"
              class="d-flex transition-fast-in-fast-out darken-2 v-card--reveal white--text productDetailHover"
              style="height: 100%;"
              @click="openProductDetail">
              <product-detail-content
                :product="product"
                :symbol="symbol"
                :max-orders-reached="maxOrdersReached"
                :hover="hover || !product.imageFiles || !product.imageFiles.length"
                :available="available" />
            </div>
          </v-expand-transition>
        </v-carousel>
        <v-card-text
          v-if="!hideButtons"
          class="pt-2"
          style="position: relative;">
          <template v-if="userData.canEdit">
            <v-btn
              :class="ordersListBtnClass"
              :title="$t('exoplatform.perkstore.label.ordersList')"
              :right="!$vuetify.rtl"
              absolute
              color="secondary"
              class="white--text orderListButton"
              fab
              top
              small
              @click="$emit('orders-list', product)">
              <v-badge
                :value="product.notProcessedOrders"
                :color="userData.canEdit ? 'red' : 'orange'"
                :right="!$vuetify.rtl"
                overlap>
                <span
                  slot="badge"
                  class="orderListBadge">
                  {{ product.notProcessedOrders }}
                </span>
                <v-icon v-if="userData.canEdit">fa-list-ul</v-icon>
                <v-icon v-else>fa-file-invoice-dollar</v-icon>
              </v-badge>
            </v-btn>
            <v-btn
              :class="editBtnClass"
              :title="$t('exoplatform.perkstore.button.editProduct')"
              :right="!$vuetify.rtl"
              absolute
              color="secondary"
              class="white--text editButton"
              fab
              top
              small
              @click="$emit('edit', product)">
              <v-icon>fa-pen</v-icon>
            </v-btn>
            <div
              v-if="!hidePending"
              class="productCardPending py-0"
              @click="openProductDetail">
              <v-hover v-if="userData.notProcessedOrders">
                <v-chip
                  slot-scope="{ hover: hoverPending }"
                  :class="`${hoverPending && 'elevation-3'} userPendingOrders clickable`"
                  @click="$emit('orders-list', product, null, true)">
                  <v-icon
                    :left="!$vuetify.rtl"
                    size="16">
                    far fa-clock
                  </v-icon>
                 2 Pending
                </v-chip>
              </v-hover>
            </div>
          </template>
          <v-btn
            v-if="displayBuyButton"
            :disabled="disabledBuy || !walletEnabled || walletLoading"
            :loading="!disabledBuy && walletLoading"
            :title="$t('exoplatform.perkstore.button.buy')"
            :right="!$vuetify.rtl"
            absolute
            class="white--text primary"
            fab
            top
            small
            @click="displayBuyModal">
            <v-icon>fa-shopping-cart</v-icon>
          </v-btn>
        </v-card-text>
        <v-card-text
          :title="product.unlimited ? $t('exoplatform.perkstore.label.unlimitedSupply') : $t('exoplatform.perkstore.label.articlesSold', {0: purchasedPercentageLabel})"
          class="pb-0 clickable productCardTitleParent"
          @click="openProductDetail">
          <span :title="product.title">{{ product.title }}</span>
        </v-card-text>
        <v-card-text class="productCardSubtitle">{{ productCreatedDate }}</v-card-text>
      </v-card>
    </v-hover>
  </v-flex>
</template>

<script>
import ProductDetailContent from './ProductDetailContent.vue';

export default {
  components: {
    ProductDetailContent
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
    hideButtons: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    hidePending: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    hideElevation: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    cardHeight: {
      type: Number,
      default: function() {
        return 600;
      },
    },
    carousselHeight: {
      type: Number,
      default: function() {
        return 500;
      },
    },
  },
  computed: {
    productCreatedDate() {
      return (this.product && this.product.createdDate && this.formatDate(new Date(this.product.createdDate))) || '';
    },
    productLink() {
      return (this.product && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore?productId=${this.product.id}`) || '#';
    },
    userData() {
      return (this.product && this.product.userData) || {};
    },
    ordersListBtnClass() {
      if (!this.product) {
        return '';
      }
      let paddingIndex = 0;
      if (this.userData.canEdit) {
        paddingIndex++;
      }
      if (this.displayBuyButton) {
        paddingIndex++;
      }
      return `left-pa${paddingIndex}`;
    },
    editBtnClass() {
      if (this.displayBuyButton) {
        return 'left-pa1';
      }
      return '';
    },
    displayBuyButton() {
      return !this.hideButtons && this.product && this.product.enabled && this.userData.canOrder && this.product.receiverMarchand && this.product.receiverMarchand.type && this.product.receiverMarchand.id && (this.product.receiverMarchand.type !== 'user' || this.product.receiverMarchand.id !== eXo.env.portal.userName);
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
      if (this.product.unlimited) {
        return 10000;
      } else {
        const available = this.product.totalSupply - this.product.purchased;
        return available > 0 ? available : 0;
      }
    },
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
    },
    formatDate(date) {
      if (!date) {
        return '';
      }
      return date.toLocaleString(eXo.env.portal.language, {year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: 'numeric'});
    },
  }
};
</script>