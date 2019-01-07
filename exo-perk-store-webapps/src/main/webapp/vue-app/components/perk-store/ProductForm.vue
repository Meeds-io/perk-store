<template>
  <v-container grid-list-xl>
    <v-layout
      wrap
      justify-space-between>
      <v-flex
        xs12
        md4>
        <v-text-field
          v-model="product.title"
          label="ProductName"
          placeholder="input a product title" />

        <v-textarea
          v-model="product.description"
          name="ProductDescription"
          label="Product description"
          placeholder="Input a product description"
          class="mt-4"
          rows="5"
          flat />

        <auto-complete
          ref="receiverMarchandAutocomplete"
          input-label="Receiver account"
          input-placeholder="Select the marchand receiver"
          no-data-label="Search for a user or a space"
          big-field
          @item-selected="selectRecipient" />
      </v-flex>

      <v-flex
        xs12
        md6>
        <v-checkbox
          v-model="product.enabled"
          label="Enabled product" />

        <v-checkbox
          v-model="product.illimited"
          label="Illimited supply" />

        <v-text-field
          v-if="!product.illimited"
          v-model.number="product.totalSupply"
          name="ProductTotalSupply"
          label="Total supply"
          placeholder="input the product total supply" />

        <v-text-field
          v-model.number="product.price"
          name="ProductPrice"
          label="Price"
          placeholder="input the product price" />

        <v-combobox
          v-model="product.orderPeriodicity"
          :items="periods"
          :return-object="false"
          label="User order limitation periodicity"
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
          name="ProductMaxOrdersPerUser"
          label="Maximum orders per user"
          placeholder="You can limit the number of user orders" />
      </v-flex>
    </v-layout>
    <v-card-actions>
      <v-spacer />
      <button
        class="btn btn-primary mr-1"
        @click="saveProduct()">
        Save
      </button>
      <v-spacer />
    </v-card-actions>
  </v-container>
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
    orderPeriodicityLabel() {
      let label = null;
      this.periods.forEach(period => {
        if(this.product.orderPeriodicity === period.value) {
          label = period.text;
        }
      });
      return label;
    }
  },
  watch: {
    product() {
      // Select item
    }
  },
  methods: {
    selectRecipient(identity) {
      this.product.receiverMarchand = identity;
    },
    saveProduct() {
      return saveProduct(this.product)
        .then(() => this.$emit('added', this.product));
    }
  }
}
</script>