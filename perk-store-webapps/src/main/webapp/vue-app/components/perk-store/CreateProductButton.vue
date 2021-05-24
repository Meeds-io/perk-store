<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <div
    class="border-box-sizing clickable addProduct">
    <button class="btn ignore-vuetify-classes me-1" @click="$emit('create-product')">
      <i class="orderDetailUiIcons fas fa-plus pr-1"></i>
      {{ $t('exoplatform.perkstore.button.addProduct') }}
    </button>
    <span class="textBalance">
      {{ $t('exoplatform.perkstore.label.balance') }}: {{ symbol }} {{ balance }}
    </span>
  </div>
</template>
<script>

export default {
  props: {
    symbol: {
      type: Object,
      default: function () {
        return null;
      },
    },
    balance: {
      type: Object,
      default: function () {
        return null;
      },
    }
  }
};
</script>