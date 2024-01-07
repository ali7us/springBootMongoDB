package com.netlex.demo.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.netlex.demo.dto.SectionDTO;
import com.netlex.demo.model.ExcelReport;
import com.netlex.demo.model.Sections;
import com.netlex.demo.repository.ExcelReportRepository;
import com.netlex.demo.repository.SearchRepository;
import com.netlex.demo.service.GenerateExcel;
import com.netlex.demo.util.ExcelHelper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@RestController
@EnableSwagger2
@CrossOrigin(origins = { "*" })
@Api(tags = "sections")
public class SectionController {

	GenerateExcel generateExcel;
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private SearchRepository searchRepository;

	@Autowired
	private ExcelReportRepository excelReportRepository;
	
	public SectionController(GenerateExcel generateExcel) {
		this.generateExcel = generateExcel;
	}

	@ApiIgnore
	@RequestMapping("/")
	public void redirect(javax.servlet.http.HttpServletResponse response) throws IOException {
		response.sendRedirect("/swagger-ui.html");
	}

	@GetMapping("/section/by-code")
	public ResponseEntity<?> getSearch(@RequestParam String code) {
		List<Sections> allData = searchRepository.findByCode(code);
		return ResponseEntity.status(HttpStatus.OK).body(allData);
	}
	
	@ApiModelProperty(value="GeologicalClasses object", example="[ section: section-1, geologicalClasses [ { name: Geo class 11, code: GC11}, { name: Geo class 12, code: GC12} ]")
	@ApiOperation(value = "${UserController.search}", response = SectionDTO.class)
	@PostMapping("/section")
	public ResponseEntity<?> addRecord(@RequestBody String section) {
		Document doc = Document.parse(section);
		Document insertedDoc = mongoTemplate.insert(doc, "sections");
		return ResponseEntity.status(HttpStatus.OK).body(insertedDoc);
	}
	
	/*
	 * ********************************************************************************
	 * Excel Import tasks start here.
	 * ********************************************************************************
	 */
	@PostMapping("/excel/import")
	public ResponseEntity<?> getImport(@RequestParam("file") MultipartFile file) throws IOException {
		
		if (ExcelHelper.checkExcelFormat(file)) {
			CompletableFuture<Void> data = generateExcel.asyncReadData(file);
			return ResponseEntity.status(HttpStatus.OK).body("file uploaded successfully " + data);
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please select valid excel file to upload.");
	}
	
	@GetMapping("/excel/import/{id}")
	public ResponseEntity<?> getImportById(@PathVariable long id) {
		List<ExcelReport> results = excelReportRepository.findAllByJobIdAndJobType(id, "import");
		if(!results.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body("Job Status: " + results.get(0).getJobStatus());
		}
		return ResponseEntity.status(HttpStatus.OK).body("Job Id not exits. ");
	}
	
	
	/*
	 * ********************************************************************************
	 * Excel Export start here.
	 * ********************************************************************************
	 */
	
	@GetMapping("/excel/export")
	public CompletableFuture<Void> getExport(HttpServletResponse response) throws InterruptedException, ExecutionException  {
		CompletableFuture<Void> result = generateExcel.asyncWrite(response );
		return result;
	}
	
	@GetMapping("/excel/export/{id}")
	public ResponseEntity<?> getExportById(@PathVariable long id) {
		List<ExcelReport> results = excelReportRepository.findAllByJobIdAndJobType(id, "export");
		if(!results.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK).body("Job Status: " + results.get(0).getJobStatus());
		}
		return ResponseEntity.status(HttpStatus.OK).body("Job Id not exits. ");
	}
	
	@GetMapping("/excel/export/{id}/file")
	public ResponseEntity<?> getExportByIdCheck(@PathVariable long id) throws InterruptedException {
		List<ExcelReport>  results = excelReportRepository.findAllByJobIdAndJobType(id, "export");
		if(!results.isEmpty() && results.get(0).getJobStatus().equals("IN PROGRESS"))
			throw new InterruptedException("Job still in process...");
		
		return ResponseEntity.status(HttpStatus.OK).body("Given job id is not in progress... ");
	}
}
