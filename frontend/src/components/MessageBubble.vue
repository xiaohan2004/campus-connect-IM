<template>
  <div 
    class="message-bubble" 
    :class="{ 
      'self': isSelf, 
      'other': !isSelf,
      'system': message.type === 'system'
    }"
  >
    <!-- 系统消息 -->
    <div v-if="message.type === 'system'" class="system-message">
      {{ message.content }}
    </div>

    <!-- 普通消息 -->
    <template v-else>
      <!-- 头像 -->
      <div class="avatar" v-if="!isSelf">
        <img :src="message.sender.avatar || defaultAvatar" alt="avatar" />
      </div>

      <div class="message-content">
        <!-- 发送者名称 -->
        <div class="sender-name" v-if="!isSelf && showName">
          {{ message.sender.nickname || '用户' }}
        </div>

        <!-- 消息内容 -->
        <div class="content" :class="{ 'image': message.contentType === 'image' }">
          <!-- 文本消息 -->
          <span v-if="message.contentType === 'text'">{{ message.content }}</span>
          
          <!-- 图片消息 -->
          <img 
            v-else-if="message.contentType === 'image'" 
            :src="message.content" 
            @click="previewImage(message.content)" 
            alt="图片消息"
          />
          
          <!-- 其他类型消息 -->
          <span v-else class="unsupported">不支持的消息类型</span>
        </div>

        <!-- 消息时间 -->
        <div class="time">{{ formatTime(message.timestamp) }}</div>
        
        <!-- 消息状态（仅自己发送的消息） -->
        <div v-if="isSelf" class="status">
          <i v-if="message.status === 'sending'" class="el-icon-loading"></i>
          <i v-else-if="message.status === 'failed'" class="el-icon-warning-outline"></i>
          <i v-else-if="message.status === 'sent'" class="el-icon-check"></i>
          <i v-else-if="message.status === 'read'" class="el-icon-finished"></i>
        </div>
      </div>

      <!-- 头像（自己发送的消息） -->
      <div class="avatar" v-if="isSelf">
        <img :src="message.sender.avatar || defaultAvatar" alt="avatar" />
      </div>
    </template>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useStore } from 'vuex';
import { formatTime } from '@/utils/format';

export default {
  name: 'MessageBubble',
  props: {
    message: {
      type: Object,
      required: true
    },
    showName: {
      type: Boolean,
      default: true
    }
  },
  setup(props) {
    const store = useStore();
    const currentUser = computed(() => store.state.user.currentUser);
    const defaultAvatar = '/src/assets/default-avatar.png';

    const isSelf = computed(() => {
      return props.message.sender.id === currentUser.value.id;
    });

    // eslint-disable-next-line no-unused-vars
    const previewImage = (url) => {
      // TODO: 实现图片预览功能
      // 将来会使用url参数和Element Plus的ImageViewer组件
      console.log('图片预览功能开发中...');
    };

    return {
      isSelf,
      defaultAvatar,
      formatTime,
      previewImage
    };
  }
};
</script>

<style lang="scss" scoped>
.message-bubble {
  display: flex;
  margin: 12px 0;
  max-width: 100%;
  
  &.self {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
      margin-right: 10px;
      
      .content {
        background-color: #007aff;
        color: white;
        border-radius: 18px 4px 18px 18px;
      }
    }
  }
  
  &.other {
    flex-direction: row;
    
    .message-content {
      align-items: flex-start;
      margin-left: 10px;
      
      .content {
        background-color: #f2f2f7;
        color: #000;
        border-radius: 4px 18px 18px 18px;
      }
    }
  }
  
  &.system {
    justify-content: center;
    
    .system-message {
      background-color: rgba(0, 0, 0, 0.1);
      color: #666;
      padding: 4px 12px;
      border-radius: 16px;
      font-size: 12px;
      max-width: 70%;
      text-align: center;
    }
  }
  
  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    overflow: hidden;
    flex-shrink: 0;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .message-content {
    display: flex;
    flex-direction: column;
    max-width: 70%;
    
    .sender-name {
      font-size: 12px;
      color: #8e8e93;
      margin-bottom: 4px;
    }
    
    .content {
      padding: 10px 14px;
      word-break: break-word;
      font-size: 16px;
      position: relative;
      
      &.image {
        padding: 4px;
        background-color: transparent !important;
        
        img {
          max-width: 200px;
          max-height: 200px;
          border-radius: 12px;
          cursor: pointer;
        }
      }
    }
    
    .time {
      font-size: 10px;
      color: #8e8e93;
      margin-top: 4px;
    }
    
    .status {
      font-size: 12px;
      color: #8e8e93;
      margin-top: 2px;
    }
  }
}
</style> 