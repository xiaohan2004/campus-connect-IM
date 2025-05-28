<template>
  <div class="reset-container">
    <div class="reset-card">
      <div class="reset-header">
        <img src="@/assets/logo.png" alt="Logo" class="reset-logo" />
        <h1>找回密码</h1>
        <p>重置您的账号密码</p>
      </div>

      <div class="reset-form">
        <div class="form-grid">
          <div class="form-group">
            <label for="email">邮箱</label>
            <input
              type="email"
              id="email"
              v-model="form.email"
              placeholder="请输入注册邮箱"
            />
          </div>

          <div class="form-group">
            <label for="phone">手机号</label>
            <input
              type="tel"
              id="phone"
              v-model="form.phone"
              placeholder="请输入注册手机号"
            />
          </div>

          <div class="form-group verification-code">
            <label for="verificationCode">验证码</label>
            <div class="code-input-group">
              <input
                type="text"
                id="verificationCode"
                v-model="form.code"
                placeholder="请输入验证码"
              />
              <button 
                class="btn btn-outline btn-code" 
                @click="sendVerificationCode" 
                :disabled="cooldown > 0"
              >
                {{ cooldown > 0 ? `${cooldown}秒后重试` : '获取验证码' }}
              </button>
            </div>
          </div>

          <div class="form-group">
            <label for="password">新密码</label>
            <input
              type="password"
              id="password"
              v-model="form.password"
              placeholder="请设置新密码，至少6位"
            />
          </div>

          <div class="form-group">
            <label for="confirmPassword">确认新密码</label>
            <input
              type="password"
              id="confirmPassword"
              v-model="form.confirmPassword"
              placeholder="请再次输入新密码"
            />
          </div>
        </div>

        <button class="btn btn-primary btn-reset" @click="handleReset" :disabled="loading">
          {{ loading ? '重置中...' : '重置密码' }}
        </button>
        
        <div class="reset-options">
          <router-link to="/login" class="reset-link">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { resetPassword, sendEmailCode } from '@/api/user';

export default {
  name: 'ResetPassword',
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const cooldown = ref(0);
    const form = reactive({
      email: '',
      phone: '',
      code: '',
      password: '',
      confirmPassword: ''
    });

    // 发送验证码
    const sendVerificationCode = async () => {
      // 邮箱验证
      if (!form.email) {
        ElMessage.warning('请输入邮箱');
        return;
      }
      
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!emailRegex.test(form.email)) {
        ElMessage.warning('请输入正确的邮箱格式');
        return;
      }

      try {
        await sendEmailCode(form.email, 'reset');
        ElMessage.success('验证码发送成功，请查收邮件');
        
        // 开始倒计时
        cooldown.value = 60;
        const timer = setInterval(() => {
          cooldown.value--;
          if (cooldown.value <= 0) {
            clearInterval(timer);
          }
        }, 1000);
      } catch (error) {
        console.error('发送验证码失败:', error);
        ElMessage.error(error.message || '发送验证码失败，请稍后重试');
      }
    };

    // 重置密码
    const handleReset = async () => {
      // 表单验证
      if (!form.email) {
        ElMessage.warning('请输入邮箱');
        return;
      }
      
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!emailRegex.test(form.email)) {
        ElMessage.warning('请输入正确的邮箱格式');
        return;
      }
      
      if (!form.phone) {
        ElMessage.warning('请输入手机号');
        return;
      }
      
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (!phoneRegex.test(form.phone)) {
        ElMessage.warning('请输入正确的手机号');
        return;
      }
      
      if (!form.code) {
        ElMessage.warning('请输入验证码');
        return;
      }
      
      if (!form.password) {
        ElMessage.warning('请设置新密码');
        return;
      }
      
      if (form.password.length < 6) {
        ElMessage.warning('密码长度至少为6位');
        return;
      }
      
      if (form.password !== form.confirmPassword) {
        ElMessage.warning('两次输入的密码不一致');
        return;
      }

      try {
        loading.value = true;
        await resetPassword({
          email: form.email,
          phone: form.phone,
          verificationCode: form.code,
          newPassword: form.password
        });
        
        ElMessage.success('密码重置成功，请使用新密码登录');
        router.push('/login');
      } catch (error) {
        console.error('密码重置失败:', error);
        ElMessage.error(error.message || '密码重置失败，请稍后重试');
      } finally {
        loading.value = false;
      }
    };

    return {
      form,
      loading,
      cooldown,
      sendVerificationCode,
      handleReset
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.reset-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: $spacing-4;
  background-color: $bg-secondary;
}

.reset-card {
  width: 100%;
  max-width: 480px;
  background-color: $white;
  border-radius: $border-radius-lg;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: $spacing-8;
  margin: $spacing-4 0;
}

.reset-header {
  text-align: center;
  margin-bottom: $spacing-6;
  
  .reset-logo {
    width: 60px;
    height: 60px;
    margin-bottom: $spacing-3;
  }
  
  h1 {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    margin-bottom: $spacing-2;
    color: $text-primary;
  }
  
  p {
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.reset-form {
  .form-grid {
    display: grid;
    grid-template-columns: repeat(1, 1fr);
    gap: $spacing-4;
    
    @media (min-width: 480px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  
  .form-group {
    margin-bottom: $spacing-4;
    
    &.verification-code {
      grid-column: 1 / -1;
    }
    
    label {
      display: block;
      margin-bottom: $spacing-2;
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $text-secondary;
    }
    
    input {
      width: 100%;
      padding: $spacing-3;
      border-radius: $border-radius;
      border: 1px solid $border-color;
      font-size: $font-size-base;
      
      &:focus {
        border-color: $primary-color;
        box-shadow: 0 0 0 2px rgba($primary-color, 0.1);
      }
    }
    
    &.verification-code {
      .code-input-group {
        display: flex;
        gap: $spacing-2;
        
        input {
          flex: 1;
        }
        
        .btn-code {
          white-space: nowrap;
          font-size: $font-size-sm;
          padding: $spacing-2 $spacing-3;
          min-width: 120px;
        }
      }
    }
  }
  
  .btn-reset {
    width: 100%;
    padding: $spacing-3;
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    margin-top: $spacing-2;
    margin-bottom: $spacing-4;
  }
  
  .reset-options {
    display: flex;
    justify-content: center;
    
    .reset-link {
      color: $primary-color;
      font-size: $font-size-sm;
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style> 