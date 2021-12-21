<template>
  <transition name="showHeader">
    <a-layout-header>
      <div class="logo" />
      <a-menu
        :default-selected-keys="[0]"
        theme="dark"
        mode="horizontal"
        :style="{ lineHeight: '64px' }"
      >
        <a-menu-item v-for="(menu, index) in menus" :key="index">
          <router-link :to="menu.path">
            {{ menu.meta.title || menu.name }}
          </router-link>
        </a-menu-item>
      </a-menu>
    </a-layout-header>
  </transition>
</template>

<script>
export default {
  name: "GlobalHeader",
  props: {
    menus: {
      type: Array,
      required: true,
    },
  },
  data() {
    return {
      visible: true,
      oldScrollTop: 0,
    };
  },
  mounted() {
    document.addEventListener("scroll", this.handleScroll, { passive: true });
  },
  methods: {
    handleScroll() {
      if (!this.autoHideHeader) {
        return;
      }
      const scrollTop =
        document.body.scrollTop + document.documentElement.scrollTop;
      if (!this.ticking) {
        this.ticking = true;
        requestAnimationFrame(() => {
          if (this.oldScrollTop > scrollTop) {
            this.visible = true;
          } else if (scrollTop > 300 && this.visible) {
            this.visible = false;
          } else if (scrollTop < 300 && !this.visible) {
            this.visible = true;
          }
          this.oldScrollTop = scrollTop;
          this.ticking = false;
        });
      }
    },
    toggle() {
      this.$emit("toggle");
    },
  },
  beforeDestroy() {
    document.body.removeEventListener("scroll", this.handleScroll, true);
  },
};
</script>

<style lang="less">
.header-animat {
  position: relative;
  z-index: 999;
}
.showHeader-enter-active {
  transition: all 0.25s ease;
}
.showHeader-leave-active {
  transition: all 0.5s ease;
}
.showHeader-enter,
.showHeader-leave-to {
  opacity: 0;
}
</style>
