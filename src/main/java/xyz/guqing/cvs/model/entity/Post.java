package xyz.guqing.cvs.model.entity;

import java.time.LocalDateTime;
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
@Table(name = "posts")
@EqualsAndHashCode(callSuper = true)
public class Post extends BaseEntity {

    @NotNull
    private String title;

    private Integer contentId;

    private Integer version;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    private LocalDateTime publishTime;

    @Override
    protected void prePersist() {
        super.prePersist();
        if(version == null) {
            version = 1;
        }
    }
}
