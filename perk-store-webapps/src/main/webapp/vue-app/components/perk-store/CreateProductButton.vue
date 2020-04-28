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
  <v-flex class="productDetailContent border-box-sizing clickable" @click="$emit('create-product')">
    <v-hover>
      <v-layout
        slot-scope="{ hover }"
        :class="`elevation-${hover ? 9 : 3} productDetailContentCard text-center`"
        column>
        <v-flex>
          <div class="addProductButton mt-4">
            <v-icon size="182" class="addProductIcon">mdi-plus</v-icon>
          </div>
        </v-flex>
        <v-flex class="addProductTitle headline productCardTitle">
          {{ $t('exoplatform.perkstore.title.createNewProduct') }}
        </v-flex>
        <v-flex class="wrap addProductDescription productCardSubtitle">
          {{ $t('exoplatform.perkstore.title.createNewProductDescription') }}
        </v-flex>
      </v-layout>
    </v-hover>
  </v-flex>
</template>