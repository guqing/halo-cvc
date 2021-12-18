package xyz.guqing.cvs.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
public class Content {
    @Id
    @Column(name = "post_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_record_id", referencedColumnName = "id",
        insertable = true, updatable = true, nullable = false)
    private ContentRecord contentRecord;

    private String content;

    private String originalContent;
}
