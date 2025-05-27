<template>
  <div class="chat-container">
    <!-- 左侧会话列表 -->
    <div class="chat-sidebar" :class="{ 'sidebar-collapsed': isMobile && showConversation }">
      <div class="navbar">
        <div class="navbar-title">消息</div>
        <div class="navbar-right">
          <button class="btn btn-primary btn-sm btn-compact" @click="openAddConversation">
            新会话
          </button>
        </div>
      </div>

      <div class="conversation-list scrollable">
        <div v-if="conversations.length === 0" class="empty-state">
          <p>暂无会话</p>
          <button class="btn btn-outline" @click="openAddConversation">
            开始新的对话
          </button>
        </div>

        <div
          v-for="conversation in conversations"
          :key="conversation.id"
          class="conversation-item"
          :class="{ active: currentConversationId === conversation.id }"
          @click="selectConversation(conversation)"
        >
          <div class="avatar-wrapper">
            <img :src="conversation.avatar || defaultAvatar" class="avatar" alt="头像" />
            <span v-if="conversation.unreadCount" class="badge">{{ conversation.unreadCount }}</span>
          </div>
          <div class="conversation-info">
            <div class="conversation-header">
              <h3 class="conversation-title">
                <i :class="['conversation-icon', conversation.conversationType === 1 ? 'el-icon-user-solid' : 'el-icon-user']"></i>
                {{ formatConversationTitle(conversation) }}
              </h3>
              <span class="conversation-time">{{ formatTime(conversation.timestamp) }}</span>
            </div>
            <p class="conversation-message">{{ conversation.lastMessage || '暂无消息' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天内容 -->
    <div class="chat-content" :class="{ 'content-expanded': isMobile && showConversation }">
      <router-view v-if="isLoggedIn" @back="handleBack" @open-new-conversation="openAddConversation"></router-view>
    </div>

    <!-- 添加会话弹窗 -->
    <el-dialog
      v-model="showAddConversation"
      title="新建会话"
      width="80%"
      :before-close="closeAddConversation"
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
            <el-select 
              v-model="selectedFriendId" 
              placeholder="请选择联系人" 
              style="width: 100%"
            >
              <el-option
                v-for="friend in friends"
                :key="friend.id"
                :label="friend.remark || friend.nickname"
                :value="Number(friend.id)"
              ></el-option>
            </el-select>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-text" @click="closeAddConversation">取消</button>
            <button class="btn btn-primary" @click="createPrivateConversation" :disabled="!selectedFriendId">
              开始聊天
            </button>
          </div>
        </div>

        <div v-if="activeTab === 'group'" class="tab-content">
          <div class="form-group">
            <div class="group-actions">
              <button class="btn btn-primary btn-sm btn-compact" @click="showCreateGroup = true">
                <i class="el-icon-plus"></i>
                <span>创建群组</span>
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label>选择群组</label>
            <el-select 
              v-model="selectedGroupId" 
              placeholder="请选择群组" 
              style="width: 100%"
            >
              <el-option
                v-for="group in groups"
                :key="group.id"
                :label="group.name"
                :value="Number(group.id)"
              ></el-option>
            </el-select>
          </div>
          
          <div v-if="groups.length === 0" class="empty-tip">
            <p>您还没有加入任何群组</p>
          </div>
          
          <div class="dialog-footer">
            <button class="btn btn-text" @click="closeAddConversation">取消</button>
            <button class="btn btn-primary" @click="createGroupConversation" :disabled="!selectedGroupId || groups.length === 0">
              加入群聊
            </button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 创建群组弹窗 -->
    <el-dialog
      v-model="showCreateGroup"
      title="创建群组"
      width="80%"
      :before-close="() => showCreateGroup = false"
    >
      <div class="dialog-content">
        <div class="form-group">
          <label>群组名称</label>
          <input
            type="text"
            v-model="createGroupForm.name"
            placeholder="请输入群组名称"
            class="form-input"
          />
        </div>
        <div class="form-group">
          <label>群组描述</label>
          <textarea
            v-model="createGroupForm.description"
            placeholder="请输入群组描述"
            class="form-input"
            rows="3"
          ></textarea>
        </div>
        <div class="form-group">
          <label>邀请好友</label>
          <el-select 
            v-model="createGroupForm.memberIds" 
            multiple
            placeholder="请选择要邀请的好友" 
            style="width: 100%"
          >
            <el-option
              v-for="friend in friends"
              :key="friend.id"
              :label="friend.remark || friend.nickname"
              :value="Number(friend.id)"
            ></el-option>
          </el-select>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-text" @click="showCreateGroup = false">取消</button>
          <button class="btn btn-primary" @click="handleCreateGroup" :disabled="!createGroupForm.name">
            创建群组
          </button>
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
    const selectedFriendId = ref(null);
    const selectedGroupId = ref(null);
    const showCreateGroup = ref(false);
    const createGroupForm = ref({
      name: '',
      description: '',
      memberIds: []
    });

    // 从store获取数据
    const isLoggedIn = computed(() => store.state.user.isLoggedIn);
    const conversations = computed(() => store.state.conversation.conversations);
    const currentConversationId = computed(() => store.state.conversation.currentConversationId);
    const friends = computed(() => store.state.friendship.friends || []);
    const groups = computed(() => store.state.group.groups || []);
    
    // 格式化会话标题
    const formatConversationTitle = (conversation) => {
      if (!conversation) return '';
      
      // 根据会话类型显示不同的标题
      if (conversation.conversationType === 1) {
        // 群聊 - 尝试从群组列表中获取更详细的信息
        const group = groups.value.find(g => Number(g.id) === Number(conversation.targetId));
        if (group) {
          return `${group.name || conversation.title || '未命名群聊'} (群聊)`;
        }
        return `${conversation.title || '未命名群聊'} (群聊)`;
      } else {
        // 私聊 - 尝试从好友列表中获取更详细的信息
        const friend = friends.value.find(f => Number(f.id) === Number(conversation.targetId));
        console.log('找到的好友信息:', friend);
        if (friend) {
          // 检查好友对象是否包含remark字段
          console.log('好友备注名:', friend.remark);
          // 优先显示备注名，如果没有备注名再显示昵称
          return `${friend.remark || friend.nickname || conversation.title || '未知联系人'} (私聊)`;
        }
        return `${conversation.title || '未知联系人'} (私聊)`;
      }
    };

    // 打开新建会话弹窗
    const openAddConversation = () => {
      showAddConversation.value = true;
    };
    
    // 关闭新建会话弹窗
    const closeAddConversation = () => {
      showAddConversation.value = false;
    };

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

    // 监听选中的好友ID变化
    watch(
      () => selectedFriendId.value,
      (newVal) => {
        if (newVal) {
          const selectedFriend = friends.value.find(f => Number(f.id) === Number(newVal));
        }
      }
    );
    
    // 监听选中的群组ID变化
    watch(
      () => selectedGroupId.value,
      (newVal) => {
        if (newVal) {
          const selectedGroup = groups.value.find(g => Number(g.id) === Number(newVal));
        }
      }
    );

    // 监听窗口大小变化
    const handleResize = () => {
      isMobile.value = window.innerWidth < 768;
    };

    onMounted(() => {
      window.addEventListener('resize', handleResize);
      
      // 获取会话列表和群组列表
      if (isLoggedIn.value) {
        store.dispatch('conversation/getConversations');
        store.dispatch('group/getGroups');
        store.dispatch('friendship/getFriendsWithDetails');
      }
    });

    // 选择会话
    const selectConversation = (conversation) => {
      store.dispatch('conversation/setCurrentConversation', conversation);
      router.push(`/chat/conversation/${conversation.id}`);
      console.log(conversation);
    };

    // 创建私聊会话
    const createPrivateConversation = async () => {
      if (!selectedFriendId.value) {
        ElMessage.warning('请选择联系人');
        return;
      }

      // 确保 targetUserId 是有效的数字
      const targetUserId = parseInt(selectedFriendId.value, 10);
      if (isNaN(targetUserId)) {
        ElMessage.warning('无效的联系人ID');
        return;
      }

      try {
        const conversation = await store.dispatch(
          'conversation/createPrivateConversation',
          { targetUserId }
        );
        closeAddConversation();
        selectedFriendId.value = null;
        router.push(`/chat/conversation/${conversation.id}`);
      } catch (error) {
        ElMessage.error(`创建会话失败: ${error.message || '未知错误'}`);
      }
    };

    // 创建群聊会话
    const createGroupConversation = async () => {
      if (!selectedGroupId.value) {
        ElMessage.warning('请选择群组');
        return;
      }

      // 确保 groupId 是有效的数字
      const groupId = parseInt(selectedGroupId.value, 10);
      if (isNaN(groupId)) {
        ElMessage.warning('无效的群组ID');
        return;
      }

      try {
        const conversation = await store.dispatch(
          'conversation/createGroupConversation',
          { groupId }
        );
        closeAddConversation();
        selectedGroupId.value = null;
        router.push(`/chat/conversation/${conversation.id}`);
      } catch (error) {
        ElMessage.error(`创建会话失败: ${error.message || '未知错误'}`);
      }
    };

    // 返回会话列表
    const handleBack = () => {
      showConversation.value = false;
      router.push('/chat');
    };

    // 创建群组
    const handleCreateGroup = async () => {
      if (!createGroupForm.value.name) {
        ElMessage.warning('请输入群组名称');
        return;
      }
      
      try {
        // 创建群组
        const groupData = {
          name: createGroupForm.value.name,
          description: createGroupForm.value.description || ''
        };
        
        const group = await store.dispatch('group/createGroup', groupData);
        
        // 如果选择了要邀请的好友，添加群成员
        if (createGroupForm.value.memberIds.length > 0) {
          await store.dispatch('group/addGroupMembers', {
            groupId: group.id,
            userIds: createGroupForm.value.memberIds
          });
        }
        
        ElMessage.success('群组创建成功');
        
        // 重置表单
        createGroupForm.value = {
          name: '',
          description: '',
          memberIds: []
        };
        
        // 关闭创建群组对话框
        showCreateGroup.value = false;
        
        // 自动选择新创建的群组
        selectedGroupId.value = group.id;
        
        // 刷新群组列表
        await store.dispatch('group/getGroups');
      } catch (error) {
        console.error('创建群组失败:', error);
        ElMessage.error(`创建群组失败: ${error.message || '未知错误'}`);
      }
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
      formatTime,
      openAddConversation,
      closeAddConversation,
      formatConversationTitle,
      showCreateGroup,
      createGroupForm,
      handleCreateGroup
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
  padding-bottom: 55px; /* 为底部导航栏留出空间 */
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
    display: flex;
    align-items: center;
    
    .conversation-icon {
      margin-right: $spacing-2;
      font-size: $font-size-base;
      color: $text-secondary;
    }
    
    .conversation-type {
      font-size: $font-size-xs;
      font-weight: normal;
      color: $text-muted;
      margin-left: $spacing-1;
    }
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

.empty-tip {
  text-align: center;
  padding: $spacing-4;
  
  p {
    color: $text-secondary;
    font-size: $font-size-sm;
    margin: 0;
  }
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
}

.group-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: $spacing-4;
}

.form-input {
  width: 100%;
  padding: $spacing-3 $spacing-4;
  border-radius: $border-radius;
  border: 1px solid $border-color;
  font-size: $font-size-base;
  
  &:focus {
    border-color: $primary-color;
    box-shadow: 0 0 0 2px rgba($primary-color, 0.1);
  }
}
</style> 