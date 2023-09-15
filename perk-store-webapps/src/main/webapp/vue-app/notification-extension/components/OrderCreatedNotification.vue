<template>
  <user-notification-template
    :notification="notification"
    :avatar-url="profileAvatarUrl"
    :message="message"
    :url="orderUrl">
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
    orderId() {
      return this.notification?.parameters?.ORDER_ID;
    },
    orderUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore?productId=${this.productId}&orderId=${this.orderId}`;
    },
    profile() {
      return this.notification?.from;
    },
    profileAvatarUrl() {
      return this.profile?.avatar;
    },
    message() {
      return this.$t('Notification.perkstore.order.added', {
        0: `<a class="user-name font-weight-bold">${this.profile?.fullname}</a>`,
      });
    },
  },
};
</script>