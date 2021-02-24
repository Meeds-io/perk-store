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
        CancelRequest: this.$t('exoplatform.perkstore.label.profile.CancelRequest'),
        Confirm: this.$t('exoplatform.perkstore.label.profile.Confirm'),
        Connect: this.$t('exoplatform.perkstore.label.profile.Connect'),
        Ignore: this.$t('exoplatform.perkstore.label.profile.Ignore'),
        RemoveConnection: this.$t('exoplatform.perkstore.label.profile.RemoveConnection'),
        StatusTitle: this.$t('exoplatform.perkstore.label.profile.StatusTitle'),
        join: this.$t('exoplatform.perkstore.label.profile.join'),
        leave: this.$t('exoplatform.perkstore.label.profile.leave'),
        members: this.$t('exoplatform.perkstore.label.profile.members'),
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
    id() {
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
