import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import axios from 'axios';

const app = createApp(App);

axios.defaults.baseURL = 'http://localhost:20041/lab1/api/'
app.config.globalProperties.$axios = axios;

app.use(router).mount('#app');
