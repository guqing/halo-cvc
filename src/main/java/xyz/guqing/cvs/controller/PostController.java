package xyz.guqing.cvs.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.guqing.cvs.model.dto.ContentRecordDTO;
import xyz.guqing.cvs.model.dto.PostDTO;
import xyz.guqing.cvs.model.dto.PostDetailDTO;
import xyz.guqing.cvs.model.entity.ContentPatchLog;
import xyz.guqing.cvs.model.entity.Post;
import xyz.guqing.cvs.model.enums.PostStatus;
import xyz.guqing.cvs.model.params.ContentParam;
import xyz.guqing.cvs.model.params.PostParam;
import xyz.guqing.cvs.model.support.PatchedContent;
import xyz.guqing.cvs.service.ContentPatchLogService;
import xyz.guqing.cvs.service.PostService;

/**
 * @author guqing
 * @date 2021-12-18
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private ContentPatchLogService contentPatchLogService;

    @PostMapping
    public ResponseEntity<PostDTO> draftPost(@Valid @RequestBody PostParam postParam) {
        PostDTO postDTO = postService.createOrUpdateDraftBy(postParam);
        return ResponseEntity.ok(postDTO);
    }

    @GetMapping
    public ResponseEntity<Page<PostDTO>> pageBy(
        @PageableDefault(sort = "createTime", direction = DESC) Pageable pageable) {
        Page<Post> postPage = postService.pageBy(pageable);
        return ResponseEntity.ok(postPage.map(this::convertTo));
    }

    @GetMapping("status/{status}")
    public ResponseEntity<Page<PostDTO>> pageByStatus(@PathVariable(name = "status") String status,
        @PageableDefault(sort = "createTime", direction = DESC) Pageable pageable) {
        PostStatus postStatus = PostStatus.valueOf(status);
        Page<Post> posts = postService.pageBy(postStatus, pageable);
        return ResponseEntity.ok(posts.map(this::convertTo));
    }

    @GetMapping("/{postId:\\d+}")
    public ResponseEntity<PostDetailDTO> getById(@PathVariable Integer postId) {
        PostDetailDTO post = postService.getById(postId);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/{postId:\\d+}/drafts")
    public ResponseEntity<PostDetailDTO> getDraftById(@PathVariable Integer postId) {
        PostDetailDTO post = postService.getDraftById(postId);
        return ResponseEntity.ok(post);
    }

    @PutMapping("{postId:\\d+}/status/draft/content")
    public PostDetailDTO updateDraftBy(@PathVariable("postId") Integer postId,
        @RequestBody ContentParam contentParam) {
        // Update draft content
        Post post = postService.updateDraftContent(postId, contentParam);

        return new PostDetailDTO().convertFrom(post);
    }

    @PostMapping("/{postId:\\d+}/publish")
    public ResponseEntity<PostDTO> publish(@PathVariable Integer postId) {
        Post post = postService.publish(postId);
        return ResponseEntity.ok(convertTo(post));
    }

    @GetMapping("/{postId}/versions/contents")
    public ResponseEntity<List<ContentRecordDTO>> listAllVersionsBy(@PathVariable Integer postId) {
        List<ContentPatchLog> records = postService.listAllVersionsBy(postId);
        List<ContentRecordDTO> results = records.stream()
            .map(record -> (ContentRecordDTO) new ContentRecordDTO().convertFrom(record))
            .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/contents/records/{id:\\d+}")
    public ResponseEntity<ContentRecordDTO> getContentRecordById(@PathVariable Integer id) {
        ContentPatchLog contentPatchLog = contentPatchLogService.getById(id);
        PatchedContent patchedContent = contentPatchLogService.applyPatch(contentPatchLog);
        ContentRecordDTO contentRecordDTO = new ContentRecordDTO().convertFrom(contentPatchLog);
        contentRecordDTO.setContent(patchedContent.getContent());
        contentRecordDTO.setOriginalContent(patchedContent.getOriginalContent());
        return ResponseEntity.ok(contentRecordDTO);
    }

    @PutMapping("/{postId:\\d+}/versions/{version:\\d+}/rollback")
    public ResponseEntity<PostDetailDTO> rollbackToVersion(@PathVariable Integer postId,
        @PathVariable Integer version) {
        PostDetailDTO post = postService.rollbackByIdAndVersion(postId, version);
        return ResponseEntity.ok(post);
    }

    private PostDTO convertTo(Post post) {
        return new PostDTO().convertFrom(post);
    }
}
