package xyz.guqing.cvs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.guqing.cvs.model.entity.Content;

/**
 * @author guqing
 * @date 2021-12-18
 */
@Repository
public interface ContentRepository extends JpaRepository<Content, Integer> {

}
