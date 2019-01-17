<template>
  <v-snackbar
    v-model="snackbar"
    :timeout="10000"
    class="orderNotificationParent"
    color="black">
    <v-card
      flat
      dark
      class="transparent">
      <template v-for="order in orders">
        <v-card-text
          :key="order.id"
          class="ellipsis orderNotificationContent"
          dark>
          New order #{{ order.id }} from {{ order.sender.displayName }}
        </v-card-text>
        <v-divider
          v-if="displayDivider"
          :key="order.id"
          dark />
      </template>
      <v-card-actions>
        <v-spacer />
        <v-btn
          dark
          flat
          title="Refresh"
          @click="refreshList">
          Refresh
        </v-btn>
        <v-spacer />
      </v-card-actions>
    </v-card>
    <v-btn
      dark
      flat
      icon
      class="ml-0"
      @click="close">
      <v-icon dark>close</v-icon>
    </v-btn>
  </v-snackbar>
</template>
<script>
export default {
  props: {
    orders: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data () {
    return {
      snackbar: false,
    }
  },
  computed: {
    displayDivider() {
      return this.orders && this.orders.length > 1;
    },
  },
  watch: {
    snackbar() {
      if(!this.snackbar) {
        this.orders.splice(0, this.orders.length);
      }
    },
    orders() {
      this.snackbar = this.orders && this.orders.length;
    },
  },
  methods: {
    refreshList() {
      this.$emit('refresh-list');
    },
    close() {
      this.orders.splice(0, this.orders.length);
    }
  },
}
</script>
