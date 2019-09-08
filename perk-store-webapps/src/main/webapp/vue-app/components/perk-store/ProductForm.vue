<template>
  <v-form ref="form" class="productFormParent">
    <v-container grid-list-xl class="white">
      <v-layout
        wrap
        justify-space-between>
        <v-flex
          xs12
          md4>
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

          <auto-complete
            ref="receiverMarchandAutocomplete"
            :rules="requiredRule"
            :input-label="`${$t('exoplatform.perkstore.label.marchandWallet')} *`"
            :input-placeholder="$t('exoplatform.perkstore.label.marchandWalletPlaceholder')"
            :no-data-label="$t('exoplatform.perkstore.label.marchandWalletSearchPlaceholder')"
            big-field
            required
            @item-selected="selectRecipient"
            @clear-selection="selectRecipient()" />

          <auto-complete
            ref="productMarchandsAutocomplete"
            :rules="requiredRule"
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

          <auto-complete
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
          xs12
          md6>

          <v-text-field
            v-model.number="product.price"
            :rules="requiredNumberRule"
            :label="`${$t('exoplatform.perkstore.label.price')} *`"
            :placeholder="$t('exoplatform.perkstore.label.pricePlaceholder')"
            name="ProductPrice"
            required />

          <upload-input
            :max-files="5"
            :max-uploads-size-in-mb="5"
            :files="product.imageFiles" />

          <v-checkbox
            v-model="product.enabled"
            :label="$t('exoplatform.perkstore.label.enabledProduct')" />

          <v-checkbox
            v-model="limitedSupply"
            :label="$t('exoplatform.perkstore.label.limitedSupply')" />

          <v-layout class="sub-text" row v-if="limitedSupply">
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

          <v-layout class="sub-text" row v-if="limitedOrdersPerUser">
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
      <v-card-actions>
        <v-spacer />
        <button
          class="ignore-vuetify-classes btn btn-primary mr-1"
          @click="saveProduct">
          {{ $t('exoplatform.perkstore.button.save') }}
        </button>
        <button
          class="ignore-vuetify-classes btn"
          @click="$event.preventDefault();$event.stopPropagation();$emit('close')">
          {{ $t('exoplatform.perkstore.button.cancel') }}
        </button>
        <v-spacer />
      </v-card-actions>
    </v-container>
  </v-form>
</template>

<script>
import UploadInput from './FileMultiUploadInput.vue';
import AutoComplete from '../AutoComplete.vue';

import {saveProduct} from '../../js/PerkStoreProduct.js';

export default {
  components: {
    UploadInput,
    AutoComplete,
  },
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
      productEditionId: null,
      limitedOrdersPerUser: false,
      limitedSupply: false,
      requiredRule: [(v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField')],
      requiredIntegerRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => !v || this.isPositiveNumber(v, true) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
      ],
      requiredNumberRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => !v || this.isPositiveNumber(v) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
      ],
      maxTextAreaSize: 2000,
    };
  },
  computed: {
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
          value: 'WEEK'
        },
        {
          text: this.$t('exoplatform.perkstore.label.month'),
          value: 'MONTH'
        },
        {
          text: this.$t('exoplatform.perkstore.label.quarter'),
          value: 'QUARTER'
        },
        {
          text: this.$t('exoplatform.perkstore.label.semester'),
          value: 'SEMESTER'
        },
        {
          text: this.$t('exoplatform.perkstore.label.year'),
          value: 'YEAR'
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
    init() {
      this.product = this.product || {};

      if(this.product.receiverMarchand) {
        this.$refs.receiverMarchandAutocomplete.selectItems(this.product.receiverMarchand);
      }

      if(!this.product.marchands && !this.product.creator) {
        this.product.marchands = [{
          type: 'user',
          id: eXo.env.portal.userName,
          disabled: true,
        }];
      }

      if(this.product.marchands) {
        this.$refs.productMarchandsAutocomplete.selectItems(this.product.marchands);
      }
      if(this.product.accessPermissions) {
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
      if(!this.product.marchands) {
        this.product.marchands = [];
      }
      if(identity) {
        this.product.marchands.push(identity);
      } else if(this.product.marchands.length) {
        this.product.marchands = [];
      }
    },
    selectAccessPermission(identity) {
      if(!this.product.accessPermissions) {
        this.product.accessPermissions = [];
      }
      if(identity) {
        this.product.accessPermissions.push(identity);
      } else if(this.product.accessPermissions.length) {
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
    saveProduct(event) {
      event.preventDefault();
      event.stopPropagation();

      if(!this.$refs.form.validate()) {
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
        .then(() => {
          this.$emit('added', this.product);
          this.$emit('close');
        })
        .catch(e => {
          console.debug("Error saving product", e);
          this.$emit('error', e && e.message ? e.message : String(e));
        });
    }
  }
}
</script>