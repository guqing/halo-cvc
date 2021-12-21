import { BasicLayout } from "@/layouts";

export const asyncRouterMap = [
  {
    path: "/",
    name: "index",
    component: BasicLayout,
    meta: { title: " 首页" },
    redirect: "/posts",
    children: [
      {
        path: "/posts",
        name: "PostList",
        component: () => import("@/views/post"),
        meta: {
          title: " 文章管理",
          hiddenHeaderContent: false,
          keepAlive: false,
        },
      },
      {
        path: "/frontend/posts",
        name: "FrontendPostList",
        component: () => import("@/views/frontend"),
        meta: {
          title: " 文章前台列表",
          hiddenHeaderContent: false,
          keepAlive: false,
        },
      },
    ],
  },
];

export const constantRouterMap = [
  {
    path: "/contents",
    name: "Content",
    hidden: true,
    component: BasicLayout,
    redirect: "/contents/diff",
    meta: { title: "文章", icon: "form" },
    children: [
      {
        path: "/contents/diff",
        name: "ContentDiff",
        component: () => import("@/views/contentDiff"),
        meta: { title: "所有文章", hiddenHeaderContent: false },
      },
    ],
  },
];
