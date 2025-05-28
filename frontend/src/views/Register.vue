<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <img src="@/assets/logo.png" alt="Logo" class="register-logo" />
        <h1>注册账号</h1>
        <p>加入校园即时通讯平台</p>
      </div>

      <div class="register-form">
        <div class="form-grid">
          <!-- 基本信息 -->
          <div class="form-group">
            <label for="nickname">昵称</label>
            <input
              type="text"
              id="nickname"
              v-model="form.nickname"
              placeholder="请输入昵称"
            />
          </div>

          <div class="form-group">
            <label for="phone">手机号</label>
            <input
              type="tel"
              id="phone"
              v-model="form.phone"
              placeholder="请输入手机号"
            />
          </div>

          <div class="form-group">
            <label for="email">邮箱</label>
            <input
              type="email"
              id="email"
              v-model="form.email"
              placeholder="请输入邮箱"
            />
          </div>

          <!-- 验证码 -->
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

          <!-- 密码 -->
          <div class="form-group">
            <label for="password">密码</label>
            <input
              type="password"
              id="password"
              v-model="form.password"
              placeholder="请设置密码，至少6位"
            />
          </div>

          <div class="form-group">
            <label for="confirmPassword">确认密码</label>
            <input
              type="password"
              id="confirmPassword"
              v-model="form.confirmPassword"
              placeholder="请再次输入密码"
            />
          </div>
        </div>

        <!-- 用户身份选择 -->
        <div class="form-group user-type">
          <label>用户身份</label>
          <div class="radio-group">
            <div class="radio-item">
              <input 
                type="radio" 
                id="userType1" 
                value="STUDENT" 
                v-model="form.userType"
              />
              <label for="userType1">校内用户</label>
            </div>
            <div class="radio-item">
              <input 
                type="radio" 
                id="userType2" 
                value="MERCHANT" 
                v-model="form.userType"
              />
              <label for="userType2">商家</label>
            </div>
          </div>
        </div>

        <!-- 注册按钮 -->
        <button class="btn btn-primary btn-register" @click="handleRegister" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
        
        <div class="register-options">
          <span>已有账号？</span>
          <router-link to="/login" class="register-link">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { register, sendEmailCode } from '@/api/user';

export default {
  name: 'Register',
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const cooldown = ref(0);
    const form = reactive({
      nickname: '',
      userType: 'STUDENT', // 默认为校内用户
      phone: '',
      email: '',
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
      
      // 校内用户邮箱必须以@st.usst.edu.cn结尾
      if (form.userType === 'STUDENT') {
        const studentEmailRegex = /@st\.usst\.edu\.cn$/;
        if (!studentEmailRegex.test(form.email)) {
          ElMessage.warning('校内用户邮箱必须以@st.usst.edu.cn结尾');
          return;
        }
      } else {
        // 商家邮箱仅进行通用格式验证
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(form.email)) {
          ElMessage.warning('请输入正确的邮箱格式');
          return;
        }
      }

      try {
        await sendEmailCode(form.email, 'register');
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

    // 注册
    const handleRegister = async () => {
      // 表单验证
      if (!form.nickname) {
        ElMessage.warning('请输入昵称');
        return;
      }
      
      if (!form.userType) {
        ElMessage.warning('请选择用户身份');
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
      
      if (!form.email) {
        ElMessage.warning('请输入邮箱');
        return;
      }
      
      // 校内用户邮箱必须以@st.usst.edu.cn结尾
      if (form.userType === 'STUDENT') {
        const studentEmailRegex = /@st\.usst\.edu\.cn$/;
        if (!studentEmailRegex.test(form.email)) {
          ElMessage.warning('校内用户邮箱必须以@st.usst.edu.cn结尾');
          return;
        }
      } else {
        // 商家邮箱仅进行通用格式验证
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(form.email)) {
          ElMessage.warning('请输入正确的邮箱格式');
          return;
        }
      }
      
      if (!form.code) {
        ElMessage.warning('请输入验证码');
        return;
      }
      
      if (!form.password) {
        ElMessage.warning('请设置密码');
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
        await register({
          nickname: form.nickname,
          userType: form.userType,
          phone: form.phone,
          email: form.email,
          verificationCode: form.code,
          password: form.password
        });
        
        ElMessage.success('注册成功，请登录');
        router.push('/login');
      } catch (error) {
        console.error('注册失败:', error);
        ElMessage.error(error.message || '注册失败，请稍后重试');
      } finally {
        loading.value = false;
      }
    };

    return {
      form,
      loading,
      cooldown,
      sendVerificationCode,
      handleRegister
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.register-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: $spacing-4;
  background-color: $bg-secondary;
}

.register-card {
  width: 100%;
  max-width: 480px;
  background-color: $white;
  border-radius: $border-radius-lg;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: $spacing-8;
  margin: $spacing-4 0;
}

.register-header {
  text-align: center;
  margin-bottom: $spacing-6;
  
  .register-logo {
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

.register-form {
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
    
    &.user-type {
      grid-column: 1 / -1;
      margin-top: $spacing-2;
      margin-bottom: $spacing-6;
    }
    
    .radio-group {
      display: flex;
      gap: $spacing-6;
      margin-top: $spacing-2;
      
      .radio-item {
        display: flex;
        align-items: center;
        
        input[type="radio"] {
          width: auto;
          margin-right: $spacing-2;
        }
        
        label {
          margin-bottom: 0;
          cursor: pointer;
        }
      }
    }
    
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
  
  .btn-register {
    width: 100%;
    padding: $spacing-3;
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    margin-bottom: $spacing-4;
  }
  
  .register-options {
    display: flex;
    justify-content: center;
    font-size: $font-size-sm;
    color: $text-secondary;
    
    .register-link {
      color: $primary-color;
      margin-left: $spacing-1;
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style> 