package com.netlex.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.netlex.demo.model.ExcelReport;


public interface ExcelReportRepository extends MongoRepository<ExcelReport, String>{

	List<ExcelReport> findAllByJobIdAndJobType(long id, String jobType);
}
