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
    <v-card slot-scope="{ hover }" :class="`elevation-${hover ? 9 : 3}`">
      <v-card-title v-if="order.sender" class="pt-1 pb-1 subtitle-1">
        <h4>
          <a
            :href="orderLink"
            rel="nofollow"
            target="_blank">
            <strong>#{{ order.id }}</strong>
          </a>
        </h4>
        <v-spacer />
        <template v-if="userData && userData.canEdit">
          <select
            v-model="order.status"
            class="small my-auto mr-2 ignore-vuetify-classes"
            @change="changeStatus()">
            <option
              v-for="option in statusList"
              :key="option"
              :value="option">
              {{ $t(`exoplatform.perkstore.label.status.${option.toLowerCase()}`) }}
            </option>
          </select>
          <div
            v-if="order.remainingQuantityToProcess"
            :title="$t('exoplatform.perkstore.label.remainingQuatityToProcess', {0: order.remainingQuantityToProcess})"
            class="orderQuantityBadgeParent">
            <div class="orderQuantityBadge">
              {{ order.remainingQuantityToProcess }}
            </div>
          </div>
        </template>
        <span v-else>{{ $t(`exoplatform.perkstore.label.status.${order.status.toLowerCase()}`) }}</span>
      </v-card-title>

      <v-divider />

      <v-list dense>
        <v-list-item>
          <v-list-item-content class="align-start">{{ $t('exoplatform.perkstore.label.buyer') }}:</v-list-item-content>
          <v-list-item-content class="align-end">
            <div class="text-truncate orderDetailText">
              <profile-link
                v-if="order.sender"
                :id="order.sender.id"
                :space-id="order.sender.spaceId"
                :url-id="order.sender.spaceURLId"
                :type="order.sender.type"
                :display-name="order.sender.displayName"
                display-avatar />
            </div>
          </v-list-item-content>
        </v-list-item>
        <v-list-item>
          <v-list-item-content class="align-start">{{ $t('exoplatform.perkstore.label.date') }}:</v-list-item-content>
          <v-list-item-content class="align-end">
            <div :title="createdDateLabel" class="orderCreatedDate text-truncate">
              {{ createdDateLabel }}
            </div>
          </v-list-item-content>
        </v-list-item>
        <v-list-item>
          <v-list-item-content class="align-start">{{ $t('exoplatform.perkstore.label.items') }}:</v-list-item-content>
          <v-list-item-content class="align-end">
            <div class="text-truncate orderDetailText">
              {{ order.quantity }} x 
              <a
                :href="productLink"
                :title="productTitle"
                @click="openProductDetail">
                {{ productTitle }}
              </a>
            </div>
          </v-list-item-content>
        </v-list-item>
        <v-list-item>
          <v-list-item-content class="align-start">
            <span>{{ $t('exoplatform.perkstore.label.payment') }}:</span>
          </v-list-item-content>
          <v-list-item-content class="align-end">
            <div class="no-wrap text-truncate orderDetailText">
              <v-icon
                v-if="order.transactionStatus === 'SUCCESS'"
                :title="$t('exoplatform.perkstore.label.transactionSuceess')"
                class="green--text"
                size="16">
                fa-check-circle
              </v-icon>
              <v-icon
                v-else-if="order.transactionStatus === 'FAILED'"
                :title="$t('exoplatform.perkstore.label.transactionFailed')"
                class="red--text"
                size="16">
                fa-exclamation-circle
              </v-icon>
              <v-icon
                v-else-if="order.transactionStatus === 'PENDING'"
                :title="$t('exoplatform.perkstore.label.transactionPending')"
                class="orange--text"
                size="16">
                far fa-clock
              </v-icon>
              <a
                v-if="transactionLink"
                :href="transactionLink"
                :title="$t('exoplatform.perkstore.label.openInWallet')"
                rel="nofollow"
                target="_blank">
                {{ orderAmount }} {{ symbol }}
              </a>
              <template v-else>
                {{ orderAmount }} {{ symbol }}
              </template>
              {{ $t('exoplatform.perkstore.label.sentTo') }}
              <profile-link
                v-if="order.receiver"
                :id="order.receiver.id"
                :space-id="order.receiver.spaceId"
                :url-id="order.receiver.spaceURLId"
                :type="order.receiver.type"
                :display-name="order.receiver.displayName" />
            </div>
          </v-list-item-content>
        </v-list-item>
      </v-list>
  
      <v-divider />
  
      <v-list dense class="orderProcessingDetails">
        <v-list-item class="orderProcessingContent">
          <v-list-item-content v-if="!userData || !userData.canEdit || (!isOrdered && !canDeliverOrder && !canRefundOrder)" class="align-start">{{ $t('exoplatform.perkstore.label.processing') }}:</v-list-item-content>
          <v-list-item-content class="align-end orderProcessingActions no-wrap">
            <div>
              <div v-if="!refunding && (!order.remainingQuantityToProcess || isError)">
                <v-icon class="green--text mr-1" size="16px">fa-check-circle</v-icon>{{ $t('exoplatform.perkstore.label.processingDone') }}
              </div>
              <template v-else-if="userData && userData.canEdit">
                <button
                  v-if="isOrdered"
                  class="ignore-vuetify-classes btn orderProcessingBtn"
                  @click="changeStatus('CANCELED')">
                  {{ $t('exoplatform.perkstore.button.cancel') }}
                </button>
                <button
                  v-if="canDeliverOrder"
                  class="ignore-vuetify-classes btn btn-primary orderProcessingBtn ml-1"
                  @click="$refs.deliverModal.openNoSelection()">
                  {{ $t('exoplatform.perkstore.button.deliver') }}
                </button>
                <button
                  v-if="canRefundOrder"
                  class="ignore-vuetify-classes btn orderProcessingBtn ml-1"
                  @click="$refs.refundModal.open()">
                  {{ $t('exoplatform.perkstore.button.refund') }}
                </button>
                <deliver-modal
                  v-if="canDeliverOrder"
                  ref="deliverModal"
                  :product="product"
                  :order="order" />
                <refund-modal
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
                <v-icon class="orange--text mr-1" size="16px">far fa-clock</v-icon>PENDING
              </div>
            </div>
          </v-list-item-content>
        </v-list-item>
        <v-list-item class="mt-1">
          <v-list-item-content class="align-start orderDeliveredLabel">
            <div class="no-wrap">
              {{ $t('exoplatform.perkstore.label.deliveredQuantity') }}:
              <v-progress-circular
                :rotate="360"
                :size="40"
                :width="5"
                :value="deliveredPercentage"
                :color="deliveredPercentageColor"
                class="ml-2">
                <span class="no-wrap">
                  {{ order.deliveredQuantity }}/{{ order.quantity }}
                </span>
              </v-progress-circular>
            </div>
          </v-list-item-content>
          <v-list-item-content v-if="order.deliveredDate" class="align-end">
            <div :title="deliveredDateLabel" class="orderDeliveredDate text-truncate">
              {{ deliveredDateLabel }}
            </div>
          </v-list-item-content>
        </v-list-item>
        <v-list-item v-if="order.refundedAmount && order.refundTransactionHash" class="mt-1">
          <v-list-item-content class="align-start orderRefundedLabel">
            <div class="no-wrap">
              {{ $t('exoplatform.perkstore.label.refundedQuantity') }}:
              <v-icon
                v-if="order.refundTransactionStatus === 'SUCCESS'"
                :title="$t('exoplatform.perkstore.label.transactionSuceess')"
                class="green--text"
                size="16">
                fa-check-circle
              </v-icon>
              <v-icon
                v-if="order.refundTransactionStatus === 'FAILED'"
                :title="$t('exoplatform.perkstore.label.transactionFailed')"
                class="red--text"
                size="16">
                fa-exclamation-circle
              </v-icon>
              <v-icon
                v-if="order.refundTransactionStatus === 'PENDING'"
                :title="$t('exoplatform.perkstore.label.transactionPending')"
                class="orange--text"
                size="16">
                far fa-clock
              </v-icon>
              <a
                v-if="refundTransactionLink"
                :href="refundTransactionLink"
                :title="$t('exoplatform.perkstore.label.openInWallet')"
                rel="nofollow"
                target="_blank">
                {{ order.refundedAmount }} {{ symbol }}
              </a>
              <template v-else>
                {{ order.refundedAmount }} {{ symbol }}
              </template>
            </div>
          </v-list-item-content>
          <v-list-item-content v-if="order.refundedDate" class="align-end">
            <div :title="refundedDateLabel" class="orderRefundedDate text-truncate">
              {{ refundedDateLabel }}
            </div>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-card>
  </v-hover>
  <span v-else class="hidden"></span>
</template>

<script>
import RefundModal from './RefundModal.vue';
import DeliverModal from './DeliverModal.vue';
import ProfileLink from '../ProfileLink.vue';

import {toFixed, saveOrderStatus} from '../../js/PerkStoreProductOrder.js';
import {formatDateTime} from '../../js/PerkStoreSettings.js';

export default {
  components: {
    DeliverModal,
    RefundModal,
    ProfileLink,
  },
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
      return (this.product && this.product.title) || (this.order && this.order.productTitle) || '';
    },
    userData() {
      return (this.product && this.product.userData) || {};
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
  }
};
</script>