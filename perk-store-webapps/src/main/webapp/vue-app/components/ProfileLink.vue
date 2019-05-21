<template>
  <a
    v-if="id"
    :id="cmpId"
    :title="id"
    :href="url"
    rel="nofollow"
    target="_blank">
    <img
      v-if="displayAvatar"
      :alt="displayName"
      :src="avatar"
      class="profileAvatarCircle">
    {{ displayName }}
  </a>
  <code v-else-if="displayName">{{ displayName }}</code>
  <code v-else>{{ type }}  {{ id }}</code>
</template>

<script>

export default {
  props: {
    id: {
      type: String,
      default: function() {
        return null;
      },
    },
    spaceId: {
      type: String,
      default: function() {
        return null;
      },
    },
    urlId: {
      type: String,
      default: function() {
        return null;
      },
    },
    type: {
      type: String,
      default: function() {
        return null;
      },
    },
    displayName: {
      type: String,
      default: function() {
        return null;
      },
    },
    displayAvatar: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    tiptipPosition: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data() {
    return {
      cmpId: `chip${parseInt(Math.random() * 10000)
        .toString()
        .toString()}`,
      labels: {
        CancelRequest: 'Cancel Request',
        Confirm: 'Confirm',
        Connect: 'Connect',
        Ignore: 'Ignore',
        RemoveConnection: 'Remove Connection',
        StatusTitle: 'Loading...',
        join: 'Join',
        leave: 'Leave',
        members: 'Members',
      },
    };
  },
  computed: {
    avatar() {
      if (this.type === 'space') {
        return `/portal/rest/v1/social/spaces/${this.id}/avatar`;
      } else {
        return `/portal/rest/v1/social/users/${this.id}/avatar`;
      }
    },
    url() {
      if (this.type === 'space') {
        return `${eXo.env.portal.context}/g/:spaces:${this.urlId}/`;
      } else {
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.id}`;
      }
    },
  },
  watch: {
    id(oldValue, newValue) {
      if (this.id) {
        // TODO disable tiptip because of high CPU usage using its code
        this.initTiptip();
      }
    },
  },
  created() {
    if (this.id) {
      // TODO disable tiptip because of high CPU usage using its code
      this.initTiptip();
    }
  },
  methods: {
    initTiptip() {
      if (this.type === 'space') {
        this.$nextTick(() => {
          $(`#${this.cmpId}`).spacePopup({
            userName: eXo.env.portal.userName,
            spaceID: this.spaceId,
            restURL: '/portal/rest/v1/social/spaces/{0}',
            membersRestURL: '/portal/rest/v1/social/spaces/{0}/users?returnSize=true',
            managerRestUrl: '/portal/rest/v1/social/spaces/{0}/users?role=manager&returnSize=true',
            membershipRestUrl: '/portal/rest/v1/social/spacesMemberships?space={0}&returnSize=true',
            defaultAvatarUrl: `/portal/rest/v1/social/spaces/${this.id}/avatar`,
            deleteMembershipRestUrl: '/portal/rest/v1/social/spacesMemberships/{0}:{1}:{2}',
            labels: this.labels,
            content: false,
            keepAlive: true,
            defaultPosition: this.tiptipPosition || 'top_left',
            maxWidth: '240px',
          });
        });
      } else {
        this.$nextTick(() => {
          $(`#${this.cmpId}`).userPopup({
            restURL: '/portal/rest/social/people/getPeopleInfo/{0}.json',
            userId: this.id,
            labels: this.labels,
            content: false,
            keepAlive: true,
            defaultPosition: this.tiptipPosition || 'top_left',
            maxWidth: '240px',
          });
        });
      }
    },
  },
};
</script>
