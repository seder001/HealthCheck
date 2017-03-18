package healthcheck;

import java.sql.DriverManager;
import java.sql.SQLException;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;


/**
 *
 */
public class MySQL
{

	/**
	 * @param jo
	 */
	public void prepareMySQL(JsonObject jo)
	{
		JsonString dbIP, dbName, dbUser, dbPass, path, info;
		JsonNumber dbPort;
		String check;

		dbIP = (JsonString) HealthCheck.getValue(jo, "ip");
		dbPort = (JsonNumber) HealthCheck.getValue(jo, "port");
		dbName = (JsonString) HealthCheck.getValue(jo, "dbname");
		dbUser = (JsonString) HealthCheck.getValue(jo, "user");
		dbPass = (JsonString) HealthCheck.getValue(jo, "pass");
		path = (JsonString) HealthCheck.getValue(jo, "path");
		info = (JsonString) HealthCheck.getValue(jo, "info");

		if (null == dbIP || null == dbPort || null == dbName || null == dbUser || null == dbPass || null == path || null == info)
		{
			return;
		}

		check = checkMySQL(dbIP.toString(), dbPort.hashCode(), dbName.toString(), dbUser.toString(), dbPass.toString(),
				path.toString());
		System.out.println(info.getString() + check);
	}

	private String checkMySQL(String dbIP, int dbPort, String dbName, String dbUser, String dbPass, String path)
	{
		String dbURL = "jdbc:mysql://" + dbIP + ":" + dbPort + "/" + dbName + path;

		try
		{
			DriverManager.getConnection(dbURL, dbUser, dbPass);
		}
		catch (SQLException e)
		{
			HealthCheck.sendEmail = true;
			return " ... NOK (connection failed!)";
		}
		return " ... OK";
	}
}
