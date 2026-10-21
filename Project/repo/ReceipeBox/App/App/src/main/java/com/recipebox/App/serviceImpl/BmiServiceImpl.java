package com.recipebox.App.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipebox.App.entity.BmiRecord;
import com.recipebox.App.enums.BMICategory;
import com.recipebox.App.repository.BmiRepository;
import com.recipebox.App.service.BmiService;

@Service
public class BmiServiceImpl implements BmiService {

	@Autowired
	private BmiRepository bmiRecordRepository;

	@Override
	public List<BmiRecord> getUserBmiHistory(int userId) {
		return bmiRecordRepository.findByUserUserIdOrderByRecordedAtDesc(userId);
	}

	@Override
	public Optional<BmiRecord> getLatestBmiRecord(int userId) {
		List<BmiRecord> records = getUserBmiHistory(userId);
		return records.isEmpty() ? Optional.empty() : Optional.of(records.get(0));
	}

	@Override
	@Transactional
	public boolean deleteBmiRecord(int recordId, Integer userId) {
		Optional<BmiRecord> recordOpt = bmiRecordRepository.findById(recordId);
		if (recordOpt.isPresent() && recordOpt.get().getUser().getUserId() == userId) {
			bmiRecordRepository.delete(recordOpt.get());
			return true;
		}
		return false;
	}

	@Override
	public Page<BmiRecord> getUserBmiHistoryPage(int userId, Pageable pageable) {
		return bmiRecordRepository.findByUserIdOrderByRecordedAtDesc(userId, pageable);
	}

	@Override
	public BmiRecord calculateBmiValues(BmiRecord bmiRecord, String unit, Double heightInches, Double feetVal) {
		// TODO Auto-generated method stub
		double weightKg;
		double heightCm;

		// Imperial , Metric Logic
		if ("imperial".equalsIgnoreCase(unit)) {
			double weightLbs = bmiRecord.getWeight() != null ? bmiRecord.getWeight() : 0.0;
			double feet = (feetVal != null) ? feetVal : 0.0;
			double inches = (heightInches != null) ? heightInches : 0.0;

			weightKg = weightLbs * 0.45359237; // lb to kg
			double totalInches = (feet * 12) + inches;
			heightCm = totalInches * 2.54; // (ft + in) to cm
		} else {
			weightKg = bmiRecord.getWeight() != null ? bmiRecord.getWeight() : 0.0;
			heightCm = bmiRecord.getHeight() != null ? bmiRecord.getHeight() : 0.0;
		}

		double h = heightCm / 100;
		double bmi = (h > 0) ? weightKg / (h * h) : 0.0;

		BMICategory category;
		if (bmi < 18.5) {
			category = BMICategory.UNDERWEIGHT;
		} else if (bmi < 25) {
			category = BMICategory.NORMAL;
		} else if (bmi < 30) {
			category = BMICategory.OVERWEIGHT;
		} else {
			category = BMICategory.OBESE;
		}
		
		weightKg = Math.round(weightKg * 100.0) / 100.0;
		heightCm = Math.round(heightCm * 100.0) / 100.0;
		double roundedBmi = Math.round(bmi * 100.0) / 100.0;
		// put calculate value in BmiRecord object
		bmiRecord.setWeight(weightKg);
		bmiRecord.setHeight(heightCm);
		bmiRecord.setBmiValue(roundedBmi);
		bmiRecord.setBmiCategory(category);

		return bmiRecord;
	}
}
