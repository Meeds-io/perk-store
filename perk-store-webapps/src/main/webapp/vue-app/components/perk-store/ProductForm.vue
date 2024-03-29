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
  <exo-drawer
    ref="productFormDrawer"
    allow-expand
    right
    @closed="onCloseDrawer">
    <template slot="title">
      {{ product && product.id ? $t('exoplatform.perkstore.button.editProduct', {0: product.title}) : $t('exoplatform.perkstore.label.addNewProduct') }}
    </template>
    <template slot="content">
      <v-form
        v-if="this.$refs.productFormDrawer && this.$refs.productFormDrawer.drawer"
        ref="form"
        v-model="valid"
        class="productFormParent">
        <v-container grid-list-xl class="white">
          <v-layout
            :class="this.$refs.productFormDrawer && this.$refs.productFormDrawer.expand ? '' : 'drawerProductExpand'"
            wrap
            justify-space-between>
            <v-flex
              :class="this.$refs.productFormDrawer && this.$refs.productFormDrawer.expand ? 'md4' : ''"
              xs12>
              <v-text-field
                v-model="product.title"
                :rules="requiredRule"
                :maxlength="200"
                :label="`${$t('exoplatform.perkstore.label.productTitle')} *`"
                :placeholder="$t('exoplatform.perkstore.label.productTitlePlaceholder')"
                name="ProductTitle"
                required
                autofocus
                counter />

              <v-textarea
                v-model="product.description"
                :maxlength="maxTextAreaSize"
                :label="$t('exoplatform.perkstore.label.productDescription')"
                :placeholder="$t('exoplatform.perkstore.label.productDescriptionPlaceholder')"
                name="ProductDescription"
                rows="5"
                flat
                counter />

              <perk-store-auto-complete
                ref="receiverMarchandAutocomplete"
                :rules="requiredRule"
                :input-label="`${$t('exoplatform.perkstore.label.marchandWallet')} *`"
                :input-placeholder="$t('exoplatform.perkstore.label.marchandWalletPlaceholder')"
                :no-data-label="$t('exoplatform.perkstore.label.marchandWalletSearchPlaceholder')"
                big-field
                required
                @item-selected="selectRecipient"
                @clear-selection="selectRecipient()" />

              <perk-store-auto-complete
                ref="productMarchandsAutocomplete"
                :rules="rules"
                :input-label="`${$t('exoplatform.perkstore.label.productEditors')} *`"
                :input-placeholder="$t('exoplatform.perkstore.label.productEditorsPlaceholder')"
                :no-data-label="$t('exoplatform.perkstore.label.productEditorsSearchPlaceholder')"
                multiple
                only-users
                no-address
                big-field
                required
                @item-selected="selectEditor"
                @clear-selection="selectEditor()" />

              <perk-store-auto-complete
                ref="productAccessPermissionAutocomplete"
                :input-label="$t('exoplatform.perkstore.label.productAllowedBuyers')"
                :input-placeholder="$t('exoplatform.perkstore.label.productAllowedBuyersPlaceholder')"
                :no-data-label="$t('exoplatform.perkstore.label.productAllowedBuyersSearchPlaceholder')"
                multiple
                no-address
                big-field
                @item-selected="selectAccessPermission"
                @clear-selection="selectAccessPermission()" />
            </v-flex>

            <v-flex
              :class="this.$refs.productFormDrawer && this.$refs.productFormDrawer.expand ? 'md6' : ''"
              xs12>
              <v-text-field
                v-model.number="product.price"
                :rules="requiredNumberRule"
                :label="`${$t('exoplatform.perkstore.label.price')} *`"
                :placeholder="$t('exoplatform.perkstore.label.pricePlaceholder')"
                name="ProductPrice"
                required />

              <perk-store-upload-input
                :max-files="3"
                :max-uploads-size-in-mb="5"
                :files="product.imageFiles" />

              <v-checkbox
                v-model="product.enabled"
                :label="$t('exoplatform.perkstore.label.enabledProduct')" />

              <v-checkbox
                v-model="limitedSupply"
                :label="$t('exoplatform.perkstore.label.limitedSupply')" />

              <v-layout
                v-if="limitedSupply"
                class="sub-text"
                row>
                <v-flex xs5>
                  <label>{{ `${$t('exoplatform.perkstore.label.totalSupply')} *` }}</label>
                </v-flex>
                <v-flex xs7>
                  <v-text-field
                    v-model.number="product.totalSupply"
                    name="ProductTotalSupply"
                    :placeholder="$t('exoplatform.perkstore.label.totalSupplyPlaceholder')"
                    :rules="requiredRule"
                    single-line
                    required />
                </v-flex>
              </v-layout>

              <v-checkbox
                v-model="limitedOrdersPerUser"
                :label="$t('exoplatform.perkstore.label.limitedOders')" />

              <v-layout
                v-if="limitedOrdersPerUser"
                class="sub-text"
                row>
                <v-flex xs5>
                  <label>{{ `${limitedOrdersPerUserLabel} *` }}</label>
                </v-flex>
                <v-flex xs7>
                  <v-text-field
                    v-model.number="product.maxOrdersPerUser"
                    :rules="requiredIntegerRule"
                    :placeholder="$t('exoplatform.perkstore.label.maxOrdersPerUserPlaceholder')"
                    name="ProductMaxOrdersPerUser"
                    single-line
                    required />
                </v-flex>

                <v-flex xs5>
                  <label>{{ $t('exoplatform.perkstore.label.userOrdersLimitationPeriodicity') }}</label>
                </v-flex>
                <v-flex xs7>
                  <v-select
                    v-if="limitedOrdersPerUser"
                    v-model="product.orderPeriodicity"
                    :items="periods"
                    :disabled="!product.maxOrdersPerUser || !limitedOrdersPerUser"
                    :placeholder="$t('exoplatform.perkstore.label.userOrdersLimitationPeriodicityPlaceholder')"
                    item-text="text"
                    item-value="value"
                    hide-no-data
                    hide-selected
                    small-chips />
                </v-flex>
              </v-layout>
            </v-flex>
          </v-layout>
        </v-container>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <button
          :disabled="enableSaveProduct"
          class="ignore-vuetify-classes btn btn-primary me-1"
          @click="saveProduct">
          {{ $t('exoplatform.perkstore.button.save') }}
        </button>
        <button
          class="ignore-vuetify-classes btn"
          @click="close">
          {{ $t('exoplatform.perkstore.button.cancel') }}
        </button>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
