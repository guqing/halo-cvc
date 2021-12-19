package xyz.guqing.cvs.model.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;
import xyz.guqing.cvs.model.enums.PostStatus;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Data
@Entity
@Table(name = "content_records")
@EqualsAndHashCode(callSuper = true)
public class ContentRecord extends BaseEntity {

    private Integer postId;

    private String content;

    private String originalContent;

    @NotNull
    private Integer version;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    /**
     * 该版本的发源地，例如该版本基于v1创建，源头就是v1
     * v1的源头是0
     */
    @ColumnDefault("0")
    private Integer sourceId;

    @Override
    protected void prePersist() {
        super.prePersist();
        if(version == null) {
            version = 1;
        }
    }
}
