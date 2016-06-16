/* ================================================================================
 * Verify the response 
 * Created by :- Amit Toshniwal
 * Date :- 05-Jan-15
 ====================================================================================*/
package naviquest;

import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.GroovyUtils;
import com.eviware.soapui.support.XmlHolder;
import com.eviware.soapui.support.types.StringToStringsMap;

class NaviQuest_ResponseVerification extends ModifiedAssertions {
	
		
	
	private XmlHolder responseXmlHolder;
	private String request_id;
	private DBConnection dbConnection =null;
	private Map<String, String> dbFieldsMap,dbGroupFieldMap;
	
	
	//Method for checking the values of nodes	
	String ValidateHTTPStatus(TestStepResult testStepResult){
		StringToStringsMap httpStatus = ((MessageExchange)testStepResult).getResponseHeaders();
		return httpStatus.get("#status#", "NONE").toString();		
	}
	
	
	
	void WriteRequestResponseContent(TestStepResult testStepResult,String AutomatedTestCaseName,String dString) throws IOException{
		
		String requestContent = ((MessageExchange)testStepResult).getRequestContent();		
		
		String responseContent = ((MessageExchange)testStepResult).getResponseContent();
		
		WriteToFile(AutomatedTestCaseName,requestContent,responseContent,dString);
		
	}
	
