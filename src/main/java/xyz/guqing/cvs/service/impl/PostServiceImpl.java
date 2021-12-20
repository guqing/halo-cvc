package xyz.guqing.cvs.service.impl;

import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import xyz.guqing.cvs.model.dto.ContentDTO;
import xyz.guqing.cvs.model.dto.PostDTO;
import xyz.guqing.cvs.model.dto.PostDetailDTO;
import xyz.guqing.cvs.model.entity.Content;
import xyz.guqing.cvs.model.entity.ContentPatchLog;
import xyz.guqing.cvs.model.entity.Post;
import xyz.guqing.cvs.model.enums.PostStatus;
import xyz.guqing.cvs.model.params.ContentParam;
import xyz.guqing.cvs.model.params.PostParam;
import xyz.guqing.cvs.model.support.PatchedContent;
import xyz.guqing.cvs.repository.ContentPatchLogRepository;
import xyz.guqing.cvs.repository.ContentRepository;
import xyz.guqing.cvs.repository.PostRepository;
import xyz.guqing.cvs.service.ContentPatchLogService;
import xyz.guqing.cvs.service.PostService;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private ContentPatchLogRepository contentPatchLogRepository;

    @Autowired
    private ContentPatchLogService contentPatchLogService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public PostDTO createOrUpdateDraftBy(PostParam postParam) {
        ContentParam contentParam = postParam.getContent();
        Integer postId = postParam.getId();
        // 1.判断是否有文章id,有则修改，否则创建
        if (postId != null) {
            Post post = postRepository.getById(postId);
            postParam.update(post);
            postRepository.save(post);

            //1).判断草稿表是否有未发布的草稿内容
            contentPatchLogService.createOrUpdate(postId, contentParam.getContent(),
                contentParam.getOriginalContent());
            return new PostDTO().convertFrom(post);
        }
        Post postCreated = createDraft(postParam.convertTo(), contentParam.convertTo());
        return new PostDTO().convertFrom(postCreated);
    }

    private Post createDraft(Post post, Content content) {
        // 设置文章到content即使用post的主键作为content的主键
        content.setPost(post);
        // 创建文章草稿
        // 1. 先保存文章(post)
        // 2. 保存文章内容到内容历史表(content_record)
        // 3. 保存到文章内容当前版本表(content)
        // 3. 更新文章中的contentId
        post.setStatus(PostStatus.DRAFT);
        postRepository.save(post);

        ContentPatchLog contentRecord = new ContentPatchLog();
        // 初次创建不存在v1所以直接使用原始内容
        contentRecord.setContentDiff(content.getContent());
        contentRecord.setContentDiff(content.getOriginalContent());
        contentRecord.setPostId(post.getId());
        contentRecord.setStatus(PostStatus.DRAFT);
        contentPatchLogRepository.save(contentRecord);

        content.setContentRecordId(contentRecord.getId());
        contentRepository.save(content);
        return post;
    }

    @Override
    public Page<Post> pageBy(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Override
    public Page<Post> pageBy(PostStatus status, Pageable pageable) {
        return postRepository.findAllByStatus(status, pageable);
    }

    @Override
    public Post updateDraftContent(Integer postId, ContentParam contentParam) {
        Post post = postRepository.getById(postId);
        // 更新最新的草稿(content_record)
        contentPatchLogService.createOrUpdate(postId, contentParam.getContent(),
            contentParam.getOriginalContent());
        return post;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Post publish(Integer postId) {
        Post post = postRepository.getById(postId);

        ContentPatchLog contentPatchLog =
            contentPatchLogRepository.findFirstByPostIdAndStatusOrderByVersionDesc(postId,
                PostStatus.DRAFT);
        contentPatchLog.setStatus(PostStatus.PUBLISHED);
        contentPatchLog.setPublishTime(new Date());
        contentPatchLogRepository.save(contentPatchLog);

        Content content = contentRepository.getById(postId);
        content.setContentRecordId(contentPatchLog.getId());

        PatchedContent patchedContent = contentPatchLogService.applyPatch(contentPatchLog);
        content.setContent(patchedContent.getContent());
        content.setContent(patchedContent.getOriginalContent());

        contentRepository.save(content);

        post.setVersion(contentPatchLog.getVersion());
        post.setPublishTime(new Date());
        post.setStatus(PostStatus.PUBLISHED);
        postRepository.save(post);
        return post;
    }

    @Override
    public PostDetailDTO getById(Integer postId) {
        Post post = postRepository.getById(postId);
        Content content = contentRepository.getById(postId);
        PostDetailDTO postDTO = new PostDetailDTO().convertFrom(post);
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setContent(content.getContent());
        contentDTO.setOriginalContent(content.getOriginalContent());
        postDTO.setContent(contentDTO);
        return postDTO;
    }

    @Override
    public PostDetailDTO getDraftById(Integer postId) {
        Post post = postRepository.getById(postId);
        ContentPatchLog contentRecord =
            contentPatchLogRepository.findFirstByPostIdOrderByVersionDesc(postId);

        PostDetailDTO postDTO = new PostDetailDTO().convertFrom(post);

        PatchedContent patchedContent = contentPatchLogService.applyPatch(contentRecord);
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setContent(patchedContent.getContent());
        contentDTO.setOriginalContent(patchedContent.getOriginalContent());

        postDTO.setContent(contentDTO);
        return postDTO;
    }

    @Override
    public List<ContentPatchLog> listAllVersionsBy(Integer postId) {
        return contentPatchLogRepository.findAllByPostIdAndStatusOrderByVersionDesc(postId,
            PostStatus.PUBLISHED);
    }

    @Override
    public PostDetailDTO rollbackByIdAndVersion(Integer postId, Integer version) {
        ContentPatchLog contentRecord =
            contentPatchLogRepository.findFirstByPostIdAndStatusOrderByVersionDesc(postId,
                PostStatus.DRAFT);
        if (contentRecord != null) {
            throw new RuntimeException("存在未发布的草稿，请先发布后在操作");
        }
        ContentPatchLog contentRecordToRollback =
            contentPatchLogRepository.findByPostIdAndVersion(postId, version);

        PatchedContent patchedContent =
            contentPatchLogService.applyPatch(contentRecordToRollback);

        Content content = contentRepository.getById(postId);
        content.setContent(patchedContent.getContent());
        content.setOriginalContent(patchedContent.getOriginalContent());
        content.setContentRecordId(contentRecordToRollback.getVersion());
        contentRepository.save(content);

        Post post = postRepository.getById(postId);
        post.setVersion(contentRecordToRollback.getVersion());
        post.setPublishTime(new Date());
        postRepository.save(post);

        PostDetailDTO postDTO = new PostDetailDTO().convertFrom(post);
        postDTO.setContent(new ContentDTO().convertFrom(content));
        return postDTO;
    }
}
