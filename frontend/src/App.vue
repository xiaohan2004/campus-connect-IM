<template>
  <router-view />
</template>

<script>
import { onMounted } from 'vue';
import { useStore } from 'vuex';

export default {
  name: 'App',
  setup() {
    const store = useStore();

    onMounted(() => {
      // 如果已登录，获取用户信息
      if (store.state.user.isLoggedIn) {
        store.dispatch('user/getUserInfo');
        // 获取会话列表
        store.dispatch('conversation/getConversations');
        // 获取好友列表
        store.dispatch('friendship/getFriends');
        // 获取未读消息数
        store.dispatch('message/getUnreadMessageCount');
        // 获取离线消息
        store.dispatch('message/getOfflineMessages').then(messages => {
          if (messages && messages.length > 0) {
            // 确认接收离线消息
            const messageIds = messages.map(msg => msg.messageId);
            store.dispatch('message/confirmOfflineMessages', messageIds);
          }
        });
      }
    });

    return {};
  }
};
</script>

<style lang="scss">
@import '@/assets/styles/global.scss';
</style> 