package io.mosip.pms.common.repository;
import io.mosip.pms.common.dto.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserDetailsRepository  extends JpaRepository<UserDetails, String> {

    @Query("SELECT e FROM UserDetails e WHERE e.userId = :userId")
    public Optional<UserDetails> findByUserId(@Param("userId") String userId);
}