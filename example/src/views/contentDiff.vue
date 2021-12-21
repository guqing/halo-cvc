<template>
  <a-row>
    <content-diff :post-id="post.id" :version="post.version"/>
  </a-row>
</template>
<script>
import ContentDiff from "../components/ContentDiff.vue";

export default {
  name: "ContentDiffList",
  components: {
    ContentDiff,
  },
  data() {
    return {
      post: {}
    }
  }
};
</script>
