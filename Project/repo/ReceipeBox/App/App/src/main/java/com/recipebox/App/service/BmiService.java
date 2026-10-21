package com.recipebox.App.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.recipebox.App.entity.BmiRecord;

public interface BmiService {

	List<BmiRecord> getUserBmiHistory(int userId);

	Optional<BmiRecord> getLatestBmiRecord(int userId);

	boolean deleteBmiRecord(int recordId, Integer userId);

	Page<BmiRecord> getUserBmiHistoryPage(int userId, Pageable pageable);

	BmiRecord calculateBmiValues(BmiRecord bmiRecord, String unit, Double heightInches, Double feet);
}
