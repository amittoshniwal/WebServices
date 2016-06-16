/* Contains the method for setting up SOAP UI Project, Test suite, Test Case, Test Step etc.
 * Created by : - Amit Toshniwal
 * Date :- 5-Jan-15
 */ 

package naviquest;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.model.support.PropertiesMap;

class NaviQuest_ProjectSetup {
	
	private WsdlProject soapUIProject;
	private WsdlTestSuite wsdlTestSuite;
	private WsdlTestCase wsdlTestCase;
	private String projectPath,soapTestSuiteName,soapTestCaseName;
	private RestTestRequestStep restTestRequestStep;
	private RestRequest restRequest;
	private String contentOriginal;
	private List<String> removableElementStrings= new ArrayList<String>();
	
	NaviQuest_ProjectSetup(String ProjectPath,String TestSuiteName, String TestCaseName,String TestStepName){
		this.projectPath = ProjectPath;
		this.soapTestSuiteName = TestSuiteName;
		this.soapTestCaseName = TestCaseName;
		
		try {
			soapUIProject = new WsdlProject(projectPath);
			wsdlTestSuite = soapUIProject.getTestSuiteByName(soapTestSuiteName);
			wsdlTestCase =wsdlTestSuite.getTestCaseByName(soapTestCaseName);
			restTestRequestStep = (RestTestRequestStep) wsdlTestCase.getTestStepByName(TestStepName);
			restRequest = restTestRequestStep.getTestRequest();
			contentOriginal = restRequest.getRequestContent();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}	
	
	WsdlTestCase setTestCaseProperties(PropertiesMap testCaseropertyMap,String[] colNames){
		removableElementStrings= new ArrayList<String>();
		for(int i = 0 ; i < colNames.length;i++){
				if((colNames[i].substring(0, 8).equalsIgnoreCase("group id") 
						&& testCaseropertyMap.get(colNames[i]).toString().equalsIgnoreCase(""))
						&& soapTestCaseName.equalsIgnoreCase("CreateDeinstallTestCase")){
					removableElementStrings.add(colNames[i]);					
					wsdlTestCase.setPropertyValue(colNames[i],"\"" + testCaseropertyMap.get(colNames[i]).toString()  + "\"");
				}
				else {
					wsdlTestCase.setPropertyValue(colNames[i],"\"" + testCaseropertyMap.get(colNames[i]).toString()  + "\"");
				}
		}
		wsdlTestCase = configureRequest(wsdlTestCase);
		return wsdlTestCase;
	}
	
	//RestTestRequestStep configureRequest(WsdlTestCase wsdlTestCase){	
		WsdlTestCase configureRequest(WsdlTestCase wsdlTestCase){
		//RestTestRequestStep restTestRequestStep = (RestTestRequestStep) wsdlTestCase.getTestStepByName("CreateDeinstall");

		RestRequest restRequestModified = restTestRequestStep.getTestRequest();
		restRequestModified.setRequestContent(contentOriginal);
		String contentStringUpdated = null;// restRequestModified.getRequestContent();
		String contentStringModified = null;
		int i = 1;
		for(String removeElement:removableElementStrings){
			contentStringUpdated = restRequestModified.getRequestContent();
			if(i==1 && i!=removableElementStrings.size()){
				contentStringModified = contentStringUpdated.replaceAll("\\,\n\\{\"id\"\\:\\$\\{\\#TestCase\\#" + removeElement + "\\}\\}\\,","");
			//wsdlTestCase.removeProperty(removeElement);
			}
			else if (i==removableElementStrings.size()&& i!= 1) {
				contentStringModified = contentStringUpdated.replaceAll("\n\\{\"id\"\\:\\$\\{\\#TestCase\\#" + removeElement + "\\}\\}","");
				//wsdlTestCase.removeProperty(removeElement);
			}
			else if (i==removableElementStrings.size()&& i== 1) {
				contentStringModified = contentStringUpdated.replaceAll("\\,\n\\{\"id\"\\:\\$\\{\\#TestCase\\#" + removeElement + "\\}\\}","");
				//wsdlTestCase.removeProperty(removeElement);
			}
			else{
				contentStringModified = contentStringUpdated.replaceAll("\\{\"id\"\\:\\$\\{\\#TestCase\\#" + removeElement + "\\}\\}\\,","");
				//wsdlTestCase.removeProperty(removeElement);
			}
			restRequestModified.setRequestContent(contentStringModified);
			i++;
		}
		//restRequestModified.setRequestContent(contentStringModified);
		return wsdlTestCase;
		
	}
}
