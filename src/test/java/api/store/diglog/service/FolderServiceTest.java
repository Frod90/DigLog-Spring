package api.store.diglog.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.folder.FolderCreateRequest;
import api.store.diglog.model.dto.folder.FolderDeleteRequest;
import api.store.diglog.model.dto.folder.FolderPostCountResponse;
import api.store.diglog.model.dto.folder.FolderResponse;
import api.store.diglog.model.entity.Folder;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import api.store.diglog.repository.FolderRepository;
import api.store.diglog.repository.MemberRepository;
import api.store.diglog.repository.PostRepository;
import jakarta.persistence.EntityManager;

@SuppressWarnings("ALL")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FolderServiceTest {

	@Autowired
	private MemberRepository memberRepository;

	@MockitoBean
	private MemberService memberService;

	@Autowired
	private FolderService folderService;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private EntityManager entityManager;

	@BeforeEach
	void setUp() {
		Member member = Member.builder()
			.email("testEmail@gmail.com")
			.username("testUser")
			.password("testPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();

		memberRepository.save(member);

		BDDMockito.given(memberService.getCurrentMember())
			.willReturn(member);
	}

	@DisplayName("폴더를 조회할 수 있다.")
	@Test
	void getFoldersWithPostCount() {

		// given
		Member member = memberRepository.findByUsername("testUser").get();

		Folder folder01 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("test01")
			.depth(0)
			.orderIndex(0)
			.parentFolder(null)
			.build();
		Folder folder02 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("test02")
			.depth(1)
			.orderIndex(1)
			.parentFolder(folder01)
			.build();
		Folder folder03 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("test03")
			.depth(2)
			.orderIndex(2)
			.parentFolder(folder02)
			.build();

		folderRepository.saveAll(List.of(folder01, folder02, folder03));
		entityManager.flush();

		List<Post> posts = List.of(
			Post.builder()
				.member(member)
				.folder(folder01)
				.title("title01")
				.content("content01")
				.build(),
			Post.builder()
				.member(member)
				.folder(folder01)
				.title("title02")
				.content("content02")
				.build(),
			Post.builder()
				.member(member)
				.folder(folder02)
				.title("title03")
				.content("content03")
				.build()
		);
		postRepository.saveAll(posts);
		entityManager.flush();

		BDDMockito.given(memberService.findActiveMemberByUsername(any()))
			.willReturn(member);

		// when
		List<FolderPostCountResponse> folderPostCountResponses = folderService.getFoldersWithPostCount(
			member.getUsername());

		// then
		assertThat(folderPostCountResponses)
			.hasSize(3)
			.extracting("folderId", "title", "depth", "orderIndex", "parentFolderId", "postCount")
			.containsExactly(
				tuple(folder01.getId(), "test01", 0, 0, null, 2L),
				tuple(folder02.getId(), "test02", 1, 1, folder01.getId(), 1L),
				tuple(folder03.getId(), "test03", 2, 2, folder02.getId(), 0L)
			);

	}

	@DisplayName("새로운 폴더 목록을 생성할 수 있다.")
	@Test
	void createAndUpdateFolders_WithNewFolders() {

		// given
		List<FolderCreateRequest> folderCreateRequests = List.of(
			FolderCreateRequest.builder()
				.title("title01")
				.depth(0)
				.orderIndex(0)
				.build(),
			FolderCreateRequest.builder()
				.title("title02")
				.depth(0)
				.orderIndex(1)
				.build(),
			FolderCreateRequest.builder()
				.title("title03")
				.depth(0)
				.orderIndex(2)
				.build()
		);

		// when
		List<FolderResponse> expectedFolders = folderService.createAndUpdateFolders(folderCreateRequests);

		// then
		assertThat(expectedFolders)
			.hasSize(3)
			.extracting("title", "depth", "orderIndex", "parentFolderId")
			.containsExactly(
				tuple("title01", 0, 0, "none"),
				tuple("title02", 0, 1, "none"),
				tuple("title03", 0, 2, "none")
			);
	}

	@DisplayName("기존 폴더에 새로운 폴더를 추가할 수 있다")
	@Test
	void createAndUpdateFolders_WithAdditionalFolders() {

		// given
		List<FolderCreateRequest> defaultFolderCreateRequests = List.of(
			FolderCreateRequest.builder()
				.title("title01")
				.depth(0)
				.orderIndex(0)
				.build(),
			FolderCreateRequest.builder()
				.title("title02")
				.depth(0)
				.orderIndex(1)
				.build(),
			FolderCreateRequest.builder()
				.title("title03")
				.depth(0)
				.orderIndex(2)
				.build()
		);
		List<FolderResponse> defaultFolderResponses = folderService.createAndUpdateFolders(defaultFolderCreateRequests);

		List<FolderCreateRequest> updateFolderCreateRequests = List.of(
			FolderCreateRequest.builder()
				.id(defaultFolderResponses.get(0).getFolderId())
				.title("title01")
				.depth(0)
				.orderIndex(0)
				.parentOrderIndex(-1)
				.build(),
			FolderCreateRequest.builder()
				.title("title01 child01")
				.depth(1)
				.orderIndex(1)
				.parentOrderIndex(0)
				.build(),
			FolderCreateRequest.builder()
				.title("title01 child01 child01")
				.depth(2)
				.orderIndex(2)
				.parentOrderIndex(1)
				.build(),
			FolderCreateRequest.builder()
				.id(defaultFolderResponses.get(1).getFolderId())
				.title("title02")
				.depth(0)
				.orderIndex(3)
				.parentOrderIndex(-1)
				.build(),
			FolderCreateRequest.builder()
				.id(defaultFolderResponses.get(2).getFolderId())
				.title("title03")
				.depth(0)
				.orderIndex(4)
				.parentOrderIndex(-1)
				.build(),
			FolderCreateRequest.builder()
				.title("title03 child01")
				.depth(1)
				.orderIndex(5)
				.parentOrderIndex(4)
				.build(),
			FolderCreateRequest.builder()
				.title("title03 child02")
				.depth(1)
				.orderIndex(6)
				.parentOrderIndex(4)
				.build(),
			FolderCreateRequest.builder()
				.title("title04")
				.depth(0)
				.orderIndex(7)
				.parentOrderIndex(-1)
				.build()
		);

		// when
		List<FolderResponse> expectedFolders = folderService.createAndUpdateFolders(updateFolderCreateRequests);

		// then
		assertThat(expectedFolders)
			.hasSize(8)
			.extracting("folderId", "title", "depth", "orderIndex", "parentFolderId")
			.containsExactly(
				tuple(expectedFolders.get(0).getFolderId(), "title01", 0, 0, "none"),
				tuple(expectedFolders.get(1).getFolderId(), "title01 child01", 1, 1,
					expectedFolders.get(0).getFolderId().toString()),
				tuple(expectedFolders.get(2).getFolderId(), "title01 child01 child01", 2, 2,
					expectedFolders.get(1).getFolderId().toString()),
				tuple(expectedFolders.get(3).getFolderId(), "title02", 0, 3, "none"),
				tuple(expectedFolders.get(4).getFolderId(), "title03", 0, 4, "none"),
				tuple(expectedFolders.get(5).getFolderId(), "title03 child01", 1, 5,
					expectedFolders.get(4).getFolderId().toString()),
				tuple(expectedFolders.get(6).getFolderId(), "title03 child02", 1, 6,
					expectedFolders.get(4).getFolderId().toString()),
				tuple(expectedFolders.get(7).getFolderId(), "title04", 0, 7, "none")
			);
	}

	@DisplayName("기존 폴더를 수정할 수 있다")
	@Test
	void createAndUpdateFolders_WithUpdateFolders() {

		// given
		List<FolderCreateRequest> defaultFolderCreateRequests = List.of(
			FolderCreateRequest.builder()
				.title("title01")
				.depth(0)
				.orderIndex(0)
				.build(),
			FolderCreateRequest.builder()
				.title("title02")
				.depth(0)
				.orderIndex(1)
				.build(),
			FolderCreateRequest.builder()
				.title("title03")
				.depth(0)
				.orderIndex(2)
				.build()
		);
		List<FolderResponse> defaultFolders = folderService.createAndUpdateFolders(defaultFolderCreateRequests);

		List<FolderCreateRequest> updateFolderCreateRequests = List.of(
			FolderCreateRequest.builder()
				.id(defaultFolders.get(0).getFolderId())
				.title("title01 -> title03")
				.depth(0)
				.orderIndex(2)
				.build(),
			FolderCreateRequest.builder()
				.id(defaultFolders.get(1).getFolderId())
				.title("title02")
				.depth(0)
				.orderIndex(1)
				.build(),
			FolderCreateRequest.builder()
				.id(defaultFolders.get(2).getFolderId())
				.title("title03 -> title01")
				.depth(0)
				.orderIndex(0)
				.build()
		);

		// when
		List<FolderResponse> expectedFolders = folderService.createAndUpdateFolders(updateFolderCreateRequests);

		// then
		assertThat(expectedFolders)
			.hasSize(3)
			.extracting("folderId", "title", "depth", "orderIndex", "parentFolderId")
			.containsExactly(
				tuple(defaultFolders.get(2).getFolderId(), "title03 -> title01", 0, 0, "none"),
				tuple(defaultFolders.get(1).getFolderId(), "title02", 0, 1, "none"),
				tuple(defaultFolders.get(0).getFolderId(), "title01 -> title03", 0, 2, "none")
			);
	}

	@DisplayName("폴더 제목은 중복될 수 없다.")
	@Test
	void createAndUpdateFolders_WithTitleDuplication() {

		// given
		List<FolderCreateRequest> folderCreateRequests = List.of(
			FolderCreateRequest.builder()
				.title("title01")
				.depth(0)
				.orderIndex(0)
				.build(),
			FolderCreateRequest.builder()
				.title("title02")
				.depth(0)
				.orderIndex(1)
				.build(),
			FolderCreateRequest.builder()
				.title("title02")
				.depth(0)
				.orderIndex(2)
				.build()
		);

		// when then
		assertThatThrownBy(() -> folderService.createAndUpdateFolders(folderCreateRequests))
			.isInstanceOf(CustomException.class)
			.hasMessage("중복된 폴더 이름이 존재합니다.");
	}

	@DisplayName("폴더 목록은 100개 이하여야 한다.")
	@Test
	void createAndUpdateFolders_OverflowFolderSize() {

		// given
		List<FolderCreateRequest> folderCreateRequests = IntStream.range(0, 101)
			.boxed()
			.map(index -> FolderCreateRequest.builder()
				.id(UUID.randomUUID())
				.title("title" + index)
				.depth(0)
				.orderIndex(index)
				.build())
			.toList();

		// when then
		assertThatThrownBy(() -> folderService.createAndUpdateFolders(folderCreateRequests))
			.isInstanceOf(CustomException.class)
			.hasMessage("최대 폴더의 개수(100개)를 초과했습니다.");
	}

	@DisplayName("폴더 제목은 중복될 수 없다.")
	@Test
	void createAndUpdateFolders_WithOrderIndexDuplication() {

		// given
		List<FolderCreateRequest> folderCreateRequests = List.of(
			FolderCreateRequest.builder()
				.title("title01")
				.depth(0)
				.orderIndex(0)
				.build(),
			FolderCreateRequest.builder()
				.title("title02")
				.depth(0)
				.orderIndex(1)
				.build(),
			FolderCreateRequest.builder()
				.title("title03")
				.depth(0)
				.orderIndex(1)
				.build()
		);

		// when then
		assertThatThrownBy(() -> folderService.createAndUpdateFolders(folderCreateRequests))
			.isInstanceOf(CustomException.class)
			.hasMessage("중복된 폴더 순서가 존재합니다.");
	}

	@DisplayName("폴더를 삭제할 수 있다.")
	@Test
	void deleteAllBy() {

		Member member = memberRepository.findByUsername("testUser").get();

		Folder folder01 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("프로젝트 A")
			.depth(0)
			.orderIndex(0)
			.parentFolder(null)
			.build();
		Folder folder02 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("DB")
			.depth(1)
			.orderIndex(1)
			.parentFolder(folder01)
			.build();
		Folder folder03 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("MySQL")
			.depth(2)
			.orderIndex(2)
			.parentFolder(folder02)
			.build();

		folderRepository.saveAll(List.of(folder01, folder02, folder03));
		entityManager.flush();

		List<FolderDeleteRequest> folderDeleteRequests = List.of(
			FolderDeleteRequest.builder().folderId(folder02.getId()).build(),
			FolderDeleteRequest.builder().folderId(folder03.getId()).build()
		);

		// when
		folderService.deleteAllBy(folderDeleteRequests);

		// then
		List<Folder> folders = folderRepository.findAll();
		assertThat(folders).hasSize(1)
			.containsExactly(folder01);

	}

	@DisplayName("하위 폴더가 존재하는 폴더는 삭제할 수 없다.")
	@Test
	void deleteAllBy_WithChildFolder() {

		Member member = memberRepository.findByUsername("testUser").get();

		Folder folder01 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("프로젝트 A")
			.depth(0)
			.orderIndex(0)
			.parentFolder(null)
			.build();
		Folder folder02 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("DB")
			.depth(1)
			.orderIndex(1)
			.parentFolder(folder01)
			.build();
		Folder folder03 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("MySQL")
			.depth(2)
			.orderIndex(2)
			.parentFolder(folder02)
			.build();

		folderRepository.saveAll(List.of(folder01, folder02, folder03));
		entityManager.flush();

		List<FolderDeleteRequest> folderDeleteRequests = List.of(
			FolderDeleteRequest.builder().folderId(folder02.getId()).build()
		);

		// when, then
		assertThatThrownBy(() -> folderService.deleteAllBy(folderDeleteRequests))
			.isInstanceOf(CustomException.class)
			.hasMessage("\"DB\" 폴더 하위에 \"MySQL\" 폴더가 존재합니다. 먼저 삭제해주세요");
	}

	@DisplayName("게시글이 존재하는 폴더는 삭제할 수 없다.")
	@Test
	void deleteAllBy_WithPost() {

		Member member = memberRepository.findByUsername("testUser").get();

		Folder folder01 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("프로젝트 A")
			.depth(0)
			.orderIndex(0)
			.parentFolder(null)
			.build();

		folderRepository.save(folder01);
		entityManager.flush();

		Post post = Post.builder()
			.member(member)
			.folder(folder01)
			.title("프로젝트 A 개요")
			.content("프로젝트 A에 관한 설명")
			.isDeleted(false)
			.build();
		postRepository.save(post);
		entityManager.flush();

		List<FolderDeleteRequest> folderDeleteRequests = List.of(
			FolderDeleteRequest.builder().folderId(folder01.getId()).build()
		);

		// when, then
		assertThatThrownBy(() -> folderService.deleteAllBy(folderDeleteRequests))
			.isInstanceOf(CustomException.class)
			.hasMessage("\"프로젝트 A\" 폴더 하위에 \"프로젝트 A 개요\" 게시글이 존재합니다. 먼저 삭제해주세요");
	}

	@DisplayName("로그인 중인 회원과 폴더 회원이 일치하지 않으면 폴더를 삭제할 수 없다.")
	@Test
	void deleteAllBy_WithDifferentMember() {

		Member member = Member.builder()
			.email("frod90@gmail.com")
			.username("frod")
			.password("testPassword")
			.roles(Set.of(Role.ROLE_USER))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();

		memberRepository.save(member);
		entityManager.flush();

		Folder folder01 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("프로젝트 A")
			.depth(0)
			.orderIndex(0)
			.parentFolder(null)
			.build();

		folderRepository.save(folder01);
		entityManager.flush();

		List<FolderDeleteRequest> folderDeleteRequests = List.of(
			FolderDeleteRequest.builder().folderId(folder01.getId()).build()
		);

		// when, then
		assertThatThrownBy(() -> folderService.deleteAllBy(folderDeleteRequests))
			.isInstanceOf(CustomException.class)
			.hasMessage("로그인 중인 회원 정보와 폴더 회원 정보가 일치하지 않습니다.");
	}

}