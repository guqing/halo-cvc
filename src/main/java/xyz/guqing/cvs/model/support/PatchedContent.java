package xyz.guqing.cvs.model.support;

import lombok.Data;

/**
 * 通过将 patch 应用到 v1版本 后得到的文章内容
 *
 * @author guqing
 * @since 2021-12-20
 */
@Data
public class PatchedContent {

    private String content;

    private String originalContent;
}
