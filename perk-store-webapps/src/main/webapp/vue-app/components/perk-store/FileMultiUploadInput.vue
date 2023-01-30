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
  <div class="fileDrop mt-4">
    <label
      v-show="files && files.length < maxFiles"
      class="dropZone clickable"
      for="perk-store-attach-file">
      <span class="dropMsg">
        <i class="uiIcon attachFileIcon"></i> {{ $t('exoplatform.perkstore.label.dropFilesAreaLabel') }}
      </span>
      <input
        id="perk-store-attach-file"
        ref="perkStoreAttachFile"
        dropzone=""
        type="file"
        class="attachFile hidden"
        accept=".gif,.jpg,.jpeg,.png"
        multiple>*
    </label>
    <div class="uploadContainer">
      <div v-if="error" class="alert alert-error v-content">
        <i class="uiIconError"></i>{{ error }}
      </div>
      <v-list
        two-line
        dense
        class="pt-0">
        <template v-for="fileDetail in files">
          <v-list-item
            :key="fileDetail.uploadId || fileDetail.id"
            ripple>
            <v-list-item-avatar>
              <img :src="fileDetail.src">
            </v-list-item-avatar>
            <v-list-item-content>
              <v-list-item-title :title="fileDetail.name">{{ fileDetail.name }}</v-list-item-title>
              <v-list-item-subtitle v-if="fileDetail.file && (fileDetail.progress < 100 || !fileDetail.finished)">
                <v-progress-linear
                  v-model="fileDetail.progress"
                  :indeterminate="!fileDetail.finished && fileDetail.progress <= 1"
                  query />
              </v-list-item-subtitle>
              <v-list-item-subtitle v-else>{{ getFileSize(fileDetail.size) }}</v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-list-item-action-text class="pb-3">
                <v-btn
                  v-if="fileDetail.progress === 100"
                  :title="$t('exoplatform.perkstore.button.delete')"
                  icon
                  ripple
                  @click="deleteFile(fileDetail.file || fileDetail.id)">
                  <i class="uiIconTrash uiIconBlue"></i>
                </v-btn>
                <v-btn
                  v-else
                  :title="$t('exoplatform.perkstore.button.cancel')"
                  icon
                  ripple
                  @click="abortUpload(fileDetail.file || fileDetail.id)">
                  <i class="uiIconTrash uiIconBlue"></i>
                </v-btn>
              </v-list-item-action-text>
            </v-list-item-action>
          </v-list-item>
          <v-divider :key="fileDetail.uploadId || fileDetail.id" />
        </template>
      </v-list>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    maxFiles: {
      type: Number,
      default: function() {
        return 0;
      },
    },
    maxUploadsSizeInMb: {
      type: Number,
      default: function() {
        return 0;
      },
    },
    files: {
      type: Array,
      default: () => null
    }
  },
  data() {
    return {
      error: ''
    };
  },
  watch: {
    error() {
      if (this.error) {
        setTimeout(() => this.error = null, 5000);
      }
    },
  },
  mounted() {
    const MAX_RANDOM_NUMBER = 100000;
    const $dropzoneContainer = $(this.$el).find('.dropZone');
    const thiss = this;

    let uploadId = '';
    $dropzoneContainer.filedrop({
      fallback_id: 'perk-store-attach-file',  // an identifier of a standard file input element
      url: function() {
        const random = Math.round(Math.random() * MAX_RANDOM_NUMBER);
        const now = Date.now();
        uploadId = `${random}-${now}`;

        return `${eXo.env.portal.context}/upload?uploadId=${uploadId}&action=upload`;
      },
      paramname: 'userfile',
      error: function(err, file) {
        thiss.error = null;
        switch (err) {
        case 'ErrorTooManyFiles':
        case 'TooManyFiles':
          thiss.error = thiss.$t('exoplatform.perkstore.error.uploadMaxFileCountReached', {0: file && file.name || '', 1: thiss.maxFiles});
          break;
        case 'ErrorFileTooLarge':
        case 'FileTooLarge':
          thiss.error = thiss.$t('exoplatform.perkstore.error.uploadMaxFileSizeExceeded', {0: file && file.name || '', 1: thiss.maxUploadsSizeInMb});
          break;
        case 'ErrorFileTypeNotAllowed':
        case 'FileTypeNotAllowed':
          thiss.error = thiss.$t('exoplatform.perkstore.error.uploadUnsupportedFileType', {0: file && file.name || ''});
          break;
        }
        if (file) {
          thiss.deleteFile(file);
        }
        if (!thiss.error) {
          thiss.error = thiss.$t('exoplatform.perkstore.error.uploadGenericError', {0: file && file.name || '', 1: thiss.maxFiles});
        }
      },
      allowedfiletypes: ['image/jpeg','image/png','image/gif'],
      allowedfileextensions: ['.jpg','.jpeg','.png','.gif'],
      maxfilesize: thiss.maxUploadsSizeInMb,
      queuefiles: 1,
      rename: thiss.getFileName,
      beforeEach: (file) => {
        const remainingMaxFiles = thiss.maxFiles - thiss.files.length;
        if (remainingMaxFiles <= 0) {
          throw new Error(thiss.$t('exoplatform.perkstore.error.uploadMaxFileCountReached', {0: file && file.name || '', 1: thiss.maxFiles}));
        }
      },
      dragOver: function() {
        $('.dropZone').addClass('hover');
      },
      dragLeave: function() {
        $('.dropZone').removeClass('hover');
      },
      uploadStarted: (i, file) => {
        // Reinit error when upload action started
        if (i === 0) {
          thiss.error = null;
        }
        const fileDetails = {
          id: null,
          uploadId: uploadId,
          name: file.name,
          size: file.size,
          src: null,
          progress: 0,
          file: file,
          finished: false,
        };

        // Add avatar
        const reader = new FileReader();
        reader.onload = (e) => {
          const data = e.target.result;
          thiss.$set(fileDetails, 'src', data);
          thiss.$set(fileDetails, 'data', data);
          thiss.$forceUpdate();
        };
        reader.readAsDataURL(file);

        // Add file to list
        thiss.files.push(fileDetails);
      },
      progressUpdated: (i, file, progress) => {
        const fileDetail = thiss.files.find(f => f.file === file);
        if (fileDetail) {
          fileDetail.progress = progress;
        }
      },
      uploadFinished: (i, file) => {
        thiss.$emit('changed');
        const fileDetailToChange = thiss.files.find(f => f.file === file);
        setTimeout(() => {
          thiss.$set(fileDetailToChange, 'finished', true);
        }, 500);
      },
    });
  },
  methods: {
    abortUpload(fileOrId) {
      this.deleteFile(fileOrId);
    },
    deleteFile(fileOrId) {
      const idx = this.files.findIndex(f => f.file === fileOrId || f.id === fileOrId);
      if (idx < 0) {
        return;
      }
      const fileDetails = this.files[idx];
      if (fileDetails.uploadId) {
        fetch(`${eXo.env.portal.context}/upload?uploadId=${fileDetails.uploadId}&action=${fileDetails.progress && fileDetails.progress < 100 ? 'abort' : 'delete'}`, {
          method: 'post',
          credentials: 'include'
        }).then(() => {
          this.files.splice(idx, 1);
          const filesList = new DataTransfer();
          Object.values(this.$refs.perkStoreAttachFile.files).forEach(file => {
            if (file !== fileOrId) {
              filesList.items.add(file);
            }
          });
          this.$refs.perkStoreAttachFile.files = filesList.files;
          this.$emit('changed');
        })
          .catch((e) => {
            this.error = e;
          });
      } else {
        this.files.splice(idx, 1);
        const filesList = new DataTransfer();
        this.$refs.perkStoreAttachFile.files.forEach(file => {
          if (file !== fileOrId) {
            filesList.files.push(file) ;
          }
        });
        this.$refs.perkStoreAttachFile.files = filesList.files;
        this.$emit('changed');
      }
    },
    getFileSize(size) {
      const kilobyte = 1024;
      const hundred = 100;
      const fixed = 2;
      if (size < kilobyte * kilobyte / hundred) {
        size = Number(size / kilobyte).toFixed(fixed);
        return this.$t('exoplatform.perkstore.label.sizeInKB', {0: size});
      } else {
        size = Number(size / (kilobyte * kilobyte)).toFixed(fixed);
        return this.$t('exoplatform.perkstore.label.sizeInMB', {0: size});
      }
    },
    getFileName(name) {
      // eslint-disable-next-line no-loop-func
      while (this.files.find(file => file.name === name)) {
        name = `${name}_`;
      }
      return name;
    }
  }
};
</script>