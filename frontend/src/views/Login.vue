<template>
  <div class="login-container">
    <div class="login-header">
      <img src="@/assets/logo.png" alt="Logo" class="login-logo" />
      <h1>校园即时通讯</h1>
      <p>随时随地保持联系</p>
    </div>

    <div class="login-form">
      <div class="form-group">
        <label for="phone">手机号</label>
        <input
          type="tel"
          id="phone"
          v-model="form.phone"
          placeholder="请输入手机号"
          @keyup.enter="handleLogin"
        />
      </div>

      <div class="form-group">
        <label for="password">密码</label>
        <input
          type="password"
          id="password"
          v-model="form.password"
          placeholder="请输入密码"
          @keyup.enter="handleLogin"
        />
      </div>

      <button class="btn btn-primary btn-login" @click="handleLogin" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

export default {
  name: 'Login',
  setup() {
    const store = useStore();
    const router = useRouter();
    const loading = ref(false);
    const form = reactive({
      phone: '',
      password: ''
    });

    const handleLogin = async () => {
      // 表单验证
      if (!form.phone) {
        ElMessage.warning('请输入手机号');
        return;
      }
      if (!form.password) {
        ElMessage.warning('请输入密码');
        return;
      }

      try {
        loading.value = true;
        await store.dispatch('user/login', {
          phone: form.phone,
          password: form.password
        });
        
        // 登录成功后获取用户信息
        await store.dispatch('user/getUserInfo');
        
        // 获取会话列表
        await store.dispatch('conversation/getConversations');
        
        // 获取好友列表
        await store.dispatch('friendship/getFriends');
        
        // 获取未读消息数
        await store.dispatch('message/getUnreadMessageCount');
        
        // 获取离线消息
        const messages = await store.dispatch('message/getOfflineMessages');
        if (messages && messages.length > 0) {
          // 确认接收离线消息
          const messageIds = messages.map(msg => msg.messageId);
          await store.dispatch('message/confirmOfflineMessages', messageIds);
        }
        
        ElMessage.success('登录成功');
        router.push('/chat');
      } catch (error) {
        console.error('登录失败:', error);
        ElMessage.error(error.message || '登录失败，请检查账号密码');
      } finally {
        loading.value = false;
      }
    };

    return {
      form,
      loading,
      handleLogin
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: $spacing-6;
  background-color: $white;
}

.login-header {
  text-align: center;
  margin-bottom: $spacing-10;
  
  .login-logo {
    width: 80px;
    height: 80px;
    margin-bottom: $spacing-4;
  }
  
  h1 {
    font-size: $font-size-2xl;
    font-weight: $font-weight-semibold;
    margin-bottom: $spacing-2;
    color: $text-primary;
  }
  
  p {
    font-size: $font-size-base;
    color: $text-secondary;
  }
}

.login-form {
  width: 100%;
  max-width: 320px;
  
  .form-group {
    margin-bottom: $spacing-6;
    
    label {
      display: block;
      margin-bottom: $spacing-2;
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $text-secondary;
    }
    
    input {
      width: 100%;
      padding: $spacing-4;
      border-radius: $border-radius;
      border: 1px solid $border-color;
      font-size: $font-size-base;
      
      &:focus {
        border-color: $primary-color;
        box-shadow: 0 0 0 2px rgba($primary-color, 0.1);
      }
    }
  }
  
  .btn-login {
    width: 100%;
    padding: $spacing-4;
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    margin-top: $spacing-4;
  }
}
</style> 