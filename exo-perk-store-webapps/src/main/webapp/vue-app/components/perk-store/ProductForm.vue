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
            name="ProductTitle"
            label="Product title"
            placeholder="input a product title"
            required />
  
          <v-text-field
            v-model="product.illustrationURL"
            name="ProductImage"
            label="Product image"
            placeholder="input the product image URL" />
  
          <v-textarea
            v-model="product.description"
            :rules="requiredRule"
            :maxlength="maxTextAreaSize"
            name="ProductDescription"
            label="Product description"
            placeholder="Input a product description"
            class="mt-4"
            rows="5"
            flat
            counter />
  
          <auto-complete
            ref="receiverMarchandAutocomplete"
            :rules="requiredRule"
            input-label="Funds receiver account"
            input-placeholder="Select the wallet of marchand"
            no-data-label="Search for a user or a space"
            big-field
            required
            @item-selected="selectRecipient"
            @clear-selection="selectRecipient()" />
  
          <auto-complete
            ref="productMarchandsAutocomplete"
            :rules="requiredRule"
            input-label="Product editors"
            input-placeholder="Select product editors"
            no-data-label="Search for a user"
            multiple
            only-users
            no-address
            big-field
            required
            @item-selected="selectEditor"
            @clear-selection="selectEditor()" />
  
          <auto-complete
            ref="productAccessPermissionAutocomplete"
            input-label="Product access permissions (optional)"
            input-placeholder="Select product access permissions"
            no-data-label="Search for a user or a space"
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
            label="Enabled product" />

          <v-checkbox
            v-model="product.unlimited"
            label="Unlimited supply" />

          <v-text-field
            v-if="!product.unlimited"
            v-model.number="product.totalSupply"
            :rules="requiredRule"
            name="ProductTotalSupply"
            label="Total supply"
            placeholder="input the product total supply"
            required />

          <v-text-field
            v-model.number="product.price"
            :rules="requiredRule"
            name="ProductPrice"
            label="Price"
            placeholder="input the product price"
            required />

          <v-checkbox
            v-model="product.allowFraction"
            label="Allow fractioned quantity"
            class="hidden" />

          <v-combobox
            v-model="product.orderPeriodicity"
            :items="periodsValues"
            :return-object="false"
            label="User orders limitation periodicity"
            placeholder="Periodicity used to limit user orders"
            hide-no-data
            hide-selected
            small-chips>
            <!-- Without slot-scope, the template isn't displayed -->
            <!-- eslint-disable-next-line vue/no-unused-vars -->
            <template slot="selection" slot-scope="data">
              {{ orderPeriodicityLabel }}
            </template>
          </v-combobox>

          <v-text-field
            v-model.number="product.maxOrdersPerUser"
            :label="maxOrdersPerUserLabel"
            name="ProductMaxOrdersPerUser"
            placeholder="You can limit the number of user orders" />
        </v-flex>
      </v-layout>
      <v-card-actions>
        <v-spacer />
        <button
          class="btn btn-primary mr-1"
          @click="saveProduct">
          Save
        </button>
        <button
          class="btn"
          @click="$event.preventDefault();$event.stopPropagation();$emit('close')">
          Cancel
        </button>
        <v-spacer />
      </v-card-actions>
    </v-container>
  </v-form>
</template>

<script>
import AutoComplete from '../AutoComplete.vue';

import {saveProduct} from '../../js/PerkStoreProduct.js';

export default {
  components: {
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
      orderPeriodicity: null,
      requiredRule: [(v) => !!v || 'Required field'],
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
    maxOrdersPerUserLabel() {
      return this.orderPeriodicityLabel ? `Max orders per user per ${this.orderPeriodicityLabel}` : 'Max orders per user'
    },
    orderPeriodicityLabel() {
      let label = null;
      if(this.product.orderPeriodicity) {
        const selectedValue = this.product.orderPeriodicity.toUpperCase();
        this.periods.forEach(period => {
          if(selectedValue === period.value) {
            label = period.text;
          }
        });
      }
      return label;
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
    saveProduct(event) {
      event.preventDefault();
      event.stopPropagation();

      if(!this.$refs.form.validate()) {
        return;
      }

      this.$emit('error', null);
      if(!this.product.title) {
        this.$emit('error', 'Empty product title');
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