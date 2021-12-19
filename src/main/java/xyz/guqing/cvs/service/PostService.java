package xyz.guqing.cvs.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.guqing.cvs.model.dto.ContentRecordDTO;
import xyz.guqing.cvs.model.dto.PostDetailDTO;
import xyz.guqing.cvs.model.entity.Content;
import xyz.guqing.cvs.model.entity.ContentRecord;
import xyz.guqing.cvs.model.entity.Post;
import xyz.guqing.cvs.model.enums.PostStatus;
import xyz.guqing.cvs.model.params.ContentParam;
import xyz.guqing.cvs.model.params.PostParam;

/**
 * @author guqing
 * @date 2021-12-18
 */
public interface PostService {

    PostDetailDTO createOrUpdateDraftBy(PostParam postParam);

    Page<Post> pageBy(Pageable pageable);

    Page<Post> pageBy(PostStatus status, Pageable pageable);

    Post updateDraftContent(Integer postId, ContentParam contentParam);

    Post publish(Integer postId);

    PostDetailDTO getById(Integer postId);

    PostDetailDTO getDraftById(Integer postId);

    List<ContentRecord> listAllVersionsBy(Integer postId);

    ContentRecord getContentRecordById(Integer contentRecordId);

    PostDetailDTO rollbackByIdAndVersion(Integer postId, Integer version);
}
