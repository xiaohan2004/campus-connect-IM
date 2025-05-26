<template>
  <div class="conversation-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <div class="navbar-left">
        <button class="btn-icon" @click="handleBack" v-if="isMobile">
          <i class="el-icon-arrow-left"></i>
        </button>
      </div>
      <div class="navbar-title">{{ conversationTitle }}</div>
      <div class="navbar-right">
        <el-dropdown trigger="click" @command="handleCommand">
          <button class="btn-icon">
            <i class="el-icon-more"></i>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="clear">清空聊天记录</el-dropdown-item>
              <el-dropdown-item command="mute" :divided="true">
                {{ isMuted ? '取消免打扰' : '设为免打扰' }}
              </el-dropdown-item>
              <el-dropdown-item command="top">
                {{ isTop ? '取消置顶' : '会话置顶' }}
              </el-dropdown-item>
              <el-dropdown-item command="delete" :divided="true">删除会话</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="message-list scrollable" ref="messageList">
      <div v-if="!messages.length" class="empty-state">
        <p>暂无消息</p>
        <p class="hint">开始发送消息吧</p>
      </div>

      <template v-else>
        <div v-for="(message, index) in messages" :key="message.messageId" class="message-wrapper">
          <!-- 日期分割线 -->
          <div v-if="showDateDivider(message, index)" class="date-divider">
            {{ formatDate(message.timestamp) }}
          </div>

          <!-- 消息气泡 -->
          <div
            class="message"
            :class="{
              sent: message.sender === currentUser.id,
              received: message.sender !== currentUser.id
            }"
          >
            <img
              v-if="message.sender !== currentUser.id"
              :src="getSenderAvatar(message.sender)"
              class="avatar message-avatar"
              alt="头像"
            />
            <div class="message-content-wrapper">
              <div class="message-content" @contextmenu.prevent="showMessageActions(message, $event)">
                <!-- 撤回消息 -->
                <template v-if="message.isRecalled">
                  <div class="recalled-message">
                    <i class="el-icon-warning-outline"></i>
                    {{ message.sender === currentUser.id ? '你' : '对方' }}撤回了一条消息
                  </div>
                </template>
                <!-- 普通消息 -->
                <template v-else>
                  {{ message.content }}
                </template>
              </div>
              <div class="message-time">
                {{ formatMessageTime(message.timestamp) }}
                <i
                  v-if="message.sender === currentUser.id"
                  :class="[
                    message.isRead ? 'el-icon-check' : 'el-icon-circle-check',
                    message.isRead ? 'read' : ''
                  ]"
                ></i>
              </div>
            </div>
            <img
              v-if="message.sender === currentUser.id"
              :src="currentUser.avatar || defaultAvatar"
              class="avatar message-avatar"
              alt="头像"
            />
          </div>
        </div>
      </template>
    </div>

    <!-- 输入工具栏 -->
    <div class="toolbar">
      <button class="toolbar-btn">
        <i class="el-icon-picture"></i>
      </button>
      <textarea
        class="toolbar-input"
        v-model="messageInput"
        placeholder="输入消息..."
        @keydown.enter.prevent="sendMessage"
      ></textarea>
      <button class="toolbar-btn" @click="sendMessage" :disabled="!messageInput.trim()">
        <i class="el-icon-position"></i>
      </button>
    </div>

    <!-- 消息操作菜单 -->
    <el-dialog v-model="showMessageMenu" title="消息操作" width="80%" custom-class="message-menu-dialog">
      <div class="message-menu">
        <button
          v-if="selectedMessage && selectedMessage.sender === currentUser.id && !selectedMessage.isRecalled"
          class="menu-item"
          @click="recallMessage"
        >
          <i class="el-icon-delete"></i>
          <span>撤回</span>
        </button>
        <button class="menu-item" @click="copyMessage">
          <i class="el-icon-document-copy"></i>
          <span>复制</span>
        </button>
        <button class="menu-item" @click="forwardMessage">
          <i class="el-icon-share"></i>
          <span>转发</span>
        </button>
        <button class="menu-item" @click="deleteMessage">
          <i class="el-icon-delete"></i>
          <span>删除</span>
        </button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useStore } from 'vuex';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import defaultAvatar from '@/assets/default-avatar.png';
import { formatTime, formatDate } from '@/utils/format';

