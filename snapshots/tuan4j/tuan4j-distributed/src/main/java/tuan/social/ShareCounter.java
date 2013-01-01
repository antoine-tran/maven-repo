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
package tuan.social;

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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import tuan.io.FileUtility;

/**
 * This class contains methods for retrieving the number of times
 * one resource is shared in social media sites (Twitter, Facebook,...)
 * 
 * @author tuan
 * @version 0.1.0
 * @since 09.10.2012
 *
 */
public class ShareCounter {

	/** API to get share FB count of a link */
	public static final String FACEBOOK_GRAPH_API = "http://graph.facebook.com/?id=";
	
	/** This web service is called inside the Tweet Button widget. Do not over-use it
	 * or Twitter will screw you */
	public static final String PRIVATE_TWEET_COUNT_API = 
			"http://urls.api.twitter.com/1/urls/count.json?url=";
	
	/** Sevenly API to Facebook Count Statistic Graph */
	public static final String FACEBOOK_SEVENLY_COUNT = 
			"https://graph.facebook.com/fql?q=SELECT%%20like_count,%%20total_count," +
			"%%20share_count,%%20click_count,%%20comment_count%%20FROM%%20link_stat%%20" +
			"WHERE%%20url%%20=%%20%%22%s%%22";
	
	/** Google Plus Share Count API */
	public static final String GOOGLE_PLUS_CNT_API = "https://clients6.google.com/rpc?key=";
	
	/** LinkedIn Count API */
	public static final String LINKEDIN_CNT_API = "http://www.linkedin.com/countserv/count/share?format=json&url=";
	
