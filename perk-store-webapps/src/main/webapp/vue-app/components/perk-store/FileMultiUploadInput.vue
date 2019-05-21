<template>
  <div class="fileDrop mt-4">
    <label
      v-show="files && files.length < maxFiles"
      class="dropZone clickable"
      for="perk-store-attach-file">
      <span class="dropMsg">
        <i class="uiIcon attachFileIcon"></i> Drop files
      </span>
      <input
        id="perk-store-attach-file"
        dropzone=""
        type="file"
        class="attachFile hidden"
        accept=".gif,.jpg,.jpeg,.png"
        multiple>
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
          <v-list-tile
            :key="fileDetail.uploadId || fileDetail.id"
            avatar
            ripple>
            <v-list-tile-avatar>
              <img :src="fileDetail.src">
            </v-list-tile-avatar>
            <v-list-tile-content>
              <v-list-tile-title :title="fileDetail.name">{{ fileDetail.name }}</v-list-tile-title>
              <v-list-tile-sub-title v-if="fileDetail.file && (fileDetail.progress < 100 || !fileDetail.finished)">
                <v-progress-linear
                  v-model="fileDetail.progress"
                  :indeterminate="!fileDetail.finished && fileDetail.progress <= 1"
                  query />
              </v-list-tile-sub-title>
              <v-list-tile-sub-title v-else>{{ getFileSize(fileDetail.size) }}</v-list-tile-sub-title>
            </v-list-tile-content>
            <v-list-tile-action>
              <v-list-tile-action-text class="pb-3">
                <v-btn
                  v-if="fileDetail.progress === 100"
                  title="Delete"
                  icon
                  ripple
                  @click="deleteFile(fileDetail.file || fileDetail.id)">
                  <i class="uiIconTrash uiIconBlue"></i>
                </v-btn>
                <v-btn
                  v-else
                  title="Cancel"
                  icon
                  ripple
                  @click="abortUpload(fileDetail.file || fileDetail.id)">
                  <i class="uiIconTrash uiIconBlue"></i>
                </v-btn>
              </v-list-tile-action-text>
            </v-list-tile-action>
          </v-list-tile>
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
          thiss.error = `Error processing files ${file && file.name || ''}: max files reached (${thiss.maxFiles})`;
          break;
        case 'ErrorFileTooLarge':
        case 'FileTooLarge':
          thiss.error = `Error processing file ${file && file.name || ''}: size exceeds maximum allowed size ${thiss.maxUploadsSizeInMb} MB`;
          break;
        case 'ErrorFileTypeNotAllowed':
        case 'FileTypeNotAllowed':
          thiss.error = `Error processing file ${file && file.name || ''}: File type not allowed`;
          break;
        }
        if (file) {
          thiss.deleteFile(file);
        }
        if(!thiss.error) {
          thiss.error = `Error processing file ${file && file.name || ''}: this could happen when max files reached (${thiss.maxFiles})`;
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
          throw new Error(`Error processing files ${file && file.name || ''}: max files reached (${thiss.maxFiles})`);
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
        }
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
      uploadFinished: (i, file, response, time) => {
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
      if(idx < 0) {
        return;
      }
      const fileDetails = this.files[idx];
      if (fileDetails.uploadId) {
        fetch(`${eXo.env.portal.context}/upload?uploadId=${fileDetails.uploadId}&action=${fileDetails.progress && fileDetails.progress < 100 ? 'abort' : 'delete'}`, {
          method: 'post',
          credentials: 'include'
        }).then(() => {
          this.files.splice(idx, 1);
          this.$emit('changed');
        })
        .catch((e) => {
          this.error = e;
        });
      } else {
        this.files.splice(idx, 1);
        this.$emit('changed');
      }
    },
    getFileSize(size) {
      const kilobyte = 1024;
      const hundred = 100;
      const fixed = 2;
      if (size < kilobyte * kilobyte / hundred) {
        size = Number(size / kilobyte).toFixed(fixed);
        return `${size} KB`;
      } else {
        size = Number(size / (kilobyte * kilobyte)).toFixed(fixed);
        return `${size} MB`;
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