export default {
  name: 'Conversation',
  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },
  emits: ['back'],
  setup(props, { emit }) {
    const store = useStore();
    const route = useRoute();
    const router = useRouter();
    
    const messageList = ref(null);
    const messageInput = ref('');
    const isMobile = ref(window.innerWidth < 768);
    const showMessageMenu = ref(false);
    const selectedMessage = ref(null);
    
    // 从store获取数据
    const currentUser = computed(() => store.state.user.userInfo || {});
    const conversation = computed(() => store.state.conversation.currentConversation);
    const messages = computed(() => {
      const conversationId = Number(props.id);
      return store.state.message.messages[conversationId] || [];
    });
    
    const conversationTitle = computed(() => conversation.value?.title || '会话');
    const isTop = computed(() => conversation.value?.isTop || false);
    const isMuted = computed(() => conversation.value?.isMuted || false);
    
    // 监听窗口大小变化
    const handleResize = () => {
      isMobile.value = window.innerWidth < 768;
    };
    
    // 获取会话详情和消息
    const fetchConversationData = async () => {
      const conversationId = Number(props.id);
      
      try {
        // 获取会话详情
        await store.dispatch('conversation/getConversationDetail', conversationId);
        
        // 根据会话类型获取消息
        if (conversation.value) {
          if (conversation.value.isGroup) {
            // 群聊消息
            await store.dispatch('message/getGroupMessages', {
              conversationId,
              groupId: conversation.value.groupId
            });
          } else {
            // 私聊消息
            await store.dispatch('message/getPrivateMessages', {
              conversationId,
              otherPhone: conversation.value.otherPhone
            });
          }
          
          // 标记会话已读
          await store.dispatch('conversation/markConversationAsRead', conversationId);
          
          // 滚动到底部
          await nextTick();
          scrollToBottom();
        }
      } catch (error) {
        ElMessage.error('获取会话数据失败');
        console.error(error);
      }
    };
    
    // 发送消息
    const sendMessage = async () => {
      if (!messageInput.value.trim()) return;
      
      const content = messageInput.value;
      messageInput.value = '';
      
      try {
        if (conversation.value.isGroup) {
          // 发送群聊消息
          await store.dispatch('message/sendGroupMessage', {
            groupId: conversation.value.groupId,
            contentType: 1, // 文本消息
            content
          });
        } else {
          // 发送私聊消息
          await store.dispatch('message/sendPrivateMessage', {
            receiverPhone: conversation.value.otherPhone,
            contentType: 1, // 文本消息
            content
          });
        }
        
        // 滚动到底部
        await nextTick();
        scrollToBottom();
      } catch (error) {
        ElMessage.error('发送消息失败');
        console.error(error);
      }
    };
    
    // 滚动到底部
    const scrollToBottom = () => {
      if (messageList.value) {
        messageList.value.scrollTop = messageList.value.scrollHeight;
      }
    };
    
    // 处理返回
    const handleBack = () => {
      emit('back');
    };
    
    // 处理下拉菜单命令
    const handleCommand = async (command) => {
      const conversationId = Number(props.id);
      
      switch (command) {
        case 'clear':
          try {
            await ElMessageBox.confirm('确定要清空聊天记录吗？', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            });
            await store.dispatch('conversation/clearConversation', conversationId);
            ElMessage.success('聊天记录已清空');
          } catch (error) {
            // 用户取消操作
          }
          break;
        case 'mute':
          try {
            await store.dispatch('conversation/muteConversation', {
              conversationId,
              isMuted: !isMuted.value
            });
            ElMessage.success(isMuted.value ? '已取消免打扰' : '已设置为免打扰');
          } catch (error) {
            ElMessage.error('操作失败');
          }
          break;
        case 'top':
          try {
            await store.dispatch('conversation/topConversation', {
              conversationId,
              isTop: !isTop.value
            });
            ElMessage.success(isTop.value ? '已取消置顶' : '已设置为置顶');
          } catch (error) {
            ElMessage.error('操作失败');
          }
          break;
        case 'delete':
          try {
            await ElMessageBox.confirm('确定要删除此会话吗？', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            });
            await store.dispatch('conversation/deleteConversation', conversationId);
            ElMessage.success('会话已删除');
            router.push('/chat');
          } catch (error) {
            // 用户取消操作
          }
          break;
      }
    };
    
    // 显示消息操作菜单
    const showMessageActions = (message) => {
      selectedMessage.value = message;
      showMessageMenu.value = true;
    };
    
    // 撤回消息
    const recallMessage = async () => {
      if (!selectedMessage.value) return;
      
      try {
        await store.dispatch('message/recallMessage', {
          conversationId: Number(props.id),
          messageId: selectedMessage.value.messageId
        });
        ElMessage.success('消息已撤回');
      } catch (error) {
        ElMessage.error('撤回失败');
      } finally {
        showMessageMenu.value = false;
      }
    };
    
    // 复制消息
    const copyMessage = () => {
      if (!selectedMessage.value || selectedMessage.value.isRecalled) return;
      
      try {
        navigator.clipboard.writeText(selectedMessage.value.content);
        ElMessage.success('已复制到剪贴板');
      } catch (error) {
        ElMessage.error('复制失败');
      } finally {
        showMessageMenu.value = false;
      }
    };
    
    // 转发消息
    const forwardMessage = () => {
      if (!selectedMessage.value || selectedMessage.value.isRecalled) return;
      
      ElMessage.info('转发功能开发中');
      showMessageMenu.value = false;
    };
    
    // 删除消息
    const deleteMessage = async () => {
      if (!selectedMessage.value) return;
      
      try {
        await store.dispatch('message/deleteMessage', {
          conversationId: Number(props.id),
          messageId: selectedMessage.value.messageId
        });
        ElMessage.success('消息已删除');
      } catch (error) {
        ElMessage.error('删除失败');
      } finally {
        showMessageMenu.value = false;
      }
    };
    
    // 获取发送者头像
    const getSenderAvatar = (senderId) => {
      // 这里应该根据senderId获取用户头像
      // 简化处理，返回默认头像
      return defaultAvatar;
    };
    
    // 判断是否显示日期分割线
    const showDateDivider = (message, index) => {
      if (index === 0) return true;
      
      const prevMessage = messages.value[index - 1];
      const prevDate = new Date(prevMessage.timestamp).toDateString();
      const currentDate = new Date(message.timestamp).toDateString();
      
      return prevDate !== currentDate;
    };
    
    // 格式化消息时间
    const formatMessageTime = (timestamp) => {
      return formatTime(timestamp, 'HH:mm');
    };
    
    onMounted(() => {
      window.addEventListener('resize', handleResize);
      fetchConversationData();
    });
    
    // 监听会话ID变化，重新获取数据
    watch(
      () => props.id,
      () => {
        fetchConversationData();
      }
    );
    
    return {
      messageList,
      messageInput,
      isMobile,
      currentUser,
      conversation,
      messages,
      conversationTitle,
      isTop,
      isMuted,
      showMessageMenu,
      selectedMessage,
      defaultAvatar,
      handleBack,
      sendMessage,
      handleCommand,
      showMessageActions,
      recallMessage,
      copyMessage,
      forwardMessage,
      deleteMessage,
      getSenderAvatar,
      showDateDivider,
      formatDate,
      formatMessageTime
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.conversation-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: $bg-secondary;
}

