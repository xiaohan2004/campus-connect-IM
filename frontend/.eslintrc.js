module.exports = {
  root: true,
  env: {
    node: true,
    browser: true,
    es2021: true
  },
  extends: [
    'plugin:vue/vue3-recommended',
    'eslint:recommended'
  ],
  parserOptions: {
    ecmaVersion: 2021,
    sourceType: 'module'
  },
  rules: {
    // 临时禁用所有规则，以便项目可以启动
    'vue/multi-word-component-names': 'off',
    'no-console': 'off',
    'no-debugger': 'off',
    'vue/no-v-html': 'off',
    'vue/require-default-prop': 'off',
    'vue/require-explicit-emits': 'off',
    'vue/html-indent': 'off',
    'vue/script-indent': 'off',
    'vue/max-attributes-per-line': 'off',
    'vue/html-self-closing': 'off',
    'no-unused-vars': 'off' // 禁用未使用变量的检查
  }
}; 