package xyz.guqing.cvs.model.entity;

import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * 当前正在使用版本的文章内容表.
 *
 * @author guqing
 * @date 2021-12-18
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "contents")
public class Content {
    @Id
    @Column(name = "post_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @Exclude
    private Post post;

    private Integer contentRecordId;

    @Lob
    private String content;

    @Lob
    private String originalContent;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(
            o)) {
            return false;
        }
        Content content = (Content) o;
        return id != null && Objects.equals(id, content.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
