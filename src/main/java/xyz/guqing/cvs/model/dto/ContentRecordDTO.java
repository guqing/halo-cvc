package xyz.guqing.cvs.model.dto;

import lombok.Data;
import xyz.guqing.cvs.model.entity.ContentPatchLog;
import xyz.guqing.cvs.model.support.OutputConverter;

/**
 * @author guqing
 * @date 2021-12-19
 */
@Data
public class ContentRecordDTO implements OutputConverter<ContentRecordDTO, ContentPatchLog> {

    private Integer id;

    private Integer postId;

    private String content;

    private String originalContent;

    private Integer version;

    private Integer sourceId;
}
