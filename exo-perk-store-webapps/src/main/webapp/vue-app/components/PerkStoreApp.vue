<template>
  <v-app 
    id="PerkStoreApp"
    class="transparent"
    flat>
    <main>
      <v-layout justify-center>
        <v-flex xs12>
          <v-toolbar
            color="white"
            class="application-toolbar"
            flat
            dense>
            <v-toolbar-title>
              <v-btn
                v-show="displayFilterButton"
                id="perkStoreAppMenuDisplayFilterButton"
                icon
                flat
                title="Display filters"
                @click="showFilters">
                <v-icon small color="primary">
                  fa-filter
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
              <template v-else-if="displayProductOrders && canEditSelectedProduct">
                - Orders list of <span class="primary--text">{{ selectedProduct.title }}</span>
              </template>
              <template v-else-if="displayProductOrders && selectedProduct">
                - My orders list of <span class="primary--text">{{ selectedProduct.title }}</span>
              </template>
              <template v-else-if="displayMyOrders">
                - My orders
              </template>
            </v-toolbar-title>
            <v-spacer />
            <v-btn
              v-if="displayProductOrders"
              id="perkStoreAppMenuCloseButton"
              icon
              flat
              title="Export as CSV"
              @click="exportOrders">
              <v-icon small>
                fa-download
              </v-icon>
            </v-btn>
            <v-btn
              v-if="displayCloseIcon"
              id="perkStoreAppMenuCloseButton"
              icon
              flat
              title="Close"
              @click="closeDetails">
              <v-icon small>
                close
              </v-icon>
            </v-btn>
            <template v-else-if="perkStoreEnabled">
              <input
                v-if="products && products.length"
                v-model="search"
                placeholder="Search in products"
                type="text"
                class="searchProductsInput mr-3">
              <v-menu offset-y>
                <v-btn
                  slot="activator"
                  icon
                  small>
                  <v-icon :color="productsFilterIconClass" small>
                    fa-filter
                  </v-icon>
                </v-btn>
                <v-list dense class="pt-0 pb-0">
                  <v-list-tile @click="filterProducts">
                    <v-list-tile-title>
                      <v-checkbox
                        v-model="productsFilters.disabled"
                        label="Disabled Products"
                        class="pt-0 pb-0" />
                    </v-list-tile-title>
                  </v-list-tile>
                  <v-list-tile @click="filterProducts">
                    <v-list-tile-title>
                      <v-checkbox
                        v-model="productsFilters.soldOut"
                        label="Sold out Products"
                        class="pt-0 pb-0" />
                    </v-list-tile-title>
                  </v-list-tile>
                </v-list>
              </v-menu>
              <v-btn
                id="perkStoreAppMenuRefreshButton"
                icon
                flat
                title="Refresh store"
                class="mr-0"
                @click="init()">
                <v-icon small>
                  refresh
                </v-icon>
              </v-btn>
              <v-btn
                id="perkStoreAppMyOrdersButton"
                icon
                flat
                title="My orders"
                class="mr-0"
                @click="displayMyOrdersList">
                <v-icon small>
                  fa-file-invoice-dollar
                </v-icon>
              </v-btn>
              <v-btn
                v-if="userSettings.canAddProduct"
                id="perkStoreAppMenuAddButton"
                icon
                flat
                title="Add product"
                class="mr-0"
                @click="newProduct()">
                <v-icon small>
                  add
                </v-icon>
              </v-btn>
              <v-btn
                v-if="userSettings.administrator"
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
            v-if="perkStoreEnabled && !selectedProduct && !loading && !walletLoading && walletWarning"
            color="transparent"
            flat>
            <v-spacer />
            <v-flex class="text-xs-center">
              <div class="alert alert-warning">
                <i class="uiIconWarning"></i>
                {{ walletWarning }}
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

          <orders-list
            v-if="displayProductOrders || displayMyOrders"
            ref="ordersList"
            :product="selectedProduct"
            :selected-order-id="selectedOrderId"
            :orders-filter="ordersFilter"
            :symbol="symbol"
            @init-wallet="initWalletAPI(true)"
            @display-product="displayProduct($event)"
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
            :symbol="symbol"
            :loading="loading"
            :wallet-loading="walletLoading"
            :wallet-enabled="walletEnabled && walletAddonInstalled"
            @product-details="displayProduct"
            @orders-list="displayProductOrdersList"
            @edit="editProduct"
            @buy="buyProduct" />

          <buy-modal
            ref="buyModal"
            :product="selectedProduct"
            :symbol="symbol"
            :need-password="walletNeedPassword" />

          <settings-modal
            ref="settingsModal"
            :wallet-symbol="walletSymbol"
            @saved="init()" />

          <product-notification
            :products="modifiedProducts"
            @refresh-list="addNewProductsToList" />
        </v-flex>
      </v-layout>
    </main>
  </v-app>
