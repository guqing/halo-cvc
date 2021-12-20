package xyz.guqing.cvs.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.guqing.cvs.model.entity.ContentPatchLog;
import xyz.guqing.cvs.model.enums.PostStatus;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Repository
public interface ContentPatchLogRepository extends JpaRepository<ContentPatchLog, Integer> {

    ContentPatchLog findFirstByPostIdAndStatusOrderByVersionDesc(Integer postId, PostStatus status);

    ContentPatchLog findFirstByPostIdOrderByVersionDesc(Integer postId);

    ContentPatchLog findByPostIdAndVersion(Integer postId, Integer version);

    List<ContentPatchLog> findAllByPostIdAndStatusOrderByVersionDesc(Integer postId, PostStatus status);
}
