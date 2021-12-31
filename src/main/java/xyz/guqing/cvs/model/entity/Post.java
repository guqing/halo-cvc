package xyz.guqing.cvs.model.entity;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import xyz.guqing.cvs.model.enums.PostStatus;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @NotNull
    private String title;

    private Integer version;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date publishTime;

    /**
     * 版本头指向content patch log记录的主键id
     */
    @NotNull
    private Integer contentHeadRef;

    @Override
    protected void prePersist() {
        super.prePersist();
        if(version == null) {
            version = 1;
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
        Post post = (Post) o;
        return getId() != null && Objects.equals(getId(), post.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