.message-list {
  flex: 1;
  padding: $spacing-4;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: $text-secondary;
  
  p {
    margin-bottom: $spacing-2;
  }
  
  .hint {
    font-size: $font-size-sm;
    color: $text-muted;
  }
}

.date-divider {
  text-align: center;
  margin: $spacing-4 0;
  font-size: $font-size-xs;
  color: $text-muted;
  position: relative;
  
  &::before, &::after {
    content: '';
    position: absolute;
    top: 50%;
    width: 20%;
    height: 1px;
    background-color: $border-color;
  }
  
  &::before {
    left: 15%;
  }
  
  &::after {
    right: 15%;
  }
}

.message-wrapper {
  margin-bottom: $spacing-4;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.recalled-message {
  color: $text-muted;
  font-style: italic;
  font-size: $font-size-sm;
  display: flex;
  align-items: center;
  
  i {
    margin-right: $spacing-2;
  }
}

.message-time {
  font-size: $font-size-xs;
  color: $text-muted;
  margin-top: $spacing-1;
  align-self: flex-end;
  display: flex;
  align-items: center;
  
  i {
    margin-left: $spacing-2;
    font-size: $font-size-xs;
    
    &.read {
      color: $primary-color;
    }
  }
}

.message-menu {
  display: flex;
  flex-direction: column;
  
  .menu-item {
    display: flex;
    align-items: center;
    padding: $spacing-4;
    border-bottom: 1px solid $border-color;
    
    &:last-child {
      border-bottom: none;
    }
    
    i {
      margin-right: $spacing-3;
      font-size: $font-size-lg;
      color: $text-secondary;
    }
    
    span {
      font-size: $font-size-base;
    }
  }
}

.message-menu-dialog {
  border-radius: $border-radius-lg;
  overflow: hidden;
}
</style> 