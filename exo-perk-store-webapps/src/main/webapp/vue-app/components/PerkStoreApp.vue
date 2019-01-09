<template>
  <v-app 
    id="PerkStoreApp"
    class="transparent"
    flat>
    <main>
      <v-layout justify-center>
        <v-flex xs12>
          <v-toolbar color="white" class="elevation-1">
            <v-toolbar-title>
              <v-btn
                v-if="displayProductOrders && selectedProduct.canEdit"
                id="perkStoreAppMenuDisplayFilterButton"
                icon
                flat
                title="Display filters"
                @click="showFilters">
                <v-icon>
                  menu
                </v-icon>
              </v-btn>
              Perk store
              <template v-if="displayProductForm && selectedProduct && selectedProduct.id">
                - edit product
              </template>
              <template v-else-if="displayProductForm && selectedProduct">
                - Add new product
              </template>
              <template v-else-if="displayProductOrders && selectedProduct && selectedProduct.canEdit">
                - Orders list of <span class="primary--text">{{ selectedProduct.title }}</span>
              </template>
              <template v-else-if="displayProductOrders && selectedProduct">
                - My orders list of <code>{{ selectedProduct.title }}</code>
              </template>
            </v-toolbar-title>
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

          <v-toolbar
            v-if="!walletAddonInstalled"
            color="transparent"
            flat>
            <v-spacer />
            <v-flex v-if="!loading && !walletLoading && !walletAddonInstalled" class="text-xs-center">
              <div class="alert alert-warning">
                <i class="uiIconWarning"></i>
                Ethereum wallet addon isn't installed, thus no payment is possible.
              </div>
            </v-flex>
            <v-spacer />
          </v-toolbar>

          <v-toolbar
            v-if="loading || error"
            color="transparent"
            flat>
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
            :orders-filter="ordersFilter"
            @loading="loading = $event"
            @error="error = $event"
            @close="closeDetails" />
          <product-form
            v-else-if="displayProductForm"
            ref="productForm"
            :product="selectedProduct"
            @added="init"
            @close="closeDetails" />
          <products-list
            v-else
            :products="filteredProducts"
            :settings="settings"
            :wallet-loading="walletLoading"
            :wallet-enabled="walletEnabled"
            @orders-list="displayCommandsList"
            @edit="editProduct"
            @buy="buyProduct" />
          <product-buy-modal
            ref="productBuyModal"
            :product="selectedProduct"
            :symbol="settings.symbol"
            :need-password="walletNeedPassword"
            @closed="selectedProduct = null" />
        </v-flex>
      </v-layout>
    </main>
  </v-app>
</template>

<script>
import ProductsList from './perk-store/ProductsList.vue';
import ProductOrdersList from './perk-store/ProductOrdersList.vue';
import ProductForm from './perk-store/ProductForm.vue';
import ProductBuyModal from './perk-store/ProductBuyModal.vue';

import {initSettings, getOrderFilter} from '../js/PerkStoreSettings.js';
import {getProductList} from '../js/PerkStoreProduct.js';

export default {
  components: {
    ProductsList,
    ProductOrdersList,
    ProductForm,
    ProductBuyModal,
  },
  data: () => ({
    error: null,
    walletAddonInstalled: false,
    walletLoading: false,
    walletEnabled: false,
    walletNeedPassword: false,
    loading: false,
    selectedProduct: null,
    displayProductForm: false,
    displayProductOrders: false,
    search: null,
    ordersFilter: {},
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
    document.addEventListener('exo-wallet-init-result', this.walletInitialized);
    if(window.walletAddonInstalled) {
      this.initWalletAPI();
    } else {
      document.addEventListener('exo-wallet-installed', this.initWalletAPI);
    }
    return this.init();
  },
  methods: {
    init() {
      this.loading = true;
      return initSettings()
      .then(() => {
        this.settings = window.perkStoreSettings;
        this.ordersFilter = getOrderFilter(this.settings);
      })
      .then(() => getProductList())
      .then((products) => {
        this.products = products;
      })
      .catch(e => {
        this.error = e;
      })
      .finally(() => {
        window.setTimeout(() => {
          if(window.walletAddonInstalled) {
            this.initWalletAPI();
          }
        }, 2000);
        this.loading = false;
      });
    },
    initWalletAPI() {
      if(!this.walletAddonInstalled) {
        this.walletLoading = true;
        this.walletAddonInstalled = true;
        document.dispatchEvent(new CustomEvent('exo-wallet-init'));
      }
    },
    walletInitialized(event) {
      this.walletLoading = false;
      const result = event && event.detail;
      if(!result || result.error) {
        this.error = `Wallet seems not configured properly ${result && result.error ? (`: ${  result.error}`) : ''}`;
        this.walletEnabled = false;
      } else {
        this.walletEnabled = true;
        this.walletNeedPassword = result.needPassword;
      }
    },
    displayCommandsList(product) {
      if (product) {
        this.selectedProduct = product;
        this.displayProductOrders = true;
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
      return this.$nextTick().then(() => this.$refs.productForm && this.$refs.productForm.init());
    },
    editProduct(product) {
      this.displayProductForm = true;
      this.selectedProduct = Object.assign({}, product);
      return this.$nextTick().then(() => this.$refs.productForm && this.$refs.productForm.init());
    },
    showFilters() {
      if(this.$refs.productOrdersList) {
        this.$refs.productOrdersList.showFilters();
      }
    },
    buyProduct(product) {
      this.selectedProduct = product;
      this.$refs.productBuyModal.open();
    },
  }
};
</script>