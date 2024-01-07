package com.netlex.demo.util;

import org.springframework.web.multipart.MultipartFile;

public class ExcelHelper {

	/**
	 * Check file type if it is provided file is excel or not.
	 * @param file
	 * @return
	 */
	public static boolean checkExcelFormat(MultipartFile file)
	{
		String contentType = file.getContentType();
		if(contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			return true;
		} else {
			return false;
		}
	}
	
}
