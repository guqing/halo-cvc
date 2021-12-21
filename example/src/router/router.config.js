import { BlankLayout } from "@/layouts";

export const asyncRouterMap = [
  {
    path: "/",
    name: "index",
    component: BlankLayout,
    meta: { title: " 首页" },
    redirect: "/posts",
    children: [
      // dashboard
      {
        path: "/posts",
        name: "PostList",
        component: () => import("@/views/post"),
        meta: {
          title: " 文章列表",
          hiddenHeaderContent: false,
          keepAlive: false,
        },
      },
    ],
  },
];

export const constantRouterMap = [];
