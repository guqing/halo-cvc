<template>
  <div id="app">
    <Post msg="Welcome to Your Vue.js App" />
  </div>
</template>

<script>
import Post from "./components/post.vue";

export default {
  name: "App",
  components: {
    Post,
  },
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
