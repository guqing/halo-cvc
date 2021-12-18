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
    };
  },
  watch: {
    originalContent(val) {
      this.originalContentData = val;
    },
    originalContentData(val) {
      this.$emit("onContentChange", val);
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
