package xyz.guqing.cvs.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.guqing.cvs.model.dto.PostDTO;
import xyz.guqing.cvs.model.dto.PostDetailDTO;
import xyz.guqing.cvs.model.entity.ContentPatchLog;
import xyz.guqing.cvs.model.entity.Post;
import xyz.guqing.cvs.model.enums.PostStatus;
import xyz.guqing.cvs.model.params.ContentParam;
import xyz.guqing.cvs.model.params.PostParam;

/**
 * @author guqing
 * @date 2021-12-18
 */
public interface PostService {

    PostDTO createOrUpdateDraftBy(PostParam postParam);

    Page<Post> pageBy(Pageable pageable);

    Page<Post> pageBy(PostStatus status, Pageable pageable);

    Post updateDraftContent(Integer postId, ContentParam contentParam);

    Post publish(Integer postId);

    PostDetailDTO getById(Integer postId);

    PostDetailDTO getDraftById(Integer postId);

    List<ContentPatchLog> listAllVersionsBy(Integer postId);

    PostDetailDTO rollbackByIdAndVersion(Integer postId, Integer version);

    String getContentDiff(Integer postId, Integer version);
}
