import { createStore } from 'vuex';
import user from './modules/user';
import conversation from './modules/conversation';
import message from './modules/message';
import friendship from './modules/friendship';
import group from './modules/group';

export default createStore({
  modules: {
    user,
    conversation,
    message,
    friendship,
    group
  }
}); 