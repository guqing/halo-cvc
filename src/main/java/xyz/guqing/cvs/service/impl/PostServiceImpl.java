package xyz.guqing.cvs.service.impl;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
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
import xyz.guqing.cvs.utils.PatchUtils;

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
        Integer postId = postParam.getId();
        // 1.判断是否有文章id,有则修改，否则创建
        if (postId != null) {
            //1).判断草稿表是否有未发布的草稿内容
            ContentRecord draftContentRecord =
                contentRecordRepository.findFirstByPostIdAndStatusOrderByVersionDesc(
                    postId, PostStatus.DRAFT);
            if (draftContentRecord != null) {
                // 草稿版本是否为1
                if (draftContentRecord.getVersion() == 1) {
                    // 是v1直接修改内容

                    updateVersionContent(draftContentRecord, contentParam.getContent(),
                        contentParam.getOriginalContent());
                } else {
                    // 查询版本1的草稿
                    ContentRecord baseContentRecord =
                        contentRecordRepository.findByPostIdAndVersion(postId, 1);
                    String content = diffToPatchString(baseContentRecord.getContent(),
                        contentParam.getContent());
                    String originalContent = diffToPatchString(baseContentRecord.getOriginalContent(),
                        contentParam.getOriginalContent());
                    updateVersionContent(draftContentRecord, content, originalContent);
                }
            } else {
                createDraftContent(postParam.getId(), contentParam.getContent(),
                    contentParam.getOriginalContent());
            }
            return null;
        }
        // 传入实际的内容
        //createDraftContent(postId, contentParam.getContent(), contentParam.getOriginalContent());
        Post post = postParam.convertTo();
        Content content = postParam.getContent().convertTo();
        createDraft(post, content);
        return null;
    }

    private void createDraftContent(Integer postId, String contentParam, String originalContentParam) {
        // 使用正式版的内容创建一份草稿
        Post currentPost = postRepository.getById(postId);
        ContentRecord contentRecord = new ContentRecord();
        if(currentPost.getVersion() == 1 && PostStatus.DRAFT.equals(currentPost.getStatus())) {
            contentRecord.setContent(contentParam);
            contentRecord.setOriginalContent(originalContentParam);
        } else {
            ContentRecord baseContentRecord =
                contentRecordRepository.findByPostIdAndVersion(postId, 1);

            String contentToUse = diffToPatchString(baseContentRecord.getContent(),
                contentParam);
            String originalContentToUse = diffToPatchString(baseContentRecord.getOriginalContent(),
                originalContentParam);
            contentRecord.setContent(contentToUse);
            contentRecord.setOriginalContent(originalContentToUse);
        }
        Content content = contentRepository.getById(postId);
        contentRecord.setPostId(postId);
        contentRecord.setStatus(PostStatus.DRAFT);
        contentRecord.setSourceId(content.getContentRecordId());
        contentRecord.setVersion(contentRecordRepository.findFirstByPostIdOrderByVersionDesc(postId).getVersion() + 1);
        // 入库
        contentRecordRepository.save(contentRecord);
    }

    private String restoreByPatchJson(String json, String original) {
        Patch<String> patch = PatchUtils.create(json);
        try {
            return String.join("\n", patch.applyTo(breakLine(original)));
        } catch (PatchFailedException e) {
            throw new RuntimeException(e);
        }
    }

    private String diffToPatchString(String original, String revised) {
        Patch<String> patch = DiffUtils.diff(breakLine(original),
            breakLine(revised));
        return PatchUtils.patchToString(patch);
    }

    private List<String> breakLine(String content) {
        String[] strings = StringUtils.tokenizeToStringArray(content, "\n");
        return Arrays.asList(strings);
    }

    private void updateVersionContent(ContentRecord contentRecord,
        String content, String originalContent) {
        contentRecord.setContent(content);
        contentRecord.setOriginalContent(originalContent);
        contentRecordRepository.save(contentRecord);
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

        content.setContentRecordId(contentRecord.getId());
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
        content.setContentRecordId(contentRecord.getId());
        if(contentRecord.getVersion() == 1) {
            content.setContent(contentRecord.getContent());
            content.setOriginalContent(contentRecord.getOriginalContent());
        } else {
            // 存在发布版本以后都是增量
            ContentRecord baseContentRecord =
                contentRecordRepository.findByPostIdAndVersion(postId, 1);
            String actualContent = restoreByPatchJson(contentRecord.getContent(), baseContentRecord.getContent());
            String actualOriginalContent = restoreByPatchJson(contentRecord.getOriginalContent(),
                baseContentRecord.getOriginalContent());

            content.setContent(actualContent);
            content.setOriginalContent(actualOriginalContent);
        }

        contentRepository.save(content);

        post.setVersion(contentRecord.getVersion());
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
        ContentRecord contentRecord =
            contentRecordRepository.findFirstByPostIdOrderByVersionDesc(postId);
        PostDetailDTO postDTO = new PostDetailDTO().convertFrom(post);
        ContentDTO contentDTO = new ContentDTO();

        decodeContentRecord(contentRecord);
        contentDTO.setContent(contentRecord.getContent());
        contentDTO.setOriginalContent(contentRecord.getOriginalContent());
        postDTO.setContent(contentDTO);
        return postDTO;
    }

    @Override
    public List<ContentRecord> listAllVersionsBy(Integer postId) {
        return contentRecordRepository.findAllByPostIdAndStatusOrderByVersionDesc(postId, PostStatus.PUBLISHED);
    }

    @Override
    public ContentRecord getContentRecordById(Integer contentRecordId) {
        ContentRecord contentRecord = contentRecordRepository.getById(contentRecordId);
        return decodeContentRecord(contentRecord);
    }

    @Override
    public PostDetailDTO rollbackByIdAndVersion(Integer postId, Integer version) {
        ContentRecord contentRecord =
            contentRecordRepository.findFirstByPostIdAndStatusOrderByVersionDesc(postId,
                PostStatus.DRAFT);
        if(contentRecord != null) {
            throw new RuntimeException("存在未发布的草稿，请先发布后在操作");
        }
        ContentRecord contentRecordToRollback =
            contentRecordRepository.findByPostIdAndVersion(postId, version);
        decodeContentRecord(contentRecordToRollback);

        Content content = contentRepository.getById(postId);
        content.setContent(contentRecordToRollback.getContent());
        content.setOriginalContent(contentRecordToRollback.getOriginalContent());
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

    private ContentRecord decodeContentRecord(ContentRecord contentRecord) {
        if(contentRecord.getVersion() != 1) {
            ContentRecord baseContentRecord =
                contentRecordRepository.findByPostIdAndVersion(contentRecord.getPostId(), 1);
            contentRecord.setContent(restoreByPatchJson(contentRecord.getContent(),
                baseContentRecord.getContent()));
            contentRecord.setOriginalContent(restoreByPatchJson(contentRecord.getOriginalContent(),
                baseContentRecord.getOriginalContent()));
        }
        return contentRecord;
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
