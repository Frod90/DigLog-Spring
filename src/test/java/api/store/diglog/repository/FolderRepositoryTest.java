package api.store.diglog.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.folder.FolderPostCountResponse;
import api.store.diglog.model.entity.Folder;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FolderRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	PostRepository postRepository;

	@Autowired
	EntityManager entityManager;

	@DisplayName("회원 이름으로 폴더를 조회할 수 있다")
	@Test
	void findAllByMember() {

		// given
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
		entityManager.flush();

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

		// when
		List<FolderPostCountResponse> folderPostCountResponses = folderRepository.findAllWithPostCountByMember(member);

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

	@DisplayName("부모 폴더 id 목록을 이용해 폴더 목록을 조회할 수 있다.")
	@Test
	void findAllByParentFolderIdIn() {
		// given
		Member member = Member.builder()
			.email("frod@gmail.com")
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
		Folder folder04 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("Spring")
			.depth(1)
			.orderIndex(3)
			.parentFolder(folder01)
			.build();
		Folder folder05 = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("AOP")
			.depth(2)
			.orderIndex(4)
			.parentFolder(folder04)
			.build();

		folderRepository.saveAll(List.of(folder01, folder02, folder03, folder04, folder05));
		entityManager.flush();

		// when
		List<Folder> folders = folderRepository.findAllByParentFolderIdIn(
			List.of(folder01.getId(), folder04.getId())
		);

		// then
		assertThat(folders).hasSize(3)
			.contains(folder02, folder04, folder05);

	}
}