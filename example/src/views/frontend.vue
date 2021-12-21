<template>
  <a-card :bordered="false">
    <a-row>
      <a-col :span="12">
        <div
          v-html="postToStage.content.content"
          style="text-align: left"
        ></div>
      </a-col>

      <a-col :span="12">
        <PostList
          :data-source="posts"
          :columns="columns"
          @onView="(record) => getById(record.id)"
          scene="frontend"
        />
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
import postApi from "@/api/post";
import PostList from "../components/PostList.vue";
import { postColumns } from "@/core/posts";

export default {
  name: "FrontendPost",
  components: {
    PostList,
  },
  data() {
    return {
      columns: postColumns,
      posts: [],
      pagination: {},
      postToStage: {
        content: {},
      },
    };
  },
  created() {
    this.listPublishedPosts();
  },
  methods: {
    listPublishedPosts() {
      postApi.listByStatus("PUBLISHED", this.pagination).then((res) => {
        this.posts = res.data.content;
      });
    },
    getById(postId) {
      postApi.getById(postId).then((res) => {
        this.postToStage = res.data;
      });
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  display: inline-block;
  margin: 0 10px;
}

a {
  color: #42b983;
}
</style>