</template>

<script>
import SettingsModal from './perk-store/SettingsModal.vue';
import ProductsList from './perk-store/ProductsList.vue';
import OrdersList from './perk-store/OrdersList.vue';
import ProductForm from './perk-store/ProductForm.vue';
import BuyModal from './perk-store/BuyModal.vue';
import ProductNotification from './perk-store/ProductNotification.vue';

import {initSettings, getOrderFilter, getProductFilter, storeProductFilter} from '../js/PerkStoreSettings.js';
import {getProductList, getProduct} from '../js/PerkStoreProduct.js';

export default {
  components: {
    SettingsModal,
    ProductsList,
    OrdersList,
    ProductForm,
    BuyModal,
    ProductNotification,
  },
  data: () => ({
    walletWarning: null,
    error: null,
    walletAddonInstalled: false,
    walletLoading: false,
    walletEnabled: false,
    perkStoreEnabled: false,
    walletNeedPassword: false,
    loading: false,
    selectedProduct: null,
    selectedOrderId: null,
    displayMyOrders: false,
    displayProductDetails: false,
    displayProductForm: false,
    displayProductOrders: false,
    search: null,
    ordersFilter: {},
    productsFilters: {},
    settings: {},
    symbol: null,
    walletSymbol: null,
    userSettings: {},
    createOrUpdateOrderEvent: 'exo.addons.perkstore.order.createOrModify',
    createOrUpdateProductEvent: 'exo.addons.perkstore.product.createOrModify',
    products: [],
    modifiedProducts: [],
  }),
  computed: {
    productsFilterIconClass() {
      return (!this.productsFilters || !this.productsFilters.disabled || !this.productsFilters.soldOut) ? 'primary' : '';
    },
    canEditSelectedProduct() {
      return  this.selectedProduct && this.selectedProduct.userData && this.selectedProduct.userData.canEdit;
    },
    displayFilterButton() {
      return (this.displayProductOrders && !this.selectedOrderId) || this.displayMyOrders;
    },
    displayCloseIcon() {
      return this.displayProductForm || this.displayProductOrders || this.displayProductDetails || this.displayMyOrders;
    },
    filteredProducts() {
      let products = this.products.slice();
      if(this.search && this.search.trim().length) {
        const searchTerm = this.search.trim().toLowerCase();
        products = products.slice().filter(product => (product.title && product.title.toLowerCase().indexOf(searchTerm)) >= 0 || (product.description && product.description.toLowerCase().indexOf(searchTerm) >= 0));
      }
      products = products.filter(product => product.enabled || (product.userData && product.userData.canEdit));
      return products;
    },
  },
  watch: {
    selectedProduct() {
      this.error = null;
    }
  },
  created() {
    document.addEventListener('exo.perkstore.settings.modified', this.refreshSettings);

    document.addEventListener(this.createOrUpdateProductEvent, this.updateProduct);

    document.addEventListener(this.createOrUpdateOrderEvent, this.updateProduct);

    document.addEventListener('exo-wallet-init-result', this.walletInitialized);
    document.addEventListener('exo-wallet-init-loading', this.walletIsLoading);
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
    refreshSettings(event) {
      if(!event || !event.detail || !event.detail.globalsettings) {
        return;
      }

      const newSettings = event.detail.globalsettings;
      this.symbol = this.settings.symbol = newSettings.symbol;
      // refresh entire application if the access permission is changed
    },
    init(selectedProductId, selectedOrderId) {
      this.products = [];
      this.productsFilters = getProductFilter() || {};
      this.loading = true;
      return initSettings()
      .then(() => {
        this.perkStoreEnabled = true;
        this.settings = window.perkStoreSettings;
        if(!this.settings) {
          this.settings = {};
        }
        this.settings.symbol = this.symbol = this.settings.symbol || this.symbol;
        this.userSettings = this.settings.userSettings;
        this.ordersFilter = getOrderFilter();
      })
      .then(() => this.refreshProductList(selectedProductId, selectedOrderId))
      .catch(e => {
        console.debug("Error initializing application", e);
        this.error = e && e.message ? e.message : String(e);
      })
      .finally(() => {
        window.setTimeout(() => {
          if(window.walletAddonInstalled) {
            this.initWalletAPI();
          } else {
            this.walletWarning = 'Ethereum wallet addon isn\'t installed, thus no payment is possible.';
          }
        }, 2000);
        this.loading = false;
      });
    },
    filterProducts(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();

        storeProductFilter(this.productsFilters);
      }
      this.loading = true;
      return this.refreshProductList().finally(() => this.loading = false);
    },
    refreshProductList(selectedProductId, selectedOrderId) {
      return getProductList()
        .then((products) => {
          this.products = (products && products.filter(product => (product.enabled || this.productsFilters.disabled) && (product.unlimited || product.totalSupply > product.purchased  || this.productsFilters.soldOut))) || [];

          if (this.products.length && selectedProductId) {
            const selectedProduct = this.products.find(product => product.id === Number(selectedProductId));
            if(selectedProduct) {
              if(selectedOrderId) {
                this.displayProductOrdersList(selectedProduct, Number(selectedOrderId));
              } else {
                this.displayProduct(selectedProduct);
              }
            }
          }
        });
    },
    walletIsLoading() {
      this.walletAddonInstalled = true;
      this.walletLoading = true;
      this.walletWarning = null;
    },
    initWalletAPI(forceReload) {
      if(forceReload || (!this.walletAddonInstalled && window.walletAddonInstalled)) {
        document.dispatchEvent(new CustomEvent('exo-wallet-init'));
      }
    },
    walletInitialized(event) {
      this.walletLoading = false;
      this.walletNeedPassword = false;
      const result = event && event.detail;
      this.walletSymbol = result && result.symbol;
      this.settings.symbol = this.symbol = this.symbol || this.walletSymbol || '';
      if(!result || result.error) {
        this.walletWarning = `${result && result.error ? (`${  result.error}`) : 'Wallet seems not configured properly'}`;
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
      this.displayMyOrders = false;
      this.selectedProduct = null;
      this.selectedOrderId = 0;
    },
    displayProductOrdersList(product, orderId) {
      if (!product) {
        return;
      }
      this.closeDetails();
      this.selectedProduct = product;
      this.selectedOrderId = orderId;
      this.displayProductOrders = true;
      return this.$nextTick().then(() => this.$refs.ordersList && this.$refs.ordersList.init());
    },
    exportOrders() {
      return this.$nextTick().then(() => this.$refs.ordersList && this.$refs.ordersList.exportOrders());
    },
    displayMyOrdersList() {
      this.closeDetails();
      this.selectedProduct = null;
      this.displayMyOrders = true;
      return this.$nextTick().then(() => this.$refs.ordersList && this.$refs.ordersList.init());
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
      if (product && !product.id) {
        // It's may be about product id and not the object
        product = this.products.find(existingProduct => existingProduct.id === product);
      }
      if(product) {
        this.closeDetails();
        this.displayProductDetails = true;
        this.selectedProduct = Object.assign({}, product);
      }
    },
    showFilters() {
      if(this.$refs.ordersList) {
        this.$refs.ordersList.showFilters();
      }
    },
    buyProduct(product) {
      this.selectedProduct = product;
      this.$refs.buyModal.open();
    },
    displaySettingsModal() {
      this.$refs.settingsModal.open();
    },
    updateProduct(event) {
      const wsMessage = event.detail;
      if(wsMessage.product && wsMessage.product.id) {
        let product = this.products && this.products.find(product => product && product.id === wsMessage.product.id);
        // Existing product
        if(product && product.id) {
          getProduct(product.id).then(freshProduct => {
            if(freshProduct && freshProduct.userData && freshProduct.userData.username === eXo.env.portal.userName) {
              // Additional check for user data target
              Object.assign(product, freshProduct);

              // Add notification to non last modifier
              /*
              Disabled to not add notification about product modification

              if(event.type === this.createOrUpdateProductEvent && (product.lastModifier && product.lastModifier.id !== eXo.env.portal.userName) || (!product.lastModifier && product.creator && product.creator.id !== eXo.env.portal.userName)) {
                this.modifiedProducts.unshift(product);
              }
              */
            }
          });
        } else {
          product = wsMessage.product;

          // New product added
          getProduct(product.id).then(freshProduct => {
            if(freshProduct && freshProduct.userData && freshProduct.userData.username === eXo.env.portal.userName) {
              // Additional check for user data target
              Object.assign(product, freshProduct);

              // Add notification to non last modifier
              if(event.type === this.createOrUpdateProductEvent && !this.modifiedProducts.find(modifiedProduct => modifiedProduct.id === product.id) && (product.lastModifier && product.lastModifier.id !== eXo.env.portal.userName) || (!product.lastModifier && product.creator && product.creator.id !== eXo.env.portal.userName)) {
                this.modifiedProducts.unshift(product);
              }
            }
          });
        }
      }
    },
    addNewProductsToList() {
      this.modifiedProducts.reverse().forEach(product => {
        if (!product.lastModifiedDate && !this.products.find(existingProduct => existingProduct.id === product.id)) {
          this.products.unshift(product);
        }
      });
      this.modifiedProducts.splice(0, this.modifiedProducts.length);
    },
  }
};
</script>