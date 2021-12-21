# halo-cvc
Halo 文章内容版本管理(content version control)示例

### 特性
- 增量存储文章内容的不同版本，减小内容占用
- 版本切换，不同版本可自由切换
- 历史版本回收，可删除任意历史版本
- 版本回溯，可方便查看版本衍生路径图例如
![版本路径图](./docs/asserts/版本路径图.png)

### 原理(TODO)

创建或更新文章草稿流程：
![创建或更新文章草稿](./docs/asserts/创建或更新文章草稿.png)

版本 HEAD 切换：
- 创建时指向 v1 的 patchLogId
- 修改时指向草稿的 patchLogId
- 发布时指向当前的 patchLogId
- 版本回退时指向回退版本的 patchLogId
