<template>
  <a-layout :class="['layout']">
    <a-layout :style="{ minHeight: '100vh' }">
      <!-- layout header -->
      <global-header :menus="menus" />

      <!-- layout content -->
      <a-layout-content
        :style="{
          height: '100%',
          margin: '24px 24px 0',
        }"
      >
        <transition name="page-transition">
          <route-view />
        </transition>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script>
import { asyncRouterMap } from "@/router/router.config.js";
import RouteView from "./RouteView";
import GlobalHeader from "./GlobalHeader";
export default {
  name: "BasicLayout",
  components: {
    RouteView,
    GlobalHeader,
  },
  data() {
    return {
      collapsed: false,
      menus: [],
    };
  },
  computed: {
    contentPaddingLeft() {
      return "80px";
    },
  },
  created() {
    this.menus = asyncRouterMap.find((item) => item.path === "/").children;
  },
  mounted() {
    const userAgent = navigator.userAgent;
    if (userAgent.indexOf("Edge") > -1) {
      this.$nextTick(() => {
        this.collapsed = !this.collapsed;
        setTimeout(() => {
          this.collapsed = !this.collapsed;
        }, 16);
      });
    }
  },
  methods: {
    paddingCalc() {
      return "256px";
    },
    menuSelect() {
      if (!this.isDesktop()) {
        this.collapsed = false;
      }
    },
    drawerClose() {
      this.collapsed = false;
    },
  },
};
</script>

<style lang="less">
.page-transition-enter {
  opacity: 0;
}
.page-transition-leave-active {
  opacity: 0;
}
.page-transition-enter .page-transition-container,
.page-transition-leave-active .page-transition-container {
  -webkit-transform: scale(1.1);
  transform: scale(1.1);
}
</style>
