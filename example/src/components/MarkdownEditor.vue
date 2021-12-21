<template>
  <halo-editor
    ref="md"
    v-model="originalContentData"
    :boxShadow="false"
    :ishljs="true"
    :toolbars="toolbars"
    autofocus
    @imgAdd="handleAttachmentUpload"
    @save="handleSaveDraft"
  />
</template>
<script>
import { toolbars } from "@/core/const";
import { haloEditor } from "halo-editor";
import "halo-editor/dist/css/index.css";

export default {
  name: "MarkdownEditor",
  components: {
    haloEditor,
  },
  props: {
    originalContent: {
      type: String,
      required: false,
      default: "",
    },
  },
  data() {
    return {
      toolbars,
      originalContentData: "",
      renderContent: ""
    };
  },
  watch: {
    originalContent(val) {
      this.originalContentData = val;
    },
    originalContentData(val) {
      this.renderContent = this.$refs.md.d_render
      this.$emit("onContentChange", val, this.renderContent);
    },
  },
  methods: {
    handleAttachmentUpload() {
      throw new Error("暂未实现该功能");
    },
    handleSaveDraft() {
      this.$emit("onSaveDraft");
    },
  },
};
</script>
