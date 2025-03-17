package api.store.diglog.service;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.constant.SearchOption;
import api.store.diglog.model.dto.post.*;
import api.store.diglog.model.entity.Folder;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.model.entity.Tag;
import api.store.diglog.model.vo.image.ImagePostVO;
import api.store.diglog.model.vo.tag.TagPostVO;
import api.store.diglog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static api.store.diglog.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberService memberService;
    private final ImageService imageService;
    private final TagService tagService;
    private final FolderService folderService;

    @Transactional
    public void save(PostRequest postRequest) {
        Member member = memberService.getCurrentMember();
        List<Tag> tags = saveNewTags(postRequest.getTagNames());
        Folder folder = folderService.getFolderByIdAndMemberId(postRequest.getFolderId(), member.getId());

        Post post = Post.builder()
                .member(member)
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .folder(folder)
                .tags(tags)
                .build();
        Post savedPost = postRepository.save(post);

        ImagePostVO imagePostVO = ImagePostVO.builder()
                .id(savedPost.getId())
                .urls(postRequest.getUrls())
                .build();
        imageService.savePostImage(imagePostVO);
    }

    @Transactional
    public void update(PostUpdateRequest postUpdateRequest) {
        Member member = memberService.getCurrentMember();

        List<Tag> tags = saveNewTags(postUpdateRequest.getTagNames());

        Post post = postRepository.findById(postUpdateRequest.getId())
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
        if (!post.getMember().equals(member)) {
            throw new CustomException(POST_NO_PERMISSION);
        }

        Folder folder = folderService.getFolderByIdAndMemberId(postUpdateRequest.getFolderId(), member.getId());

        Post updatePost = postUpdateRequest.toPost(post, folder, tags);
        postRepository.save(updatePost);

        ImagePostVO imagePostVO = ImagePostVO.builder()
                .id(post.getId())
                .urls(postUpdateRequest.getUrls())
                .build();
        imageService.saveUpdatedPostImage(imagePostVO);
    }

    private List<Tag> saveNewTags(List<String> tagNames) {
        TagPostVO tagPostVO = TagPostVO.builder()
                .tagNames(tagNames)
                .build();
        return tagService.saveAll(tagPostVO);
    }

    @Transactional
    public void updateFolder(PostFolderUpdateRequest postFolderUpdateRequest) {
        Member member = memberService.getCurrentMember();
        Folder folder = folderService.getFolderByIdAndMemberId(postFolderUpdateRequest.getFolderId(), member.getId());

        List<Post> posts = postRepository.findAllByIdInAndMemberId(postFolderUpdateRequest.getPostIds(), member.getId());
        posts.forEach(post -> {
            if (!post.getMember().equals(member)) {
                throw new CustomException(POST_NO_PERMISSION);
            }
        });

        posts.forEach(post -> post.updateFolder(folder));
        postRepository.saveAll(posts);
    }

    public PostResponse getPost(UUID id) {
        Post post = postRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        return new PostResponse(post);
    }

    public Page<PostResponse> getPosts(PostListSearchRequest postListSearchRequest) {
        Pageable pageable = getPageable(postListSearchRequest);

        try {
            return postRepository.findAllByIsDeletedFalse(pageable).map(PostResponse::new);
        } catch (Exception e) {
            throw new CustomException(POST_INVALID_SORT);
        }
    }

    public Page<PostResponse> searchPosts(PostListSearchRequest postListSearchRequest) {
        Pageable pageable = getPageable(postListSearchRequest);
        SearchOption option = postListSearchRequest.getOption();

        String title = postListSearchRequest.getKeyword();
        String tagName = postListSearchRequest.getKeyword();

        return switch (option) {
            case ALL ->
                    postRepository.findAllByTitleContainingIgnoreCaseOrTagsNameContainingIgnoreCaseAndIsDeletedFalse(title, tagName, pageable)
                            .map(PostResponse::new);
            case TITLE -> postRepository.findAllByTitleContainingIgnoreCaseAndIsDeletedFalse(title, pageable)
                    .map(PostResponse::new);
            case TAG -> postRepository.findAllByTagsNameContainingIgnoreCaseAndIsDeletedFalse(tagName, pageable)
                    .map(PostResponse::new);
        };
    }

    private Pageable getPageable(PostListSearchRequest postListSearchRequest) {
        int page = postListSearchRequest.getPage();
        int size = postListSearchRequest.getSize();
        List<Sort.Order> orders = new ArrayList<>(postListSearchRequest.getSorts().stream()
                .map(Sort.Order::by)
                .toList());
        orders.addLast(Sort.Order.by("id"));

        Pageable pageable;
        if (postListSearchRequest.getIsDescending()) {
            pageable = PageRequest.of(page, size, Sort.by(orders).descending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by(orders).ascending());
        }

        return pageable;
    }

    public Page<PostResponse> getMemberPosts(PostListMemberRequest postListMemberRequest) {
        Pageable pageable = PageRequest.of(postListMemberRequest.getPage(), postListMemberRequest.getSize(),
                Sort.by("createdAt", "id").descending());
        Member member = memberService.findActiveMemberByUsername(postListMemberRequest.getUsername());

        if (postListMemberRequest.getFolderIds() == null || postListMemberRequest.getFolderIds().isEmpty()) {
            return postRepository.findAllByMemberIdAndIsDeletedFalse(member.getId(), pageable)
                    .map(PostResponse::new);
        }

        List<UUID> folderIds = folderService.getFoldersByIdList(postListMemberRequest.getFolderIds())
                .stream().map(Folder::getId)
                .toList();
        return postRepository.findAllByMemberIdAndFolderIdInAndIsDeletedFalse(member.getId(), folderIds, pageable)
                .map(PostResponse::new);
    }

    public Page<PostResponse> getMemberTagPosts(PostListMemberTagRequest postListMemberTagRequest) {
        Pageable pageable = PageRequest.of(postListMemberTagRequest.getPage(), postListMemberTagRequest.getSize(),
                Sort.by("createdAt", "id").descending());
        String username = postListMemberTagRequest.getUsername();
        UUID tagId = postListMemberTagRequest.getTagId();

        return postRepository.findAllByMemberUsernameAndTagsIdAndIsDeletedFalse(username, tagId, pageable)
                .map(PostResponse::new);
    }

    @Transactional
    public void delete(UUID id) {
        Member member = memberService.getCurrentMember();

        int deletedRows = postRepository.updatePostIsDeleted(id, member);

        if (deletedRows == 0) {
            throw new CustomException(POST_DELETE_FAILED);
        }
    }
}
