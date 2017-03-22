package healthcheck;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;


/**
 *
 */
public class HealthCheck
{
	public static boolean sendEmail = false;

	// TODO: find connection check for GAT and Mule
	// TODO: find a way to read application context

	public static JsonValue getValue(JsonObject jo, String val)
	{
		if (null == jo.get(val))
		{
			System.out.println(val + " entry for " + jo.getString("type") + " missing");
		}
		else if (jo.get(val) instanceof JsonString)
		{
			return jo.getJsonString(val);
		}
		else if (jo.get(val) instanceof JsonNumber)
		{
			return jo.getJsonNumber(val);
		}
		return null;
	}

/*	private String getFlags(JsonArray jar)
	{
		if (null == jar)
		{
			return "";
		}

		String flags = "?";

		for (int i = 0; i < jar.size(); i++)
		{
			if (null != jar.getJsonString(i))
			{
				String s = jar.getString(i);
				flags += jar.size() - 1 > i ? s + "&" : s;
			}
		}

		return flags;
	}
*/
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Webservice ws = new Webservice();
		MySQL mysql = new MySQL();
		MongoDB mongo = new MongoDB();

		try
		{
			InputStream is = new FileInputStream("settings.json");
			JsonReader reader = Json.createReader(is);
			JsonArray jarr = reader.readArray();

			if (null == jarr)
			{
				System.out.println("It looks like, that settings.json is not in a correct JSON format.");
				Runtime.getRuntime().exit(-1);
			}

			JsonObject jo;

			for (int i = 0; i < jarr.size(); i++)
			{
				jo = jarr.getJsonObject(i);

				if (null == jo.getJsonString("type"))
				{
					System.out.println("Type field is missing for the " + (i + 1) + ". entry");
				}
				else
				{
					String s = jo.getString("type");

					switch (s.toLowerCase())
					{
						case "mongo":
						case "mongodb":
							mongo.prepareMongo(jo);
							break;
						case "mysql":
							mysql.prepareMySQL(jo);
							break;
						case "webservice":
							ws.prepareWebservice(jo);
							break;
						default:
							System.out.print(s + " is an unknown type.");
					}
				}
			}
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("Please create the file settings.json");
		}
		catch (JsonParsingException jpe)
		{
			jpe.printStackTrace();
		}
		catch (NullPointerException npe)
		{
			npe.printStackTrace();
		}
	}
}
