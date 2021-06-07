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
    <v-card
      :class="hideElevation && 'productDetailContentCard' || `elevation-${hover ? 9 : 1} productDetailContentCard`"
      :max-width="cardHeight">
      <v-carousel
        :show-arrows="false"
        :interval="3000"
        :height="carousselHeight"
        hide-delimiters
        cycle
        class="carousselParent">
        <template v-if="product.imageFiles">
          <v-carousel-item
            v-for="(imageFile,i) in product.imageFiles"
            :key="i"
            :src="imageFile.src"
            max="300"
            class="carousselImage">
            <v-toolbar
              color="transparent"
              class="toolbarCard"
              flat>
              <v-btn
                icon
                small
                :title="$t('exoplatform.perkstore.button.productInfo')"
                class="peopleInfoIcon productCardAction d-flex">
                <v-icon size="12">fa-info</v-icon>
              </v-btn>
              <v-spacer />
              <v-menu
                v-if="userData.canEdit"
                v-model="showMenu"
                offset-y
                attach
                :nudge-left="110">
                <template v-slot:activator="{ on}">
                  <v-btn
                    dark
                    :title="$t('exoplatform.perkstore.button.editProduct', {0: product.title})"
                    icon
                    class="productCardAction mr-0"
                    v-on="on"
                    @blur="closeMenu">
                    <v-icon>mdi-dots-vertical</v-icon>
                  </v-btn>
                </template>
                <v-list class="pa-0 ma-0">
                  <v-list-item class="editLabelProduct" @mousedown="$event.preventDefault()">
                    <v-list-item-title class="editProductMenu ml-n2" @click="$emit('edit', product)">
                      <i class="uiIconEdit mr-2"> </i>{{ $t('exoplatform.perkstore.button.menuEditProduct') }}
                    </v-list-item-title>
                  </v-list-item>
                </v-list>
              </v-menu>
            </v-toolbar>
          </v-carousel-item>
        </template>
      </v-carousel>
      <v-card-text
        v-if="!hideButtons"
        class="pt-2 pendingCard"
        v-on="!cantBuyProduct && !userData.notProcessedOrders ? { click: displayBuyModal } : {}">
        <div
          v-if="!hidePending"
          class="productCardPending py-0">
          <v-hover v-if="userData.notProcessedOrders">
            <v-chip
              slot-scope="{ hover: hoverPending }"
              :class="`${hoverPending && 'elevation-3'} userPendingOrders clickable`"
              @click="$emit('orders-list', product, null, true)">
              <v-icon
                :left="!$vuetify.rtl"
                size="16"
                class="userPendingOrdersIcon">
                far fa-clock
              </v-icon>
              {{ userData.notProcessedOrders }} {{ $t('exoplatform.perkstore.label.pending') }}
            </v-chip>
          </v-hover>
        </div>
        <v-btn
          :disabled="(disabledBuy || !walletEnabled || walletLoading) || !displayBuyButton"
          :loading="!disabledBuy && walletLoading"
          :title="buyButtonTitle"
          :right="!$vuetify.rtl"
          :class="buyButtonClass"
          class="white--text buyButton"
          absolute
          fab
          top
          icon
          @click="displayBuyModal">
          <v-icon>fa-shopping-cart</v-icon>
        </v-btn>
      </v-card-text>
      <div class="footerCardProduct" v-on="!cantBuyProduct ? { click: displayBuyModal } : {}">
        <v-card-text
          :title="product.unlimited ? $t('exoplatform.perkstore.label.unlimitedSupply') : $t('exoplatform.perkstore.label.articlesSold', {0: purchasedPercentageLabel})"
          class="pb-0 clickable productCardTitleParent">
          <span :title="product.title">{{ product.title }}</span>
        </v-card-text>
        <v-card-text
          class="productCardSubtitle">
          <span class="priceSymbol">{{ symbol }}</span> {{ product.price }}
        </v-card-text>
      </div>
    </v-card>
  </v-flex>
</template>

<script>
export default {
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
  data: () => ({
    showMenu: false
  }),
  computed: {
    cantBuyProduct() {
      return (this.disabledBuy || !this.walletEnabled || this.walletLoading) || !this.displayBuyButton;
    },
    buyButtonClass(){
      return this.displayBuyButton ? 'btn btn-primary' : 'disabledBuyButton';
    },
    buyButtonTitle(){
      return this.displayBuyButton ? this.$t('exoplatform.perkstore.button.buy') : this.$t('exoplatform.perkstore.button.disabledBuyButton');
    },
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
    closeMenu() {
      this.showMenu = false;
    },
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