package naviquest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.xmlbeans.XmlException;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.support.PropertiesMap;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.SoapUIException;

public class NaviQuest_SearchDeinstall {
	NaviQuest_ExcelReader naviQuest_ExcelReader_writer ;
	NaviQuest_ProjectSetup naviQuest_ProjectSetup;
	NaviQuest_ResponseVerification naviQuest_ResponseVerification;
	
	WsdlTestCaseRunner wsdlTestCaseRunner;

	
	WsdlProject wsdlProject;
	WsdlTestSuite searchDeinstallTestSuite;
	WsdlTestCase searchDeintallTestCase;
	String projectPath,testSuiteName,testCaseName,testStepName;
	InputStream configFileInputStream;
	Properties configProperties;
	String projectPathString;
	String[] AutomatedTestCaseNamesArray = null;
	String[] skipTestData = null;
	String automatedTestCaseName;
	String dateString;
	Map<String, TestProperty> testPropertiesMap;
	
	int automatedTestCaseNamesArrayIndex=0,dataset=-1,executionCount=-1;
	
	String workbookPath, worksheetName;
	Boolean TestFail=false,TestPass,TestSkip=false;
	
	
	@BeforeClass()
	public void settingUp() throws XmlException, IOException, SoapUIException{
		try {

		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
		Date date = new Date();
		dateString = dateFormat.format(date);

		configFileInputStream = new FileInputStream("Resources\\config.properties");
		configProperties = new Properties();
		configProperties.load(configFileInputStream);
		configFileInputStream.close();
		
		projectPath = System.getProperty("user.dir")+ configProperties.getProperty("soapUIProjectPath");
		testSuiteName = configProperties.getProperty("search_testSuiteName");
		testCaseName =  configProperties.getProperty("search_testCaseName");
		testStepName =  configProperties.getProperty("search_testStepName");
		
		naviQuest_ProjectSetup = new NaviQuest_ProjectSetup(projectPath,testSuiteName,testCaseName,testStepName);		
	
		workbookPath = System.getProperty("user.dir") + configProperties.getProperty("testInputDataWorkBookPath");
		
		worksheetName = configProperties.getProperty("search_testInputDataSheetName");
	
		naviQuest_ExcelReader_writer = new NaviQuest_ExcelReader(workbookPath,worksheetName);
		//Clear the results
		naviQuest_ExcelReader_writer.clearResult(worksheetName,(short) 9,"Results");
		//Declare NaviQuest_ResponseVerification instance 
		naviQuest_ResponseVerification = new NaviQuest_ResponseVerification();
		//Get the names of the Automated Test Cases
		AutomatedTestCaseNamesArray = naviQuest_ExcelReader_writer.AutomatedTestCaseNames(worksheetName);
		//Get flags for skipping test data
		skipTestData = naviQuest_ExcelReader_writer.retrieveToRunFlagTestData(worksheetName, "Include");
		
	} catch (Exception e) {
		e.printStackTrace();
	}		
		
	}
	

	
	@BeforeMethod()
	public void setUp() throws FileNotFoundException, IOException{
		try {
			automatedTestCaseName = AutomatedTestCaseNamesArray[automatedTestCaseNamesArrayIndex];
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	@DataProvider()
	public Object[][] dataProvider() throws IOException{
		PropertiesMap[] propertiesMap = naviQuest_ExcelReader_writer.ExcelSheetReader(worksheetName);
		Object[][] object =new Object[propertiesMap.length][1];
		for(int i=0;i<propertiesMap.length;i++){
			object[i][0]=propertiesMap[i];
		}

		return object;
	}
	
	
	@Test(dataProvider = "dataProvider")
	public void runTests(PropertiesMap propertiesMap) throws IOException{
		dataset++;		
		if(!skipTestData[dataset].equalsIgnoreCase("Y")){
			TestSkip = true;
			throw new SkipException("Test Data is marked " + dataset + " hence skipped");
		}
		//testPropertiesMap =propertiesMap;
		searchDeintallTestCase = naviQuest_ProjectSetup.setTestCaseProperties(propertiesMap,naviQuest_ExcelReader_writer.getColumnNames(worksheetName));
		wsdlTestCaseRunner = new WsdlTestCaseRunner(searchDeintallTestCase,propertiesMap);
		//wsdlTestCaseRunner.
		
		wsdlTestCaseRunner.run();
		testPropertiesMap = searchDeintallTestCase.getProperties();
		executionCount++;		
		}
	
	@AfterMethod
	public void VerifyResponse() throws Exception{
		Boolean TestResult;
		
		if(executionCount!=-1 && wsdlTestCaseRunner.getResults().size()!=0)
		{			
		TestStepResult testStepResult = wsdlTestCaseRunner.getResults().get(0);
		if(naviQuest_ResponseVerification.ValidateHTTPStatus(testStepResult).equals("HTTP/1.1 200 OK")){
			TestResult = naviQuest_ResponseVerification.verifyResponseNodes(testStepResult,testPropertiesMap);
		}
		else if(naviQuest_ResponseVerification.ValidateHTTPStatus(testStepResult).equals("HTTP/1.1 500 Internal Server Error")){
			TestResult = true;
		}
		else {
			TestResult = false;
		}
		
		if(TestResult){
			naviQuest_ExcelReader_writer.writeResult(worksheetName, "Results", dataset+3, "PASS");
		}else{
			naviQuest_ExcelReader_writer.writeResult(worksheetName, "Results", dataset+3, "FAIL");
		}
		
		naviQuest_ResponseVerification.WriteRequestResponseContent(testStepResult,automatedTestCaseName,dateString);
		
		testPropertiesMap.clear();
		wsdlTestCaseRunner.getResults().clear();
		}else {
			naviQuest_ExcelReader_writer.writeResult(worksheetName, "Results", dataset+3, "SKIP");
		}		
		automatedTestCaseNamesArrayIndex = automatedTestCaseNamesArrayIndex +1;
		TestSkip=false;
		TestResult = false;

	}
	
	@AfterTest
	public void fillColorsInCell() throws IOException{
		System.out.println("executed only once");
		naviQuest_ExcelReader_writer.fillColors("Results", worksheetName);
	}

}
