package api.store.diglog.service;

import static api.store.diglog.common.exception.ErrorCode.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.dto.folder.FolderCreateRequest;
import api.store.diglog.model.dto.folder.FolderDeleteRequest;
import api.store.diglog.model.dto.folder.FolderPostCountResponse;
import api.store.diglog.model.dto.folder.FolderResponse;
import api.store.diglog.model.entity.Folder;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.repository.FolderRepository;
import api.store.diglog.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {
	private static final int MAX_FOLDER_SIZE = 100;

	private static final String UNDER_BAR_SIGN = "_";

	private final FolderRepository folderRepository;
	private final MemberService memberService;
	private final PostRepository postRepository;

	public List<FolderPostCountResponse> getFoldersWithPostCount(String username) {

		Member member = memberService.findActiveMemberByUsername(username);
		return folderRepository.findAllWithPostCountByMember(member);
	}

	@Transactional
	public List<FolderResponse> createAndUpdateFolders(List<FolderCreateRequest> folderCreateRequests) {

		validateDuplicationParentFolderAndTitle(folderCreateRequests);
		validateFolderSize(folderCreateRequests);
		validateDuplicationOrderIndex(folderCreateRequests);

		Member member = memberService.getCurrentMember();
		Map<Integer, Folder> allFolders = new TreeMap<>();

		int maxDepth = calculateMaxDepthFromRequests(folderCreateRequests);
		IntStream.range(0, maxDepth + 1)
			.forEach(depth -> {
				Map<Integer, Folder> foldersAtCurrentDepth = createAndUpdateFoldersByDepth(
					folderCreateRequests,
					member,
					depth,
					allFolders
				);

				allFolders.putAll(foldersAtCurrentDepth);
			});

		List<Folder> savedFolders = folderRepository.saveAll(allFolders.values());
		return savedFolders.stream()
			.map(folder -> FolderResponse.builder()
				.folder(folder)
				.build())
			.toList();
	}

	private void validateDuplicationParentFolderAndTitle(List<FolderCreateRequest> folderCreateRequests) {

		Set<String> uniqueFolders = new HashSet<>();

		folderCreateRequests.forEach(request -> {
			if (!uniqueFolders.add(request.getParentOrderIndex() + UNDER_BAR_SIGN + request.getTitle())) {
				throw new CustomException(
					FOLDER_DUPLICATION_TITLE,
					FOLDER_DUPLICATION_TITLE.getMessage()
				);
			}
		});
	}

	private void validateFolderSize(List<FolderCreateRequest> folderCreateRequests) {
		if (folderCreateRequests.size() > MAX_FOLDER_SIZE) {
			throw new CustomException(
				FOLDER_OVER_FLOW_SIZE,
				String.format(FOLDER_OVER_FLOW_SIZE.getMessage(), MAX_FOLDER_SIZE)
			);
		}
	}

	private void validateDuplicationOrderIndex(List<FolderCreateRequest> folderCreateRequests) {

		Set<Integer> uniqueOrderIndexes = folderCreateRequests.stream()
			.map(FolderCreateRequest::getOrderIndex)
			.collect(Collectors.toSet());

		if (folderCreateRequests.size() != uniqueOrderIndexes.size()) {
			throw new CustomException(
				FOLDER_DUPLICATION_ORDER_INDEX,
				FOLDER_DUPLICATION_ORDER_INDEX.getMessage()
			);
		}
	}

	private int calculateMaxDepthFromRequests(List<FolderCreateRequest> folderCreateRequests) {
		return folderCreateRequests.stream()
			.mapToInt(FolderCreateRequest::getDepth)
			.max()
			.orElse(0);
	}

	private Map<Integer, Folder> createAndUpdateFoldersByDepth(
		List<FolderCreateRequest> requests,
		Member member,
		int depth,
		Map<Integer, Folder> existingFolders
	) {
		return requests.stream()
			.filter(request -> request.getDepth() == depth)
			.collect(Collectors.toMap(
				FolderCreateRequest::getOrderIndex,
				request -> createAndUpdateFolder(request, member, existingFolders)
			));
	}

	private Folder createAndUpdateFolder(
		FolderCreateRequest folderCreateRequest,
		Member member,
		Map<Integer, Folder> existingFolders
	) {
		Folder parentFolder = null;
		if (folderCreateRequest.getDepth() > 0) {
			parentFolder = existingFolders.get(folderCreateRequest.getParentOrderIndex());
		}

		UUID folderId = folderCreateRequest.getId();
		if (folderId == null) {
			folderId = UUID.randomUUID();
		}

		return Folder.builder()
			.id(folderId)
			.member(member)
			.title(folderCreateRequest.getTitle())
			.depth(folderCreateRequest.getDepth())
			.orderIndex(folderCreateRequest.getOrderIndex())
			.parentFolder(parentFolder)
			.build();
	}

	public Folder getFolderByIdAndMemberId(UUID folderId, UUID memberId) {
		if (folderId == null) {
			return null;
		}

		return folderRepository.findByIdAndMemberId(folderId, memberId)
			.orElseThrow(() -> new CustomException(FOLDER_OWNER_MISMATCH));
	}

	public List<Folder> getFoldersByIdList(List<UUID> folderIds) {
		return folderRepository.findAllByIdIn(folderIds);
	}

	@Transactional
	public void deleteAllBy(List<FolderDeleteRequest> folderDeleteRequests) {


		List<UUID> folderIds = folderDeleteRequests.stream()
			.map(FolderDeleteRequest::getFolderId)
			.toList();

		validateChildFolders(folderIds);
		validateContainsPosts(folderIds);

		Member currentMember = memberService.getCurrentMember();
		List<Folder> folders = folderRepository.findAllByIdIn(folderIds);
		validateFolderMember(currentMember, folders);

		int max = calculateMaxDepthFromFolders(folders);
		int min = calculateMinDepthFromFolders(folders);

		for (int depth = max; depth >= min; depth--) {
			deleteAllByDepth(folders, depth);
		}

	}

	private void validateChildFolders(List<UUID> folderIds) {
		List<Folder> childFolders = folderRepository.findAllByParentFolderIdIn(folderIds);

		childFolders.stream()
			.filter(childFolder -> !folderIds.contains(childFolder.getId()))
			.findFirst()
			.ifPresent(folder -> {
				throw new CustomException(
					FOLDER_EXIST_CHILD_FOLDER,
					String.format(
						FOLDER_EXIST_CHILD_FOLDER.getMessage(),
						folder.getParentFolder().getTitle(),
						folder.getTitle()
					)
				);
			});
	}

	private void validateContainsPosts(List<UUID> folderIds) {
		List<Post> posts = postRepository.findAllByFolderIdIn(folderIds);

		if (!posts.isEmpty()) {
			Post post = posts.getFirst();
			throw new CustomException(
				FOLDER_CONTAIN_POST,
				String.format(FOLDER_CONTAIN_POST.getMessage(),
					post.getFolder().getTitle(),
					post.getTitle())
			);
		}
	}

	private void validateFolderMember(Member currentMember, List<Folder> folders) {

		boolean isNotMatchMember = folders.stream()
			.anyMatch(folder -> !folder.getMember().equals(currentMember));

		if (isNotMatchMember) {
			throw new CustomException(
				FOLDER_NOT_MATCH_MEMBER,
				FOLDER_NOT_MATCH_MEMBER.getMessage()
			);
		}
	}

	private int calculateMaxDepthFromFolders(List<Folder> folders) {
		return folders.stream()
			.mapToInt(Folder::getDepth)
			.max()
			.orElse(0);
	}

	private int calculateMinDepthFromFolders(List<Folder> folders) {
		return folders.stream()
			.mapToInt(Folder::getDepth)
			.min()
			.orElse(0);
	}

	private void deleteAllByDepth(List<Folder> folders, int depth) {
		folderRepository.deleteAllInBatch(
			folders.stream()
				.filter(folder -> folder.getDepth() == depth)
				.toList()
		);
	}
}
