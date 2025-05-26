<template>
  <div class="search-bar">
    <div class="search-input-wrapper">
      <i class="el-icon-search search-icon"></i>
      <input
        ref="inputRef"
        type="text"
        :placeholder="placeholder"
        v-model="inputValue"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      <i 
        v-if="inputValue && isFocused" 
        class="el-icon-close clear-icon"
        @click="clearInput"
      ></i>
    </div>
    <div v-if="showCancel" class="cancel-button" @click="handleCancel">
      取消
    </div>
  </div>
</template>

<script>
import { ref, watch } from 'vue';

export default {
  name: 'SearchBar',
  props: {
    placeholder: {
      type: String,
      default: '搜索'
    },
    value: {
      type: String,
      default: ''
    },
    debounce: {
      type: Number,
      default: 300
    },
    showCancelButton: {
      type: Boolean,
      default: true
    }
  },
  emits: ['update:value', 'search', 'clear', 'focus', 'blur', 'cancel'],
  setup(props, { emit }) {
    const inputValue = ref(props.value);
    const isFocused = ref(false);
    const showCancel = ref(false);
    const inputRef = ref(null);
    let debounceTimer = null;

    watch(() => props.value, (newValue) => {
      inputValue.value = newValue;
    });

    const handleInput = () => {
      emit('update:value', inputValue.value);
      
      if (debounceTimer) {
        clearTimeout(debounceTimer);
      }
      
      debounceTimer = setTimeout(() => {
        emit('search', inputValue.value);
      }, props.debounce);
    };

    const handleFocus = () => {
      isFocused.value = true;
      if (props.showCancelButton) {
        showCancel.value = true;
      }
      emit('focus');
    };

    const handleBlur = () => {
      isFocused.value = false;
      // 延迟隐藏取消按钮，以便可以点击
      setTimeout(() => {
        if (!isFocused.value) {
          showCancel.value = false;
        }
      }, 200);
      emit('blur');
    };

    const clearInput = () => {
      inputValue.value = '';
      emit('update:value', '');
      emit('clear');
      emit('search', '');
      // 聚焦回输入框
      inputRef.value.focus();
    };

    const handleCancel = () => {
      inputValue.value = '';
      emit('update:value', '');
      emit('clear');
      emit('search', '');
      emit('cancel');
      // 失去焦点
      inputRef.value.blur();
      showCancel.value = false;
    };

    return {
      inputValue,
      isFocused,
      showCancel,
      inputRef,
      handleInput,
      handleFocus,
      handleBlur,
      clearInput,
      handleCancel
    };
  }
};
</script>

<style lang="scss" scoped>
.search-bar {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  transition: all 0.3s;
  
  .search-input-wrapper {
    flex: 1;
    display: flex;
    align-items: center;
    height: 36px;
    background-color: #f2f2f7;
    border-radius: 10px;
    padding: 0 10px;
    position: relative;
    transition: all 0.3s;
    
    .search-icon {
      color: #8e8e93;
      font-size: 18px;
      margin-right: 8px;
    }
    
    input {
      flex: 1;
      height: 100%;
      border: none;
      background-color: transparent;
      font-size: 16px;
      color: #000;
      outline: none;
      
      &::placeholder {
        color: #8e8e93;
      }
    }
    
    .clear-icon {
      color: #8e8e93;
      font-size: 16px;
      padding: 4px;
      cursor: pointer;
      border-radius: 50%;
      
      &:hover {
        background-color: rgba(0, 0, 0, 0.1);
      }
    }
  }
  
  .cancel-button {
    color: #007aff;
    font-size: 16px;
    padding-left: 16px;
    cursor: pointer;
    white-space: nowrap;
  }
}
</style> 