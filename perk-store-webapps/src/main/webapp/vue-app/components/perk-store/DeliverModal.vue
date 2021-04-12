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
  <v-dialog
    v-model="dialog"
    content-class="uiPopup with-overflow"
    class="DeliveredProductModal"
    width="300px"
    max-width="100vw"
    eager>
    <v-card class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="dialog = false"></a>
        <span class="ignore-vuetify-classes PopupTitle popupTitle text-truncate">
          {{ $t('exoplatform.perkstore.title.deliverOrderModal', {0: order && order.id}) }}
        </span>
      </div>
      <v-list flat class="pt-0 pb-0">
        <v-list-item
          v-if="error && !loading"
          class="mb-3 mt-2"
          dense>
          <v-list-item-content>
            <div class="alert alert-error v-content">
              <i class="uiIconError"></i>
              {{ error }}
            </div>
          </v-list-item-content>
        </v-list-item>
        <v-list-item>
          <v-list-item-content>{{ $t('exoplatform.perkstore.label.buyer') }}:</v-list-item-content>
          <v-list-item-content class="align-end">
            <div class="text-truncate orderDetailText">
              <profile-link
                :id="order.sender.id"
                :space-id="order.sender.spaceId"
                :url-id="order.sender.spaceURLId"
                :type="order.sender.type"
                :display-name="order.sender.displayName"
                display-avatar />
            </div>
          </v-list-item-content>
        </v-list-item>
        <template v-if="order && order.remainingQuantityToProcess">
          <label class="align-center">
            {{ $t('exoplatform.perkstore.label.quantityWithMax', {0: order.remainingQuantityToProcess}) }}
          </label>
          <v-list-item dense>
            <v-list-item-content class="align-center">
              <v-text-field
                v-model.number="quantity"
                :disabled="loading"
                :rules="requiredNumberRule"
                append-icon="fa-plus"
                prepend-inner-icon="fa-minus"
                class="text-center orderDeliverQuantity"
                name="quantity"
                required
                @click:prepend-inner="decrementQuantity"
                @click:append="incrementQuantity" />
            </v-list-item-content>
          </v-list-item>
        </template>
      </v-list>
      <v-card-actions>
        <v-spacer />
        <button
          v-if="order && order.remainingQuantityToProcess"
          :disabled="disableDeliverButton"
          class="ignore-vuetify-classes btn btn-primary me-1"
          @click="deliverProduct">
          {{ $t('exoplatform.perkstore.button.save') }}
        </button>
        <button
          class="ignore-vuetify-classes btn"
          :disabled="loading"
          @click="dialog = false">
          {{ $t('exoplatform.perkstore.button.close') }}
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import ProfileLink from '../ProfileLink.vue';

import {saveOrderStatus} from '../../js/PerkStoreProductOrder.js';

export default {
  components: {
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
    visibleButton: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
  },
  data() {
    return {
      dialog: false,
      loading: false,
      quantity: null,
      error: null,
      requiredNumberRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => !v || this.isPositiveNumber(v) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
        (v) => !v || this.quantity <= this.order.remainingQuantityToProcess || this.$t('exoplatform.perkstore.warning.maxQuantityReached', {0: this.remainingQuantityToProcess}),
      ],
    };
  },
  computed: {
    totalDeliveredQuantity() {
      return Number(this.quantity) + Number(this.order.deliveredQuantity);
    },
    disableDeliverButton() {
      return this.loading || !this.order || !this.isPositiveNumber(this.quantity) || this.quantity > this.order.remainingQuantityToProcess;
    },
  },
  watch: {
    error() {
      if(this.error) {
        this.loading = false;
      }
    },
    order() {
      this.validateOrder();
    },
    quantity() {
      this.validateOrder();
    },
    dialog() {
      if (this.dialog) {
        this.quantity = 1;
        this.warning = null;
        this.error = null;
        this.loading = false;
      } else {
        this.$emit('closed');
      }
    },
  },
  methods: {
    openNoSelection() {
      this.dialog = true;
    },
    open(productId, orderId, senderId) {
      if (!productId || !orderId || !senderId) {
        return;
      }
      this.dialog = true;
      this.$nextTick(() => this.validateOrder(productId, orderId, senderId));
    },
    close() {
      this.dialog = false;
    },
    incrementQuantity() {
      this.quantity = this.quantity && (this.quantity + 1) > 0 ? this.quantity + 1 : 1;
      this.quantity = Math.floor(this.quantity);
    },
    decrementQuantity() {
      this.quantity = this.quantity && (this.quantity - 1) > 0 ? this.quantity - 1 : 0;
      this.quantity = Math.floor(this.quantity);
    },
    isPositiveNumber(value) {
      return value && !isNaN(value) && value > 0 && Number.isFinite(value);
    },
    validateOrder(productId, orderId, senderId) {
      this.error = null;

      if (!this.order) {
        this.error = this.$t('exoplatform.perkstore.warning.noSelectedOrder');
        return;
      }

      if (!this.order.remainingQuantityToProcess) {
        this.error = this.$t('exoplatform.perkstore.warning.orderIsCompletelyDelivered');
        return;
      }

      if (orderId && this.order.id && String(orderId) !== String(this.order.id)) {
        this.error = this.$t('exoplatform.perkstore.warning.invalidProductOrder');
        return;
      }

      if (productId && this.order.productId && String(productId) !== String(this.order.productId)) {
        this.error = this.$t('exoplatform.perkstore.warning.invalidProductOrder');
        return;
      }

      if (senderId && (!this.order.sender || senderId !== this.order.sender.id)) {
        this.error = this.$t('exoplatform.perkstore.warning.invalidOrderBuyer');
        return;
      }

      if (!this.isPositiveNumber(this.quantity) || (!this.product.allowFraction && !Number.isSafeInteger(this.quantity))) {
        this.error = this.$t('exoplatform.perkstore.warning.invalidQuantity');
        return;
      }

      if (this.quantity > this.order.remainingQuantityToProcess) {
        this.error = this.$t('exoplatform.perkstore.warning.invalidDeliverQuantity', {0: this.order.remainingQuantityToProcess});
        return;
      }

      if (!this.order.sender) {
        this.error = this.$t('exoplatform.perkstore.warning.noOrderBuyer');
        return;
      }
    },
    deliverProduct() {
      this.validateOrder();
      if (this.error) {
        return;
      }

      this.loading = true;
      return saveOrderStatus({
        id: this.order.id,
        productId: this.order.productId,
        deliveredQuantity: Number(this.totalDeliveredQuantity),
      }, 'DELIVERED_QUANTITY')
        .then((order) => {
          this.dialog = false;
          this.loading = false;
          this.$emit('delivered', order);
        })
        .catch(e => {
          console.debug("Error saving delivered quantity", e);
          this.loading = false;
          this.error = e && e.message ? e.message : String(e);
        });
    },
  },
};
</script>
