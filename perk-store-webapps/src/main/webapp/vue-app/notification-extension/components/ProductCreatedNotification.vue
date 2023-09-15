<template>
  <user-notification-template
    :notification="notification"
    :message="message"
    :url="productUrl">
    <template #avatar>
      <v-icon size="32">fa-store-alt</v-icon>
    </template>
    <template #actions>
      <div class="text-truncate">
        <v-icon size="14" class="me-1">fa-shopping-cart</v-icon>
        {{ productTitle }}
      </div>
    </template>
  </user-notification-template>
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
  computed: {
    productTitle() {
      return this.notification?.parameters?.PRODUCT_TITLE;
    },
    productId() {
      return this.notification?.parameters?.PRODUCT_ID;
    },
    productUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/catalog/${this.productId}`;
    },
    message() {
      return this.$t('UINotification.label.ProductAddedNotificationPlugin');
    },
  },
};
</script>