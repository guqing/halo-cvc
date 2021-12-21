<template>
  <a-card>
    <page-view :title="postToStage.title ? postToStage.title : '新文章'" affix>
      <template slot="extra" v-if="editorVisible">
        <a-space>
          <a-button @click="handleSaveDraft"> 保存</a-button>
          <a-button type="primary" @click="handlePublish"> 发布</a-button>
        </a-space>
      </template>
      <a-row :gutter="12">
        <a-col :span="12" v-if="editorVisible">
          <div class="mb-4">
            <a-input
              v-model="postToStage.title"
              placeholder="请输入文章标题"
              size="large"
            />
          </div>
          <div id="editor">
            <MarkdownEditor
              :style="{ height: '500px' }"
              :originalContent="postToStage.content.originalContent"
              @onContentChange="onContentChange"
              @onSaveDraft="handleSaveDraft"
            />
          </div>
        </a-col>
        <a-col :span="12" v-if="!editorVisible">
          <div
            v-html="postToStage.content.content"
            style="text-align: left"
          ></div>
        </a-col>

        <a-col :span="12">
          <a-row :gutter="12" style="margin-top: 30px">
            文章列表
            <PostList
              :data-source="backend.posts"
              scene="backend"
              :columns="backend.columns"
              @onView="(record) => handleView(record.id)"
              @onChangeLog="(record) => handleViewChangeLog(record)"
              @onEdit="(record) => handleEdit(record.id)"
            />
          </a-row>
        </a-col>
      </a-row>
    </page-view>
  </a-card>
</template>

<script>
import MarkdownEditor from "../components/MarkdownEditor.vue";
import postApi from "@/api/post";
import PostList from "../components/PostList.vue";
import { postColumns } from "@/core/posts";
import { PageView } from "@/layouts";

export default {
  name: "Post",
  components: {
    MarkdownEditor,
    PostList,
    PageView,
  },
  data() {
    return {
      editorVisible: true,
      backend: {
        columns: postColumns,
        posts: [],
        pagination: {},
      },
      postToStage: {
        content: {},
      },
      contentChanges: 0,
    };
  },
  created() {
    this.listAllPostsByPage();
  },
  methods: {
    handleView(id) {
      this.editorVisible = false;
      this.getDraftById(id);
    },
    handleEdit(id) {
      this.editorVisible = true;
      this.postToStage = {
        content: {},
      };
      this.getDraftById(id);
    },
    listAllPostsByPage() {
      postApi.list().then((res) => {
        this.backend.posts = res.data.content;
      });
    },
    handleSaveDraft() {
      if (!this.postToStage.title) {
        this.$message.warning("请输入文章标题");
        return;
      }
      postApi.draftPost(this.postToStage).then(() => {
        this.$message.success("保存成功");
        this.postToStage = {
          content: {},
        };
        this.listAllPostsByPage();
      });
    },
    getDraftById(postId) {
      postApi.getDraftById(postId).then((res) => {
        this.postToStage = res.data;
      });
    },
    onContentChange(val, htmlContent) {
      this.contentChanges++;
      this.postToStage.content = {
        originalContent: val,
        content: htmlContent,
      };
    },
    handlePublish() {
      postApi.publish(this.postToStage.id).then((res) => {
        console.log(res);
        this.$message.success("发布成功");
        this.listAllPostsByPage();
        this.listPublishedPosts();
        this.listAllContentVersions(this.postToStage.id);
        this.postToStage = {
          content: {},
        };
      });
    },
    handleViewChangeLog(record) {
      this.$router.push({ name: "ContentDiff", params: { post: record } });
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
