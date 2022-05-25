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
  <v-flex class="perkStoreDetailContent">
    <v-card
      class="productDetailContentCard"
      :max-width="cardHeight">
      <v-carousel
        :show-arrows="false"
        :interval="3000"
        :continuous="!showMenu"
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
              <v-spacer />
              <v-menu
                v-if="isProductOwner"
                v-model="showMenu"
                offset-x
                offset-y
                :left="rtlDisplay"
                attach>
                <template #activator="{ on}">
                  <v-btn
                    dark
                    icon
                    class="productCardAction mr-0"
                    v-on="on"
                    @blur="closeMenu">
                    <v-icon>mdi-dots-vertical</v-icon>
                  </v-btn>
                </template>
                <v-list class="pa-0 ma-0">
                  <v-list-item
                    v-if="userData.canEdit"
                    class="editLabelProduct"
                    @mousedown="$event.preventDefault()">
                    <v-list-item-title class="editProductMenu ml-n2" @click="$emit('edit', product)">
                      <i class="uiIconEdit" :class="iconClass"> </i>{{ $t('exoplatform.perkstore.button.menuEditProduct') }}
                    </v-list-item-title>
                  </v-list-item>
                  <v-list-item class="editLabelProduct" @mousedown="$event.preventDefault()">
                    <v-list-item-title class="editProductMenu ml-n2" @click="$emit('orders-list', product, null)">
                      <i class="fas fa-list primary--text" :class="iconClass"> </i>{{ $t('exoplatform.perkstore.button.menuProductOrders') }}
                    </v-list-item-title>
                  </v-list-item>
                  <v-list-item class="editLabelProduct" @mousedown="$event.preventDefault()">
                    <v-list-item-title class="editProductMenu ml-n2" @click="confirmDelete()">
                      <i class="uiIconTrash primary--text" :class="iconClass"> </i>{{ $t('exoplatform.perkstore.button.menuProductDelete') }}
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
        :class="cardTextClass"
        class="pt-2 pendingCard pb-0"
        v-on="!cantBuyProduct && !userData.notProcessedOrders ? { click: displayBuyModal } : {}">
        <div
          v-if="!hidePending"
          class="productCardPending py-0">
          <v-hover v-if="userData.notProcessedOrders">
            <v-chip
              slot-scope="{ hover: hoverPending }"
              :class="`${hoverPending && 'elevation-3'} userPendingOrders clickable`"
              @click="$emit('orders-list', product, null)">
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
          :disabled="(disabledBuy || !walletEnabled || walletLoading || walletDeleted) || !displayBuyButton"
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
      <div
        :class="cardTextClass"
        class="pt-4"
        v-on="!cantBuyProduct ? { click: displayBuyModal } : {}">
        <v-card-text
          :title="product.unlimited ? $t('exoplatform.perkstore.label.unlimitedSupply') : $t('exoplatform.perkstore.label.articlesSold', {0: purchasedPercentageLabel})"
          class="pb-0 productCardTitleParent">
          <span :title="product.title">{{ product.title }}</span>
        </v-card-text>
        <v-card-text
          class="productCardSubtitle">
          <span class="priceSymbol">{{ symbol }}</span> {{ product.price }}
        </v-card-text>
      </div>
    </v-card>
    <exo-confirm-dialog
      ref="deleteProductConfirmDialog"
      :title="$t('exoplatform.perkstore.label.delete')"
      :message="$t('exoplatform.perkstore.label.deleteConfirmMessage')"
      :ok-label="$t('exoplatform.perkstore.label.ok')"
      :cancel-label="$t('exoplatform.perkstore.label.cancel')"
      @ok="removeProduct" />
  </v-flex>
</template>

<script>
import {deleteProduct} from '../../js/PerkStoreProduct.js';

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
    walletDeleted: {
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
    isProductOwner() {
      return this.product.userData.canEdit || this.product.userData.username === this.product.creator.id || this.product.userData.username === this.product.receiverMarchand.id ;
    },
    cardTextClass() {
      return this.cantBuyProduct ? '' : 'clickable ';
    },
    cantBuyProduct() {
      return (this.disabledBuy || !this.walletEnabled || this.walletLoading || this.walletDeleted ) || !this.displayBuyButton;
    },
    buyButtonClass(){
      return !this.cantBuyProduct ? 'btn btn-primary' : 'disabledBuyButton';
    },
    buyButtonTitle(){
      if (!this.walletEnabled || this.walletDeleted) {
        return this.$t('exoplatform.perkstore.label.disabledOrDeletedWallet');
      }  else if (this.product.receiverMarchand.id === eXo.env.portal.userName){
        return this.$t('exoplatform.perkstore.button.disabledBuyButton');
      } else if (!this.product.enabled){
        return  this.$t('exoplatform.perkstore.label.disabledProduct');
      } else if (this.maxOrdersReached){
        return this.$t('exoplatform.perkstore.label.maxOrdersIsReached', {0: this.product.maxOrdersPerUser, });
      }  else if (!this.product.unlimited && !this.available){
        return this.$t('exoplatform.perkstore.label.SoldOut');
      } else {
        return this.$t('exoplatform.perkstore.button.buy');
      }
    },
    productCreatedDate() {
      return (this.product && this.product.createdDate && this.formatDate(new Date(this.product.createdDate))) || '';
    },
    productLink() {
      return (this.product && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/catalog?productId=${this.product.id}`) || '#';
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
      return !this.hideButtons && this.product && this.product.enabled && this.userData.canOrder
          && this.product.receiverMarchand && this.product.receiverMarchand.type && this.product.receiverMarchand.id
          && (this.product.receiverMarchand.type !== 'user' || this.product.receiverMarchand.id !== eXo.env.portal.userName)
          && this.product.creator && this.product.creator.type && this.product.creator.id;
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
    iconClass () {
      return !this.$vuetify.rtl && 'mr-2 ' || 'ml-2';
    },
    rtlDisplay() {
      return !this.$vuetify.rtl;
    }
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
    confirmDelete() {
      this.$refs.deleteProductConfirmDialog.open();
    },
    removeProduct() {
      deleteProduct(this.product.id)
        .then(() => {
          this.$root.$emit('show-alert', {type: 'success',message: this.$t('exoplatform.perkstore.label.deleteSuccess')});
          this.$emit('product-deleted');
        }).catch(e => {
          let msg = '';
          if (e.message === '403') {
            msg = this.$t('exoplatform.perkstore.label.deletePermissionDenied');
          } else if (e.message  === '404') {
            msg = this.$t('exoplatform.perkstore.label.notFound');
          } else  {
            msg = this.$t('exoplatform.perkstore.label.deleteError');
          }
          this.$root.$emit('show-alert', {type: 'error',message: msg});
        });
    },
  }
};
</script>