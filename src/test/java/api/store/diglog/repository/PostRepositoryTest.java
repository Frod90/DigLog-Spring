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
import api.store.diglog.model.entity.Folder;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.entity.Post;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	PostRepository postRepository;

	@Autowired
	EntityManager entityManager;

	@DisplayName("폴더 id를 이용해 게시글 목록을 조회할 수 있다.")
	@Test
	void findAllByFolderIdIn() {

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

		folderRepository.saveAll(List.of(folder01, folder02));
		entityManager.flush();

		Post projectAPost = Post.builder()
			.member(member)
			.folder(folder01)
			.title("프로젝트 A 개요")
			.content("프로젝트 A에 관한 설명")
			.isDeleted(false)
			.build();
		Post dbPost01 = Post.builder()
			.member(member)
			.folder(folder02)
			.title("DB 설정")
			.content("프로젝트 A의 DB 설정에 관한 설명")
			.isDeleted(false)
			.build();
		Post dbPost02 = Post.builder()
			.member(member)
			.folder(folder02)
			.title("정규화, 반정규화 개요")
			.content("프로젝트 A의 정규화, 반정규화에 관한 설명")
			.isDeleted(false)
			.build();

		postRepository.saveAll(List.of(projectAPost, dbPost01, dbPost02));
		entityManager.flush();

		// when
		List<Post> posts = postRepository.findAllByFolderIdIn(List.of(folder02.getId()));

		// then
		assertThat(posts).hasSize(2)
			.extracting("id", "member", "folder", "title", "content", "isDeleted")
			.contains(
				tuple(dbPost01.getId(), member, folder02, "DB 설정", "프로젝트 A의 DB 설정에 관한 설명", false),
				tuple(dbPost02.getId(), member, folder02, "정규화, 반정규화 개요", "프로젝트 A의 정규화, 반정규화에 관한 설명", false)
			);

	}

}