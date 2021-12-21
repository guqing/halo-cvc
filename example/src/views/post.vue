<template>
  <page-view :title="postToStage.title ? postToStage.title : '新文章'" affix>
    <template slot="extra">
      <a-space>
        <a-button @click="handleSaveDraft"> 保存</a-button>
        <a-button type="primary" @click="handlePublish"> 发布</a-button>
      </a-space>
    </template>
    <a-row :gutter="12">
      <a-col :span="24">
        <div class="mb-4">
          <a-input
            v-model="postToStage.title"
            placeholder="请输入文章标题"
            size="large"
          />
        </div>
        <div id="editor" :style="{ height: '300px' }">
          <MarkdownEditor
            :originalContent="postToStage.content.originalContent"
            @onContentChange="onContentChange"
            @onSaveDraft="handleSaveDraft"
          />
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="12" style="margin-top: 30px">
      <a-tabs default-active-key="1">
        <a-tab-pane key="1" tab="管理后台">
          <a-row :gutter="12" style="margin-top: 30px">
            <a-col :span="12">
              文章列表
              <PostList
                :data-source="backend.posts"
                scene="backend"
                :columns="backend.columns"
                @onView="(record) => getDraftById(record.id)"
                @onChangeLog="(record) => listAllContentVersions(record.id)"
                @onEdit="(record) => getDraftById(record.id)"
              />
            </a-col>

            <a-col :span="12">
              文章版本
              <PostList
                :data-source="changelogs.data"
                :columns="changelogs.columns"
                @onView="(record) => getContentRecord(record.id)"
                @onRollback="
                  (record) => rollbackToVersion(record.postId, record.version)
                "
                scene="changelog"
              />
            </a-col>
          </a-row>
        </a-tab-pane>
        <a-tab-pane key="2" tab="前台" force-render>
          <PostList
            :data-source="frontend.posts"
            :columns="frontend.columns"
            @onView="(record) => getById(record.id)"
            scene="frontend"
          />
        </a-tab-pane>
      </a-tabs>
    </a-row>
  </page-view>
</template>

<script>
import MarkdownEditor from "../components/MarkdownEditor.vue";
import { PageView } from "@/layouts";
import postApi from "@/api/post";
import PostList from "../components/PostList.vue";

const postColumns = [
  {
    title: "ID",
    dataIndex: "id",
    key: "id",
  },
  {
    title: "标题",
    dataIndex: "title",
  },
  {
    title: "版本",
    dataIndex: "version",
    customRender: (version) => {
      return `v${version}`;
    },
  },
  {
    title: "状态",
    key: "status",
    scopedSlots: { customRender: "status" },
  },
  {
    title: "操作",
    key: "action",
    scopedSlots: { customRender: "action" },
  },
];

const changelogColumns = [
  {
    title: "ID",
    dataIndex: "id",
    key: "id",
  },
  {
    title: "父ID",
    dataIndex: "sourceId",
  },
  {
    title: "版本",
    dataIndex: "version",
    customRender: (version) => {
      return `v${version}`;
    },
  },
  {
    title: "状态",
    key: "status",
    scopedSlots: { customRender: "status" },
  },
  {
    title: "操作",
    key: "action",
    scopedSlots: { customRender: "action" },
  },
];

export default {
  name: "Post",
  props: {
    msg: String,
  },
  components: {
    MarkdownEditor,
    PostList,
    PageView,
  },
  data() {
    return {
      backend: {
        columns: postColumns,
        posts: [],
        pagination: {},
      },
      changelogs: {
        data: [],
        columns: changelogColumns,
      },
      frontend: {
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
    this.listPublishedPosts();
  },
  methods: {
    listAllPostsByPage() {
      postApi.list().then((res) => {
        this.backend.posts = res.data.content;
      });
    },
    listPublishedPosts() {
      postApi
        .listByStatus("PUBLISHED", this.frontend.pagination)
        .then((res) => {
          this.frontend.posts = res.data.content;
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
    getById(postId) {
      postApi.getById(postId).then((res) => {
        this.postToStage = res.data;
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
    listAllContentVersions(postId) {
      postApi.listAllContentVersions(postId).then((res) => {
        console.log(res);
        this.changelogs.data = res.data;
      });
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
    getContentRecord(id) {
      postApi.getContentRecordById(id).then((res) => {
        console.log(res);
        const { content, originalContent, postId } = res.data;
        this.postToStage.id = postId;
        this.postToStage.content = {
          content,
          originalContent,
        };
      });
    },
    rollbackToVersion(postId, version) {
      postApi.rollbackToVersion(postId, version).then((res) => {
        this.postToStage = res.data;
        this.$message.success("回退成功");
        this.listAllPostsByPage();
        this.listPublishedPosts();
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
