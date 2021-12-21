<template>
  <a-table
    :columns="columns"
    :data-source="dataSource"
    :pagination="pagination"
    rowKey="id"
    @change="handleTableChange"
  >
    <span slot="status" slot-scope="record">
      <a-tag :color="record.status === 'DRAFT' ? 'volcano' : 'green'">
        {{ record.status === "DRAFT" ? "草稿" : "已发布" }}
      </a-tag>
    </span>
    <span slot="action" slot-scope="text, record">
      <a v-if="actions.view.includes(scene)" @click="handleView(record)">
        查看
      </a>
      <span v-if="actions.edit.includes(scene)">
        <a-divider type="vertical" />
        <a @click="handleEdit(record)">编辑</a>
      </span>
      <span v-if="actions.changelog.includes(scene)">
        <a-divider type="vertical" />
        <a @click="handleChangeLog(record)"> 版本历史 </a>
      </span>
      <span v-if="actions.rollback.includes(scene)">
        <a-divider type="vertical" />
        <a @click="handleRollback(record)">回退</a>
      </span>
    </span>
  </a-table>
</template>
<script>
const scenes = ["backend", "changelog", "frontend"];
const actions = {
  view: [...scenes],
  changelog: ["backend"],
  edit: ["backend"],
  rollback: ["changelog"],
};
export default {
  name: "PostList",
  props: {
    dataSource: {
      type: Array,
      default: () => {
        return [];
      },
    },
    scene: {
      type: String,
      default: "backend",
    },
    columns: {
      type: Array,
      default: () => {
        return [];
      },
    },
  },
  data() {
    return {
      actions,
      pagination: {},
    };
  },
  methods: {
    handleTableChange() {
      this.$emit("onChange", this.pagination);
    },
    handleView(record) {
      this.$emit("onView", record);
    },
    handleChangeLog(record) {
      this.$emit("onChangeLog", record);
    },
    handleEdit(record) {
      this.$emit("onEdit", record);
    },
    handleRollback(record) {
      this.$emit("onRollback", record);
    },
  },
};
</script>
