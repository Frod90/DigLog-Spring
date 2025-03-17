package api.store.diglog.controller;

import api.store.diglog.model.dto.comment.CommentListRequest;
import api.store.diglog.model.dto.comment.CommentRequest;
import api.store.diglog.model.dto.comment.CommentResponse;
import api.store.diglog.model.dto.comment.CommentUpdateRequest;
import api.store.diglog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.UUID;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody CommentRequest commentRequest) {
        commentService.save(commentRequest);

        return ResponseEntity.status(HttpStatusCode.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getComments(@ParameterObject @ModelAttribute CommentListRequest commentListRequest) {
        Page<CommentResponse> comments = commentService.getComments(commentListRequest);

        return ResponseEntity.ok().body(comments);
    }

    @PatchMapping
    public ResponseEntity<Void> update(@RequestBody CommentUpdateRequest commentUpdateRequest) {
        commentService.update(commentUpdateRequest);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        commentService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
