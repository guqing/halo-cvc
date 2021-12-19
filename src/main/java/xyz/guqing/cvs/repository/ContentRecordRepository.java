package xyz.guqing.cvs.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.guqing.cvs.model.entity.ContentRecord;
import xyz.guqing.cvs.model.enums.PostStatus;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Repository
public interface ContentRecordRepository extends JpaRepository<ContentRecord, Integer> {

    ContentRecord findFirstByPostIdAndStatusOrderByVersionDesc(Integer postId, PostStatus status);

    ContentRecord findFirstByPostIdOrderByVersionDesc(Integer postId);

    ContentRecord findByPostIdAndVersion(Integer postId, Integer version);

    List<ContentRecord> findAllByPostIdAndStatusOrderByVersionDesc(Integer postId, PostStatus status);
}
