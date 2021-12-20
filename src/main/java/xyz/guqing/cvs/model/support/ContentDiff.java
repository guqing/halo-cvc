package xyz.guqing.cvs.model.support;

import lombok.Data;

/**
 * 内容与v1的差异值
 *
 * @author guqing
 * @since 2021-12-20
 */
@Data
public class ContentDiff {

    private String content;

    private String originalContent;
}
