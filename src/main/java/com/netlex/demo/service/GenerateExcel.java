package com.netlex.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.netlex.demo.model.ExcelReport;
import com.netlex.demo.model.GeologicalClasses;
import com.netlex.demo.model.Sections;
import com.netlex.demo.repository.ExcelReportRepository;
import com.netlex.demo.repository.SearchRepository;

@Service
public class GenerateExcel {

	private static final Logger LOGGER = LogManager.getLogger(GenerateExcel.class);
	
	@Autowired
	private SearchRepository searchRepository;
	
	@Autowired
	private ExcelReportRepository excelReportRepository;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	/**
	 * Async. job to export data into excel file.
	 * 
	 * @param response
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public CompletableFuture<Void> asyncWrite(HttpServletResponse response ) throws InterruptedException, ExecutionException {
		CompletableFuture<Void> completableFuture = 
		CompletableFuture.supplyAsync(() -> readData())
						.thenApply(sections -> writeFile(sections, response))
						.thenAccept(excelReport -> updateDB(excelReport));
		return completableFuture;
	}
	
	/**
	 * Read data from DB.
	 * @return
	 */
	private List<Sections> readData() {
		LOGGER.info("Export excel read data from DB threadId - ThreadName " + Thread.currentThread().getId() + "-  " + Thread.currentThread().getName());
		return searchRepository.findAll();
	}
	
	/**
	 * Write data into excel file.
	 * @param allData
	 * @param response
	 * @return
	 */
	public ExcelReport writeFile(List<Sections> allData, HttpServletResponse response) 
	{
		long currentThreadId = Thread.currentThread().getId();
		ExcelReport excelReport = new ExcelReport("IN PROGRESS", "export", currentThreadId);
		
		LOGGER.info("Export excel write file > current thread id - name: " + currentThreadId + " - "+ Thread.currentThread().getName());
		
		try
		{
			String headerKey = "Content-Disposition";
			String headerValue = "attachment;filename=sections.xls";
			response.setContentType("application/octet-stream");		
			response.setHeader(headerKey, headerValue);
			
			int dataRowCounter = 1;
			if(!allData.isEmpty()) 
			{
				HSSFWorkbook workBook = new HSSFWorkbook();
				HSSFSheet sheet = workBook.createSheet("Section");
				createHeader(workBook,sheet);
				
				for(Sections section : allData) {
					HSSFRow dataRow = sheet.createRow(dataRowCounter++);
					dataRow.createCell(0).setCellValue(section.getName());
					
					int detailCounter = 1;
					List<GeologicalClasses> geoList = section.getGeologicalClasses();
					for(GeologicalClasses obj : geoList) {
						dataRow.createCell(detailCounter++).setCellValue(obj.getName());
						dataRow.createCell(detailCounter++).setCellValue(obj.getCode());
					}			
				}
				
				ServletOutputStream ops = response.getOutputStream();
				workBook.write(ops);
				workBook.close();
				ops.close();
			}

			excelReport = new ExcelReport("DONE", "export", currentThreadId);
			
		} catch(Exception e) {
			excelReport = new ExcelReport("Error", "export", currentThreadId);
			e.printStackTrace();
		}
		return excelReport;
	}
	
	
	private long updateDB(ExcelReport excelReport) {
		LOGGER.info("Export excel writing into DB threadId - ThreadName " + Thread.currentThread().getId() + "-  " + Thread.currentThread().getName());
		excelReportRepository.save(excelReport);
		return excelReport.getJobId();
	}
		
	/**
	 * Async. job to import excel file data into DB.
	 * @param file
	 * @return
	 */
	public CompletableFuture<Void> asyncReadData(MultipartFile file) {
		CompletableFuture<Void> completableFuture = 
				CompletableFuture.supplyAsync(() -> asyncReadDataProcess(file))
				.thenApply(sections -> saveIntoDb(sections))
				.thenAccept(excelReport -> updateDB(excelReport));
		return completableFuture;
	}
	
