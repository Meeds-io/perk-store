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
              <template v-if="displayProductDetails && selectedProduct">
                - Details of <span class="primary--text">{{ selectedProduct.title }}</span>
              </template>
              <template v-else-if="displayProductForm && selectedProduct && selectedProduct.id">
                - edit product
              </template>
              <template v-else-if="displayProductForm && selectedProduct">
                - Add new product
              </template>
              <template v-else-if="displayProductOrders && selectedProduct && selectedOrderId">
                - Order <span class="primary--text">#{{ selectedOrderId }}</span> of <span class="primary--text">{{ selectedProduct.title }}</span>
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
              v-if="displayProductForm || displayProductOrders || displayProductDetails"
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
                v-if="filteredProducts && filteredProducts.length"
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
                @click="init()">
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
                v-if="settings.administrator"
                id="perkStoreAppMenuSettingsButton"
                class="mr-0 ml-0"
                icon
                flat
                title="Settings"
                @click="displaySettingsModal">
                <v-icon size="17px">
                  fa-cog
                </v-icon>
              </v-btn>
            </template>
          </v-toolbar>

          <v-toolbar
            v-if="!selectedProduct && !loading && !walletLoading && warning"
            color="transparent"
            flat>
            <v-spacer />
            <v-flex class="text-xs-center">
              <div class="alert alert-warning">
                <i class="uiIconWarning"></i>
                {{ warning }}
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
            :selected-order-id="selectedOrderId"
            :orders-filter="ordersFilter"
            @loading="loading = $event"
            @error="error = $event"
            @close="closeDetails" />
          <product-form
            v-else-if="displayProductForm"
            ref="productForm"
            :product="selectedProduct"
            @added="init()"
            @error="error=$event"
            @close="closeDetails" />
          <products-list
            v-else-if="!error || (filteredProducts && filteredProducts.length)"
            ref="productsList"
            :products="filteredProducts"
            :selected-product="displayProductDetails && selectedProduct"
            :settings="settings"
            :loading="loading"
            :wallet-loading="walletLoading"
            :wallet-enabled="walletEnabled && walletAddonInstalled"
            @product-details="displayProduct"
            @orders-list="displayCommandsList"
            @edit="editProduct"
            @buy="buyProduct" />

          <product-buy-modal
            ref="productBuyModal"
            :product="selectedProduct"
            :symbol="settings.symbol"
            :need-password="walletNeedPassword" />

          <settings-modal
            ref="settingsModal"
            @saved="init()" />
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
import SettingsModal from './perk-store/SettingsModal.vue';

import {initSettings, getOrderFilter} from '../js/PerkStoreSettings.js';
import {getProductList} from '../js/PerkStoreProduct.js';

export default {
  components: {
    ProductsList,
    ProductOrdersList,
    ProductForm,
    ProductBuyModal,
    SettingsModal,
  },
  data: () => ({
    warning: null,
    error: null,
    walletAddonInstalled: false,
    walletLoading: false,
    walletEnabled: false,
    walletNeedPassword: false,
    loading: false,
    selectedProduct: null,
    selectedOrderId: null,
    displayProductDetails: false,
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
    },
  },
  watch: {
    selectedProduct() {
      this.error = null;
    }
  },
  created() {
    document.addEventListener('exo-wallet-init-result', this.walletInitialized);
    if(window.walletAddonInstalled) {
      this.initWalletAPI();
    } else {
      document.addEventListener('exo-wallet-installed', this.initWalletAPI);
    }
    const search = document.location.search.substring(1);
    let parameters = null;
    if(search) {
      parameters = JSON.parse(
          `{"${decodeURI(search)
            .replace(/"/g, '\\"')
            .replace(/&/g, '","')
            .replace(/=/g, '":"')}"}`
        );
    }
    return this.init(parameters && parameters.productId, parameters && parameters.orderId);
  },
  methods: {
    init(selectedProductId, selectedOrderId) {
      this.products = [];
      this.loading = true;
      return initSettings()
      .then(() => {
        this.settings = window.perkStoreSettings;
        this.ordersFilter = getOrderFilter();
      })
      .then(() => getProductList())
      .then((products) => {
        this.products = products || [];

        if (this.products.length && selectedProductId) {
          const selectedProduct = this.products.find(product => product.id === Number(selectedProductId));
          if(selectedProduct) {
            if(selectedOrderId) {
              this.displayCommandsList(selectedProduct, Number(selectedOrderId));
            } else {
              this.displayProduct(selectedProduct);
            }
          }
        }
      })
      .catch(e => {
        console.debug("Error initializing application", e);
        this.error = e && e.message ? e.message : String(e);
      })
      .finally(() => {
        window.setTimeout(() => {
          if(window.walletAddonInstalled) {
            this.initWalletAPI();
          } else {
            this.warning = 'Ethereum wallet addon isn\'t installed, thus no payment is possible.';
          }
        }, 2000);
        this.loading = false;
      });
    },
    initWalletAPI() {
      if(!this.walletAddonInstalled && window.walletAddonInstalled) {
        this.walletLoading = true;
        this.walletAddonInstalled = true;
        document.dispatchEvent(new CustomEvent('exo-wallet-init'));
      }
    },
    walletInitialized(event) {
      this.walletLoading = false;
      const result = event && event.detail;
      if(!result || result.error) {
        this.warning = `${result && result.error ? (`${  result.error}`) : 'Wallet seems not configured properly'}`;
        this.walletEnabled = false;
      } else {
        this.walletEnabled = true;
        this.walletNeedPassword = result.needPassword;
      }
    },
    closeDetails() {
      this.displayProductDetails = false;
      this.displayProductForm = false;
      this.displayProductOrders = false;
      this.selectedProduct = null;
      this.selectedOrderId = 0;
    },
    displayCommandsList(product, orderId) {
      if (!product) {
        return;
      }
      this.closeDetails();
      this.selectedProduct = product;
      this.selectedOrderId = orderId;
      this.displayProductOrders = true;
      return this.$nextTick().then(() => this.$refs.productOrdersList && this.$refs.productOrdersList.init());
    },
    newProduct() {
      this.closeDetails();
      this.displayProductForm = true;
      this.selectedProduct = {};
      return this.$nextTick().then(() => this.$refs.productForm && this.$refs.productForm.init());
    },
    editProduct(product) {
      this.closeDetails();
      this.displayProductForm = true;
      this.selectedProduct = Object.assign({}, product);
      return this.$nextTick().then(() => this.$refs.productForm && this.$refs.productForm.init());
    },
    displayProduct(product) {
      this.closeDetails();
      this.displayProductDetails = true;
      this.selectedProduct = Object.assign({}, product);
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
    displaySettingsModal() {
      this.$refs.settingsModal.open();
    },
  }
};
</script>