<template>
  <div class="profile-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <div class="navbar-title">个人资料</div>
      <div class="navbar-right">
        <button class="btn-icon" @click="handleLogout">
          <i class="el-icon-switch-button"></i>
        </button>
      </div>
    </div>

    <!-- 个人资料内容 -->
    <div class="profile-content scrollable">
      <!-- 头像和昵称 -->
      <div class="profile-header">
        <div class="avatar-container" @click="handleChangeAvatar">
          <img :src="userInfo.avatar || defaultAvatar" class="avatar avatar-lg" alt="头像" />
          <div class="avatar-overlay">
            <i class="el-icon-camera"></i>
          </div>
        </div>
        <h2 class="profile-name">{{ userInfo.nickname }}</h2>
        <p class="profile-phone">{{ userInfo.phone }}</p>
      </div>

      <!-- 个人信息列表 -->
      <div class="profile-list">
        <div class="profile-item" @click="showEditNickname = true">
          <span class="item-label">昵称</span>
          <div class="item-content">
            <span>{{ userInfo.nickname }}</span>
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>

        <div class="profile-item">
          <span class="item-label">手机号</span>
          <div class="item-content">
            <span>{{ userInfo.phone }}</span>
          </div>
        </div>

        <div class="profile-item">
          <span class="item-label">账号状态</span>
          <div class="item-content">
            <span class="status-badge" :class="{ 'online': userInfo.status === 0 }">
              {{ userInfo.status === 0 ? '在线' : '离线' }}
            </span>
          </div>
        </div>
      </div>

      <!-- 设置列表 -->
      <div class="settings-list">
        <h3 class="settings-title">设置</h3>

        <div class="profile-item">
          <span class="item-label">消息通知</span>
          <div class="item-content">
            <el-switch v-model="settings.notifications" />
          </div>
        </div>

        <div class="profile-item">
          <span class="item-label">声音</span>
          <div class="item-content">
            <el-switch v-model="settings.sound" />
          </div>
        </div>

        <div class="profile-item">
          <span class="item-label">振动</span>
          <div class="item-content">
            <el-switch v-model="settings.vibration" />
          </div>
        </div>

        <div class="profile-item">
          <span class="item-label">深色模式</span>
          <div class="item-content">
            <el-switch v-model="settings.darkMode" />
          </div>
        </div>
      </div>

      <!-- 关于信息 -->
      <div class="about-section">
        <h3 class="settings-title">关于</h3>

        <div class="profile-item">
          <span class="item-label">版本</span>
          <div class="item-content">
            <span>1.0.0</span>
          </div>
        </div>

        <div class="profile-item">
          <span class="item-label">隐私政策</span>
          <div class="item-content">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>

        <div class="profile-item">
          <span class="item-label">用户协议</span>
          <div class="item-content">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
      </div>

      <!-- 退出登录按钮 -->
      <div class="logout-section">
        <button class="btn btn-danger btn-logout" @click="handleLogout">
          退出登录
        </button>
      </div>
    </div>

    <!-- 修改昵称弹窗 -->
    <el-dialog
      v-model="showEditNickname"
      title="修改昵称"
      width="80%"
      :before-close="() => showEditNickname = false"
    >
      <div class="dialog-content">
        <div class="form-group">
          <label>昵称</label>
          <input
            type="text"
            v-model="nicknameForm.nickname"
            placeholder="请输入昵称"
            class="form-input"
          />
        </div>
        <div class="dialog-footer">
          <button class="btn btn-text" @click="showEditNickname = false">取消</button>
          <button class="btn btn-primary" @click="handleUpdateNickname">
            保存
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
import { ref, computed, reactive, onMounted, watch } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import defaultAvatar from '@/assets/default-avatar.png';

