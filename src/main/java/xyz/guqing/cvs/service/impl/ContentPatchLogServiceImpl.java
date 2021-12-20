package xyz.guqing.cvs.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import xyz.guqing.cvs.model.entity.Content;
import xyz.guqing.cvs.model.entity.ContentPatchLog;
import xyz.guqing.cvs.model.entity.Post;
import xyz.guqing.cvs.model.enums.PostStatus;
import xyz.guqing.cvs.model.support.ContentDiff;
import xyz.guqing.cvs.model.support.PatchedContent;
import xyz.guqing.cvs.repository.ContentPatchLogRepository;
import xyz.guqing.cvs.repository.ContentRepository;
import xyz.guqing.cvs.repository.PostRepository;
import xyz.guqing.cvs.service.ContentPatchLogService;
import xyz.guqing.cvs.utils.PatchUtils;

/**
 * @author guqing
 * @since 2021-12-20
 */
@Service
public class ContentPatchLogServiceImpl implements ContentPatchLogService {

    @Autowired
    private ContentPatchLogRepository contentPatchLogRepository;
    @Autowired
    private ContentRepository contentRepository;
    @Autowired
    private PostRepository postRepository;

    @Override
    public ContentPatchLog getById(Integer id) {
        return contentPatchLogRepository.getById(id);
    }

    @Override
    public ContentPatchLog createOrUpdate(Integer postId, String content, String originalContent) {
        if (existDraftBy(postId)) {
            return updateDraftBy(postId, content, originalContent);
        }
        return createDraftContent(postId, content, originalContent);
    }

    private ContentPatchLog createDraftContent(Integer postId, String contentParam,
        String originalContentParam) {
        Post post = postRepository.getById(postId);
        ContentPatchLog contentPatchLog = new ContentPatchLog();
        if (post.getVersion() == 1 && PostStatus.DRAFT.equals(post.getStatus())) {
            contentPatchLog.setContentDiff(contentParam);
            contentPatchLog.setOriginalContentDiff(originalContentParam);
        } else {
            ContentDiff contentDiff =
                generateDiff(postId, contentParam, originalContentParam);
            contentPatchLog.setContentDiff(contentDiff.getContent());
            contentPatchLog.setOriginalContentDiff(contentDiff.getOriginalContent());
        }
        Content content = contentRepository.getById(postId);
        contentPatchLog.setPostId(postId);
        contentPatchLog.setStatus(PostStatus.DRAFT);
        contentPatchLog.setSourceId(content.getContentRecordId());
        contentPatchLog.setVersion(
            contentPatchLogRepository.findFirstByPostIdOrderByVersionDesc(postId).getVersion() + 1);
        // 保存
        contentPatchLogRepository.save(contentPatchLog);
        return contentPatchLog;
    }

    private boolean existDraftBy(Integer postId) {
        ContentPatchLog contentPatchLog = new ContentPatchLog();
        contentPatchLog.setPostId(postId);
        contentPatchLog.setStatus(PostStatus.DRAFT);
        Example<ContentPatchLog> example = Example.of(contentPatchLog);
        return contentPatchLogRepository.exists(example);
    }

    private ContentPatchLog updateDraftBy(Integer postId, String content, String originalContent) {
        ContentPatchLog draftPatchLog =
            contentPatchLogRepository.findFirstByPostIdAndStatusOrderByVersionDesc(
                postId, PostStatus.DRAFT);
        // 草稿版本是否为1
        if (draftPatchLog.getVersion() == 1) {
            // 是v1直接修改内容
            draftPatchLog.setContentDiff(content);
            draftPatchLog.setOriginalContentDiff(originalContent);
            contentPatchLogRepository.save(draftPatchLog);
            return draftPatchLog;
        }
        // 生成差异信息
        ContentDiff contentDiff = generateDiff(postId, content, originalContent);
        draftPatchLog.setContentDiff(contentDiff.getContent());
        draftPatchLog.setOriginalContentDiff(contentDiff.getOriginalContent());
        contentPatchLogRepository.save(draftPatchLog);
        return draftPatchLog;
    }

    @Override
    public PatchedContent applyPatch(ContentPatchLog contentRecord) {
        Assert.notNull(contentRecord, "The contentRecord must not be null.");
        Assert.notNull(contentRecord.getVersion(), "The contentRecord.version must not be null.");
        Assert.notNull(contentRecord.getPostId(), "The contentRecord.postId must not be null.");

        PatchedContent patchedContent = new PatchedContent();
        if (contentRecord.getVersion() == 1) {
            patchedContent.setContent(contentRecord.getContentDiff());
            patchedContent.setOriginalContent(contentRecord.getOriginalContentDiff());
            return patchedContent;
        }

        ContentPatchLog baseContentRecord =
            contentPatchLogRepository.findByPostIdAndVersion(contentRecord.getPostId(), 1);

        String content = PatchUtils.restoreContent(
            contentRecord.getContentDiff(), baseContentRecord.getContentDiff());
        patchedContent.setContent(content);

        String originalContent =
            PatchUtils.restoreContent(contentRecord.getOriginalContentDiff(),
                baseContentRecord.getOriginalContentDiff());
        patchedContent.setOriginalContent(originalContent);
        return patchedContent;
    }

    @Override
    public ContentDiff generateDiff(Integer postId, String content,
        String originalContent) {
        ContentPatchLog baseContentRecord =
            contentPatchLogRepository.findByPostIdAndVersion(postId, 1);

        ContentDiff contentDiff = new ContentDiff();
        String contentChanges = PatchUtils.diffToPatchString(baseContentRecord.getContentDiff(),
            content);
        contentDiff.setContent(contentChanges);

        String originalContentChanges =
            PatchUtils.diffToPatchString(baseContentRecord.getOriginalContentDiff(),
                originalContent);
        contentDiff.setOriginalContent(originalContentChanges);
        return contentDiff;
    }
}
