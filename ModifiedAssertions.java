package naviquest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;

class ModifiedAssertions {
	
	private static Map<ITestResult, List<Throwable>> verificationFailuresMap = new HashMap<ITestResult, List<Throwable>>();

    private static void assertTrue(boolean condition) {
    	Assert.assertTrue(condition);
    }
    
    private static void assertTrue(boolean condition, String message) {
    	Assert.assertTrue(condition, message);
    }
    
    private static void assertFalse(boolean condition) {
    	Assert.assertFalse(condition);
    }
    
    private static void assertFalse(boolean condition, String message) {
    	Assert.assertFalse(condition, message);
    }
    
    private static void assertEquals(boolean actual, boolean expected) {
    	Assert.assertEquals(actual, expected);
    }
    
    private static void assertEquals(Object actual, Object expected) {
    	Assert.assertEquals(actual, expected);
    }
    
    private static void assertEquals(Object[] actual, Object[] expected) {
    	Assert.assertEquals(actual, expected);
    }
    
    
    private static void assertEquals(String actual,String expected) {
    	Assert.assertEquals(actual, expected);
    }
    public static void assertEquals(Object actual, Object expected, String message) { // NO_UCD (unused code)
    	Assert.assertEquals(actual, expected, message);
    }
    
    public static void verifyTrue(boolean condition) { // NO_UCD (unused code)
    	try {
    		assertTrue(condition);
    	} catch(Throwable e) {
    		addVerificationFailure(e);
    	}
    }
    
    public static void verifyTrue(boolean condition, String message) { // NO_UCD (unused code)
    	try {
    		assertTrue(condition, message);
    	} catch(Throwable e) {
    		addVerificationFailure(e);
    	}
    }
    
    public static void verifyFalse(boolean condition) { // NO_UCD (unused code)
    	try {
    		assertFalse(condition);
		} catch(Throwable e) {
    		addVerificationFailure(e);
		}
    }
    
    public static void verifyFalse(boolean condition, String message) { // NO_UCD (unused code)
    	try {
    		assertFalse(condition, message);
    	} catch(Throwable e) {
    		addVerificationFailure(e);
    	}
    }
    
    public static void verifyEquals(boolean actual, boolean expected) { // NO_UCD (unused code)
    	try {
    		assertEquals(actual, expected);
		} catch(Throwable e) {
    		addVerificationFailure(e);
		}
    }

    public static boolean verifyEquals(Object actual, Object expected) { // NO_UCD (use default)
    	try {
    		assertEquals(actual, expected);
    		return true;
		} catch(Throwable e) {
    		addVerificationFailure(e);
    		return false;
		}
    }
    
    static boolean verifyNull(Object actual) {
    	try {
    		assertNull(actual);
    		return true;
		} catch(Throwable e) {
    		addVerificationFailure(e);
    		return false;
		}
    }
    
    private static void assertNull(Object actual) {
		// TODO Auto-generated method stub
    	Assert.assertNull(actual);
		
	}

	static boolean verifyEquals(String actual, String expected) { // NO_UCD (test only)
    	try {
    		assertEquals(actual, expected);
    		return true;
		} catch(Throwable e) {
    		addVerificationFailure(e);
    		//throw e;
    		return false;
		}
    }
    public static void verifyEquals(Object[] actual, Object[] expected) { // NO_UCD (unused code)
    	try {
    		assertEquals(actual, expected);
		} catch(Throwable e) {
    		addVerificationFailure(e);
		}
    }

    public static void fail(String message) {
    	Assert.fail(message);
    }
    
	static List<Throwable> getVerificationFailures() {
		List<Throwable> verificationFailures = verificationFailuresMap.get(Reporter.getCurrentTestResult());
		return verificationFailures == null ? new ArrayList<Throwable>() : verificationFailures;
	}
	
	private static void addVerificationFailure(Throwable e) {
		List<Throwable> verificationFailures = getVerificationFailures();
		verificationFailuresMap.put(Reporter.getCurrentTestResult(), verificationFailures);
		verificationFailures.add(e);
	}
	
}
