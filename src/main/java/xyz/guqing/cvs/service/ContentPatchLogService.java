package xyz.guqing.cvs.service;

import xyz.guqing.cvs.model.entity.ContentPatchLog;
import xyz.guqing.cvs.model.support.ContentDiff;
import xyz.guqing.cvs.model.support.PatchedContent;

/**
 * @author guqing
 * @since 2021-12-20
 */
public interface ContentPatchLogService {

    ContentPatchLog getById(Integer id);

    ContentPatchLog createOrUpdate(Integer postId, String content, String originalContent);

    PatchedContent applyPatch(ContentPatchLog contentRecord);

    ContentDiff generateDiff(Integer postId, String content, String originalContent);
}
