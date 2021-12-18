package xyz.guqing.cvs.model.params;

import javax.validation.constraints.NotNull;
import lombok.Data;
import xyz.guqing.cvs.model.entity.Post;
import xyz.guqing.cvs.model.support.InputConverter;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Data
public class PostParam implements InputConverter<Post> {

    private Integer id;

    private String title;

    @NotNull
    private ContentParam content;
}
