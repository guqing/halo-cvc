<template>
  <page-view :title="postToStage.title ? postToStage.title : '新文章'" affix>
    <template slot="extra">
      <a-space>
        <a-button @click="handleSaveDraft"> 保存 </a-button>
        <a-button type="primary" @click="handlePublish"> 发布 </a-button>
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
      <a-col :span="8">
        管理后台文章列表
        <a-list :data-source="backend.posts" :pagination="backend.pagination">
          <a-list-item slot="renderItem" slot-scope="item" style="width: 100%">
            <div @click="listAllContentVersions(item.id)">
              id:{{ item.id }} - title:{{ item.title }} - version:{{ item.version }} - status:
              {{ item.status === 'DRAFT' ? '草稿' : '已发布' }}
              <a
                  slot="actions"
                  style="margin-left: 15px"
                  @click="getDraftById(item.id)"
              >
                edit
              </a>
            </div>
          </a-list-item>
        </a-list>
      </a-col>
      <a-col :span="8">
        文章版本列表
        <a-list :data-source="backend.versions">
          <a-list-item slot="renderItem" slot-scope="item" style="width: 100%">
            <div>
              version:{{ item.version }} - sourceId:{{ item.sourceId }}
              <a
                slot="actions"
                style="margin-left: 15px"
                @click="getContentRecord(item.id)"
              >
                查看
              </a>
              <a
                slot="actions"
                style="margin-left: 15px"
                @click="rollbackToVersion(item.postId, item.version)"
              >
                回退
              </a>
            </div>
          </a-list-item>
        </a-list>
      </a-col>
      <a-col :span="8">
        前台文章列表
        <a-list :data-source="frontend.posts" :pagination="frontend.pagination">
          <a-list-item slot="renderItem" slot-scope="item">
            <p>
              {{ item.id }} - {{ item.title }} - {{ item.version }}
              <a
                slot="actions"
                style="margin-left: 15px"
                @click="getById(item.id)"
                >edit</a
              >
            </p>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>
  </page-view>
</template>

<script>
import MarkdownEditor from "./MarkdownEditor.vue";
import {PageView} from "@/layouts";
import postApi from "@/api/post";

export default {
  name: "Post",
  props: {
    msg: String,
  },
  components: {
    MarkdownEditor,
    PageView,
  },
  data() {
    return {
      backend: {
        posts: [],
        versions: [],
        pagination: {
          onChange: (page) => {
            console.log(page);
            this.listAllPostsByPage();
          },
          pageSize: 10,
        },
      },
      frontend: {
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
          content: {}
        }
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
    onContentChange(val) {
      this.contentChanges++;
      this.postToStage.content = {
        originalContent: val,
        content: val,
      };
    },
    listAllContentVersions(postId) {
      postApi.listAllContentVersions(postId).then((res) => {
        console.log(res);
        this.backend.versions = res.data;
      });
    },
    handlePublish() {
      postApi.publish(this.postToStage.id).then((res) => {
        console.log(res);
        this.$message.success("发布成功");
        this.listAllPostsByPage();
        this.listPublishedPosts();
        this.postToStage = {
          content: {}
        }
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
