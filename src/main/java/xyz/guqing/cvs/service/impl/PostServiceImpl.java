package xyz.guqing.cvs.service.impl;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRowGenerator;
import com.github.difflib.unifieddiff.UnifiedDiff;
import com.github.difflib.unifieddiff.UnifiedDiffFile;
import com.github.difflib.unifieddiff.UnifiedDiffWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
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

            //1).判断草稿表是否有未发布的草稿内容
            ContentPatchLog contentPatchLog =
                contentPatchLogService.createOrUpdate(postId, contentParam.getContent(),
                    contentParam.getOriginalContent());

            Post post = postRepository.getById(postId);
            postParam.update(post);

            // 更新头指针
            post.setContentHeadRef(contentPatchLog.getId());
            postRepository.save(post);

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
        post.setContentHeadRef(0);
        postRepository.save(post);

        ContentPatchLog contentPatchLog = new ContentPatchLog();
        // 初次创建不存在v1所以直接使用原始内容
        contentPatchLog.setContentDiff(content.getContent());
        contentPatchLog.setOriginalContentDiff(content.getOriginalContent());
        contentPatchLog.setPostId(post.getId());
        contentPatchLog.setStatus(PostStatus.DRAFT);
        contentPatchLogRepository.save(contentPatchLog);

        content.setContentRecordId(contentPatchLog.getId());
        contentRepository.save(content);

        // 更新头指针
        post.setContentHeadRef(contentPatchLog.getId());
        postRepository.save(post);
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
        if (contentPatchLog == null) {
            throw new RuntimeException("没有需要发布的草稿，请先保存");
        }
        contentPatchLog.setStatus(PostStatus.PUBLISHED);
        contentPatchLog.setPublishTime(new Date());
        contentPatchLogRepository.save(contentPatchLog);

        Content content = contentRepository.getById(postId);
        content.setContentRecordId(contentPatchLog.getId());

        PatchedContent patchedContent = contentPatchLogService.applyPatch(contentPatchLog);
        content.setContent(patchedContent.getContent());
        content.setOriginalContent(patchedContent.getOriginalContent());

        contentRepository.save(content);

        post.setVersion(contentPatchLog.getVersion());
        post.setPublishTime(new Date());
        post.setStatus(PostStatus.PUBLISHED);
        post.setContentHeadRef(contentPatchLog.getId());
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
        // 使用post中存储的head指针
        ContentPatchLog contentRecord =
            contentPatchLogRepository.getById(post.getContentHeadRef());

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
        Integer oldContentRecordId = content.getContentRecordId();
        content.setContent(patchedContent.getContent());
        content.setOriginalContent(patchedContent.getOriginalContent());
        content.setContentRecordId(contentRecordToRollback.getVersion());
        contentRepository.save(content);

        // 新创建一条回退的patch log与切换到的 patch log变更一致
        ContentPatchLog revertContentPatchLog = new ContentPatchLog();
        BeanUtils.copyProperties(contentRecordToRollback, revertContentPatchLog);
        revertContentPatchLog.setId(null);
        revertContentPatchLog.setStatus(PostStatus.PUBLISHED);
        revertContentPatchLog.setSourceId(oldContentRecordId);
        Integer maxVersion =
            contentPatchLogRepository.findFirstByPostIdOrderByVersionDesc(postId).getVersion();
        revertContentPatchLog.setVersion(maxVersion + 1);
        contentPatchLogRepository.save(revertContentPatchLog);

        Post post = postRepository.getById(postId);
        post.setVersion(revertContentPatchLog.getVersion());
        post.setPublishTime(new Date());
        post.setContentHeadRef(revertContentPatchLog.getId());
        postRepository.save(post);

        PostDetailDTO postDTO = new PostDetailDTO().convertFrom(post);
        postDTO.setContent(new ContentDTO().convertFrom(content));
        return postDTO;
    }

    @Override
    public String getContentDiff(Integer postId, Integer version) {
        List<ContentPatchLog> patchLogs =
            contentPatchLogRepository.findByPostIdAndVersion(postId, version, PostStatus.PUBLISHED);
        if (CollectionUtils.isEmpty(patchLogs)) {
            return StringUtils.EMPTY;
        }
        if (version == 1) {
            ContentPatchLog contentPatchLog = patchLogs.get(0);
            return getUnifiedDiffString(contentPatchLog, contentPatchLog);
        }
        if (patchLogs.size() < 2) {
            return StringUtils.EMPTY;
        }

        return getUnifiedDiffString(patchLogs.get(0), patchLogs.get(1));
    }

    private String getUnifiedDiffString(ContentPatchLog current, ContentPatchLog prev) {
        Assert.notNull(current, "The current must not be null.");
        Assert.notNull(prev, "The last must not be null.");
        String fromFile = null;
        if (current.getVersion() != 1) {
            fromFile = "v" + prev.getVersion();
        }
        PatchedContent currentContent = contentPatchLogService.applyPatch(current);
        PatchedContent prevContent = contentPatchLogService.applyPatch(prev);
        // 当前版本(revised)与上一个版本(original)比较
        List<String> revised = PatchUtils.breakLine(currentContent.getOriginalContent());
        List<String> original = PatchUtils.breakLine(prevContent.getOriginalContent());

        Patch<String> patch = DiffUtils.diff(original, revised);
        UnifiedDiff diff = UnifiedDiff.from("", "",
            UnifiedDiffFile.from(fromFile, "v" + current.getVersion(), patch));
        StringWriter writer = new StringWriter();
        try {
            UnifiedDiffWriter.write(diff, f -> original, writer, 5);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
