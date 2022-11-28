package cn.edu.ldu.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBUtils {
     //Derby数据库Url
   // private static final String DBURL="jdbc:derby://localhost:1527/QQDB";
	static String driver = "org.apache.derby.jdbc.EmbeddedDriver";//第一步：给出驱动类名和连接数据库的字符串
	
	static String dbName = "QQDB";
	static String connectionURL = "jdbc:derby:" + dbName + ";create=true";
	//private static final String DBURL="jdbc:derby:D:\\workspace\\case_chapter7_server\\QQDB;create=true";
   // private static final String USERNAME="nbuser";//Derby数据库用户名
   // private static final String PASSWORD="password";//Derby登录密码
	/*** Check for USER table ****/
	public static boolean checkTable(Connection conTst) throws SQLException {
		try {
			Statement s = conTst.createStatement();
			s.execute("update MEMBER set name= 'TEST', time = CURRENT_TIMESTAMP where 1=3");
		} catch (SQLException sqle) {
			String theError = (sqle).getSQLState();
			// System.out.println("  Utils GOT:  " + theError);
			/** If table exists will get - WARNING 02000: No row was found **/
			if (theError.equals("42X05")) // Table does not exist
			{
				return false;
			} else if (theError.equals("42X14") || theError.equals("42821")) {
				System.out
						.println("checkTable: Incorrect table definition. Drop table USERTABLE and rerun this program");
				throw sqle;
			} else {
				System.out.println("checkTable: Unhandled SQLException");
				throw sqle;
			}
		}
		return true;
	}
	public static Connection getConnection() throws SQLException, ClassNotFoundException {
    	Class.forName(driver);//第二步：加载驱动类
     // return DriverManager.getConnection(DBURL, USERNAME, PASSWORD);
    	Connection conn =DriverManager.getConnection(connectionURL);
    	Statement s = conn.createStatement();//第四步：创建语句对象
		// Call utility method to check if table exists.
		// Create the table if needed
		if (!checkTable(conn)) {
			System.out.println(" . . . . creating table MEMBER");
			// "DROP TABLE MEMBER;"
			String createString =
					"CREATE TABLE MEMBER ( " // 表名
					+ "ID INTEGER primary key not null ,  "
					+ "NAME varchar(32) , " // 用户名
					+ "PASSWORD varchar(256) , " // 口令的HASH值
					+ "EMAIL varchar(64)  , " // 邮箱地址
					+ "HEADIMAGE varchar(128)  , " // 头像
					+ "TIME timestamp default CURRENT_TIMESTAMP)"; // 注册时间

			s.execute(createString);//第五步：执行语句
		}
		s.close();
		System.out.println("Database openned normally");
	
    	return  conn;
    }//end getConnection
	public static ArrayList<String> getUserlist() {
		ArrayList<String> userArrayList=new ArrayList<String>();
		try {
			
			Class.forName(driver);
			Connection conn =DriverManager.getConnection(connectionURL);
	    	
			// Call utility method to check if table exists.
			// Create the table if needed
			
				// "DROP TABLE MEMBER;"
				String getUserlist =
						"SELECT ID FROM MEMBER" ;
				PreparedStatement s = conn.prepareStatement(getUserlist);		
				ResultSet resultSet= s.executeQuery();//第五步：执行语句
			while(resultSet.next()) {
				userArrayList.add(resultSet.getString(1));
			}
			
			s.close();
          
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userArrayList;
	}
}//end class
