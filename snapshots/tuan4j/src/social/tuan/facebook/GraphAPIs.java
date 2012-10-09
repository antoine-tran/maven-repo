/**
 * ==================================
 * 
 * Copyright (c) 2010-2012 Anh Tuan Tran
 *
 * URL: http://www.mpi-inf.mpg.de/~attran,
 *          http://www.l3s.de/~ttran
 *
 * Email: tranatuan24@gmail.com
 * ==================================
 * 
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 */
package tuan.facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.mortbay.jetty.HttpGenerator;

/**
 * A Java wrapper for Facebook Open Graph API
 * @author tuan
 * @version 0.1
 * @since 09 Oct 2012
 *
 */
public class GraphAPIs {

	/** API to get share count of a link */
	public static final String LINK_SHARE_CNT_API = "http://graph.facebook.com/?id=";
	
	/** this method returns the number of times a web page ss shared on Facebook 
	 * @param url the raw HTTP URL address of the web page
	 * @throws IOException */
	public static int getShareCount(String link) throws IOException {
		
		// Issue a request to the API
		String encoded = URLEncoder.encode(link, "ISO-8859-1");
		URL url = new URL(LINK_SHARE_CNT_API + encoded);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		InputStream response = conn.getInputStream();
		InputStreamReader stream = new InputStreamReader(response, "UTF-8");
		BufferedReader reader = new BufferedReader(stream);
		String line;
		StringBuilder output = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			output.append(line);
		}
		
		// Release resource
		reader.close();
		stream.close();
		response.close();
		conn.disconnect();
		
		// now extract the count
		String json = output.toString();
		int shareId =  json.indexOf("\"shares\":");
		int closeBracketId = json.indexOf("}", shareId);
		String countStr = json.substring(shareId + 9, closeBracketId).trim();		
		try {
			return Integer.parseInt(countStr); 
		} catch (NumberFormatException e) {
			throw new IOException("Mal-formed JSON response: ", e);
		}		
	}
	
	/** this method returns the number of times a web page ss shared on Facebook. It works
	 * through either a proxy or a direct connection  
	 * @param url the raw HTTP URL address of the web page
	 * @throws IOException */
	public static int getShareCount(String link, HttpClient client) throws IOException {
		
		// Issue a request to the API
		String encoded = URLEncoder.encode(link, "ISO-8859-1");
		HttpGet req = new HttpGet(LINK_SHARE_CNT_API + encoded);
		HttpResponse response = client.execute(req);
		HttpEntity entity = response.getEntity();	
			
		// now extract the count
		String json = EntityUtils.toString(entity);
		int shareId =  json.indexOf("\"shares\":");
		int closeBracketId = json.indexOf("}", shareId);
		String countStr = json.substring(shareId + 9, closeBracketId).trim();		
		try {
			return Integer.parseInt(countStr); 
		} catch (NumberFormatException e) {
			throw new IOException("Mal-formed JSON response: ", e);
		}		
	}
	
	// Test routine
	public static void main(String[] args) {
		try {
			System.out.println(GraphAPIs.getShareCount(
					"http://news.yahoo.com/california-gasoline-prices-set-plunge-spike-ends-004934596.html"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
