package api.store.diglog.controller;

import api.store.diglog.model.dto.post.*;
import api.store.diglog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody PostRequest postRequest) {
        postService.save(postRequest);

        return ResponseEntity.ok().build();
    }

    @PatchMapping
    public ResponseEntity<Void> update(@RequestBody PostUpdateRequest postUpdateRequest) {
        postService.update(postUpdateRequest);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/folder")
    public ResponseEntity<Void> updateFolder(@RequestBody PostFolderUpdateRequest postFolderUpdateRequest) {
        postService.updateFolder(postFolderUpdateRequest);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable("id") UUID id) {
        PostResponse postResponse = postService.getPost(id);

        return ResponseEntity.ok().body(postResponse);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(@ParameterObject @ModelAttribute PostListSearchRequest postListSearchRequest) {
        Page<PostResponse> postResponses = postService.getPosts(postListSearchRequest);

        return ResponseEntity.ok().body(postResponses);
    }

    @GetMapping("/member")
    public ResponseEntity<Page<PostResponse>> getMemberPosts(@ParameterObject @ModelAttribute PostListMemberRequest postListMemberRequest) {
        Page<PostResponse> postResponses = postService.getMemberPosts(postListMemberRequest);

        return ResponseEntity.ok().body(postResponses);
    }

    @GetMapping("/member/tag")
    public ResponseEntity<Page<PostResponse>> getMemberTagPosts(@ParameterObject @ModelAttribute PostListMemberTagRequest postListMemberTagRequest) {
        Page<PostResponse> postResponses = postService.getMemberTagPosts(postListMemberTagRequest);

        return ResponseEntity.ok().body(postResponses);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(@ParameterObject @ModelAttribute PostListSearchRequest postListSearchRequest) {
        Page<PostResponse> postResponses = postService.searchPosts(postListSearchRequest);

        return ResponseEntity.ok().body(postResponses);
    }

    @PatchMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        postService.delete(id);

        return ResponseEntity.ok().build();
    }
}
