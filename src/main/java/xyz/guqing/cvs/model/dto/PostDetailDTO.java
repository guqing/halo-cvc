package xyz.guqing.cvs.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostDetailDTO extends PostDTO {

    private String content;

    private String originalContent;
}
