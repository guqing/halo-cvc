<template>
  <a-card>
    标题:{{ post.title }}，当前所处版本: v{{ post.version }}
    <a-row>
      <a-col :span="12">
        <div v-html="prettyHtml" />
      </a-col>
      <a-col :span="12">
        <PostList
          :data-source="data"
          scene="changelog"
          :columns="columns"
          @onView="(record) => onPatchLogView(record.version)"
          @onChangeLog="(record) => listAllContentVersions()"
          @onRollback="(record) => rollbackToVersion(record.version)"
        />
      </a-col>
    </a-row>
  </a-card>
</template>
<script>
import { changelogColumns } from "@/core/posts";
import postApi from "@/api/post";
import PostList from "../components/PostList.vue";
import * as Diff2Html from "diff2html";
import "diff2html/bundles/css/diff2html.min.css";

export default {
  name: "ContentDiffList",
  components: {
    PostList,
  },
  data() {
    return {
      diffs: "",
      data: [],
      diffVisible: false,
      columns: changelogColumns,
      post: {},
      postId: null,
    };
  },
  created() {
    this.postId = this.$route.params.post.id;
    this.post = this.$route.params.post;
    this.listAllContentVersions();
  },
  methods: {
    getContentDiff(postId, version) {
      postApi.getContentDiff(postId, version).then((res) => {
        this.diffs = res.data;
      });
    },
    onPatchLogView(version) {
      this.post.version = version;
      this.getContentDiff(this.postId, version);
    },
    listAllContentVersions() {
      postApi.listAllContentVersions(this.postId).then((res) => {
        this.data = res.data;
      });
    },
    getContentRecord() {
      this.diffVisible = true;
      this.editorVisible = false;
      postApi.getContentRecordById(this.postId).then((res) => {
        const { content, originalContent } = res.data;
        this.post.content = {
          content,
          originalContent,
        };
      });
    },
    rollbackToVersion(version) {
      postApi.rollbackToVersion(this.postId, version).then((res) => {
        this.post = res.data;
        this.$message.success("回退成功");
        this.listAllPostsByPage();
        this.listPublishedPosts();
      });
    },
  },
  computed: {
    prettyHtml() {
      return Diff2Html.html(this.diffs, {
        drawFileList: true,
        matching: "lines",
        //outputFormat: "side-by-side",
      });
    },
  },
};
</script>
