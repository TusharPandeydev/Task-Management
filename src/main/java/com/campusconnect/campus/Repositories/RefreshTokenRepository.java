package com.campusconnect.campus.Repositories;

import com.campusconnect.campus.Model.RefreshToken;
import com.campusconnect.campus.Model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    Optional<RefreshToken> findByUser(User user);
    boolean existsByUser(User user);
    @Modifying // this is done which shows that database there will be rows get affected
    @Query("UPDATE RefreshToken r set r.revoked=true where r.familyId=:familyId")
    void revokeRefreshToken(@Param("familyId")String familyId);


}