export default {
  name: 'Profile',
  setup() {
    const store = useStore();
    const router = useRouter();
    
    const showEditNickname = ref(false);
    const nicknameForm = reactive({
      nickname: ''
    });
    
    // 从store获取用户信息
    const userInfo = computed(() => store.state.user.userInfo || {});
    
    // 设置选项
    const settings = reactive({
      notifications: true,
      sound: true,
      vibration: true,
      darkMode: false
    });
    
    // 监听用户信息变化，更新昵称表单
    watch(
      () => userInfo.value,
      (newUserInfo) => {
        if (newUserInfo) {
          nicknameForm.nickname = newUserInfo.nickname || '';
        }
      },
      { immediate: true }
    );
    
    // 更新昵称
    const handleUpdateNickname = async () => {
      if (!nicknameForm.nickname) {
        ElMessage.warning('请输入昵称');
        return;
      }
      
      try {
        await store.dispatch('user/updateUser', {
          nickname: nicknameForm.nickname
        });
        
        ElMessage.success('昵称修改成功');
        showEditNickname.value = false;
        
        // 重新获取用户信息
        store.dispatch('user/getUserInfo');
      } catch (error) {
        ElMessage.error('修改失败');
      }
    };
    
    // 修改头像
    const handleChangeAvatar = () => {
      ElMessage.info('头像修改功能开发中');
    };
    
    // 退出登录
    const handleLogout = () => {
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        store.dispatch('user/logout');
        router.push('/login');
        ElMessage.success('已退出登录');
      }).catch(() => {
        // 用户取消操作
      });
    };
    
    onMounted(() => {
      // 获取用户信息
      if (store.state.user.isLoggedIn && !userInfo.value.id) {
        store.dispatch('user/getUserInfo');
      }
    });
    
    return {
      userInfo,
      settings,
      showEditNickname,
      nicknameForm,
      defaultAvatar,
      handleUpdateNickname,
      handleChangeAvatar,
      handleLogout
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.profile-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: $bg-secondary;
}

.profile-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 50px;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-8 $spacing-4;
  background-color: $white;
  margin-bottom: $spacing-4;
  
  .avatar-container {
    position: relative;
    margin-bottom: $spacing-4;
    
    .avatar-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba($black, 0.3);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: $transition-base;
      cursor: pointer;
      
      i {
        color: $white;
        font-size: $font-size-xl;
      }
    }
    
    &:hover .avatar-overlay {
      opacity: 1;
    }
  }
  
  .profile-name {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    margin: 0 0 $spacing-2;
  }
  
  .profile-phone {
    font-size: $font-size-base;
    color: $text-secondary;
    margin: 0;
  }
}

.profile-list, .settings-list, .about-section {
  background-color: $white;
  margin-bottom: $spacing-4;
}

.settings-title {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-secondary;
  padding: $spacing-4;
  margin: 0;
  border-bottom: 1px solid $border-color;
}

.profile-item {
  display: flex;
  align-items: center;
  padding: $spacing-4;
  border-bottom: 1px solid $border-color;
  
  &:last-child {
    border-bottom: none;
  }
  
  .item-label {
    flex: 1;
    font-size: $font-size-base;
  }
  
  .item-content {
    display: flex;
    align-items: center;
    color: $text-secondary;
    
    span {
      margin-right: $spacing-2;
    }
    
    i {
      color: $gray-400;
    }
  }
  
  .status-badge {
    padding: $spacing-1 $spacing-3;
    border-radius: $border-radius-full;
    font-size: $font-size-xs;
    background-color: $gray-300;
    color: $text-secondary;
    
    &.online {
      background-color: rgba($success-color, 0.2);
      color: $success-color;
    }
  }
}

.logout-section {
  padding: $spacing-6 $spacing-4;
  
  .btn-logout {
    width: 100%;
    background-color: $danger-color;
    
    &:hover {
      background-color: darken($danger-color, 5%);
    }
    
    &:active {
      background-color: darken($danger-color, 10%);
    }
  }
}

.form-group {
  margin-bottom: $spacing-4;
  
  label {
    display: block;
    margin-bottom: $spacing-2;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $text-secondary;
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
</style> 