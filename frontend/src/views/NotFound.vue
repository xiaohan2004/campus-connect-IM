<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-message">您访问的页面不存在或已被删除</p>
      <button class="btn btn-primary" @click="goHome">
        返回首页
      </button>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';

export default {
  name: 'NotFound',
  setup() {
    const router = useRouter();
    
    const goHome = () => {
      router.push('/');
    };
    
    return {
      goHome
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.not-found-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: $bg-primary;
}

.not-found-content {
  text-align: center;
  padding: $spacing-6;
  
  .error-code {
    font-size: 120px;
    font-weight: $font-weight-bold;
    color: $primary-color;
    margin: 0;
    line-height: 1;
  }
  
  .error-title {
    font-size: $font-size-2xl;
    font-weight: $font-weight-semibold;
    margin: $spacing-4 0;
  }
  
  .error-message {
    font-size: $font-size-base;
    color: $text-secondary;
    margin-bottom: $spacing-6;
  }
  
  .btn {
    padding: $spacing-3 $spacing-8;
  }
}
</style> 