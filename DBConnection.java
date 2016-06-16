package naviquest;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//import com.mysql.jdbc.Statement;

class DBConnection {
	
	private Connection conn;
	private Statement statement;
	
	public DBConnection() throws IOException {
		
		FileInputStream configFileInputStream = new FileInputStream("Resources\\config.properties");
		Properties configProperties = new Properties();
		configProperties.load(configFileInputStream);
		configFileInputStream.close();
		String url = configProperties.getProperty("url");
		String dbName = configProperties.getProperty("databaseName");
		String driver = configProperties.getProperty("driver");
		String userName = configProperties.getProperty("username");
		String password = configProperties.getProperty("password");
		try { 
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url+dbName,userName,password);
			statement = conn.createStatement();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}	
	
	Map<String, String> getRequestFieldsFromDBMap(String requestIdString) throws SQLException{
		Map<String, String> dbFields = new HashMap<String, String>();
		try {			
			ResultSet resultSet =statement.executeQuery("Select * from deinstall_requests where request_id = " + requestIdString);
			ResultSetMetaData resultSetMetaData =resultSet.getMetaData();
			int count = resultSetMetaData.getColumnCount();
			String columnNames[] = new String[count];	
			for(int i =1;i<=count;i++){
				columnNames[i-1] = resultSetMetaData.getColumnName(i);
			}
			
			while(resultSet.next()){
			for(int i =1;i<=columnNames.length;i++){			
				//System.out.println(columnNames[i-1] + "-->" + resultSet.getString(columnNames[i-1]));
				dbFields.put(columnNames[i-1], resultSet.getString(columnNames[i-1]));
				
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbFields;
		
	}
	
	
	public Map<String,String> getRequestGroupFieldsFromDB(String requestIdString){
		Map<String, String> dbGroupFields = new HashMap<String, String>();
		try {			
			ResultSet resultSet;			
			resultSet=statement.executeQuery("select * from deinstall_request_groups where deinstall_request_id" +
					" = (select id from deinstall_requests where request_id = " + requestIdString +")");
			
			while(resultSet.next()){
				dbGroupFields.put(resultSet.getString(3),null);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbGroupFields;
		
	}
}