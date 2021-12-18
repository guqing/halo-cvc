package xyz.guqing.cvs.service;

import xyz.guqing.cvs.model.dto.PostDetailDTO;
import xyz.guqing.cvs.model.entity.Content;
import xyz.guqing.cvs.model.entity.Post;
import xyz.guqing.cvs.model.params.PostParam;

/**
 * @author guqing
 * @date 2021-12-18
 */
public interface PostService {

    PostDetailDTO createOrUpdateDraftBy(PostParam postParam);
}
