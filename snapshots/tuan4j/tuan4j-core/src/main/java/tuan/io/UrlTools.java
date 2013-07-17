package tuan.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/** The small utility with urls */
public class UrlTools {

	/** A small utility that takes a shortened url and outputs
	 *  its actual url, together with the header and content. 
	 *  Thanks to Nihed's sharing !! For the courtesy, I keep all
	 *  boilerplate code, including System.out.println stuff
	 *  
	 *  @author Nihed MBAREK
	 *  */
	public static Map<String, String> fetchTextHTML(String shortUrl) {
		URL url;
		URLConnection conn;

		HashMap<String, String> output = new HashMap<String, String>();
		String result = shortUrl;
		String header = null;
		int itr = 0;

		System.out.println("------------ Processing short URL: " + shortUrl);
		try{
			do{

				// Break infinite loop
				if(itr > 10) {
					return null;
				}

				url = new URL(result);
				HttpURLConnection.setFollowRedirects(false);
				conn = url.openConnection();
				header = conn.getHeaderField(null);
				System.out.println("\n*Itr[" + itr + "]\theader=" + header);
				System.out.println("\tcurrent result URL=" + result);
				String location = conn.getHeaderField("location");
				System.out.println("\tlocation=" + location);

				if (location != null) {
					// relative path
					if(location.startsWith("/")) {
						int i = result.indexOf("://") + 3;
						int j = result.indexOf("/", i);

						String domain = "";
						if(j != -1) {
							domain = result.substring(0, j);
							//System.out.println("\tdomain = " + domain);
						} else {
							domain = result.substring(0);
						}
						result = domain + location;
					} else {
						result = location;
					}
					System.out.println("\tnew result URL=" + result);
				}

			    itr++;
			} while((header != null) && header.contains(" 30"));

			if((header != null) && !header.contains(" 4") && !header.contains(" 5")) {
				// Get the response
			    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			    String resLine;
			    String content = "";

			    System.out.println("\n\t\t\t##### BEGIN URL["+result+"]-[" + conn.getContentType() + "] #####");

			    if(conn.getContentType().startsWith("text/html")) {
			    	while((resLine = rd.readLine()) != null) {
				    	//System.out.println(resLine);
				    	content += resLine;
				    }
				    System.out.println("\t\t\t##### END URL["+result+"] #####\n");
				    rd.close();

					output.put("header", header);	//header of the expanded url
					output.put("expanded_url", result);
					output.put("content", content);
			    } else {
			    	System.out.println("Content type is not 'text/html'.");
					output.put("header", header);	//header of the expanded url
					output.put("expanded_url", result);
					output.put("content", null);
			    }
			} else {
				System.out.println("Cannot read contents.");
				output.put("header", header);	//header of the expanded url
				output.put("expanded_url", result);
				output.put("content", null);
			}
		} catch(MalformedURLException me) {
        	me.printStackTrace();
        	System.out.println("Error MalformedURL = " + result);
        	return null;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: Exception in getLongUrl().");
			return null;
		}

		return output;
	}
}
