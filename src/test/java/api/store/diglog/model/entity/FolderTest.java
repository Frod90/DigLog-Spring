package api.store.diglog.model.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;

class FolderTest {

	@Test
	@DisplayName("폴더를 생성할 수 있습니다.")
	public void createFolder() {

		// given
		Member member = Member.builder()
			.id(UUID.randomUUID())
			.email("testEmail@gmail.com")
			.username("testUser")
			.password("testPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();

		UUID folderId = UUID.randomUUID();

		// when
		Folder folder = Folder.builder()
			.id(folderId)
			.member(member)
			.title("testTitle")
			.depth(0)
			.orderIndex(1)
			.parentFolder(null)
			.build();

		// then
		assertThat(folder)
			.extracting("id", "member", "title", "depth", "orderIndex", "parentFolder")
			.containsExactly(folderId, member, "testTitle", 0, 1, null);
	}

	@Test
	@DisplayName("상위 폴더가 있는 폴더를 생성할 수 있습니다.")
	public void createFolderWithParentFolder() {

		// given
		Member member = Member.builder()
			.id(UUID.randomUUID())
			.email("testEmail@gmail.com")
			.username("testUser")
			.password("testPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();

		Folder parentFolder = Folder.builder()
			.id(UUID.randomUUID())
			.member(member)
			.title("testTitle")
			.depth(0)
			.orderIndex(1)
			.parentFolder(null)
			.build();

		UUID folderId = UUID.randomUUID();

		// when
		Folder folder = Folder.builder()
			.id(folderId)
			.member(member)
			.title("testTitle")
			.depth(1)
			.orderIndex(2)
			.parentFolder(parentFolder)
			.build();

		// then
		assertThat(folder)
			.extracting("id", "member", "title", "depth", "orderIndex", "parentFolder")
			.containsExactly(folderId, member, "testTitle", 1, 2, parentFolder);
	}

	@DisplayName("폴더의 depth는 3미만만 허용됩니다.")
	@ParameterizedTest(name = "depth가 {0}인 폴더는 생성할 수 없습니다..")
	@ValueSource(ints = {-99, -1, 3, 99})
	public void createFolderWithOverFlowDepth(int overFlowDepth) {

		// given
		Member member = Member.builder()
			.id(UUID.randomUUID())
			.email("testEmail@gmail.com")
			.username("testUser")
			.password("testPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();

		// when, then
		assertThatThrownBy(() ->
			Folder.builder()
				.id(UUID.randomUUID())
				.member(member)
				.title("testTitle")
				.depth(overFlowDepth)
				.orderIndex(2)
				.parentFolder(null)
				.build())
			.isInstanceOf(CustomException.class)
			.hasMessage("하위 폴더의 깊이는 3까지 허용됩니다.");
	}

	@DisplayName("폴더의 순서는 99미만만 허용됩니다.")
	@ParameterizedTest(name = "순서가 {0}인 폴더는 생성할 수 없습니다.")
	@ValueSource(ints = {-99, -1, 100, 101, 999})
	public void createFolderWithOverFlowOrderIndex(int overFlowOrderIndex) {

		// given
		Member member = Member.builder()
			.id(UUID.randomUUID())
			.email("testEmail@gmail.com")
			.username("testUser")
			.password("testPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();

		// when, then
		assertThatThrownBy(() ->
			Folder.builder()
				.id(UUID.randomUUID())
				.member(member)
				.title("testTitle")
				.depth(0)
				.orderIndex(overFlowOrderIndex)
				.parentFolder(null)
				.build())
			.isInstanceOf(CustomException.class)
			.hasMessage("최대 폴더 순서(100번)를 초과했습니다.");
	}

	@DisplayName("폴더의 제목은 25자 이하만 허용됩니다.")
	@ParameterizedTest(name = "제목이 \"{0}\"인 폴더는 생성할 수 없습니다.")
	@ValueSource(strings = {
		"12345678901234567890123456",
		"1234567890123456789012345678901234567890123456789"
	})
	public void createFolderWithOverFlowOrderIndex(String overFlowTitle) {

		// given
		Member member = Member.builder()
			.id(UUID.randomUUID())
			.email("testEmail@gmail.com")
			.username("testUser")
			.password("testPassword")
			.roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.build();

		// when, then
		assertThatThrownBy(() ->
			Folder.builder()
				.id(UUID.randomUUID())
				.member(member)
				.title(overFlowTitle)
				.depth(0)
				.orderIndex(0)
				.parentFolder(null)
				.build())
			.isInstanceOf(CustomException.class)
			.hasMessage("폴더 제목은 25자 까지만 허용됩니다.");
	}
}