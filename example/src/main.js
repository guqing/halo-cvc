import Vue from "vue";
import App from "./App.vue";
import "ant-design-vue/dist/antd.css";
import Antd from "ant-design-vue";
import Router from "vue-router";
import { VueAxios } from "@/core/request";

Vue.config.productionTip = false;
Vue.use(Router);
Vue.use(Antd);
Vue.use(VueAxios);

new Vue({
  render: (h) => h(App),
  Router,
}).$mount("#app");
