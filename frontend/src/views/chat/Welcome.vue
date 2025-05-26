<template>
  <div class="welcome-container">
    <img src="@/assets/chat-illustration.png" alt="Welcome" class="welcome-image" />
    <h2>欢迎使用校园即时通讯</h2>
    <p>选择一个会话开始聊天，或者创建新的会话</p>
    <button class="btn btn-primary" @click="$parent.showAddConversation = true">
      开始新的对话
    </button>
  </div>
</template>

<script>
export default {
  name: 'Welcome'
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.welcome-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: $spacing-6;
  text-align: center;
  background-color: $bg-primary;
  
  .welcome-image {
    width: 200px;
    height: 200px;
    margin-bottom: $spacing-6;
    opacity: 0.8;
  }
  
  h2 {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    margin-bottom: $spacing-4;
    color: $text-primary;
  }
  
  p {
    font-size: $font-size-base;
    color: $text-secondary;
    margin-bottom: $spacing-6;
    max-width: 300px;
  }
  
  .btn {
    padding: $spacing-3 $spacing-8;
  }
}

@media (max-width: 767px) {
  .welcome-container {
    display: none;
  }
}
</style> 