package com.netlex.demo.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

@Document(collection = "excelReport")
public class ExcelReport {

	@Id
	private String id;

	private long jobId;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private Date createdDate = new Date();
	
	private String jobStatus;
	private String jobType;
	
	
	public ExcelReport() { super(); }
	
	public ExcelReport(String jobStatus, String jobType, long jobId) {
		super();
		if(jobStatus == null) {
			this.jobStatus = "IN PROGRESS";
		}
		this.jobStatus = jobStatus;
		this.jobType = jobType;
		this.jobId = jobId;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getJobId() {
		return jobId;
	}
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getJobStatus() {
		return jobStatus;
	}
	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
}
