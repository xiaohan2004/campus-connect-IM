<template>
  <div class="empty-state">
    <div class="empty-icon">
      <i v-if="icon" :class="icon"></i>
      <slot name="icon" v-else></slot>
    </div>
    <h3 class="empty-title">{{ title }}</h3>
    <p class="empty-description">{{ description }}</p>
    <div v-if="$slots.action || actionText" class="empty-action">
      <slot name="action">
        <button v-if="actionText" class="action-button" @click="handleAction">
          {{ actionText }}
        </button>
      </slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'EmptyState',
  props: {
    icon: {
      type: String,
      default: 'el-icon-info'
    },
    title: {
      type: String,
      default: '没有数据'
    },
    description: {
      type: String,
      default: '暂时没有任何数据可以显示'
    },
    actionText: {
      type: String,
      default: ''
    }
  },
  emits: ['action'],
  setup(props, { emit }) {
    const handleAction = () => {
      emit('action');
    };

    return {
      handleAction
    };
  }
};
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  text-align: center;
  
  .empty-icon {
    font-size: 48px;
    color: #c8c7cc;
    margin-bottom: 16px;
    
    i {
      font-size: inherit;
    }
  }
  
  .empty-title {
    font-size: 18px;
    font-weight: 600;
    color: #000;
    margin: 0 0 8px;
  }
  
  .empty-description {
    font-size: 14px;
    color: #8e8e93;
    margin: 0 0 24px;
    max-width: 300px;
  }
  
  .empty-action {
    .action-button {
      background-color: #007aff;
      color: white;
      border: none;
      border-radius: 8px;
      padding: 8px 16px;
      font-size: 16px;
      font-weight: 500;
      cursor: pointer;
      transition: background-color 0.2s;
      
      &:hover {
        background-color: #0066cc;
      }
      
      &:active {
        background-color: #0055b3;
      }
    }
  }
}
</style> 