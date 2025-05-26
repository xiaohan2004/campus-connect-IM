<template>
  <div class="nav-bar" :class="{ 'with-border': showBorder }">
    <!-- 左侧按钮区域 -->
    <div class="left-actions">
      <div v-if="showBack" class="back-button" @click="handleBack">
        <i class="el-icon-arrow-left"></i>
        <span v-if="backText">{{ backText }}</span>
      </div>
      <slot name="left"></slot>
    </div>

    <!-- 标题区域 -->
    <div class="title" :class="{ 'center': centerTitle }">
      <slot name="title">
        {{ title }}
      </slot>
    </div>

    <!-- 右侧按钮区域 -->
    <div class="right-actions">
      <slot name="right"></slot>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';

export default {
  name: 'NavBar',
  props: {
    title: {
      type: String,
      default: ''
    },
    showBack: {
      type: Boolean,
      default: false
    },
    backText: {
      type: String,
      default: ''
    },
    centerTitle: {
      type: Boolean,
      default: true
    },
    showBorder: {
      type: Boolean,
      default: true
    },
    backPath: {
      type: [String, Object],
      default: null
    }
  },
  emits: ['back'],
  setup(props, { emit }) {
    const router = useRouter();

    const handleBack = () => {
      emit('back');
      
      if (props.backPath) {
        router.push(props.backPath);
      } else {
        router.back();
      }
    };

    return {
      handleBack
    };
  }
};
</script>

<style lang="scss" scoped>
.nav-bar {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 16px;
  position: relative;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  z-index: 10;
  
  &.with-border {
    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  }
  
  .left-actions {
    display: flex;
    align-items: center;
    min-width: 60px;
    justify-content: flex-start;
    
    .back-button {
      display: flex;
      align-items: center;
      color: #007aff;
      font-size: 16px;
      cursor: pointer;
      
      i {
        font-size: 20px;
        margin-right: 4px;
      }
    }
  }
  
  .title {
    flex: 1;
    font-size: 17px;
    font-weight: 600;
    color: #000;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    
    &.center {
      position: absolute;
      left: 50%;
      transform: translateX(-50%);
      text-align: center;
      max-width: 60%;
    }
  }
  
  .right-actions {
    display: flex;
    align-items: center;
    min-width: 60px;
    justify-content: flex-end;
  }
}
</style> 