import {saveProduct} from '../../js/PerkStoreProduct.js';

export default {
  props: {
    product: {
      type: Object,
      default: function() {
        return {};
      },
    },
  },
  data() {
    return {
      valid: true,
      productEditionId: null,
      limitedOrdersPerUser: false,
      limitedSupply: false,
      rules: [v => !!(v && v.length) || this.$t('exoplatform.perkstore.warning.requiredField')],
      requiredRule: [(v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField')],
      requiredIntegerRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => !v || this.isPositiveNumber(v, true) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
      ],
      requiredNumberRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => this.isPositiveNumber(v) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
        (v) => this.isValidDecimals(v) || this.$t('exoplatform.perkstore.warning.invalidNumberOfDecimals', {0: 3}),
      ],
      maxTextAreaSize: 2000,
    };
  },
  computed: {
    enableSaveProduct() {
      return !this.valid || this.product?.imageFiles && this.product?.imageFiles.length === 0;
    },
    orderPeriodicity() {
      return (this.product && this.product.orderPeriodicity !== 'none' && this.product.orderPeriodicity) || '';
    },
    orderPeriodicitySuffix() {
      return (this.orderPeriodicity && `Per${this.capitalize(this.orderPeriodicity)}`) || '';
    },
    limitedOrdersPerUserLabel() {
      return this.$t(`exoplatform.perkstore.label.maxOrdersPerUser${this.orderPeriodicitySuffix}`);
    },
    periods() {
      return [
        {
          text: this.$t('exoplatform.perkstore.label.noPeriodicity'),
          value: 'none'
        },
        {
          text: this.$t('exoplatform.perkstore.label.week'),
          value: 'Week'
        },
        {
          text: this.$t('exoplatform.perkstore.label.month'),
          value: 'Month'
        },
        {
          text: this.$t('exoplatform.perkstore.label.quarter'),
          value: 'Quarter'
        },
        {
          text: this.$t('exoplatform.perkstore.label.semester'),
          value: 'Semester'
        },
        {
          text: this.$t('exoplatform.perkstore.label.year'),
          value: 'Year'
        }
      ];
    }
  },
  watch: {
    limitedSupply() {
      this.product.unlimited = !this.limitedSupply;
    },
    limitedOrdersPerUser() {
      this.product.orderPeriodicity = this.product.orderPeriodicity || 'none';
    },
  },
  methods: {
    onCloseDrawer() {
      if (Object.keys(this.product).length !== 0) {
        this.$emit('closeDrawer');
      }
    },
    close(){
      this.$refs.productFormDrawer.close();
    },
    async open() {
      await this.$refs.productFormDrawer.open();
      this.init();
    },
    init() {
      this.product = this.product || {};

      if (this.product.receiverMarchand) {
        this.$refs.receiverMarchandAutocomplete.selectItems(this.product.receiverMarchand);
      }

      if (!this.product.marchands && !this.product.creator) {
        this.product.marchands = [{
          type: 'user',
          id: eXo.env.portal.userName,
          disabled: true,
        }];
      }

      if (this.product.marchands) {
        this.$refs.productMarchandsAutocomplete.selectItems(this.product.marchands);
      }
      if (!this.product.receiverMarchand && this.product.marchands) {
        this.$refs.receiverMarchandAutocomplete.selectItems(this.product.marchands);
      }
      if (this.product.accessPermissions) {
        this.$refs.productAccessPermissionAutocomplete.selectItems(this.product.accessPermissions);
      }

      this.productEditionId = `FileMultiUploadComponent${parseInt(Math.random() * this.MAX_RANDOM_NUMBER)}`;
      this.limitedOrdersPerUser = this.product && this.product.maxOrdersPerUser && this.product.maxOrdersPerUser > 0;
      this.product.orderPeriodicity = (this.product && this.product.orderPeriodicity) || 'none';
      this.limitedSupply = this.product && this.product.id && !this.product.unlimited;
      this.product.enabled = !this.product.id || this.product.enabled;
    },
    selectRecipient(identity) {
      this.product.receiverMarchand = identity;
    },
    selectEditor(identity) {
      if (!this.product.marchands) {
        this.product.marchands = [];
      }
      if (identity) {
        this.product.marchands.push(identity);
      } else if (this.product.marchands.length) {
        this.product.marchands = [];
      }
    },
    selectAccessPermission(identity) {
      if (!this.product.accessPermissions) {
        this.product.accessPermissions = [];
      }
      if (identity) {
        this.product.accessPermissions.push(identity);
      } else if (this.product.accessPermissions.length) {
        this.product.accessPermissions = [];
      }
    },
    isPositiveNumber(value, isInt) {
      return value && !isNaN(value) && value > 0 && Number.isFinite(value) && (!isInt || Number.isSafeInteger(value));
    },
    capitalize(label) {
      if (!label) {
        return '';
      }
      return label.charAt(0).toUpperCase() + label.slice(1).toLowerCase();
    },
    isValidDecimals(value) {
      if (String(value).indexOf('.') >= 0) {
        return String(value).split('.')[1].length <= 3;
      } else {
        return true;
      }
    },
    saveProduct(event) {
      event.preventDefault();
      event.stopPropagation();

      if (!this.$refs.form.validate()) {
        return false;
      }

      this.$emit('error', null);

      if (this.product && this.product.imageFiles) {
        this.product.imageFiles.forEach(imageFile => {
          delete imageFile.src;
          delete imageFile.data;
          delete imageFile.file;
          delete imageFile.progress;
          delete imageFile.finished;
        });
      }

      this.product.unlimited = !this.limitedSupply;
      if (this.product.unlimited) {
        this.product.totalSupply = 0;
      }

      if (!this.limitedOrdersPerUser) {
        this.product.maxOrdersPerUser = 0;
        this.product.orderPeriodicity = 'none';
      }
      return saveProduct(this.product)
        .then(() =>{
          this.$emit('refreshProduct');
          this.close();
          this.product = {};
        })
        .catch(e => {
          console.error('Error saving product', e);
          this.$emit('error', e && e.message ? e.message : String(e));
          this.close();
        });
    }
  }
};
</script>