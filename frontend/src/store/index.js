import { createStore } from 'vuex';
import user from './modules/user';
import conversation from './modules/conversation';
import message from './modules/message';
import friendship from './modules/friendship';

export default createStore({
  modules: {
    user,
    conversation,
    message,
    friendship
  }
}); 