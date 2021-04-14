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
  <v-layout
    v-if="hasImages"
    class="no-wrap">
    <v-flex v-if="displaySelector" xs2>
      <v-layout
        row
        wrap
        class="imageMiniPreviewParent mx-0">
        <v-flex
          v-for="(image, i) in images"
          :key="image.id"
          class="clickable"
          @click="selectedImageIndex = i">
          <v-card class="imageMiniPreview">
            <v-img
              :src="image.src"
              aspect-ratio="1"
              max-height="70px"
              max-width="70px" />
          </v-card>
        </v-flex>
      </v-layout>
    </v-flex>
    <v-flex class="productImageContainer" :class="imageClass">
      <v-img
        :src="selectedImage.src"
        max-height="100%"
        max-width="100%"
        width="100%"
        class="productImage"
        contain />
    </v-flex>
  </v-layout>
  <div v-else class="productNoImageContainer">
    <v-icon class="productNoImages">fa-images</v-icon>
  </div>
</template>
<script>
export default {
  props: {
    images: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data() {
    return {
      selectedImageIndex: 0
    };
  },
  computed: {
    hasImages() {
      return this.images && this.images.length;
    },
    imageClass() {
      return this.displaySelector ? 'xs10' : 'standaloneImage xs12';
    },
    displaySelector() {
      return this.images && this.images.length > 1;
    },
    selectedImage() {
      if (this.images && this.images.length) {
        return this.images[this.selectedImageIndex];
      }
      return null;
    },
  },
};
</script>