	/**
	 * Read Data and create excel file.
	 * @param file
	 * @return
	 */
	private List<Sections> asyncReadDataProcess(MultipartFile file) {
		List<Sections> sectionList = new ArrayList<>();
		try {
			sectionList = convertExcelToListOfSections(file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sectionList;
	}
	
	/**
	 * to write data into DB.
	 * @param sections
	 * @return
	 */
	private ExcelReport saveIntoDb(List<Sections> sections) {
		LOGGER.info("data written into DB");
		mongoTemplate.insert(sections, "sections");
		
		long currentThreadId = Thread.currentThread().getId();
		ExcelReport excelReport = new ExcelReport("DONE", "import", currentThreadId);
		return excelReport;
	}
	
	/**
	 * to create excel sheet and return data for saving into DB.
	 * @param is
	 * @return
	 */
	private List<Sections> convertExcelToListOfSections(InputStream is)
	{
		List<Sections> sectionsList = new ArrayList<>();
		try
		{
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			XSSFSheet sheet = workbook.getSheetAt(0);
			
			if(sheet != null) 
			{
				int rowNumber = 0;
				Iterator<Row> itrRow = sheet.iterator();
				while(itrRow.hasNext())
				{
					// skip first header row.
					if(rowNumber==0) {
						rowNumber++;
						continue;
					}
				
					Sections section = new Sections();
					List<GeologicalClasses> geoList = new ArrayList<>();
					GeologicalClasses geo = new GeologicalClasses();
					
					XSSFRow row = (XSSFRow) itrRow.next();
					Iterator<Cell> itrCell = row.iterator();
					int cid = 0;
					while(itrCell.hasNext())
					{
						XSSFCell cell = (XSSFCell) itrCell.next();
						switch(cid) 
						{
							case 0:
								section.setName(cell.getStringCellValue());
								section.setGeologicalClasses(geoList);
								cid++;
								break;
								
							case 1:
								geo.setName(cell.getStringCellValue());
								cid++;
								break;
							case 2:
								geo.setCode(cell.getStringCellValue());
								geoList.add(geo);
								cid++;
								break;
								
							case 3:
								geo = new GeologicalClasses();
								geo.setName(cell.getStringCellValue());
								cid++;
								break;
							case 4:
								geo.setCode(cell.getStringCellValue());
								geoList.add(geo);
								cid++;
								break;
								
							case 5:
								geo = new GeologicalClasses();
								geo.setName(cell.getStringCellValue());
								cid++;
								break;
							case 6:
								geo.setCode(cell.getStringCellValue());
								geoList.add(geo);
								cid++;
								break;
								
							case 7:
								geo = new GeologicalClasses();
								geo.setName(cell.getStringCellValue());
								cid++;
								break;
							case 8:
								geo.setCode(cell.getStringCellValue());
								geoList.add(geo);
								cid++;
								break;
							
							case 9:
								geo = new GeologicalClasses();
								geo.setName(cell.getStringCellValue());
								cid++;
								break;
							case 10:
								geo.setCode(cell.getStringCellValue());
								geoList.add(geo);
								cid++;
								break;
						}
					}
					sectionsList.add(section);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return sectionsList;
	}
	
	/**
	 * To create workbook's sheet file headers
	 * @param workBook
	 * @param sheet
	 */
	private void createHeader(HSSFWorkbook workBook, HSSFSheet sheet) {
		HSSFRow row= sheet.createRow(0);
		
		HSSFFont headerFont = workBook.createFont();
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.BLUE_GREY.getIndex());
		
		HSSFCellStyle headerStyle = workBook.createCellStyle();
		headerStyle.setFont(headerFont);
		
		
		HSSFCell cell0 = row.createCell(0);
		cell0.setCellValue("Section Name");
		cell0.setCellStyle(headerStyle);
		
		HSSFCell cell1 = row.createCell(1);
		cell1.setCellValue("Class 1 name");
		cell1.setCellStyle(headerStyle);
		
		HSSFCell cell2 = row.createCell(2);
		cell2.setCellValue("Class 1 code");
		cell2.setCellStyle(headerStyle);
		
		HSSFCell cell3 = row.createCell(3);
		cell3.setCellValue("Class 2 name");
		cell3.setCellStyle(headerStyle);
		
		HSSFCell cell4 = row.createCell(4);
		cell4.setCellValue("class 2 code");
		cell4.setCellStyle(headerStyle);
		
		HSSFCell cell5 = row.createCell(5);
		cell5.setCellValue("Class M name");
		cell5.setCellStyle(headerStyle);
		
		HSSFCell cell6 = row.createCell(6);
		cell6.setCellValue("Class M code");
		cell6.setCellStyle(headerStyle);
	}


	
	
	
}
