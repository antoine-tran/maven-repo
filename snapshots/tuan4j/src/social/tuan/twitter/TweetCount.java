/**
 * ==================================
 * 
 * Copyright (c) 2010 Anh Tuan Tran
 *
 * URL: http://www.mpi-inf.mpg.de/~attran,
 *          http://www.l3s.de/~ttran
 *
 * Email: tranatuan24@gmail.com
 * ==================================
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 */
package tuan.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import tuan.facebook.GraphAPIs;

/**
 * This class contains methods for counting tweets from open (not public) Twitter API
 * @author tuan
 *
 */
public class TweetCount {

	/** This method is called inside the Twee Button widget. Do not over-use it
	 * or Twitter will screw you */
	public static final String PRIVATE_TWEET_COUNT_API = 
			"http://urls.api.twitter.com/1/urls/count.json?url=";

	/** This method gets the number of times a webpage is tweeted using a private (and
	 * fORBIDDEN >:D ) API. Use it only for experiment purpose please !!!
	 * @throws IOException */
	public static int getDirtyCount(String link) throws IOException {

		// Issue a request to the API
		String encoded = URLEncoder.encode(link, "ISO-8859-1");
		URL url = new URL(PRIVATE_TWEET_COUNT_API + encoded);
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
		
		// Extract the count
		String json = output.toString();
		int offset = json.indexOf("{\"count\":");
		int comma = json.indexOf(',');
		String cntStr = json.substring(offset + 9, comma).trim();
		try {
			return Integer.parseInt(cntStr); 
		} catch (NumberFormatException e) {
			throw new IOException("Mal-formed JSON response: ", e);
		}
	}

	/** This method gets the number of times a webpage is tweeted using a private (and
	 * fORBIDDEN >:D ) API. It can run through a proxy server.
	 * NOte: Use it only for experiment purpose please !!!
	 * @throws IOException */
	public static int getDirtyCount(String link, HttpClient client) throws IOException {
		
		// Issue a request to the API
		String encoded = URLEncoder.encode(link, "ISO-8859-1");
		HttpGet req = new HttpGet(PRIVATE_TWEET_COUNT_API + encoded);
		HttpResponse response = client.execute(req);
		HttpEntity entity = response.getEntity();	
			
		// now extract the count
		String json = EntityUtils.toString(entity);
		int offset = json.indexOf("{\"count\":");
		int comma = json.indexOf(',');
		String cntStr = json.substring(offset + 9, comma).trim();
		try {
			return Integer.parseInt(cntStr); 
		} catch (NumberFormatException e) {
			throw new IOException("Mal-formed JSON response: ", e);
		}
	}
	
	// Test routine
	public static void main(String[] args) {
		try {
			System.out.println(TweetCount.getDirtyCount(
					"http://news.yahoo.com/california-gasoline-prices-set-plunge-spike-ends-004934596.html"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
