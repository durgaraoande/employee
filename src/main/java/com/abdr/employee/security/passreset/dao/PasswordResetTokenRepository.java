package com.abdr.employee.security.passreset.dao;

import com.abdr.employee.security.entities.User;
import com.abdr.employee.security.passreset.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findAllByExpiryDateBeforeAndUsedFalse(LocalDateTime now);
    Optional<PasswordResetToken> findByUserAndUsedFalse(User user);

    void deleteByUser(User user);
}
