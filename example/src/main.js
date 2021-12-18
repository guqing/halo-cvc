import Vue from "vue";
import App from "./App.vue";
import "ant-design-vue/dist/antd.css";
import Antd from "ant-design-vue";
import Router from "vue-router";
Vue.config.productionTip = false;
Vue.use(Router);
Vue.use(Antd);

new Vue({
  render: (h) => h(App),
  Router,
}).$mount("#app");
