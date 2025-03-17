package api.store.diglog.repository;

import api.store.diglog.model.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByUsernameAndIsDeletedFalse(String username);

    Page<Member> findAllByUsernameContainingIgnoreCaseAndIsDeletedFalse(String username, Pageable pageable);

    @Modifying
    @Query("UPDATE Member m SET m.username = :username WHERE m.email = :email")
    void updateUsername(@Param("username") String username, @Param("email") String email);

    int countByUsername(String username);

    void deleteAllByEmail(String email);
}
