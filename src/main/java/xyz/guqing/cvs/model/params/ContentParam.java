package xyz.guqing.cvs.model.params;

import lombok.Data;
import xyz.guqing.cvs.model.entity.Content;
import xyz.guqing.cvs.model.support.InputConverter;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Data
public class ContentParam implements InputConverter<Content> {

    private String content;

    private String originalContent;
}
