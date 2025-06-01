<template>
  <div id="app">
    <router-view />
  </div>
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
        // 获取群组列表
        store.dispatch('group/getGroups');
        // 获取未读消息数
        store.dispatch('message/getUnreadMessageCount');
        // 获取离线消息
        store.dispatch('message/getOfflineMessages')
          .then(messages => {
            console.log('[App] 获取到离线消息:', messages);
            if (messages && messages.length > 0) {
              const messageIds = messages.map(msg => msg.messageId);
              console.log('[App] 准备确认离线消息, messageIds:', messageIds);
              // 确认接收离线消息
              return store.dispatch('message/confirmOfflineMessages', messageIds)
                .then(() => {
                  console.log('[App] 确认离线消息成功');
                })
                .catch(error => {
                  console.error('[App] 确认离线消息失败:', error);
                  // 不抛出错误，让应用继续运行
                });
            } else {
              console.log('[App] 没有离线消息需要确认');
            }
          })
          .catch(error => {
            console.error('[App] 获取离线消息失败:', error);
            // 不抛出错误，让应用继续运行
          });
      }
    });

    return {};
  }
};
</script>

<style lang="scss">
@import '@/assets/styles/global.scss';

/* 全局样式优化 */
:root {
  --primary-color: #409eff;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;
  --text-color: #303133;
  --text-color-secondary: #606266;
  --text-color-placeholder: #909399;
  --border-color: #ebeef5;
  --background-color: #f5f7fa;
}

body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--text-color);
  background-color: var(--background-color);
}

#app {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

/* 导航栏全局样式 */
.tabbar {
  display: flex;
  height: 55px;
  background-color: #fff;
  border-top: 1px solid var(--border-color);
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  
  .tabbar-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-decoration: none;
    color: var(--text-color-placeholder);
    position: relative;
    transition: all 0.2s;
    
    &.active {
      color: var(--primary-color);
      
      i {
        transform: translateY(-2px);
      }
      
      span {
        transform: translateY(2px);
        font-weight: 500;
      }
    }
    
    i {
      font-size: 22px;
      margin-bottom: 3px;
      transition: transform 0.2s;
    }
    
    span {
      font-size: 12px;
      transition: transform 0.2s;
    }
    
    .badge {
      position: absolute;
      top: 0;
      right: calc(50% - 15px);
      background-color: var(--danger-color);
      color: #fff;
      font-size: 12px;
      min-width: 18px;
      height: 18px;
      line-height: 18px;
      text-align: center;
      border-radius: 9px;
      padding: 0 5px;
      font-weight: bold;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }
}

/* 通用容器样式 */
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding-bottom: 55px; /* 为底部导航栏留出空间 */
}

/* 通用导航栏样式 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 50px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 10;
  
  .navbar-title {
    font-size: 18px;
    font-weight: 500;
    color: var(--text-color);
  }
  
  .navbar-right {
    display: flex;
    align-items: center;
  }
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  font-size: 14px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
  
  &.btn-primary {
    background-color: var(--primary-color);
    color: #fff;
    
    &:hover {
      background-color: darken(#409eff, 10%);
    }
  }
  
  &.btn-outline {
    background-color: transparent;
    border: 1px solid var(--primary-color);
    color: var(--primary-color);
    
    &:hover {
      background-color: rgba(64, 158, 255, 0.1);
    }
  }
  
  &.btn-text {
    background-color: transparent;
    color: var(--text-color-secondary);
    
    &:hover {
      color: var(--text-color);
    }
  }
  
  &.btn-danger {
    background-color: var(--danger-color);
    color: #fff;
    
    &:hover {
      background-color: darken(#f56c6c, 10%);
    }
  }
}

.btn-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background-color: rgba(0, 0, 0, 0.05);
  }
  
  i {
    font-size: 20px;
    color: var(--text-color-secondary);
  }
}

/* 头像样式 */
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #eee;
  
  &.avatar-lg {
    width: 60px;
    height: 60px;
  }
  
  &.avatar-sm {
    width: 30px;
    height: 30px;
  }
}

/* 表单样式 */
.form-group {
  margin-bottom: 15px;
  
  label {
    display: block;
    margin-bottom: 5px;
    font-size: 14px;
    color: var(--text-color-secondary);
  }
  
  .form-input {
    width: 100%;
    padding: 10px 12px;
    font-size: 14px;
    border: 1px solid var(--border-color);
    border-radius: 4px;
    transition: all 0.2s;
    box-sizing: border-box;
    
    &:focus {
      border-color: var(--primary-color);
      outline: none;
    }
  }
}

/* 对话框样式 */
.dialog-content {
  padding: 10px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  gap: 10px;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  
  p {
    font-size: 16px;
    color: var(--text-color-secondary);
    margin-bottom: 20px;
  }
}

/* 可滚动区域 */
.scrollable {
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.2);
    border-radius: 2px;
  }
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}
</style> 