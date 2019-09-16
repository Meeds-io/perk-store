<template>
  <v-app 
    id="PerkStoreApp"
    class="transparent VuetifyApp"
    flat>
    <main>
      <v-layout justify-center>
        <v-flex xs12>
          <v-toolbar
            color="white"
            class="application-toolbar"
            flat
            dense>
            <v-toolbar-title class="d-flex mt-1">
              <v-btn
                v-show="displayFilterButton"
                id="perkStoreAppMenuDisplayFilterButton"
                :title="$t('exoplatform.perkstore.button.displayFilters')"
                icon
                text
                @click="showFilters">
                <v-icon color="primary">
                  fa-filter
                </v-icon>
              </v-btn>
              <span>{{ $t('exoplatform.perkstore.title.perkStoreApplication') }}</span>
              <template v-if="displayProductDetails && selectedProduct">
              </template>
              <template v-else-if="displayProductForm && selectedProduct && selectedProduct.id">
                - <span class="ml-2 primary--text">{{ selectedProduct.title }}</span>
              </template>
              <template v-else-if="displayProductForm && selectedProduct">
                - {{ $t('exoplatform.perkstore.title.createNewProduct') }}
              </template>
              <template v-else-if="displayProductOrders && selectedProduct && selectedOrderId">
                - {{ $t('exoplatform.perkstore.title.order') }} <span class="primary--text">#{{ selectedOrderId }}</span> : <span class="primary--text">{{ selectedProduct.title }}</span>
              </template>
              <template v-else-if="displayProductOrders && canEditSelectedProduct">
                - {{ $t('exoplatform.perkstore.title.ordersListOf') }} <span class="primary--text">{{ selectedProduct.title }}</span>
              </template>
              <template v-else-if="displayProductOrders && selectedProduct">
                - {{ $t('exoplatform.perkstore.title.myOrdersListOf') }} <span class="primary--text">{{ selectedProduct.title }}</span>
              </template>
              <template v-else-if="displayMyOrders">
                - {{ $t('exoplatform.perkstore.title.myOrders') }}
              </template>
              <template v-else-if="perkStoreEnabled && products && products.length">
                <v-text-field
                  v-model="search"
                  :placeholder="$t('exoplatform.perkstore.label.productSearchPlaceholder')"
                  prepend-inner-icon="search"
                  single-line
                  hide-details
                  autofocus
                  class="searchProductsInput ml-3 mt-1 py-0 d-inline-flex" />
              </template>
            </v-toolbar-title>
            <v-spacer />
            <template v-if="displayProductOrders">
              <template v-if="barcodeReader">
                <v-btn
                  id="perkStoreAppMenuOrdersListButton"
                  :title="$t('exoplatform.perkstore.button.showOrderList')"
                  icon
                  text
                  @click="$refs.ordersList.closeBarcodeReader()">
                  <v-icon>
                    fa-list
                  </v-icon>
                </v-btn>
              </template>
              <template v-else>
                <v-btn
                  v-if="canEditSelectedProduct"
                  id="perkStoreAppMenuBarcodeButton"
                  :title="$t('exoplatform.perkstore.button.switchToBarCodeReader')"
                  icon
                  text
                  @click="$refs.ordersList.openBarcodeReader()">
                  <v-icon>
                    fa-barcode
                  </v-icon>
                </v-btn>
                <v-btn
                  id="perkStoreAppMenuDownloadButton"
                  :title="$t('exoplatform.perkstore.button.exportAsCSV')"
                  icon
                  text
                  @click="exportOrders">
                  <v-icon>
                    fa-download
                  </v-icon>
                </v-btn>
              </template>
            </template>
            <v-btn
              v-if="displayProductDetails && selectedProduct && selectedProduct.userData && selectedProduct.userData.canEdit"
              id="perkStoreAppMenuEditButton"
              :title="$t('exoplatform.perkstore.button.edit')"
              class="primary"
              icon
              text
              dark
              fab
              small
              @click="editProduct(selectedProduct)">
              <v-icon>fa-pen</v-icon>
            </v-btn>
            <v-btn
              v-if="displayCloseIcon"
              id="perkStoreAppMenuCloseButton"
              :title="$t('exoplatform.perkstore.button.close')"
              class="secondary ml-3 mr-3"
              icon
              text
              dark
              fab
              small
              @click="closeDetails">
              <v-icon>
                close
              </v-icon>
            </v-btn>
            <template v-else-if="perkStoreEnabled && !barcodeReader">
              <div id="productFilterMenu">
                <v-menu
                  v-model="productFilterMenu"
                  attach="#productFilterMenu"
                  offset-y>
                  <v-list dense class="pt-0 pb-0">
                    <v-list-item @click="filterProducts">
                      <v-list-item-title>
                        <v-checkbox
                          v-model="productsFilters.disabled"
                          :label="$t('exoplatform.perkstore.label.productFiltersDisabledProducts')"
                          class="pt-0 pb-0" />
                      </v-list-item-title>
                    </v-list-item>
                    <v-list-item @click="filterProducts">
                      <v-list-item-title>
                        <v-checkbox
                          v-model="productsFilters.soldOut"
                          :label="$t('exoplatform.perkstore.label.productFiltersSoldOutProducts')"
                          class="pt-0 pb-0" />
                      </v-list-item-title>
                    </v-list-item>
                    <v-list-item @click="filterProducts">
                      <v-list-item-title>
                        <v-checkbox
                          v-model="productsFilters.mine"
                          :label="$t('exoplatform.perkstore.label.createdByMe')"
                          class="pt-0 pb-0" />
                      </v-list-item-title>
                    </v-list-item>
                  </v-list>
                </v-menu>
              </div>
              <v-btn
                :title="$t('exoplatform.perkstore.label.productFilters')"
                text
                small
                class="toolbarButton"
                @click="productFilterMenu = true">
                <v-icon
                  :color="productsFilterIconClass"
                  class="mr-2"
                  size="17">
                  fa-filter
                </v-icon>
                <span class="d-sm-inline-flex d-none">
                  {{ $t('exoplatform.perkstore.label.productFilters') }}
                </span>
              </v-btn>
              <span class="d-sm-inline-flex d-none ml-4"></span>
              <v-btn
                id="perkStoreAppMyOrdersButton"
                :title="$t('exoplatform.perkstore.button.myOrders')"
                text
                small
                class="toolbarButton"
                @click="displayMyOrdersList">
                <v-icon class="mr-2" size="17">
                  fa-file-invoice-dollar
                </v-icon>
                <span class="d-sm-inline-flex d-none">
                  {{ $t('exoplatform.perkstore.button.myOrders') }}
                </span>
              </v-btn>
              <v-btn
                v-if="userSettings.canAddProduct"
                id="perkStoreAppMenuAddButton"
                :title="$t('exoplatform.perkstore.button.addProduct')"
                text
                small
                class="toolbarButton d-md-none"
                @click="newProduct()">
                <v-icon size="17">
                  add
                </v-icon>
              </v-btn>
              <template v-if="userSettings.canAddProduct">
                <span class="d-sm-inline-flex d-none ml-3"></span>
                <v-btn
                  v-if="userSettings.administrator"
                  id="perkStoreAppMenuSettingsButton"
                  :title="$t('exoplatform.perkstore.button.settings')"
                  class="toolbarButton"
                  text
                  small
                  @click="displaySettingsModal">
                  <v-icon class="mr-2">
                    fa-cog
                  </v-icon>
                  <span class="d-sm-inline-flex d-none">
                    {{ $t('exoplatform.perkstore.button.settings') }}
                  </span>
                </v-btn>
              </template>
              <div class="primary dark toolbarBalance no-wrap">
                <span class="d-none d-sm-inline-block">
                  {{ $t('exoplatform.perkstore.label.balance') }}:
                </span>
                {{ balance }} {{ symbol }}
              </div>
            </template>
          </v-toolbar>

          <v-toolbar
            v-if="perkStoreEnabled && !loading && !walletLoading && walletWarning"
            color="transparent"
            flat>
            <v-spacer />
            <v-flex class="text-center">
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
            <v-flex v-else-if="error" class="text-center">
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
            @close="closeDetails"
            @reader-closed="barcodeReader = false"
            @reader-opened="barcodeReader = true" />
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
            :need-password="walletNeedPassword"
            :can-add-product="userSettings.canAddProduct"
            :wallet-loading="walletLoading"
            :wallet-enabled="walletEnabled && walletAddonInstalled"
            @product-details="displayProduct"
            @create-product="newProduct"
            @orders-list="displayProductOrdersList"
            @edit="editProduct"
            @buy="buyProduct"
            @close="closeDetails" />

          <buy-modal
            ref="buyModal"
            :product="selectedProduct"
            :symbol="symbol"
            :need-password="walletNeedPassword"
            :wallet-loading="walletLoading"
            :wallet-enabled="walletEnabled" />

          <settings-modal
            ref="settingsModal"
            @saved="init()" />

          <product-notification
            :products="modifiedProducts"
            @refresh-list="addNewProductsToList" />
        </v-flex>
      </v-layout>
      <div id="perkStoreDialogsParent">
      </div>
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
import {toFixed} from '../js/PerkStoreProductOrder.js';
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
    productFilterMenu: false,
    walletWarning: null,
    error: null,
    walletAddonInstalled: false,
    walletLoading: false,
    walletEnabled: false,
    barcodeReader: false,
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
    wallet: null,
    contractDetail: null,
    ordersFilter: {},
    productsFilters: {},
    settings: {},
    userSettings: {},
    createOrUpdateOrderEvent: 'exo.addons.perkstore.order.createOrModify',
    createOrUpdateProductEvent: 'exo.addons.perkstore.product.createOrModify',
    products: [],
    modifiedProducts: [],
  }),
  computed: {
    balance() {
      return (this.wallet && this.wallet.tokenBalance && toFixed(this.wallet.tokenBalance)) || 0;
    },
    symbol() {
      return this.contractDetail && this.contractDetail.symbol;
    },
    productsFilterIconClass() {
      return (!this.productsFilters || !this.productsFilters.disabled || !this.productsFilters.soldOut) ? 'primary' : '';
    },
    canEditSelectedProduct() {
      return  this.selectedProduct && this.selectedProduct.userData && this.selectedProduct.userData.canEdit;
    },
    displayFilterButton() {
      return (this.displayProductOrders && !this.barcodeReader && !this.selectedOrderId) || this.displayMyOrders;
    },
    displayCloseIcon() {
      return !this.barcodeReader && (this.displayProductForm || this.displayProductOrders || this.displayProductDetails || this.displayMyOrders);
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

    document.addEventListener('exo-wallet-settings-loaded', this.walletSettingsLoaded);
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

      this.init();
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
            this.walletWarning = this.$t('exoplatform.perkstore.warning.walletNotInstalled');
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
          this.products = (products && products.filter(product => (!this.productsFilters.mine || (product.creator && product.creator.type === 'user' && product.creator.id === eXo.env.portal.userName)) && (product.enabled || this.productsFilters.disabled) && (product.unlimited || product.totalSupply > product.purchased  || this.productsFilters.soldOut))) || [];

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
    walletSettingsLoaded(event) {
      this.contractDetail = event && event.detail && event.detail.contractDetail;
      this.wallet = event && event.detail && event.detail.wallet;
    },
    walletInitialized(event) {
      this.walletLoading = false;
      this.walletNeedPassword = false;
      const result = event && event.detail;
      if(!result || result.error) {
        this.walletWarning = `${result && result.error ? (`${  result.error}`) : this.$t('exoplatform.perkstore.warning.walletNotConfiguredProperly')}`;
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
    displayProductOrdersList(product, orderId, currentUserOrders) {
      if (!product) {
        return;
      }
      this.closeDetails();
      this.selectedProduct = product;
      this.selectedOrderId = orderId;
      this.displayProductOrders = true;
      return this.$nextTick().then(() => this.$refs.ordersList && this.$refs.ordersList.init(currentUserOrders));
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
      this.selectedProduct = {
        imageFiles: []
      };
      return this.$nextTick().then(() => this.$refs.productForm && this.$refs.productForm.init());
    },
    editProduct(product) {
      this.closeDetails();
      this.displayProductForm = true;
      this.selectedProduct = Object.assign({}, product);
      this.selectedProduct.imageFiles = this.selectedProduct.imageFiles || [];
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

              if (this.selectedProduct && this.selectedProduct.id === freshProduct.id) {
                Object.assign(this.selectedProduct, freshProduct);
              }
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