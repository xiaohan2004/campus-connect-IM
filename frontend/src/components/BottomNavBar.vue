<template>
  <div class="bottom-nav-bar">
    <router-link to="/chat" class="nav-item" active-class="active">
      <i class="el-icon-chat-dot-round"></i>
      <span>聊天</span>
    </router-link>
    <router-link to="/contacts" class="nav-item" active-class="active">
      <i class="el-icon-user"></i>
      <span>联系人</span>
    </router-link>
    <router-link to="/profile" class="nav-item" active-class="active">
      <i class="el-icon-setting"></i>
      <span>我的</span>
    </router-link>
  </div>
</template>

<script>
export default {
  name: 'BottomNavBar'
};
</script>

<style lang="scss" scoped>
.bottom-nav-bar {
  display: flex;
  height: 56px;
  background-color: #ffffff;
  border-top: 1px solid #e0e0e0;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  
  .nav-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #8e8e93;
    text-decoration: none;
    transition: color 0.2s;
    
    i {
      font-size: 22px;
      margin-bottom: 2px;
    }
    
    span {
      font-size: 12px;
    }
    
    &.active {
      color: #007aff;
    }
    
    &:hover {
      color: #007aff;
    }
  }
}

@media (min-width: 768px) {
  .bottom-nav-bar {
    display: none;
  }
}
</style> 