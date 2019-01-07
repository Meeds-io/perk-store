<template>
  <v-app 
    id="PerkStoreApp" 
    color="transaprent" 
    flat>
    <main>
      <v-layout justify-center>
        <v-flex xs12>
          <v-toolbar color="white">
            <v-toolbar-title>Perk store</v-toolbar-title>
            <v-spacer />
            <v-btn
              v-if="displayProductForm || displayProductOrders"
              id="perkStoreAppMenuCloseButton"
              icon
              flat
              title="Close"
              @click="closeDetails">
              <v-icon size="20px">
                close
              </v-icon>
            </v-btn>
            <template v-else>
              <v-text-field
                v-model="search"
                append-icon="search"
                label="Search in products"
                single-line
                solo
                hide-details
                class="searchProductsInput mr-3" />
              <v-btn
                id="perkStoreAppMenuRefreshButton"
                icon
                flat
                title="Refresh store"
                class="mr-0"
                @click="$emit('refresh')">
                <v-icon size="20px">
                  refresh
                </v-icon>
              </v-btn>
              <v-btn
                v-if="settings.canAddProduct"
                id="perkStoreAppMenuAddButton"
                icon
                flat
                title="Add product"
                class="mr-0"
                @click="newProduct()">
                <v-icon size="20px">
                  add
                </v-icon>
              </v-btn>
              <v-btn
                v-if="settings.isAdministrator"
                id="perkStoreAppMenuSettingsButton"
                class="mr-0 ml-0"
                icon
                flat
                title="Settings"
                @click="$emit('modify-settings')">
                <v-icon size="17px">
                  fa-cog
                </v-icon>
              </v-btn>
            </template>
          </v-toolbar>

          <v-toolbar v-if="loading || error" color="white">
            <v-spacer />
            <v-progress-circular
              v-if="loading"
              color="primary"
              class="mb-2"
              indeterminate />
            <v-flex v-else-if="error" class="text-xs-center">
              <div class="alert alert-error">
                <i class="uiIconError"></i> {{ error }}
              </div>
            </v-flex>
            <v-spacer />
          </v-toolbar>

          <product-orders-list
            v-if="displayProductOrders"
            ref="productOrdersList"
            :product="selectedProduct"
            @loading="loading = $event"
            @error="error = $event"
            @close="displayProductOrders = false; selectedProduct = null;" />
          <product-form
            v-else-if="displayProductForm"
            ref="productForm"
            :product="selectedProduct"
            @added="init" />
          <products-list
            v-else
            :products="filteredProducts"
            :settings="settings"
            @orders-list="displayCommandsList"
            @edit="editProduct"
            @buy="buyProduct" />
        </v-flex>
      </v-layout>
    </main>
  </v-app>
</template>

<script>
import ProductsList from './perk-store/ProductsList.vue';
import ProductOrdersList from './perk-store/ProductOrdersList.vue';
import ProductForm from './perk-store/ProductForm.vue';

import {initSettings} from '../js/PerkStoreSettings.js';
import {getProductList} from '../js/PerkStoreProduct.js';

export default {
  components: {
    ProductsList,
    ProductOrdersList,
    ProductForm,
  },
  data: () => ({
    error: null,
    loading: false,
    selectedProduct: null,
    displayProductForm: false,
    displayProductOrders: false,
    search: null,
    settings: {},
    products: [],
  }),
  computed: {
    filteredProducts() {
      if(this.search && this.search.trim().length) {
        const searchTerm = this.search.trim().toLowerCase();
        return this.products.slice().filter(product => (product.title && product.title.toLowerCase().indexOf(searchTerm)) >= 0 || (product.description && product.description.toLowerCase().indexOf(searchTerm) >= 0));
      } else {
        return this.products.slice();
      }
    }
  },
  created() {
    return this.init();
  },
  methods: {
    init() {
      this.loading = true;
      return initSettings()
      .then(() => {
        this.settings = window.perkStoreSettings;
      })
      .then(() => getProductList())
      .then((products) => {
        this.products = products;
      })
      .catch(e => {
        this.error = e;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    displayCommandsList(product) {
      if (product && product.canEdit) {
        this.displayProductOrders = true;
        this.selectedProduct = product;
        return this.$nextTick().then(() => this.$refs.productOrdersList && this.$refs.productOrdersList.init());
      }
    },
    closeDetails() {
      this.displayProductForm = false;
      this.displayProductOrders = false;
      this.selectedProduct = null;
    },
    newProduct() {
      this.displayProductForm = true;
      this.selectedProduct = {};
      return this.$nextTick().then(() => this.$refs.productOrdersList && this.$refs.productForm.init());
    },
    editProduct(product) {
      this.displayProductForm = true;
      this.selectedProduct = Object.assign({}, product);
      return this.$nextTick().then(() => this.$refs.productOrdersList && this.$refs.productForm.init());
    },
    buyProduct(product) {
      // Display buy product form
    },
  }
};
</script>