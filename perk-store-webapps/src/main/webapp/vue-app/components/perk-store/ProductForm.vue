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
            :label="$t('exoplatform.perkstore.label.productTitle')"
            :placeholder="$t('exoplatform.perkstore.label.productTitlePlaceholder')"
            name="ProductTitle"
            required
            autofocus
            counter />

          <upload-input
            :max-files="5"
            :max-uploads-size-in-mb="5"
            :files="product.imageFiles" />

          <v-textarea
            v-model="product.description"
            :rules="requiredRule"
            :maxlength="maxTextAreaSize"
            :label="$t('exoplatform.perkstore.label.productDescription')"
            :placeholder="$t('exoplatform.perkstore.label.productDescriptionPlaceholder')"
            name="ProductDescription"
            class="mt-4"
            rows="5"
            flat
            counter />

          <auto-complete
            ref="receiverMarchandAutocomplete"
            :rules="requiredRule"
            :input-label="$t('exoplatform.perkstore.label.marchandWallet')"
            :input-placeholder="$t('exoplatform.perkstore.label.marchandWalletPlaceholder')"
            :no-data-label="$t('exoplatform.perkstore.label.marchandWalletSearchPlaceholder')"
            big-field
            required
            @item-selected="selectRecipient"
            @clear-selection="selectRecipient()" />

          <auto-complete
            ref="productMarchandsAutocomplete"
            :rules="requiredRule"
            :input-label="$t('exoplatform.perkstore.label.productEditors')"
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
          <v-checkbox
            v-model="product.enabled"
            :label="$t('exoplatform.perkstore.label.enabledProduct')" />

          <v-checkbox
            v-model="product.unlimited"
            :label="$t('exoplatform.perkstore.label.unlimitedSupply')" />

          <v-checkbox
            v-model="product.allowFraction"
            :label="$t('exoplatform.perkstore.label.fractionedQuantity')"
            class="hidden" />

          <v-text-field
            v-if="!product.unlimited"
            v-model.number="product.totalSupply"
            name="ProductTotalSupply"
            :label="$t('exoplatform.perkstore.label.totalSupply')"
            :placeholder="$t('exoplatform.perkstore.label.totalSupplyPlaceholder')" />

          <v-text-field
            v-model.number="product.price"
            :rules="requiredNumberRule"
            :label="$t('exoplatform.perkstore.label.price')"
            :placeholder="$t('exoplatform.perkstore.label.pricePlaceholder')"
            name="ProductPrice"
            required />

          <v-text-field
            v-model.number="product.maxOrdersPerUser"
            :rules="integerRule"
            :label="$t(`exoplatform.perkstore.label.maxOrdersPerUser${orderPeriodicitySuffix}`)"
            :placeholder="$t('exoplatform.perkstore.label.maxOrdersPerUserPlaceholder')"
            name="ProductMaxOrdersPerUser" />

          <v-combobox
            v-model="product.orderPeriodicity"
            :items="periodsValues"
            :return-object="false"
            :label="$t('exoplatform.perkstore.label.userOrdersLimitationPeriodicity')"
            :placeholder="$t('exoplatform.perkstore.label.userOrdersLimitationPeriodicityPlaceholder')"
            hide-no-data
            hide-selected
            small-chips>
            <!-- Without slot-scope, the template isn't displayed -->
            <!-- eslint-disable-next-line vue/no-unused-vars -->
            <template slot="selection" slot-scope="data">
              {{ product && product.orderPeriodicity && $t(`exoplatform.perkstore.label.${product.orderPeriodicity.toLowerCase()}`) || '' }}
            </template>
          </v-combobox>
        </v-flex>
      </v-layout>
      <v-card-actions>
        <v-spacer />
        <button
          class="btn btn-primary mr-1"
          @click="saveProduct">
          {{ $t('exoplatform.perkstore.button.save') }}
        </button>
        <button
          class="btn"
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
      requiredRule: [(v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField')],
      integerRule: [
        (v) => !v || this.isPositiveNumber(v, true) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
      ],
      requiredIntegerRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => !v || this.isPositiveNumber(v, true) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
      ],
      requiredNumberRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => !v || this.isPositiveNumber(v) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
      ],
      maxTextAreaSize: 2000,
      periodsValues: ['Week', 'Month', 'Quarter', 'Semester', 'Year'],
      periods: [
        {
          text: 'Week',
          value: 'WEEK'
        },
        {
          text: 'Month',
          value: 'MONTH'
        },
        {
          text: 'Quarter',
          value: 'QUARTER'
        },
        {
          text: 'Semester',
          value: 'SEMESTER'
        },
        {
          text: 'Year',
          value: 'YEAR'
        }
      ],
    };
  },
  computed: {
    orderPeriodicity() {
      return (this.product && this.product.orderPeriodicity) || '';
    },
    orderPeriodicitySuffix() {
      return (this.orderPeriodicity && `Per${this.orderPeriodicity}`) || '';
    },
  },
  watch: {
    product(value, oldValue) {
      if(value && value !== oldValue) {
        this.productEditionId = `FileMultiUploadComponent${parseInt(Math.random() * this.MAX_RANDOM_NUMBER)}`;
      }
    }
  },
  methods: {
    init() {
      if(this.product) {
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
      }
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