	private void WriteToFile(String fileName,String requestContentString, String responseContentString,String dString) throws IOException{
		try {	
		File dirFile = new File(System.getProperty("user.dir") + "\\TestResults\\" + dString); 
		if(!dirFile.exists()){
			dirFile.mkdir();
		}
		File file = new File(System.getProperty("user.dir") + "\\TestResults\\" + dString+ "\\" + fileName + ".txt");
		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bufferedWriter = new  BufferedWriter(fileWriter);
		bufferedWriter.write("Request");
		bufferedWriter.newLine();
		bufferedWriter.append(requestContentString);
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		bufferedWriter.append("Response");
		bufferedWriter.newLine();
		bufferedWriter.append(responseContentString);
		bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	Boolean verifyResponseNodes(TestStepResult testStepResult,Map<String, TestProperty> propertiesMap) throws Exception{
		//boolean[] results = null;
		//boolean result=false;
		List<Boolean> result = new ArrayList<Boolean>();
		dbConnection = new DBConnection();
		
		String testCaseName = testStepResult.getTestStep().getTestCase().getName();
		String name = testStepResult.getTestStep().getName().toString();
		WsdlTestRunContext wsdlTestRunContext = new WsdlTestRunContext((TestStep)testStepResult.getTestStep());		
		GroovyUtils groovyUtils = new GroovyUtils(wsdlTestRunContext) ;

		
		responseXmlHolder = groovyUtils.getXmlHolder(name + "#ResponseAsXml");

		responseXmlHolder.declareNamespace("xmlns", "https://207.211.9.20/rest/1.0/deinstall/4477");	


		XmlSlurper xmlSlurper = new XmlSlurper();		
		GPathResult node = xmlSlurper.parseText(responseXmlHolder.getXml());
		
		if(testCaseName.equalsIgnoreCase("SearchDeinstallTestCase")){
			if (node.children().size()!=0){
				int count = Integer.parseInt(responseXmlHolder.getNodeValue("count(//e)"));
				for(int i = 1;i<=count;i++){
					result.add(verifySearchNodes(i,propertiesMap,testCaseName));
				}
			}else{
				System.out.println("Response is NULL");
				result.add(true);
			}
		}else if (testCaseName.equalsIgnoreCase("CreateDeinstallTestCase")) {
			result.add(verifyCreateNodes(propertiesMap,testCaseName));
			request_id = responseXmlHolder.getNodeValue("//xmlns:requestId");
			dbFieldsMap = dbConnection.getRequestFieldsFromDBMap(request_id);
			dbGroupFieldMap = dbConnection.getRequestGroupFieldsFromDB(request_id);
			result.add(verifyDBFields(propertiesMap,dbFieldsMap));
			result.add(verifyGroups(propertiesMap, dbGroupFieldMap));			
		}else {
			result.add(true);
		}
		boolean finalResult = allTrue(result);
		return finalResult;
		
	}
	

	private static boolean allTrue (List<Boolean> values) {
	    for (boolean value : values) {
	        if (!value)
	            return false;
	    }
	    return true;
	}
	
	
	
	private boolean verifyCreateNodes(Map<String, TestProperty> propertiesMap, String testCaseName) throws XmlException{
	List<Boolean> results = new ArrayList<Boolean>();
		for(String key:propertiesMap.keySet()){
			try {
				if(!(key.length()>8 && key.substring(0, 8).equalsIgnoreCase("group id"))){
					if(!(key.equalsIgnoreCase("requestorEmail"))){
						if(!(propertiesMap.get(key).getValue().equalsIgnoreCase("\"\""))){
							results.add(verifyEquals(propertiesMap.get(key).getValue(),"\"" + responseXmlHolder.getNodeValue("//xmlns:" + key ) + "\""));						
						}else{
							results.add(verifyNull(responseXmlHolder.getNodeValue("//xmlns:" + key )));
						}
					}
				}else {
					if(!propertiesMap.get(key).getValue().equalsIgnoreCase("\"\"")){
						String test = "//xmlns:groups/xmlns:e[" + key.substring(9) + "]/xmlns:id";
						results.add(verifyEquals(propertiesMap.get(key).getValue(), "\"" + responseXmlHolder.getNodeValue(test) + "\""));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				results.add(false);
			}
		}
		boolean result = allTrue(results);
		return result;
	}
	
	private Boolean verifySearchNodes(int i,Map<String, TestProperty> propertiesMap,String testCaseName) throws XmlException{
		//Checking value of deinstall Type id
	//Boolean result_deinstall = null,result_Status=null,result_submitter=null;
	//if (testCaseName.equalsIgnoreCase("SearchDeinstallTestCase")){
	List<Boolean> results = new ArrayList<Boolean>();
	
		if(!(propertiesMap.get("deinstallTypeId").getValue().equalsIgnoreCase("\"\""))){
			results.add(verifyEquals(  "\"" + responseXmlHolder.getNodeValue("//e["+i+"]/deinstallTypeId") + "\"",propertiesMap.get("deinstallTypeId").getValue() ));
		}
		
		//checking value of status id		
			results.add(verifyEquals( "\"" + responseXmlHolder.getNodeValue("//e["+i+"]/statusId") + "\"",propertiesMap.get("statusId").getValue()));

		//Checking value of submitter id
		if(propertiesMap.get("submitterId").getValue().equals("\"\"")){
			//Checking null value of submitter id
			results.add(verifyNull(responseXmlHolder.getNodeValue("//e["+i+"]/requestorId")));
		}else{			
			results.add(verifyEquals( "\"" + responseXmlHolder.getNodeValue("//e["+i+"]/requestorId") + "\"",propertiesMap.get("submitterId").getValue()));
		}		
		
		boolean result = allTrue(results);
		return result;
	}


	private boolean verifyDBFields(Map<String, TestProperty> propertiesMap,Map<String, String> databaseFieldsMap){
		
		//System.out.println(propertiesMap.get("deinstallTypeId").getValue());
		//System.out.println(databaseFieldsMap.get("type_id"));
		List<Boolean> list = new ArrayList<Boolean>();
		try{			
		list.add(verifyEquals(propertiesMap.get("deinstallTypeId").getValue(), "\"" + databaseFieldsMap.get("type_id") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallReasonId").getValue(), "\"" + databaseFieldsMap.get("reason_id") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallServiceId").getValue(), "\"" + databaseFieldsMap.get("service_id") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallCompany").getValue(), "\"" + databaseFieldsMap.get("company") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallCustomerName").getValue(), "\"" + databaseFieldsMap.get("customer_name") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallCustomerPhone").getValue(), "\"" + databaseFieldsMap.get("customer_phone") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallCustomerEmailAddress").getValue(), "\"" + databaseFieldsMap.get("customer_email_address") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallService").getValue(), "\"" + databaseFieldsMap.get("service") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallSummary").getValue(), "\"" + databaseFieldsMap.get("summary") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallNotes").getValue(), "\"" + databaseFieldsMap.get("notes") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallLocationId").getValue(), "\"" + databaseFieldsMap.get("location_id") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallEstimatedLostMrr").getValue(), "\"" + databaseFieldsMap.get("estimated_lost_mrr") + "\"" ));
		list.add(verifyEquals(propertiesMap.get("deinstallRetentionDate").getValue(), "\"" + databaseFieldsMap.get("data_retention_date") + "\"" ));
		}catch(Exception e){
			e.printStackTrace();
			list.add(false);
		}
		
		boolean result = allTrue(list);
		return result;
		
	}

	
	private boolean verifyGroups(Map<String, TestProperty> propertiesMap,Map<String, String> databaseFieldsMap){
		
		List<Boolean> list = new ArrayList<Boolean>();
		try{
			for(String key:propertiesMap.keySet()){
				if(key.length()>8 && key.substring(0, 8).equalsIgnoreCase("group id")){
					//list.add(verifyEquals(propertiesMap.get(key).getValue(), expected))
					String valueString = propertiesMap.get(key).getValue();
					for(String dbKey:databaseFieldsMap.keySet()){
						if(("\"" + dbKey + "\"").equalsIgnoreCase(valueString)){
							list.add(true);
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			list.add(false);
		}
		
		boolean result = allTrue(list);
		return result;
		
		
	}

}




	
