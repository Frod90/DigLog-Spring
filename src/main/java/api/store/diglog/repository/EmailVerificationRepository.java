package api.store.diglog.repository;

import api.store.diglog.model.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    Optional<EmailVerification> findByEmail(String email);

    void deleteAllByEmail(String email);

    @Modifying
    @Query("UPDATE EmailVerification ev SET ev.verified = true WHERE ev.email = :email")
    void updateVerifiedTrue(@Param("email") String email);
}
