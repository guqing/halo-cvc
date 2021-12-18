package xyz.guqing.cvs.model.dto;

import lombok.Data;
import xyz.guqing.cvs.model.entity.Content;
import xyz.guqing.cvs.model.support.OutputConverter;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Data
public class ContentDTO implements OutputConverter<ContentDTO, Content> {
    private String content;

    private String originalContent;
}
