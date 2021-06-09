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
  <v-hover v-if="order" class="orderDetailParent">
    <v-card slot-scope="{ hover }" :class="`elevation-${hover ? 9 : 1}`">
      <v-card-title v-if="order.sender" class="pt-1 pb-1 subtitle-1 orderCardTitle no-wrap">
        <div class="text-truncate orderDetailText">
          <perk-store-profile-link
            v-if="order.sender"
            :id="order.sender.id"
            :space-id="order.sender.spaceId"
            :url-id="order.sender.spaceURLId"
            :type="order.sender.type"
            :display-name="order.sender.displayName"
            display-avatar />
        </div>
        <v-spacer />
        <i v-if="!orderCheckIn" class="uiIconEcmsCheckOut orderDetailCheckOutUiIcons"></i>
        <i v-else class="uiIconEcmsCheckIn orderDetailCheckInUiIcons"></i>
      </v-card-title>
      <v-divider />

      <v-list dense>
        <v-list-item class="mb-1 pl-4 pb-3 pr-2 pt-1 orderCardSubtitle">
          <v-list-item-content class="align-start">
            <h4>
              <a
                :href="orderLink"
                rel="nofollow"
                target="_blank">
                <strong>#{{ order.id }}</strong>
              </a>
            </h4>
          </v-list-item-content>
          <v-list-item-content class="align-end">
            <template v-if="!orderCheckIn">
              <select
                v-model="order.status"
                class="small my-auto me-2 ignore-vuetify-classes"
                @change="changeStatus()">
                <option
                  v-for="option in statusList"
                  :key="option"
                  :value="option">
                  {{ $t(`exoplatform.perkstore.label.status.${option.toLowerCase()}`) }}
                </option>
              </select>
            </template>
            <span v-else>{{ $t(`exoplatform.perkstore.label.status.${order.status.toLowerCase()}`) }}</span>
          </v-list-item-content>
        </v-list-item>
        <v-divider />
        <v-list-item>
          <i class="uiIconDatePicker orderDetailUiIcons"></i>
          <div :title="createdDateLabel" class="orderDetailText text-truncate">
            {{ createdDateLabel }}
          </div>
        </v-list-item>
        <v-list-item>
          <i class="uiIconTag orderDetailUiIcons"></i>
          <div class="text-truncate orderDetailText">
            {{ order.quantity }} x
            <a
              :href="productLink"
              :title="productTitle"
              @click="openProductDetail">
              {{ productTitle }}
            </a>
          </div>
        </v-list-item>
        <v-list-item>
          <div class="no-wrap text-truncate orderDetailText">
            <i class="uiIconCard orderDetailUiIcons"></i>
            <a
              v-if="transactionLink"
              :href="transactionLink"
              :title="$t('exoplatform.perkstore.label.openInWallet')"
              rel="nofollow"
              target="_blank">
              {{ symbol }} {{ orderAmount }}
            </a>
            <template v-else>
              {{ symbol }} {{ orderAmount }}
            </template>
            {{ $t('exoplatform.perkstore.label.sentTo') }}
            <perk-store-profile-link
              v-if="order.receiver"
              :id="order.receiver.id"
              :space-id="order.receiver.spaceId"
              :url-id="order.receiver.spaceURLId"
              :type="order.receiver.type"
              :display-name="order.receiver.displayName" />
          </div>
        </v-list-item>
      </v-list>
      <v-divider />
      <v-list dense class="orderProcessingDetails ">
        <v-list-item
          class="orderProcessingContent orderCardSubtitle"
          :class="orderProcessingClass">
          <div class="no-wrap">
            <div v-if="!refunding && (!order.remainingQuantityToProcess || isError)">
              <v-icon class="green--text me-1" size="16">fa-check-circle</v-icon>{{ $t('exoplatform.perkstore.label.processingDone') }}
            </div>
            <template v-else-if="userData && userData.canEdit">
              <button
                v-if="isOrdered"
                class="ignore-vuetify-classes btn orderProcessingBtn text-truncate"
                @click="changeStatus('CANCELED')">
                {{ $t('exoplatform.perkstore.button.cancel') }}
              </button>
              <button
                v-if="canDeliverOrder"
                class="ignore-vuetify-classes btn btn-primary orderProcessingBtn text-truncate me-1 ms-1"
                @click="$refs.deliverModal.openNoSelection()">
                {{ $t('exoplatform.perkstore.button.deliver') }}
              </button>
              <button
                v-if="canRefundOrder"
                class="ignore-vuetify-classes btn orderProcessingBtn text-truncate text-truncate me-1 ms-1"
                @click="$refs.refundModal.open()">
                {{ $t('exoplatform.perkstore.button.refund') }}
              </button>
              <perk-store-deliver-modal
                v-if="canDeliverOrder"
                ref="deliverModal"
                :product="product"
                :order="order" />
              <perk-store-refund-modal
                v-if="canRefundOrder"
                ref="refundModal"
                :product="product"
                :order="order"
                :symbol="symbol"
                @refunding="refunding = true"
                @refunded="refunded"
                @closed="refundDialogClosed" />
            </template>
            <div v-else-if="isCanceled">
              {{ $t('exoplatform.perkstore.label.status.canceled') }}
            </div>
            <div v-else>
              <v-icon class="orange--text me-1" size="16">far fa-clock</v-icon>PENDING
            </div>
          </div>
          <div v-if="order.deliveredDate" class="align-end">
            <div :title="deliveredDateLabel" class=" text-truncate orderDeliveredDate ">
              {{ deliveredDateLabel }}
            </div>
          </div>
        </v-list-item>
        <v-list-item class="mt-1 justify-center">
          <div class="no-wrap orderDeliveredLabel ">
            <v-progress-circular
              :rotate="360"
              :size="40"
              :width="5"
              :value="deliveredPercentage"
              :color="deliveredPercentageColor"
              class="ms-2">
              <span class="no-wrap">
                {{ order.deliveredQuantity }}/{{ order.quantity }}
              </span>
            </v-progress-circular>
          </div>
        </v-list-item>
      </v-list>
    </v-card>
  </v-hover>
