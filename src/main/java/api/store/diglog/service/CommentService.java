package api.store.diglog.service;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.dto.comment.CommentListRequest;
import api.store.diglog.model.dto.comment.CommentRequest;
import api.store.diglog.model.dto.comment.CommentResponse;
import api.store.diglog.model.dto.comment.CommentUpdateRequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static api.store.diglog.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberService memberService;

    private static final int MAX_DEPTH = 2;

    @Transactional
    public void save(CommentRequest commentRequest) {
        Member member = memberService.getCurrentMember();
        Post post = Post.builder().id(commentRequest.getPostId()).build();
        Comment parentComment = getParentComment(commentRequest.getParentCommentId());
        Member taggedMember = getTaggedMember(commentRequest.getTaggedUsername());

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content(commentRequest.getContent())
                .parentComment(parentComment)
                .taggedMember(taggedMember)
                .build();
        commentRepository.save(comment);
    }

    private Comment getParentComment(UUID parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }

        Comment parentComment = commentRepository.findByIdAndIsDeletedFalse(parentCommentId)
                .orElseThrow(() -> new CustomException(COMMENT_PARENT_ID_NOT_FOUND));

        int parentDepth = commentRepository.getDepthByParentCommentId(parentCommentId, MAX_DEPTH);
        if (parentDepth + 1 >= MAX_DEPTH) {
            throw new CustomException(COMMENT_MAX_DEPTH_EXCEEDED);
        }

        return parentComment;
    }

    private Member getTaggedMember(String username) {
        return (username != null) ? memberService.findActiveMemberByUsername(username) : null;
    }

    public Page<CommentResponse> getComments(CommentListRequest commentListRequest) {
        Pageable pageable = PageRequest.of(commentListRequest.getPage(), commentListRequest.getSize(), Sort.by("createdAt"));
        Page<Comment> comments = commentRepository.findByPostIdAndParentCommentIdAndIsDeletedFalse(commentListRequest.getPostId(), commentListRequest.getParentCommentId(), pageable);

        return comments.map(this::getCommentResponse);
    }

    private CommentResponse getCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .member(memberService.getCommentMember(comment.getMember().getId()))
                .isDeleted(false)
                .taggedUsername(getTaggedUsername(comment))
                .createdAt(comment.getCreatedAt())
                .replyCount(commentRepository.countByParentCommentIdAndIsDeletedFalse(comment.getId()))
                .build();
    }

    private String getTaggedUsername(Comment comment) {
        return comment.getTaggedMember() != null
                ? memberService.findMemberById(comment.getTaggedMember().getId()).getUsername()
                : null;
    }

    @Transactional
    public void update(CommentUpdateRequest commentUpdateRequest) {
        Member member = memberService.getCurrentMember();

        Comment comment = commentRepository.findById(commentUpdateRequest.getId())
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(member.getId())) {
            throw new CustomException(COMMENT_UPDATE_NO_AUTHORITY);
        }

        comment.updateContent(commentUpdateRequest.getContent());
        comment.updateTaggedMember(getTaggedMember(commentUpdateRequest.getTaggedUsername()));

        commentRepository.save(comment);
    }

    @Transactional
    public void delete(UUID commentId) {
        Member member = memberService.getCurrentMember();

        int result = commentRepository.updateIsDeletedByCommentIdAndMemberId(commentId, member.getId());

        if (result <= 0) {
            throw new CustomException(COMMENT_IS_DELETED_NO_CHANGE);
        }
    }
}