	/** this method returns the number of times a web page ss shared on Facebook 
	 * @param url the raw HTTP URL address of the web page
	 * @throws IOException */
	public static int facebookShareCnt(String link) throws IOException {
		
		// Issue a request to the API
		String encoded = URLEncoder.encode(link, "ISO-8859-1");
		URL url = new URL(FACEBOOK_GRAPH_API + encoded);
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
	public static int facebookShareCnt(String link, HttpClient client) throws IOException {
		
		// Issue a request to the API
		String encoded = URLEncoder.encode(link, "ISO-8859-1");
		HttpGet req = new HttpGet(FACEBOOK_GRAPH_API + encoded);
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
	
	/** This method returns a number of count statistics on Facebook regarding to a link
	 * @throws IOException */
	public static FacebookCount facebookCnt(String link) throws IOException {
		URL url = new URL(String.format(FACEBOOK_SEVENLY_COUNT, link));

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
		try {
			int offset = json.indexOf("\"like_count\":");
			int end = json.indexOf(',', offset);
			String likeStr = json.substring(offset + 13, end).trim();
			int like = Integer.parseInt(likeStr);
			
			offset = json.indexOf("\"share_count\":", end);
			end = json.indexOf(',', offset);
			String shareStr = json.substring(offset + 14, end).trim();
			int share = Integer.parseInt(shareStr);
			
			offset = json.indexOf("\"comment_count\":", end);
			end = json.indexOf('}', offset);
			String commentStr = json.substring(offset + 16, end).trim();
			int comment = Integer.parseInt(commentStr);
			
			return new FacebookCount(share, like, comment);

		} catch (NumberFormatException e) {
			throw new IOException("Mal-formed JSON response: ", e);
		}		
	}
	
	/** This method returns a number of count statistics on Facebook regarding to a link. It can work
	 * through a proxy
	 * @throws IOException */
	public static FacebookCount facebookCnt(String link, HttpClient client) throws IOException {
		HttpGet req = new HttpGet(String.format(FACEBOOK_SEVENLY_COUNT, link));
		HttpResponse response = client.execute(req);
		HttpEntity entity = response.getEntity();	
			
		// now extract the count
		String json = EntityUtils.toString(entity);
		try {
			int offset = json.indexOf("\"like_count\":");
			int end = json.indexOf(',', offset);
			String likeStr = json.substring(offset + 13, end).trim();
			int like = Integer.parseInt(likeStr);
			
			offset = json.indexOf("\"share_count\":", end);
			end = json.indexOf(',', offset);
			String shareStr = json.substring(offset + 14, end).trim();
			int share = Integer.parseInt(shareStr);
			
			offset = json.indexOf("\"comment_count\":", end);
			end = json.indexOf('}', offset);
			String commentStr = json.substring(offset + 16, end).trim();
			int comment = Integer.parseInt(commentStr);
			
			return new FacebookCount(share, like, comment);

		} catch (NumberFormatException e) {
			throw new IOException("Mal-formed JSON response: ", e);
		}		
	}
	
	/** This method gets the number of times a webpage is tweeted using a private (and
	 * fORBIDDEN >:D ) API. Use it only for experiment purpose please !!!
	 * @throws IOException */
	public static int tweetDirtyCount(String link) throws IOException {

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
	public static int tweetDirtyCount(String link, HttpClient client) throws IOException {
		
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
	
	/**
	 * this method gets the number of share count on Goolge Plus 
	 **/
	public static int googlePlus(String link, String keyPath) throws IOException {
		
		// Get the private API key
		String api = null;
		for (String str : FileUtility.readLines(keyPath, null)) {
			api = str;
		}
		
		// Register POST request and response
		HttpClient client = new DefaultHttpClient();
		if (api == null) {
			throw new RuntimeException("API key is null");
		} else {
			HttpPost req = new HttpPost("https://clients6.google.com/rpc?key=" + api);
			
			// Generate POST input from the following JSON format:
			// [{
			//     "method":"pos.plusones.get",
			//     "id":"p",
			//     "params":{
			//         "nolog":true,
			//         "id":"http://stylehatch.co/",
			//         "source":"widget",
			//         "userId":"@viewer",
			//         "groupId":"@self"
			//         },
			//     "jsonrpc":"2.0",
			//     "key":"p",
			//     "apiVersion":"v1"
			// }]
			StringBuilder postData =
		            new StringBuilder("{\"method\":\"pos.plusones.get\",");
		    postData.append("\"id\":\"p\",");
		    postData.append("\"params\":");
		    postData.append("{\"nolog\":true,");
		    postData.append("\"id\":\"");
		    postData.append(link);
		    postData.append("\",");
		    postData.append("\"source\":\"widget\",");
		    postData.append("\"userId\":\"@viewer\",");
		    postData.append("\"groupId\":\"@self\"");
		    postData.append("},");
		    postData.append("\"jsonrpc\":\"2.0\",");
		    postData.append("\"key\":\"p\",");
		    postData.append("\"apiVersion\":\"v1\"}");			
			StringEntity params = new StringEntity(postData.toString(), "ISO-8859-1");
			req.addHeader("content-type", "application/json");
			req.setEntity(params);
			
			// Get response and close the connection
			HttpResponse res = client.execute(req);
			HttpEntity entity = res.getEntity();
			client.getConnectionManager().shutdown();
			
			// extract the result
			String json = EntityUtils.toString(entity);
			int offset = json.indexOf("\"count\":");
			int end = json.indexOf("}", offset);
			String cntStr = json.substring(offset + 8, end).trim();
			Double cnt = Double.parseDouble(cntStr);
			
			return cnt.intValue();
		}
	}
	
	/** This method returns the number of times a link is shared on Google Plus */
	public static int googlePlus(String link, String keyPath, HttpClient client) throws IOException {
		
		// Get the private API key
		String api = null;
		for (String str : FileUtility.readLines(keyPath, null)) {
			api = str;
		}
		
		// Register POST request and response
		if (api == null) {
			throw new RuntimeException("API key is null");
		} else {
			HttpPost req = new HttpPost("https://clients6.google.com/rpc?key=" + api);
			
			// Generate POST input from the following JSON format:
			// [{
			//     "method":"pos.plusones.get",
			//     "id":"p",
			//     "params":{
			//         "nolog":true,
			//         "id":"http://stylehatch.co/",
			//         "source":"widget",
			//         "userId":"@viewer",
			//         "groupId":"@self"
			//         },
			//     "jsonrpc":"2.0",
			//     "key":"p",
			//     "apiVersion":"v1"
			// }]
			StringBuilder postData = new StringBuilder("{\"method\":");
			postData.append("\"pos.plusones.get\",");
		    postData.append("\"id\":\"p\",");
		    postData.append("\"params\":");
		    postData.append("{\"nolog\":true,");
		    postData.append("\"id\":\"");
		    postData.append(link);
		    postData.append("\",");
		    postData.append("\"source\":\"widget\",");
		    postData.append("\"userId\":\"@viewer\",");
		    postData.append("\"groupId\":\"@self\"");
		    postData.append("},");
		    postData.append("\"jsonrpc\":\"2.0\",");
		    postData.append("\"key\":\"p\",");
		    postData.append("\"apiVersion\":\"v1\"}");			
			StringEntity params = new StringEntity(postData.toString(), "ISO-8859-1");
			req.addHeader("content-type", "application/json");
			req.setEntity(params);
			
			// Get response and close the connection
			HttpResponse res = client.execute(req);
			HttpEntity entity = res.getEntity();
			
			// extract the result
			String json = EntityUtils.toString(entity);
			int offset = json.indexOf("\"count\":");
			int end = json.indexOf("}", offset);
			String cntStr = json.substring(offset + 8, end).trim();
			Double cnt = Double.parseDouble(cntStr);
			
			return cnt.intValue();
		}
	}
	
	/**
	 * this method gets the number of share count on Goolge Plus with direct API key 
	 **/
	public static int googlePlus1(String link, String api) throws IOException {
			
		
		// Register POST request and response
		HttpClient client = new DefaultHttpClient();
		if (api == null) {
			throw new RuntimeException("API key is null");
		} else {
			HttpPost req = new HttpPost("https://clients6.google.com/rpc?key=" + api);
			
			// Generate POST input from the following JSON format:
			// [{
			//     "method":"pos.plusones.get",
			//     "id":"p",
			//     "params":{
			//         "nolog":true,
			//         "id":"http://stylehatch.co/",
			//         "source":"widget",
			//         "userId":"@viewer",
			//         "groupId":"@self"
			//         },
			//     "jsonrpc":"2.0",
			//     "key":"p",
			//     "apiVersion":"v1"
			// }]
			StringBuilder postData =
		            new StringBuilder("{\"method\":\"pos.plusones.get\",");
		    postData.append("\"id\":\"p\",");
		    postData.append("\"params\":");
		    postData.append("{\"nolog\":true,");
		    postData.append("\"id\":\"");
		    postData.append(link);
		    postData.append("\",");
		    postData.append("\"source\":\"widget\",");
		    postData.append("\"userId\":\"@viewer\",");
		    postData.append("\"groupId\":\"@self\"");
		    postData.append("},");
		    postData.append("\"jsonrpc\":\"2.0\",");
		    postData.append("\"key\":\"p\",");
		    postData.append("\"apiVersion\":\"v1\"}");			
			StringEntity params = new StringEntity(postData.toString(), "ISO-8859-1");
			req.addHeader("content-type", "application/json");
			req.setEntity(params);
			
			// Get response and close the connection
			HttpResponse res = client.execute(req);
			HttpEntity entity = res.getEntity();
			client.getConnectionManager().shutdown();
			
			// extract the result
			String json = EntityUtils.toString(entity);
			int offset = json.indexOf("\"count\":");
			int end = json.indexOf("}", offset);
			String cntStr = json.substring(offset + 8, end).trim();
			Double cnt = Double.parseDouble(cntStr);
			
			return cnt.intValue();
		}
	}
	
	/** This method returns the number of times a link is shared on Google Plus with direct API key */
	public static int googlePlus1(String link, String api, HttpClient client) throws IOException {
		
		// Register POST request and response
		if (api == null) {
			throw new RuntimeException("API key is null");
		} else {
			HttpPost req = new HttpPost("https://clients6.google.com/rpc?key=" + api);
			
			// Generate POST input from the following JSON format:
			// [{
			//     "method":"pos.plusones.get",
			//     "id":"p",
			//     "params":{
			//         "nolog":true,
			//         "id":"http://stylehatch.co/",
			//         "source":"widget",
			//         "userId":"@viewer",
			//         "groupId":"@self"
			//         },
			//     "jsonrpc":"2.0",
			//     "key":"p",
			//     "apiVersion":"v1"
			// }]
			StringBuilder postData = new StringBuilder("{\"method\":");
			postData.append("\"pos.plusones.get\",");
		    postData.append("\"id\":\"p\",");
		    postData.append("\"params\":");
		    postData.append("{\"nolog\":true,");
		    postData.append("\"id\":\"");
		    postData.append(link);
		    postData.append("\",");
		    postData.append("\"source\":\"widget\",");
		    postData.append("\"userId\":\"@viewer\",");
		    postData.append("\"groupId\":\"@self\"");
		    postData.append("},");
		    postData.append("\"jsonrpc\":\"2.0\",");
		    postData.append("\"key\":\"p\",");
		    postData.append("\"apiVersion\":\"v1\"}");			
			StringEntity params = new StringEntity(postData.toString(), "ISO-8859-1");
			req.addHeader("content-type", "application/json");
			req.setEntity(params);
			
			// Get response and close the connection
			HttpResponse res = client.execute(req);
			HttpEntity entity = res.getEntity();
			
			// extract the result
			String json = EntityUtils.toString(entity);
			int offset = json.indexOf("\"count\":");
			int end = json.indexOf("}", offset);
			String cntStr = json.substring(offset + 8, end).trim();
			Double cnt = Double.parseDouble(cntStr);
			
			return cnt.intValue();
		}
	}
	
	/** This method returns the number of times a link is shared on LinkedIn */
	public static int linkedIn(String link) throws IOException {
		
		// Issue a request to the API
		String encoded = URLEncoder.encode(link, "ISO-8859-1");
		URL url = new URL(LINKEDIN_CNT_API + encoded);
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
		int shareId =  json.indexOf("\"count\":");
		int end = json.indexOf(",", shareId);
		String countStr = json.substring(shareId + 8, end).trim();		
		try {
			return Integer.parseInt(countStr); 
		} catch (NumberFormatException e) {
			throw new IOException("Mal-formed JSON response: ", e);
		}		
	}
	
	/** this method returns the number of times a web page ss shared on LinkedIn. It works
	 * through either a proxy or a direct connection  
	 * @param url the raw HTTP URL address of the web page
	 * @throws IOException */
	public static int linkedIn(String link, HttpClient client) throws IOException {
		
		// Issue a request to the API
		String encoded = URLEncoder.encode(link, "ISO-8859-1");
		HttpGet req = new HttpGet(LINKEDIN_CNT_API + encoded);
		HttpResponse response = client.execute(req);
		HttpEntity entity = response.getEntity();	
			
		// now extract the count
		String json = EntityUtils.toString(entity);
		int shareId =  json.indexOf("\"count\":");
		int end = json.indexOf(",", shareId);
		String countStr = json.substring(shareId + 8, end).trim();	
		try {
			return Integer.parseInt(countStr); 
		} catch (NumberFormatException e) {
			throw new IOException("Mal-formed JSON response: ", e);
		}		
	}

	public static class FacebookCount {
		public int shareCnt;
		public int likeCnt;
		public int commentCnt;

		public FacebookCount(int shareCnt, int likeCnt, int commentCnt) {
			this.shareCnt = shareCnt;
			this.likeCnt = likeCnt;
			this.commentCnt = commentCnt;
		}
		
		@Override
		public String toString() {
			return "[" + shareCnt + ", " + likeCnt + ", " + commentCnt + "]";
		}
	}
	
	// Test routine
	 public static void main(String[] args) {	
	
		try {
			System.out.println(facebookCnt(
			"http://news.yahoo.com/california-gasoline-prices-set-plunge-spike-ends-004934596.html"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
}
