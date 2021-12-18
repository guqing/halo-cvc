package xyz.guqing.cvs.model.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "post_id", referencedColumnName = "id",
//        insertable = true, updatable = true, nullable = false)
    //private Post post;
    private Integer postId;

    private String content;

    private String originalContent;

    @NotNull
    private Integer version;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Override
    protected void prePersist() {
        super.prePersist();
        if(version == null) {
            version = 1;
        }
    }
}
