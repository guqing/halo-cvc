package xyz.guqing.cvs.service.impl;

import java.time.LocalDateTime;
import javax.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import xyz.guqing.cvs.model.dto.ContentDTO;
import xyz.guqing.cvs.model.dto.PostDTO;
import xyz.guqing.cvs.model.dto.PostDetailDTO;
import xyz.guqing.cvs.model.entity.Content;
import xyz.guqing.cvs.model.entity.ContentRecord;
import xyz.guqing.cvs.model.entity.Post;
import xyz.guqing.cvs.model.enums.PostStatus;
import xyz.guqing.cvs.model.params.ContentParam;
import xyz.guqing.cvs.model.params.PostParam;
import xyz.guqing.cvs.repository.ContentRecordRepository;
import xyz.guqing.cvs.repository.ContentRepository;
import xyz.guqing.cvs.repository.PostRepository;
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
    private ContentRecordRepository contentRecordRepository;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public PostDetailDTO createOrUpdateDraftBy(PostParam postParam) {
        ContentParam contentParam = postParam.getContent();

        // 1.判断是否有文章id,有则修改，否则创建
        if (postParam.getId() != null) {
            // 更新文章草稿
            Post postToUpdate = postRepository.getById(postParam.getId());
            postParam.update(postToUpdate);

            ContentRecord contentRecord = updateDraft(postToUpdate, contentParam);
            Content content = new Content();
            content.setContent(contentRecord.getContent());
            content.setOriginalContent(contentRecord.getOriginalContent());
            return convertTo(postToUpdate, content);
        }
        Post post = postParam.convertTo();
        Content content = postParam.getContent().convertTo();
        createDraft(post, content);

        return convertTo(post, content);
    }

    private ContentRecord updateDraft(Post post, ContentParam content) {
        ContentRecord contentRecord =
            contentRecordRepository.findFirstByPostIdAndStatusOrderByVersionDesc(post.getId(),
                PostStatus.DRAFT);
        // 更新当前版本的内容草稿历史
        contentRecord.setContent(content.getContent());
        contentRecord.setOriginalContent(content.getOriginalContent());
        contentRecord.setPostId(post.getId());
        contentRecordRepository.save(contentRecord);
        return contentRecord;
    }

    private void createDraft(Post post, Content content) {
        // 设置文章到content即使用post的主键作为content的主键
        content.setPost(post);
        // 创建文章草稿
        // 1. 先保存文章(post)
        // 2. 保存文章内容到内容历史表(content_record)
        // 3. 保存到文章内容当前版本表(content)
        // 3. 更新文章中的contentId
        post.setStatus(PostStatus.DRAFT);
        postRepository.save(post);

        ContentRecord contentRecord = createContentRecordBy(post, content);
        contentRecord.setPostId(post.getId());
        contentRecord.setStatus(PostStatus.DRAFT);
        contentRecordRepository.save(contentRecord);

        content.setContentRecord(contentRecord);
        contentRepository.save(content);
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
        // 更新最新的草稿(content_record)
        Post post = postRepository.getById(postId);

        updateDraft(post, contentParam);
        return post;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Post publish(Integer postId) {
        Post post = postRepository.getById(postId);

        ContentRecord contentRecord =
            contentRecordRepository.findFirstByPostIdAndStatusOrderByVersionDesc(postId,
                PostStatus.DRAFT);
        contentRecord.setStatus(PostStatus.PUBLISHED);
        contentRecordRepository.save(contentRecord);

        Content content = contentRepository.getById(postId);
        content.setContentRecord(contentRecord);
        content.setContent(contentRecord.getContent());
        content.setOriginalContent(contentRecord.getOriginalContent());
        contentRepository.save(content);

        post.setVersion(contentRecord.getVersion());
        post.setPublishTime(LocalDateTime.now());
        post.setStatus(PostStatus.PUBLISHED);
        postRepository.save(post);

        return post;
    }

    @Override
    public PostDetailDTO getById(Integer postId) {
        Post post = postRepository.getById(postId);
        ContentRecord contentRecord =
            contentRecordRepository.findFirstByPostIdAndStatusOrderByVersionDesc(postId,
                PostStatus.DRAFT);
        if (contentRecord == null) {
            // 创建新草稿
            Content content = contentRepository.getById(postId);
            ContentRecord contentRecordToSave = createContentRecordBy(post, content);
            ContentRecord latestRecord = contentRecordRepository.findFirstByPostIdOrderByVersionDesc(postId);
            contentRecordToSave.setVersion(latestRecord.getVersion() + 1);
            contentRecordToSave.setPostId(postId);
            contentRecordToSave.setStatus(PostStatus.DRAFT);
            contentRecordRepository.save(contentRecordToSave);

            Content contentFromRecord = new Content();
            contentFromRecord.setContent(contentFromRecord.getContent());
            contentFromRecord.setOriginalContent(contentFromRecord.getOriginalContent());
            return convertTo(post, contentFromRecord);
        }
        PostDetailDTO postDTO = new PostDetailDTO().convertFrom(post);

        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setContent(contentRecord.getContent());
        contentDTO.setOriginalContent(contentRecord.getOriginalContent());

        postDTO.setContent(contentDTO);
        return postDTO;
    }

    private PostDetailDTO convertTo(Post post, Content content) {
        PostDetailDTO postDTO = new PostDetailDTO().convertFrom(post);

        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setContent(content.getContent());
        contentDTO.setOriginalContent(content.getOriginalContent());

        postDTO.setContent(contentDTO);
        return postDTO;
    }

    private ContentRecord createContentRecordBy(Post post, Content content) {
        Assert.notNull(post, "The post must not be null.");
        Assert.notNull(content, "The content must not be null");
        ContentRecord contentRecord = new ContentRecord();
        BeanUtils.copyProperties(content, contentRecord);
        contentRecord.setId(null);
        return contentRecord;
    }
}
