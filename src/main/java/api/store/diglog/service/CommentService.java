package api.store.diglog.service;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.dto.comment.CommentListRequest;
import api.store.diglog.model.dto.comment.CommentRequest;
import api.store.diglog.model.dto.comment.CommentResponse;
import api.store.diglog.model.entity.Comment;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static api.store.diglog.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberService memberService;

    public void save(CommentRequest commentRequest) {
        Member member = memberService.getCurrentMember();
        Post post = Post.builder().id(commentRequest.getPostId()).build();
        Comment parentComment = null;

        if (commentRequest.getParentCommentId() != null) {
            parentComment = commentRepository.findByIdAndIsDeletedFalse(commentRequest.getParentCommentId())
                    .orElseThrow(() -> new CustomException(COMMENT_PARENT_ID_NOT_FOUND));

            int MAX_DEPTH = 3;
            int parentDepth = commentRepository.getDepthByParentCommentId(commentRequest.getParentCommentId(), MAX_DEPTH);
            if (parentDepth + 1 >= MAX_DEPTH) {
                throw new CustomException(COMMENT_MAX_DEPTH_EXCEEDED);
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content(commentRequest.getContent())
                .parentComment(parentComment)
                .build();
        commentRepository.save(comment);
    }

    public Page<CommentResponse> getComments(CommentListRequest commentListRequest) {
        Pageable pageable = PageRequest.of(commentListRequest.getPage(), commentListRequest.getSize(), Sort.by("createdAt"));
        Page<Comment> comments = commentRepository.findByPostIdAndParentCommentId(commentListRequest.getPostId(), commentListRequest.getParentCommentId(), pageable);

        return comments.map(this::getCommentResponse);
    }
    private CommentResponse getCommentResponse(Comment comment) {
        if (comment.isDeleted()) {
            return CommentResponse.builder()
                    .id(comment.getId())
                    .isDeleted(true)
                    .build();
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .member(memberService.getProfile(comment.getMember().getId()))
                .isDeleted(false)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
