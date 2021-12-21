package xyz.guqing.cvs.model.entity;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import xyz.guqing.cvs.model.enums.PostStatus;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "content_patch_logs")
public class ContentPatchLog extends BaseEntity {

    private Integer postId;

    @Lob
    private String contentDiff;

    @Lob
    private String originalContentDiff;

    @NotNull
    private Integer version;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date publishTime;

    /**
     * 该版本的发源地，例如该版本基于v1创建，源头就是v1 v1的源头是0
     */
    @NotNull
    private Integer sourceId;

    @Override
    protected void prePersist() {
        super.prePersist();
        if (version == null) {
            version = 1;
        }

        if (sourceId == null) {
            sourceId = 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(
            o)) {
            return false;
        }
        ContentPatchLog that = (ContentPatchLog) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
