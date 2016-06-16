package naviquest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.eviware.soapui.model.support.PropertiesMap;

class NaviQuest_ExcelReader {

	private XSSFWorkbook xssfWorkbook;
	
	private DataFormatter dataFormatter;
	private XSSFFormulaEvaluator xssfFormulaEvaluator;
	
	
	
	// FileInputStream io;
	private XSSFSheet xssfSheet;
	
	
	private PropertiesMap propertiesMap;
	
	// PropertiesMap[] arrMaps ;
	private PropertiesMap[] arrMaps;
	private FileInputStream ipstr;
	
	
	private FileOutputStream opstr = null;
	private String filelocation;


	NaviQuest_ExcelReader(String filelocation,String workSheetName) {
		this.filelocation = filelocation;
		try {
			ipstr = new FileInputStream(filelocation);
			xssfWorkbook = new XSSFWorkbook(ipstr);
			xssfSheet = xssfWorkbook.getSheet(workSheetName);
			//xssfSheet = xssfWorkbook.getSheetAt(0);
			ipstr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// replace void with TestCase
	private PropertiesMap getSearchDeinstallTestCell(int Row, XSSFFormulaEvaluator xssfFormulaEvaluator,String[] columnNames) {

		dataFormatter = new DataFormatter();
		propertiesMap = new PropertiesMap();

		int ci=3;
		for(int i = 0;i<columnNames.length;i++,ci++){
			propertiesMap.put(columnNames[i], dataFormatter.formatCellValue(
					xssfSheet.getRow(Row).getCell(ci), xssfFormulaEvaluator));
		}
		return propertiesMap;
	}

	PropertiesMap[] ExcelSheetReader(String worksheetName) throws IOException {

		xssfFormulaEvaluator = new XSSFFormulaEvaluator(xssfWorkbook);
		dataFormatter = new DataFormatter();
		int rows = retrieveNoOfRows(worksheetName);
		arrMaps = new PropertiesMap[rows - 3];
		String[] columnNames =  getColumnNames(worksheetName);

		int ci = 0;
		for (int i = 3; i < rows; i++, ci++) {
			arrMaps[ci] = getSearchDeinstallTestCell(i,xssfFormulaEvaluator,columnNames);
		}

		return arrMaps;
	}

	String[] AutomatedTestCaseNames(String workSheetName) throws FileNotFoundException, IOException {

		//xssfWorkbook1 = new XSSFWorkbook(new FileInputStream(new File(workbookPath)));
		//xssfSheet = xssfWorkbook.getSheet(workSheetName);
		String colName = "Automated Test Case Name";

		//int lastRow = xssfSheet.getLastRowNum();
		
		int colNum = retrieveNoOfCols(workSheetName);
		int rows = retrieveNoOfRows(workSheetName);
		int colNumber = -1;
		
		XSSFRow Suiterow = xssfSheet.getRow(0);
		for (int i = 0; i < colNum; i++) {
			if (Suiterow.getCell(i).getStringCellValue()
					.equals(colName.trim())) {
				colNumber = i;
			}
		}
		
		String[] automatedTestCaseNamesArray = new String[rows - 3];
		int cj = 0;
		for (int i = 3; i < rows-1; i++, cj++) {
			automatedTestCaseNamesArray[cj] = xssfSheet.getRow(i).getCell(colNumber)
					.toString();
		}
		//xssfWorkbook.close();
		return automatedTestCaseNamesArray;

	}

	String[] retrieveToRunFlagTestData(String wsName, String colName)
			throws FileNotFoundException, IOException {

		int sheetIndex = xssfWorkbook.getSheetIndex(wsName);
		if (sheetIndex == -1)
			return null;
		else {
			int rowNum = retrieveNoOfRows(wsName);
			int colNum = retrieveNoOfCols(wsName);
			int colNumber = -1;

			XSSFRow Suiterow = xssfSheet.getRow(0);
			for (int i = 0; i < colNum; i++) {
				if (Suiterow.getCell(i).getStringCellValue()
						.equals(colName.trim())) {
					colNumber = i;
				}
			}

			String data[] = new String[rowNum - 3];
			if (colNumber == -1) {
				return null;
			}
			int cj = 0;
			for (int j = 3; j < rowNum; j++, cj++) {

				XSSFRow Row = xssfSheet.getRow(j);

				if (Row == null) {
					data[cj] = "";
				} else {
					XSSFCell cell = Row.getCell(colNumber);
					if (cell == null) {
						data[cj] = "";
					} else {
						String value = cellToString(cell);
						data[cj] = value;
					}
				}
			}

			return data;
		}
	}

	private static String cellToString(XSSFCell cell) {
		int type;
		Object result;
		type = cell.getCellType();

		switch (type) {
		case 0:
			result = cell.getNumericCellValue();
			break;

		case 1:
			result = cell.getStringCellValue();
			break;

		default:
			throw new RuntimeException("Unsupportd cell.");
		}
		return result.toString();
	}

	// To retrieve No Of Rows from .xls file's sheets.
	private int retrieveNoOfRows(String wsName) {
		int sheetIndex = xssfWorkbook.getSheetIndex(wsName);
		if (sheetIndex == -1)
			return 0;
		else {
			xssfSheet = xssfWorkbook.getSheetAt(sheetIndex);
			int rowCount = xssfSheet.getLastRowNum() + 1;
			return rowCount;
		}
	}

	// To retrieve No Of Columns from .cls file's sheets.
	private int retrieveNoOfCols(String wsName) {
		int sheetIndex = xssfWorkbook.getSheetIndex(wsName);
		if (sheetIndex == -1)
			return 0;
		else {
			xssfSheet = xssfWorkbook.getSheetAt(sheetIndex);
			int colCount = xssfSheet.getRow(0).getLastCellNum();
			return colCount;
		}
	}

	boolean writeResult(String wsName, String colName, int rowNumber,
			String Result) {
		try {

			int sheetIndex = xssfWorkbook.getSheetIndex(wsName);

			if (sheetIndex == -1)
				return false;
			int colNum = retrieveNoOfCols(wsName);
			int colNumber = -1;

			XSSFRow Suiterow = xssfSheet.getRow(0);
			for (int i = 0; i < colNum; i++) {
				if (Suiterow.getCell(i).getStringCellValue()
						.equals(colName.trim())) {
					colNumber = i;
				}
			}

			if (colNumber == -1) {
				return false;
			}

			XSSFRow Row = xssfSheet.getRow(rowNumber);
			XSSFCell cell = Row.getCell(colNumber);
			if (cell == null)
				cell = Row.createCell(colNumber);

			cell.setCellValue(Result);


			opstr = new FileOutputStream(filelocation);
			xssfWorkbook.write(opstr);

			opstr.close();

			

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	void clearResult(String wsName, int colorIndex,String colName) {
		try {

			xssfSheet = xssfWorkbook.getSheet(wsName);
			int colNum = retrieveNoOfCols(wsName);
			int rows = retrieveNoOfRows(wsName);
			int colNumber = -1;
			
			XSSFRow Suiterow = xssfSheet.getRow(0);
			for (int i = 0; i < colNum; i++) {
				if (Suiterow.getCell(i).getStringCellValue()
						.equals(colName.trim())) {
					colNumber = i;
				}
			}




			for (int i = 3; i < rows; i++) {

				XSSFCell curCell = xssfSheet.getRow(i).getCell(colNumber);
				curCell.setCellValue("");
				XSSFCellStyle curCellStyle = curCell.getCellStyle();

				curCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
				curCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
				curCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
				curCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
				curCellStyle.setFillPattern(XSSFCellStyle.NO_FILL);
				curCell.setCellStyle(curCellStyle);
				
				opstr = new FileOutputStream(filelocation);
				xssfWorkbook.write(opstr);
				opstr.close();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	
	String[] getColumnNames(String worksheetName){
		
		int col = retrieveNoOfCols(worksheetName);
		int cj= 0;
		String[] columnNames = new String[col-6];
		for(int i= 3;i<col-3;i++,cj++){
			columnNames[cj] = xssfSheet.getRow(1).getCell(i).toString();
		}
		return columnNames;
		
	}
	
	public void fillColors(String colName,String wsName) throws IOException{
		int colNum =retrieveNoOfCols(wsName);
		int row = retrieveNoOfRows(wsName);
		int colNumber = -1;
		
		
		XSSFRow Suiterow = xssfSheet.getRow(0);
		for (int i = 0; i < colNum; i++) {
			if (Suiterow.getCell(i).getStringCellValue()
					.equals(colName.trim())) {
				colNumber = i;
			}
		}
		
		for(int i = 3; i <row;i++){
			
			XSSFCell cell = xssfSheet.getRow(i).getCell(colNumber);
			
			if(cell.getStringCellValue().equalsIgnoreCase("pass")){

				configureCellstyle(cell,(short)42);
				
			}else if (cell.getStringCellValue().equalsIgnoreCase("fail")) {

				configureCellstyle(cell,(short)10);
				
			}else {
				
				configureCellstyle(cell,(short)43);
				
			}
			
			opstr = new FileOutputStream(filelocation);
			xssfWorkbook.write(opstr);
			opstr.close();
		}
		
	}
	
	
	void configureCellstyle(XSSFCell cell,short index){
		XSSFCellStyle curCellStyle = xssfWorkbook.createCellStyle();
		//curCellStyle = cell.getCellStyle();
		if(index==9){
			curCellStyle.setFillForegroundColor((short) index);
			curCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			curCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			curCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
			curCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
			curCellStyle.setFillPattern(CellStyle.NO_FILL);
		}else{
			
			if (index==43) {
				curCellStyle.setFillForegroundColor((short) index);
			}else if(index==10){
				curCellStyle.setFillForegroundColor((short) index);
			}else {
				curCellStyle.setFillForegroundColor((short) index);
			}			
			curCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THICK);
			curCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THICK);
			curCellStyle.setBorderRight(XSSFCellStyle.BORDER_THICK);
			curCellStyle.setBorderTop(XSSFCellStyle.BORDER_THICK);
			curCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		}		
		cell.setCellStyle(curCellStyle);
	}
	
	
}
