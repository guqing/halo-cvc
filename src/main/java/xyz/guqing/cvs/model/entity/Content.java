package xyz.guqing.cvs.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;
import xyz.guqing.cvs.model.enums.PostStatus;

/**
 * 当前正在使用版本的文章内容表.
 *
 * @author guqing
 * @date 2021-12-18
 */
@Data
@Entity
@Table(name = "contents")
@EqualsAndHashCode(callSuper = true)
public class Content extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id",
        insertable = true, updatable = true, nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_record_id", referencedColumnName = "id",
        insertable = true, updatable = true, nullable = false)
    private ContentRecord contentRecord;

    private String content;

    private String originalContent;

    @Enumerated(EnumType.STRING)
    private PostStatus status;
}
