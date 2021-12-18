<template>
  <page-view :title="postToStage.title ? postToStage.title : '新文章'" affix>
    <template slot="extra">
      <a-space>
        <a-button> 保存 </a-button>
        <a-button type="primary"> 发布 </a-button>
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
            :originalContent="postToStage.originalContent"
            @onContentChange="onContentChange"
            @onSaveDraft="handleSaveDraft(true)"
          />
        </div>
      </a-col>
    </a-row>
  </page-view>
</template>

<script>
import MarkdownEditor from "./MarkdownEditor.vue";
import { PageView } from "@/layouts";
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
      postToStage: {},
      contentChanges: 0,
    };
  },
  methods: {
    handleSaveDraft() {},
    onContentChange(val) {
      this.contentChanges++;
      this.postToStage.originalContent = val;
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
