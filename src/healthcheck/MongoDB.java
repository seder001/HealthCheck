package healthcheck;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;


/**
 *
 */
public class MongoDB
{
	// TODO: Username + Password
	/**
	 * @param jo
	 */
	public void prepareMongo(JsonObject jo)
	{
		JsonString dbIP, dbName, info;
		JsonNumber dbPort;
		String check;

		dbIP = (JsonString) HealthCheck.getValue(jo, "ip");
		dbPort = (JsonNumber) HealthCheck.getValue(jo, "port");
		dbName = (JsonString) HealthCheck.getValue(jo, "dbname");
		info = (JsonString) HealthCheck.getValue(jo, "info");

		if (null == dbIP || null == dbPort || null == dbName || null == info)
		{
			return;
		}

		check = checkMongo(dbIP.getString(), dbPort.intValue(), dbName.getString());
		System.out.println(info.getString() + check);
	}

	private String checkMongo(String mongoIP, int mongoPort, String dbName)
	{
		//String dbURI = "mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]";
		String dbURI = "mongodb://" + mongoIP + ":" + mongoPort + "/" + dbName;
		MongoClientURI mcURI = new MongoClientURI(dbURI);

		try
		{
			MongoClient mc = new MongoClient(mcURI);

			return " ... OK";
		}
		catch (Exception e)
		{
			HealthCheck.sendEmail = true;
			return " ... NOK (connection failed!)";
		}
	}

}
