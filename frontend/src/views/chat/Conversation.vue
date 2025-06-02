<template>
  <div class="conversation-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <div class="navbar-left">
        <button class="btn btn-primary btn-sm btn-compact" @click="handleBack" v-if="isMobile">
          <i class="el-icon-arrow-left"></i>
          <span>返回</span>
        </button>
      </div>
      <div class="navbar-title">{{ conversationTitle }}</div>
      <div class="navbar-right">
        <el-dropdown trigger="click" @command="handleCommand">
          <button class="btn btn-primary btn-sm btn-compact">
            <i class="el-icon-more"></i>
            <span>更多</span>
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
              sent: message.sender === currentUser.id || message.senderId === currentUser.id,
              received: message.sender !== currentUser.id && message.senderId !== currentUser.id
            }"
          >
            <!-- 头像：自己发的在右边，对方发的在左边 -->
            <img
              class="avatar message-avatar"
              :src="getSenderAvatar(message.sender || message.senderId)"
              alt="头像"
            />
            <div class="message-content-wrapper">
              <div class="message-content" @contextmenu.prevent="showMessageActions(message, $event)">
                <!-- 撤回消息 -->
                <template v-if="message.isRecalled">
                  <div class="recalled-message">
                    <i class="el-icon-warning-outline"></i>
                    {{ (message.sender === currentUser.id || message.senderId === currentUser.id) ? '你' : '对方' }}撤回了一条消息
                  </div>
                </template>
                <!-- 普通消息 -->
                <template v-else>
                  <div v-html="message.content"></div>
                </template>
              </div>
              <div class="message-time">
                {{ formatMessageTime(message.timestamp) }}
                <i
                  v-if="message.sender === currentUser.id || message.senderId === currentUser.id"
                  :class="[
                    message.isRead ? 'el-icon-check' : 'el-icon-circle-check',
                    message.isRead ? 'read' : ''
                  ]"
                ></i>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 输入工具栏 -->
    <div class="toolbar">
      <button class="btn btn-primary btn-sm btn-compact" @click="showGoodsTable">
        <span>广告</span>
      </button>
      <textarea
        class="toolbar-input"
        v-model="messageInput"
        placeholder="输入消息..."
        @keydown.enter.prevent="sendMessage"
      ></textarea>
      <button 
        class="btn btn-primary btn-sm btn-compact" 
        @click="sendMessage" 
        :disabled="!messageInput.trim()"
      >
        发送
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

    <!-- 商品表格弹窗 -->
    <el-dialog
      title="商品列表"
      v-model="goodsDialogVisible"
      width="70%"
      :close-on-click-modal="false"
      :destroy-on-close="true"
      @closed="handleDialogClosed"
    >
      <div class="goods-table-container">
        <el-table 
          :data="goodsList" 
          style="width: 100%"
          :max-height="400"
          v-loading="loading"
        >
          <el-table-column prop="name" label="商品名称" min-width="120" />
          <el-table-column prop="intro" label="商品描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click="generateAd(scope.row)"
              >
                生成广告文案
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useStore } from 'vuex';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import defaultAvatar from '@/assets/default-avatar.png';
import { formatTime, formatDate } from '@/utils/format';
import { getGoodsByPhone, generateAIAd } from '@/api/goods';

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
    const goodsDialogVisible = ref(false);
    const goodsList = ref([]);
    const loading = ref(false);
    
    // 从store获取数据
    const currentUser = computed(() => store.state.user.userInfo || {});
    const conversation = computed(() => store.state.conversation.currentConversation);
    const messages = computed(() => {
      const conversationId = Number(props.id);
      const messageList = store.state.message.messages[conversationId] || [];
      console.log('[Conversation] 获取消息列表', { 
        conversationId, 
        messageCount: messageList.length,
        messages: messageList 
      });
      return messageList;
    });
    const friends = computed(() => store.state.friendship.friends || []);
    const groups = computed(() => store.state.group.groups || []);
    
    const conversationTitle = computed(() => {
      if (!conversation.value) return '会话';
      
      // 根据会话类型显示不同的标题
      if (conversation.value.conversationType === 1) {
        // 群聊 - 尝试从群组列表中获取更详细的信息
        const group = groups.value.find(g => Number(g.id) === Number(conversation.value.targetId));
        console.log('[Conversation] 群组信息:', {
          group,
          groupName: group?.name,
          targetId: conversation.value.targetId,
          groups: groups.value
        });
        if (group) {
          // 明确检查 group.name 是否为 undefined
          const finalName = (group.name === undefined || group.name === null || group.name === '') 
            ? '相亲相爱一家人' 
            : group.name;
          console.log('[Conversation] 最终群聊名称:', finalName);
          return finalName;
        }
        // 如果没有找到群组信息，返回默认名称
        console.log('[Conversation] 未找到群组信息，使用默认名称');
        return '相亲相爱一家人';
      } else {
        // 私聊 - 尝试从好友列表中获取更详细的信息
        const friend = friends.value.find(f => Number(f.id) === Number(conversation.value.targetId));
        console.log('Conversation.vue - 找到的好友信息:', friend);
        if (friend) {
          // 检查好友对象是否包含remark字段
          console.log('Conversation.vue - 好友备注名:', friend.remark);
          // 优先显示备注名，如果没有备注名再显示昵称
          return friend.remark || friend.nickname || conversation.value.title || '私聊';
        }
        return conversation.value.title || '私聊';
      }
    });
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
        
        console.log('[Conversation] 获取到的会话详情:', conversation.value);
        
        // 根据会话类型获取消息
        if (conversation.value) {
          // 检查 conversation.value.isGroup 是否存在
          console.log('[Conversation] 会话类型:', conversation.value.conversationType);
          
          // 根据 conversationType 判断是否为群聊
          // 假设 conversationType = 1 表示群聊，conversationType = 0 表示私聊
          const isGroup = conversation.value.conversationType === 1;
          
          if (isGroup) {
            console.log('[Conversation] 这是群聊会话，群组ID:', conversation.value.targetId);
            // 群聊消息
            const groupMessages = await store.dispatch('message/getGroupMessages', {
              conversationId,
              groupId: conversation.value.targetId // 使用 targetId 作为 groupId
            });
            console.log('[Conversation] 获取到群聊消息:', groupMessages);
          } else {
            console.log('[Conversation] 这是私聊会话，对方ID:', conversation.value.targetId);
            
            // 检查targetId是否存在
            if (!conversation.value.targetId) {
              console.error('[Conversation] 对方ID为空，无法获取消息');
              ElMessage.error('对方ID为空，无法获取消息');
              return;
            }
            
            // 私聊消息
            const privateMessages = await store.dispatch('message/getPrivateMessages', {
              conversationId,
              otherUserId: conversation.value.targetId // 使用 targetId 作为 otherUserId
            });
            console.log('[Conversation] 获取到私聊消息:', privateMessages);
          }
          
          console.log('[Conversation] 获取到的消息列表:', messages.value);
          
          // 标记会话已读
          await store.dispatch('conversation/markConversationAsRead', conversationId);
          
          // 滚动到底部
          await nextTick();
          scrollToBottom();
        } else {
          console.error('[Conversation] 会话详情为空');
        }
      } catch (error) {
        console.error('[Conversation] 获取会话数据错误:', error);
        // 优先使用后端返回的错误信息
        const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '获取会话数据失败';
        ElMessage.error(errorMsg);
      }
    };
    
    // 发送消息
    const sendMessage = async () => {
      if (!messageInput.value.trim()) return;
      
      const content = messageInput.value;
      messageInput.value = '';
      
      try {
        console.log('[Conversation] 开始发送消息:', content);
        console.log('[Conversation] 会话信息:', conversation.value);
        
        if (!conversation.value) {
          console.error('[Conversation] 会话信息为空，无法发送消息');
          ElMessage.error('会话信息获取失败，请刷新页面重试');
          return;
        }
        
        // 根据 conversationType 判断是否为群聊
        const isGroup = conversation.value.conversationType === 1;
        const conversationId = Number(props.id);
        
        console.log('[Conversation] 发送消息前的会话ID:', conversationId);
        console.log('[Conversation] 发送消息前的消息列表长度:', (store.state.message.messages[conversationId] || []).length);
        
        if (isGroup) {
          console.log('[Conversation] 发送群聊消息，群组ID:', conversation.value.targetId);
          // 发送群聊消息
          const result = await store.dispatch('message/sendGroupMessage', {
            groupId: conversation.value.targetId,
            contentType: 1, // 文本消息
            content
          });
          console.log('[Conversation] 群聊消息发送结果:', result);
        } else {
          console.log('[Conversation] 发送私聊消息，对方ID:', conversation.value.targetId);
          
          // 确保 targetId 存在且有效
          if (!conversation.value.targetId) {
            console.error('[Conversation] 接收者ID为空');
            ElMessage.error('接收者ID为空，无法发送消息');
            return;
          }
          
          // 发送私聊消息
          const result = await store.dispatch('message/sendPrivateMessage', {
            receiverId: conversation.value.targetId,
            contentType: 1, // 文本消息
            content
          });
          console.log('[Conversation] 私聊消息发送结果:', result);
        }
        
        console.log('[Conversation] 发送消息后的消息列表长度:', (store.state.message.messages[conversationId] || []).length);
        
        // 滚动到底部
        await nextTick();
        scrollToBottom();
      } catch (error) {
        console.error('[Conversation] 发送消息错误:', error);
        // 优先使用后端返回的错误信息
        const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '发送消息失败';
        ElMessage.error(errorMsg);
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
            console.error('操作失败:', error);
            // 优先使用后端返回的错误信息
            const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '操作失败';
            ElMessage.error(errorMsg);
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
            console.error('操作失败:', error);
            // 优先使用后端返回的错误信息
            const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '操作失败';
            ElMessage.error(errorMsg);
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
        // 获取会话信息
        if (!conversation.value) {
          console.error('会话信息不存在');
          ElMessage.error('无法撤回消息，会话信息不存在');
          return;
        }
        
        const conversationType = conversation.value.conversationType;
        const targetId = conversation.value.targetId;
        
        await store.dispatch('message/recallMessage', {
          conversationId: Number(props.id),
          messageId: selectedMessage.value.messageId,
          conversationType,
          targetId
        });
        ElMessage.success('消息已撤回');
      } catch (error) {
        console.error('撤回失败:', error);
        // 优先使用后端返回的错误信息
        const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '撤回失败';
        ElMessage.error(errorMsg);
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
        console.error('复制失败:', error);
        // 优先使用后端返回的错误信息
        const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '复制失败';
        ElMessage.error(errorMsg);
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
        console.error('删除失败:', error);
        // 优先使用后端返回的错误信息
        const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '删除失败';
        ElMessage.error(errorMsg);
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
      if (!timestamp) return '';
      try {
        return formatTime(timestamp, 'HH:mm');
      } catch (error) {
        console.error('[Conversation] 格式化消息时间错误:', error);
        return '';
      }
    };
    
    const showGoodsTable = async () => {
      try {
        loading.value = true;
        // 获取当前用户手机号
        const phone = store.state.user.userInfo?.phone;
        if (!phone) {
          ElMessage.error('获取用户信息失败');
          return;
        }
        
        // 请求商品数据
        const { data } = await getGoodsByPhone(phone);
        if (data && Array.isArray(data)) {
          goodsList.value = data;
          goodsDialogVisible.value = true;
        } else {
          ElMessage.warning('暂无商品数据');
        }
      } catch (error) {
        console.error('获取商品列表失败:', error);
        ElMessage.error('获取商品列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    const generateAd = async (goods) => {
      let loadingInstance = null;
      try {
        // 显示加载提示
        loadingInstance = ElLoading.service({
          lock: true,
          text: '正在生成广告文案...',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        // 调用AI广告生成接口
        const { data } = await generateAIAd({
          gid: goods.gid.toString(),
          name: goods.name,
          intro: goods.intro
        });

        // 将生成的广告文案填入消息框
        messageInput.value = data;
        
        // 关闭商品列表弹窗
        goodsDialogVisible.value = false;
        
        // 提示成功
        ElMessage.success('广告文案生成成功');
      } catch (error) {
        console.error('生成广告文案失败:', error);
        ElMessage.error('生成广告文案失败');
      } finally {
        // 确保在任何情况下都关闭加载提示
        if (loadingInstance) {
          loadingInstance.close();
        }
      }
    };

    const handleDialogClosed = () => {
      goodsList.value = [];
    };
    
    onMounted(() => {
      window.addEventListener('resize', handleResize);
      // 获取好友和群组列表
      store.dispatch('friendship/getFriendsWithDetails');
      store.dispatch('group/getGroups');
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
      formatMessageTime,
      goodsDialogVisible,
      goodsList,
      showGoodsTable,
      generateAd,
      loading,
      handleDialogClosed
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
  padding-bottom: 55px; /* 为底部导航栏留出空间 */
}

.toolbar {
  display: flex;
  align-items: center;
  padding: $spacing-3;
  background-color: $white;
  border-top: 1px solid $border-color;
  
  .toolbar-btn {
    padding: $spacing-2;
    border-radius: $border-radius;
    color: $text-secondary;
    background: none;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    
    &:hover {
      background-color: $gray-100;
    }
    
    &:disabled {
      color: $text-muted;
      cursor: not-allowed;
    }
    
    i {
      font-size: $font-size-lg;
    }
  }
  
  .toolbar-input {
    flex: 1;
    border: none;
    padding: $spacing-3;
    margin: 0 $spacing-2;
    border-radius: $border-radius;
    background-color: $gray-100;
    resize: none;
    height: 40px;
    max-height: 120px;
    
    &:focus {
      outline: none;
      background-color: $gray-200;
    }
  }
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

.message {
  display: flex;
  align-items: flex-start;
  margin-bottom: $spacing-2;
  
  &.sent {
    flex-direction: row-reverse;
    
    .message-content {
      background-color: $primary-color;
      color: $white;
      border-radius: 12px 2px 12px 12px;
      margin-left: $spacing-2;

      :deep(a) {
        color: #FFD700 !important;
        text-decoration: none;
        font-weight: 500;
        
        &:hover {
          text-decoration: underline;
          opacity: 0.9;
        }
      }
    }
    
    .message-content-wrapper {
      align-items: flex-end;
      margin-right: $spacing-2;
    }
  }
  
  &.received {
    .message-content {
      background-color: $white;
      border-radius: 2px 12px 12px 12px;
      margin-right: $spacing-2;

      :deep(a) {
        color: #409EFF !important;
        text-decoration: none;
        font-weight: 500;
        
        &:hover {
          text-decoration: underline;
          opacity: 0.9;
        }
      }
    }
    
    .message-content-wrapper {
      align-items: flex-start;
      margin-left: $spacing-2;
    }
  }
}

.message-content {
  padding: $spacing-3;
  max-width: 100%;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.el-dialog {
  border-radius: 8px;
}

.el-table {
  margin: 10px 0;
}

.goods-table-container {
  margin: -20px;
  padding: 20px;
  
  .el-table {
    --el-table-border-color: var(--el-border-color-lighter);
    
    :deep(th.el-table__cell) {
      background-color: var(--el-fill-color-light);
    }
  }
}
</style> 