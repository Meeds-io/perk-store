import AutoComplete from './components/AutoComplete.vue';
import BuyForm from './components/perk-store/BuyForm.vue';
import NoResultFound from './components/perk-store/NoResultFound.vue';
import BuyModal from './components/perk-store/BuyModal.vue';
import ProductsList from './components/perk-store/ProductsList.vue';
import ProductDetail from './components/perk-store/ProductDetail.vue';
import ProductForm from './components/perk-store/ProductForm.vue';
import CreateProductButton from './components/perk-store/CreateProductButton.vue';
import DeliverModal from './components/perk-store/DeliverModal.vue';
import FileMultiUploadInput from './components/perk-store/FileMultiUploadInput.vue';
import ImageAttachmentSelector from './components/perk-store/ImageAttachmentSelector.vue';
import OrderDetail from './components/perk-store/OrderDetail.vue';
import OrderNotification from './components/perk-store/OrderNotification.vue';
import OrdersFilter from './components/perk-store/OrdersFilter.vue';
import OrdersList from './components/perk-store/OrdersList.vue';
import ProductDetailContent from './components/perk-store/ProductDetailContent.vue';
import ProductNotification from './components/perk-store/ProductNotification.vue';
import PerkStoreProductSearchCard from './components/perk-store-search/PerkStoreProductSearchCard.vue';
import PerkStoreAlert from './components/perk-store/PerkStoreAlert.vue';
import RefundDrawer from './components/perk-store/RefundDrawer.vue';

const components = {
  'perk-store-auto-complete': AutoComplete,
  'perk-store-buy-form': BuyForm,
  'perk-store-catalog-no-result': NoResultFound,
  'perk-store-buy-modal': BuyModal,
  'perk-store-products-list': ProductsList,
  'perk-store-product-detail': ProductDetail,
  'perk-store-product-form': ProductForm,
  'perk-store-create-product-button': CreateProductButton,
  'perk-store-deliver-modal': DeliverModal,
  'perk-store-upload-input': FileMultiUploadInput,
  'perk-store-image-attachment-selector': ImageAttachmentSelector,
  'perk-store-order-detail': OrderDetail,
  'perk-store-order-notification': OrderNotification,
  'perk-store-orders-filter': OrdersFilter,
  'perk-store-orders-list': OrdersList,
  'perk-store-product-detail-content': ProductDetailContent,
  'perk-store-product-notification': ProductNotification,
  'perk-store-product-search-card': PerkStoreProductSearchCard,
  'perk-store-alert': PerkStoreAlert,
  'perk-store-refund-drawer': RefundDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}