</template>

<script>
import {toFixed, saveOrderStatus} from '../../js/PerkStoreProductOrder.js';
import {formatDateTime} from '../../js/PerkStoreSettings.js';
import {getProduct} from '../../js/PerkStoreProduct.js';

export default {
  props: {
    product: {
      type: Object,
      default: function() {
        return {};
      },
    },
    order: {
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
  },
  data() {
    return {
      // Attention: Used to close modal only when refunding process
      // is finished
      refunding: false,
      orderProduct: {},
      statusList: [
        'ORDERED',
        'CANCELED',
        'ERROR',
        'PAID',
        'PARTIAL',
        'DELIVERED',
        'REFUNDED',
        'FRAUD',
      ],
    };
  },
  computed: {
    orderProcessingClass() {
      return this.order.deliveredDate ? '' : 'justify-center';
    },
    orderCheckIn() {
      return this.order.sender.id === eXo.env.portal.userName ;
    },
    orderAmount() {
      return (this.order && this.order.amount && toFixed(this.order.amount)) || 0;
    },
    transactionLink() {
      if (this.order.transactionHash && this.order) {
        if ((this.order.sender && this.order.sender.type === 'user' && this.order.sender.id === eXo.env.portal.userName) || (this.order.receiver && this.order.receiver.type === 'user' && this.order.receiver.id === eXo.env.portal.userName)) {
          return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/wallet?hash=${this.order.transactionHash}&principal=true`;
        } else if (this.order.receiver && this.order.receiver.type === 'space') {
          return `${eXo.env.portal.context}/g/:spaces:${this.order.receiver.spaceURLId}/${this.order.receiver.id}/SpaceWallet?hash=${this.order.transactionHash}&principal=true`;
        }
      }
      return null;
    },
    refundTransactionLink() {
      if (this.order && this.order.refundTransactionHash) {
        if ((this.order.receiver && this.order.receiver.type === 'user' && this.order.receiver.id === eXo.env.portal.userName) || (this.order.sender && this.order.sender.type === 'user' && this.order.sender.id === eXo.env.portal.userName)) {
          return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/wallet?hash=${this.order.refundTransactionHash}&principal=true`;
        } else if (this.order.receiver && this.order.receiver.type === 'space') {
          return `${eXo.env.portal.context}/g/:spaces:${this.order.receiver.spaceURLId}/${this.order.receiver.id}/SpaceWallet?hash=${this.order.refundTransactionHash}&principal=true`;
        }
      }
      return null;
    },
    orderLink() {
      return (this.order && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore?productId=${this.order.productId}&orderId=${this.order.id}`) || '#';
    },
    productLink() {
      return (this.order && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore?productId=${this.order.productId}`) || '#';
    },
    productTitle() {
      return (this.orderProduct && this.orderProduct.title) || (this.order && this.order.productTitle) || '';
    },
    userData() {
      this.getProductById();
      return (this.orderProduct && this.orderProduct.userData) || {};
    },
    createdDateLabel() {
      return formatDateTime(this.order.createdDate);
    },
    deliveredDateLabel() {
      return formatDateTime(this.order.deliveredDate);
    },
    refundedDateLabel() {
      return formatDateTime(this.order.refundedDate);
    },
    deliveredPercentageColor() {
      return (!this.order.deliveredQuantity && 'grey') || (this.order.refundedQuantity && 'orange') || 'teal';
    },
    deliveredPercentage() {
      return parseInt((this.order.deliveredQuantity * 100) / this.order.quantity);
    },
    statusLowerCase() {
      return this.order.status && this.order.status.toLowerCase();
    },
    isError() {
      return this.statusLowerCase === 'error';
    },
    isPaid() {
      return this.statusLowerCase === 'paid';
    },
    isDelivered() {
      return this.statusLowerCase === 'delivered';
    },
    isRefunded() {
      return this.statusLowerCase === 'refunded';
    },
    isCanceled() {
      return this.statusLowerCase === 'canceled';
    },
    isOrdered() {
      return this.statusLowerCase === 'ordered';
    },
    isPartial() {
      return this.statusLowerCase === 'partial';
    },
    isBuyer() {
      return this.order && this.order.sender && this.order.sender.id === eXo.env.portal.userName;
    },
    canDeliverOrder() {
      return this.order.remainingQuantityToProcess && this.order.remainingQuantityToProcess > 0 && (this.isPaid || this.isPartial);
    },
    canRefundOrder() {
      return this.refunding || (this.order.remainingQuantityToProcess && (this.order.refundedQuantity === 0 || this.order.refundTransactionStatus === 'FAILED') && this.order.remainingQuantityToProcess > 0 && (this.isPaid || this.isPartial) && (this.order.receiver.type === 'space' || this.order.receiver.id === eXo.env.portal.userName));
    },
  },
  methods: {
    openProductDetail(event) {
      event.stopPropagation();
      event.preventDefault();

      this.$emit('display-product', this.order.productId);
    },
    refunded() {
      // Nothing to do, the update will be made by Websocket
    },
    refundDialogClosed() {
      // We have to re-init wallet settings to the current user instead of wallet of Order receiver
      this.$emit('init-wallet');
      this.refunding = false;
    },
    changeStatus(newStatus) {
      return saveOrderStatus({
        id: this.order.id,
        productId: this.order.productId,
        status: newStatus || this.order.status,
      }, 'STATUS')
        .then(order => {
          this.$emit('changed', order);
          this.$forceUpdate();
        })
        .catch(e => {
          console.error('Error saving status', e);
          this.$emit('error', e && e.message ? e.message : String(e));
        });
    },
    getProductById() {
      getProduct(this.order.productId).then((product) => {
        if (product){
          this.orderProduct = product;
        }
      }).catch(e => {
        console.error('Error getting product', e);
        this.$emit('error', e && e.message ? e.message : String(e));
      });
    },
  }
};
</script>