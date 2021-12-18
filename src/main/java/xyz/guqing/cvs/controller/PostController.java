package xyz.guqing.cvs.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.guqing.cvs.model.dto.PostDetailDTO;
import xyz.guqing.cvs.model.params.PostParam;
import xyz.guqing.cvs.service.PostService;

/**
 * @author guqing
 * @date 2021-12-18
 */
@RestController
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<PostDetailDTO> draftPost(@Valid @RequestBody PostParam postParam) {
        PostDetailDTO postDetail = postService.createOrUpdateDraftBy(postParam);
        return ResponseEntity.ok(postDetail);
    }
}
