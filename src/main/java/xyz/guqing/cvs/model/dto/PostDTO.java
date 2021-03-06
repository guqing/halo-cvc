package xyz.guqing.cvs.model.dto;

import java.util.Date;
import lombok.Data;
import xyz.guqing.cvs.model.entity.Post;
import xyz.guqing.cvs.model.support.OutputConverter;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Data
public class PostDTO implements OutputConverter<PostDTO, Post> {

    private Integer id;

    private String title;

    private Date createTime;

    private String status;

    private Integer version;
}
