<template>
  <div 
    class="avatar" 
    :class="[sizeClass, { 'clickable': clickable }]"
    @click="handleClick"
  >
    <img 
      v-if="src" 
      :src="src" 
      :alt="alt || '头像'" 
      @error="handleError" 
    />
    <div v-else class="avatar-placeholder">
      <span>{{ initials }}</span>
    </div>
    <div v-if="showStatus" class="status-indicator" :class="statusClass"></div>
  </div>
</template>

<script>
import { computed, ref } from 'vue';

export default {
  name: 'Avatar',
  props: {
    src: {
      type: String,
      default: ''
    },
    alt: {
      type: String,
      default: ''
    },
    name: {
      type: String,
      default: ''
    },
    size: {
      type: String,
      default: 'medium', // small, medium, large
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    status: {
      type: String,
      default: '', // online, offline, away, busy
      validator: (value) => ['', 'online', 'offline', 'away', 'busy'].includes(value)
    },
    clickable: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click', 'error'],
  setup(props, { emit }) {
    const loadError = ref(false);

    const initials = computed(() => {
      if (!props.name) return '?';
      
      const nameParts = props.name.split(' ');
      if (nameParts.length === 1) {
        return nameParts[0].charAt(0).toUpperCase();
      } else {
        return (nameParts[0].charAt(0) + nameParts[nameParts.length - 1].charAt(0)).toUpperCase();
      }
    });

    const sizeClass = computed(() => `size-${props.size}`);

    const showStatus = computed(() => props.status !== '');

    const statusClass = computed(() => `status-${props.status}`);

    const handleClick = () => {
      if (props.clickable) {
        emit('click');
      }
    };

    const handleError = (e) => {
      loadError.value = true;
      emit('error', e);
    };

    return {
      initials,
      sizeClass,
      showStatus,
      statusClass,
      handleClick,
      handleError
    };
  }
};
</script>

<style lang="scss" scoped>
.avatar {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  background-color: #e0e0e0;
  
  &.size-small {
    width: 32px;
    height: 32px;
    
    .avatar-placeholder {
      font-size: 14px;
    }
    
    .status-indicator {
      width: 8px;
      height: 8px;
    }
  }
  
  &.size-medium {
    width: 48px;
    height: 48px;
    
    .avatar-placeholder {
      font-size: 18px;
    }
    
    .status-indicator {
      width: 10px;
      height: 10px;
    }
  }
  
  &.size-large {
    width: 64px;
    height: 64px;
    
    .avatar-placeholder {
      font-size: 24px;
    }
    
    .status-indicator {
      width: 12px;
      height: 12px;
    }
  }
  
  &.clickable {
    cursor: pointer;
    
    &:hover {
      opacity: 0.9;
    }
  }
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .avatar-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background-color: #8e8e93;
    color: white;
    font-weight: 500;
  }
  
  .status-indicator {
    position: absolute;
    bottom: 0;
    right: 0;
    border-radius: 50%;
    border: 2px solid white;
    
    &.status-online {
      background-color: #34c759;
    }
    
    &.status-offline {
      background-color: #8e8e93;
    }
    
    &.status-away {
      background-color: #ff9500;
    }
    
    &.status-busy {
      background-color: #ff3b30;
    }
  }
}
</style> 