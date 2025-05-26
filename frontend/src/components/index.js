import Avatar from './Avatar.vue';
import BottomNavBar from './BottomNavBar.vue';
import ConversationItem from './ConversationItem.vue';
import EmptyState from './EmptyState.vue';
import LoadingIndicator from './LoadingIndicator.vue';
import MessageBubble from './MessageBubble.vue';
import NavBar from './NavBar.vue';
import SearchBar from './SearchBar.vue';

export {
  Avatar,
  BottomNavBar,
  ConversationItem,
  EmptyState,
  LoadingIndicator,
  MessageBubble,
  NavBar,
  SearchBar
};

// 全局注册组件
export default {
  install(app) {
    app.component('Avatar', Avatar);
    app.component('BottomNavBar', BottomNavBar);
    app.component('ConversationItem', ConversationItem);
    app.component('EmptyState', EmptyState);
    app.component('LoadingIndicator', LoadingIndicator);
    app.component('MessageBubble', MessageBubble);
    app.component('NavBar', NavBar);
    app.component('SearchBar', SearchBar);
  }
}; 