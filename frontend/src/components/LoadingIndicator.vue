<template>
  <div class="loading-indicator" :class="[`size-${size}`, { 'with-text': text }]">
    <div class="spinner">
      <svg viewBox="0 0 50 50">
        <circle
          class="path"
          cx="25"
          cy="25"
          r="20"
          fill="none"
          stroke-width="4"
        />
      </svg>
    </div>
    <div v-if="text" class="loading-text">{{ text }}</div>
  </div>
</template>

<script>
export default {
  name: 'LoadingIndicator',
  props: {
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    text: {
      type: String,
      default: ''
    }
  }
};
</script>

<style lang="scss" scoped>
.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  &.with-text {
    .spinner {
      margin-bottom: 12px;
    }
  }
  
  &.size-small {
    .spinner {
      width: 24px;
      height: 24px;
    }
    
    .loading-text {
      font-size: 12px;
    }
  }
  
  &.size-medium {
    .spinner {
      width: 36px;
      height: 36px;
    }
    
    .loading-text {
      font-size: 14px;
    }
  }
  
  &.size-large {
    .spinner {
      width: 48px;
      height: 48px;
    }
    
    .loading-text {
      font-size: 16px;
    }
  }
  
  .spinner {
    position: relative;
    
    svg {
      animation: rotate 2s linear infinite;
      transform-origin: center center;
      width: 100%;
      height: 100%;
      position: absolute;
      top: 0;
      left: 0;
      
      .path {
        stroke: #007aff;
        stroke-dasharray: 90, 150;
        stroke-dashoffset: 0;
        stroke-linecap: round;
        animation: dash 1.5s ease-in-out infinite;
      }
    }
  }
  
  .loading-text {
    color: #8e8e93;
  }
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}
</style> 