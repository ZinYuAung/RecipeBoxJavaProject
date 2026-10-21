package com.recipebox.App.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.recipebox.App.entity.BmiRecord;

public interface BmiRepository extends JpaRepository<BmiRecord, Integer >{

	List<BmiRecord> findByUserUserIdOrderByRecordedAtDesc(int userId);

	@Query("SELECT b FROM BmiRecord b WHERE b.user.userId = :userId ORDER BY b.recordedAt DESC")
    Page<BmiRecord> findByUserIdOrderByRecordedAtDesc(@Param("userId") int userId, Pageable pageable);
	
}
