import AutoComplete from './components/AutoComplete.vue';
import ProfileLink from './components/ProfileLink.vue';
import BuyForm from './components/perk-store/BuyForm.vue';
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
import ProductDetailMaximized from './components/perk-store/ProductDetailMaximized.vue';
import ProductNotification from './components/perk-store/ProductNotification.vue';
import RefundModal from './components/perk-store/RefundModal.vue';
import SettingsModal from './components/perk-store/SettingsModal.vue';
import PerkstoreOrderApp from './components/perk-store-order/PerkstoreOrderApp.vue';
import PerkStoreProductSearchCard from './components/perk-store-search/PerkStoreProductSearchCard.vue';


const components = {
  'perk-store-auto-complete': AutoComplete,
  'perk-store-profile-link': ProfileLink,
  'perk-store-buy-form': BuyForm,
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
  'perk-store-product-detail-maximized': ProductDetailMaximized,
  'perk-store-product-notification': ProductNotification,
  'perk-store-refund-modal': RefundModal,
  'perk-store-settings-modal': SettingsModal,
  'perk-store-order-app': PerkstoreOrderApp,
  'perk-store-product-search-card': PerkStoreProductSearchCard,
};

for (const key in components) {
  Vue.component(key, components[key]);
}