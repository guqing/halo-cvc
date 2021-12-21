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

export { postColumns, changelogColumns };
