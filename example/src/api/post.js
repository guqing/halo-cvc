import { axios } from "@/core/request";

const postApi = {};

postApi.draftPost = (params) => {
  return axios({
    url: "/posts",
    method: "post",
    data: params,
  });
};

postApi.getById = (id) => {
  return axios({
    url: `/posts/${id}`,
    method: "get",
  });
};

postApi.getDraftById = (id) => {
  return axios({
    url: `/posts/${id}/drafts`,
    method: "get",
  });
};

postApi.list = (params) => {
  return axios({
    url: "/posts",
    method: "get",
    params: params,
  });
};

postApi.listByStatus = (status, params) => {
  return axios({
    url: `/posts/status/${status}`,
    method: "get",
    params: params,
  });
};

postApi.updateDraftContent = (postId, params) => {
  return axios({
    url: `/posts/${postId}/status/draft/content`,
    method: "get",
    params: params,
  });
};

postApi.publish = (postId) => {
  return axios({
    url: `/posts/${postId}/publish`,
    method: "post",
  });
};

postApi.listAllContentVersions = (postId) => {
  return axios({
    url: `/posts/${postId}/versions/contents`,
    method: "get",
  });
};

postApi.getContentRecordById = (contentRecordId) => {
  return axios({
    url: `/posts/contents/records/${contentRecordId}`,
    method: "get",
  });
};

postApi.rollbackToVersion = (postId, version) => {
  return axios({
    url: `/posts/${postId}/versions/${version}/rollback`,
    method: "put",
  });
};
postApi.getContentDiff = (postId, version) => {
  return axios({
    url: `/posts/${postId}/versions/${version}/diffs`,
    method: "get",
  });
};
export default postApi;
