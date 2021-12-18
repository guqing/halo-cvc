package xyz.guqing.cvs.service.impl;

import javax.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
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

        if(postParam.getId() != null) {
            // 更新文章草稿
            Post postToUpdate = postRepository.getById(postParam.getId());
            postParam.update(postToUpdate);

            // 更新内容草稿
            Content contentToUpdate = contentRepository.getById(postToUpdate.getContentId());
            contentParam.update(contentToUpdate);

            updateDraft(postToUpdate, contentToUpdate);
            return convertTo(postToUpdate, contentToUpdate);
        }
        Post post = postParam.convertTo();
        Content content = postParam.getContent().convertTo();
        createDraft(post, content);

        return convertTo(post, content);
    }

    private PostDetailDTO convertTo(Post post, Content content) {
        PostDetailDTO postDTO = new PostDetailDTO().convertFrom(post);
        postDTO.setContent(content.getContent());
        postDTO.setOriginalContent(content.getOriginalContent());
        return postDTO;
    }

    private void updateDraft(Post post, Content content) {
        // 更新当前版本的内容草稿历史
        ContentRecord contentRecord = content.getContentRecord();
        contentRecord.setContent(content.getContent());
        contentRecord.setOriginalContent(content.getOriginalContent());
        contentRecordRepository.save(contentRecord);

        // 更新当前版本的文章内容
        content.setContentRecord(contentRecord);
        content.setPost(post);
        contentRepository.save(content);

        // 更新文章
        postRepository.save(post);
    }

    private void createDraft(Post post, Content content) {
        // 创建文章草稿
        // 1. 先保存文章(post)
        // 2. 保存文章内容到内容历史表(content_record)
        // 3. 保存到文章内容当前版本表(content)
        // 3. 更新文章中的contentId
        post.setStatus(PostStatus.DRAFT);
        content.setStatus(PostStatus.DRAFT);

        postRepository.save(post);

        ContentRecord contentRecord = createContentRecordBy(post, content);
        contentRecordRepository.save(contentRecord);

        content.setContentRecord(contentRecord);
        content.setPost(post);
        contentRepository.save(content);

        // update content id
        post.setContentId(content.getId());
        postRepository.save(post);
    }

    private ContentRecord createContentRecordBy(Post post, Content content) {
        Assert.notNull(post, "The post must not be null.");
        Assert.notNull(content, "The content must not be null");
        ContentRecord contentRecord = new ContentRecord();
        BeanUtils.copyProperties(content, contentRecord);
        contentRecord.setPost(post);
        return contentRecord;
    }
}
