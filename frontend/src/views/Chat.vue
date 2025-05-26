<template>
  <div class="chat-container">
    <!-- 左侧会话列表 -->
    <div class="chat-sidebar" :class="{ 'sidebar-collapsed': isMobile && showConversation }">
      <div class="navbar">
        <div class="navbar-title">消息</div>
        <div class="navbar-right">
          <button class="btn-icon" @click="showAddConversation = true">
            <i class="el-icon-plus"></i>
          </button>
        </div>
      </div>

      <div class="conversation-list scrollable">
        <div v-if="conversations.length === 0" class="empty-state">
          <p>暂无会话</p>
          <button class="btn btn-outline" @click="showAddConversation = true">
            开始新的对话
          </button>
        </div>

        <div
          v-for="conversation in conversations"
          :key="conversation.conversationId"
          class="conversation-item"
          :class="{ active: currentConversationId === conversation.conversationId }"
          @click="selectConversation(conversation)"
        >
          <div class="avatar-wrapper">
            <img :src="conversation.avatar || defaultAvatar" class="avatar" alt="头像" />
            <span v-if="conversation.unreadCount" class="badge">{{ conversation.unreadCount }}</span>
          </div>
          <div class="conversation-info">
            <div class="conversation-header">
              <h3 class="conversation-title">{{ conversation.title }}</h3>
              <span class="conversation-time">{{ formatTime(conversation.timestamp) }}</span>
            </div>
            <p class="conversation-message">{{ conversation.lastMessage || '暂无消息' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天内容 -->
    <div class="chat-content" :class="{ 'content-expanded': isMobile && showConversation }">
      <router-view v-if="isLoggedIn" @back="handleBack"></router-view>
    </div>

    <!-- 添加会话弹窗 -->
    <el-dialog
      v-model="showAddConversation"
      title="新建会话"
      width="80%"
      :before-close="() => showAddConversation = false"
    >
      <div class="dialog-content">
        <div class="tabs">
          <div
            class="tab"
            :class="{ active: activeTab === 'private' }"
            @click="activeTab = 'private'"
          >
            私聊
          </div>
          <div
            class="tab"
            :class="{ active: activeTab === 'group' }"
            @click="activeTab = 'group'"
          >
            群聊
          </div>
        </div>

        <div v-if="activeTab === 'private'" class="tab-content">
          <div class="form-group">
            <label>选择联系人</label>
            <el-select v-model="selectedFriendId" placeholder="请选择联系人" style="width: 100%">
              <el-option
                v-for="friend in friends"
                :key="friend.friendId"
                :label="friend.nickname"
                :value="friend.friendId"
              ></el-option>
            </el-select>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-text" @click="showAddConversation = false">取消</button>
            <button class="btn btn-primary" @click="createPrivateConversation" :disabled="!selectedFriendId">
              开始聊天
            </button>
          </div>
        </div>

        <div v-if="activeTab === 'group'" class="tab-content">
          <div class="form-group">
            <label>选择群组</label>
            <el-select v-model="selectedGroupId" placeholder="请选择群组" style="width: 100%">
              <el-option
                v-for="group in groups"
                :key="group.id"
                :label="group.name"
                :value="group.id"
              ></el-option>
            </el-select>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-text" @click="showAddConversation = false">取消</button>
            <button class="btn btn-primary" @click="createGroupConversation" :disabled="!selectedGroupId">
              加入群聊
            </button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 底部导航栏 -->
    <div class="tabbar">
      <router-link to="/chat" class="tabbar-item" :class="{ active: $route.path.startsWith('/chat') }">
        <i class="el-icon-chat-dot-round"></i>
        <span>消息</span>
      </router-link>
      <router-link to="/contacts" class="tabbar-item" :class="{ active: $route.path.startsWith('/contacts') }">
        <i class="el-icon-user"></i>
        <span>联系人</span>
      </router-link>
      <router-link to="/profile" class="tabbar-item" :class="{ active: $route.path.startsWith('/profile') }">
        <i class="el-icon-setting"></i>
        <span>我的</span>
      </router-link>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue';
import { useStore } from 'vuex';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import defaultAvatar from '@/assets/default-avatar.png';
import { formatTime } from '@/utils/format';

export default {
  name: 'Chat',
  setup() {
    const store = useStore();
    const router = useRouter();
    const route = useRoute();
    
    const isMobile = ref(window.innerWidth < 768);
    const showConversation = ref(false);
    const showAddConversation = ref(false);
    const activeTab = ref('private');
    const selectedFriendId = ref('');
    const selectedGroupId = ref('');

    // 从store获取数据
    const isLoggedIn = computed(() => store.state.user.isLoggedIn);
    const conversations = computed(() => store.state.conversation.conversations);
    const currentConversationId = computed(() => store.state.conversation.currentConversationId);
    const friends = computed(() => store.state.friendship.friends);
    
    // 模拟群组数据，实际应该从API获取
    const groups = ref([
      { id: 1, name: '班级群' },
      { id: 2, name: '学习小组' },
      { id: 3, name: '兴趣社团' }
    ]);

    // 监听路由变化
    watch(
      () => route.path,
      (newPath) => {
        if (newPath.includes('/conversation/')) {
          showConversation.value = true;
        } else {
          showConversation.value = false;
        }
      },
      { immediate: true }
    );

    // 监听窗口大小变化
    const handleResize = () => {
      isMobile.value = window.innerWidth < 768;
    };

    onMounted(() => {
      window.addEventListener('resize', handleResize);
      
      // 获取会话列表
      if (isLoggedIn.value) {
        store.dispatch('conversation/getConversations');
      }
    });

    // 选择会话
    const selectConversation = (conversation) => {
      store.dispatch('conversation/setCurrentConversation', conversation);
      router.push(`/chat/conversation/${conversation.conversationId}`);
    };

    // 创建私聊会话
    const createPrivateConversation = async () => {
      if (!selectedFriendId.value) {
        ElMessage.warning('请选择联系人');
        return;
      }

      try {
        const conversation = await store.dispatch(
          'conversation/createPrivateConversation',
          selectedFriendId.value
        );
        showAddConversation.value = false;
        selectedFriendId.value = '';
        router.push(`/chat/conversation/${conversation.conversationId}`);
      } catch (error) {
        ElMessage.error('创建会话失败');
      }
    };

    // 创建群聊会话
    const createGroupConversation = async () => {
      if (!selectedGroupId.value) {
        ElMessage.warning('请选择群组');
        return;
      }

      try {
        const conversation = await store.dispatch(
          'conversation/createGroupConversation',
          selectedGroupId.value
        );
        showAddConversation.value = false;
        selectedGroupId.value = '';
        router.push(`/chat/conversation/${conversation.conversationId}`);
      } catch (error) {
        ElMessage.error('创建会话失败');
      }
    };

    // 返回会话列表
    const handleBack = () => {
      showConversation.value = false;
      router.push('/chat');
    };

    return {
      isLoggedIn,
      conversations,
      currentConversationId,
      isMobile,
      showConversation,
      showAddConversation,
      activeTab,
      selectedFriendId,
      selectedGroupId,
      friends,
      groups,
      defaultAvatar,
      selectConversation,
      createPrivateConversation,
      createGroupConversation,
      handleBack,
      formatTime
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.chat-container {
  display: flex;
  height: 100vh;
  position: relative;
}

.chat-sidebar {
  width: 320px;
  border-right: 1px solid $border-color;
  display: flex;
  flex-direction: column;
  background-color: $white;
  
  @media (max-width: 767px) {
    width: 100%;
    position: absolute;
    left: 0;
    top: 0;
    bottom: 50px;
    z-index: 10;
    transition: transform 0.3s ease;
    
    &.sidebar-collapsed {
      transform: translateX(-100%);
    }
  }
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  
  @media (max-width: 767px) {
    position: absolute;
    left: 0;
    top: 0;
    right: 0;
    bottom: 50px;
    z-index: 5;
    transform: translateX(100%);
    transition: transform 0.3s ease;
    
    &.content-expanded {
      transform: translateX(0);
    }
  }
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: $spacing-4;
  border-bottom: 1px solid $border-color;
  cursor: pointer;
  transition: $transition-base;
  
  &:hover {
    background-color: $gray-100;
  }
  
  &.active {
    background-color: rgba($primary-color, 0.1);
  }
  
  .avatar-wrapper {
    position: relative;
    margin-right: $spacing-4;
    
    .badge {
      position: absolute;
      top: -5px;
      right: -5px;
    }
  }
  
  .conversation-info {
    flex: 1;
    min-width: 0;
  }
  
  .conversation-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-1;
  }
  
  .conversation-title {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    margin: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .conversation-time {
    font-size: $font-size-xs;
    color: $text-muted;
    white-space: nowrap;
  }
  
  .conversation-message {
    font-size: $font-size-sm;
    color: $text-secondary;
    margin: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: $spacing-6;
  text-align: center;
  
  p {
    margin-bottom: $spacing-4;
    color: $text-secondary;
  }
}

.btn-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: $primary-color;
  
  &:hover {
    background-color: $gray-100;
  }
}

.tabs {
  display: flex;
  border-bottom: 1px solid $border-color;
  margin-bottom: $spacing-6;
  
  .tab {
    padding: $spacing-3 $spacing-6;
    cursor: pointer;
    font-weight: $font-weight-medium;
    color: $text-secondary;
    position: relative;
    
    &.active {
      color: $primary-color;
      
      &::after {
        content: '';
        position: absolute;
        bottom: -1px;
        left: 0;
        right: 0;
        height: 2px;
        background-color: $primary-color;
      }
    }
  }
}

.tab-content {
  padding: $spacing-2 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-6;
  
  .btn {
    margin-left: $spacing-4;
  }
}

.tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  
  @media (min-width: 768px) {
    display: none;
  }
}
</style> 