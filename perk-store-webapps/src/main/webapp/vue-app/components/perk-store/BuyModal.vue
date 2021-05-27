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
    class="buyProductModal"
    width="500px"
    max-width="100vw"
    persistent
    eager
    @keydown.esc="close">
    <v-card class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="close">
        </a>
        <span class="ignore-vuetify-classes PopupTitle popupTitle text-truncate">
          {{ $t('exoplatform.perkstore.title.buyProductModal', {0: product && product.title}) }}
        </span>
      </div>
      <perk-store-buy-form
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
      if (!this.loading) {
        this.dialog = false;
      }
    },
  },
};
</script>
