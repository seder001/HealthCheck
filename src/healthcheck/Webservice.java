package healthcheck;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 *
 */
public class Webservice
{

	/**
	 * Host name verifier that does not perform nay checks.
	 */
	private static class NullHostnameVerifier implements HostnameVerifier
	{
		public boolean verify(String hostname, SSLSession session)
		{
			return true;
		}
	}

	private TrustManager[] trustCerts()
	{
		return new TrustManager[]
		{ new X509TrustManager()
		{
			public java.security.cert.X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType)
			{
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType)
			{
			}
		} };
	}

	/**
	 * @param jo
	 */
	public void prepareWebservice(JsonObject jo)
	{
		JsonString webIP, info, path, protocol;
		JsonNumber webPort;
		String check;

		webIP = (JsonString) HealthCheck.getValue(jo, "ip");
		webPort = (JsonNumber) HealthCheck.getValue(jo, "port");
		protocol = (JsonString) HealthCheck.getValue(jo, "protocol");
		info = (JsonString) HealthCheck.getValue(jo, "info");
		path = (JsonString) HealthCheck.getValue(jo, "path");

		if (null == webIP || null == webPort || null == protocol || null == path || null == info)
		{
			return;
		}

		check = checkWebservice(webIP.getString(), webPort.intValue(), protocol.getString(), path.getString(), jo);
		System.out.println(info.getString() + check);
	}

	private void getWebserviceRequest(HttpURLConnection c, JsonObject jo)
	{
		if (null != jo.getJsonString("method"))
		{
			try
			{
				c.setRequestMethod(jo.getString("method"));
			}
			catch (ProtocolException e)
			{
				System.out.println("Method " + jo.getString("method") + " is not available.");
			}
		}

		JsonArray jar = jo.getJsonArray("property");
		if (null != jar)
		{
			for (int i = 0; i < jar.size(); i++)
			{

				JsonObject jtemp = jar.getJsonObject(i);
				for (Map.Entry<String, JsonValue> entry : jtemp.entrySet())
				{
					c.setRequestProperty(entry.getKey(), jtemp.getString(entry.getKey()));
				}
			}
		}
	}

	private String checkWebservice(String webIP, int webPort, String protocol, String path, JsonObject jo)
	{
		int code = 0;

		try
		{
			URL url = new URL(protocol + "://" + webIP + ":" + webPort + path);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();

			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = trustCerts();

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());

			if (null != jo.getJsonObject("request"))
			{
				getWebserviceRequest(c, jo.getJsonObject("request"));
			}

			c.connect();

			code = c.getResponseCode();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			HealthCheck.sendEmail = true;
			return " ... NOK (connection failed!)";
		}
		catch (NoSuchAlgorithmException nsae)
		{
			nsae.printStackTrace();
		}
		catch (KeyManagementException kme)
		{
			kme.printStackTrace();
		}

		if (code == 200)
		{
			return " ... OK";
		}
		else
		{
			HealthCheck.sendEmail = true;
			return " ... NOK (response code " + code + " )";
		}
	}
}
