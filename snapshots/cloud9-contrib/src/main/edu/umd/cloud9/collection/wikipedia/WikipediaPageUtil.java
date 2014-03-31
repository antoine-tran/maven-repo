package edu.umd.cloud9.collection.wikipedia;

import java.util.List;

import com.google.common.collect.Lists;

/** utility methods to complement the WikipediaPage format
 * @author tuan 
 */
public class WikipediaPageUtil  {

	public static List<Link> getTemplates(String rawContent) {
		int start = 0;
	    List<Link> links = Lists.newArrayList();

	    while (true) {
	      start = rawContent.indexOf("{{", start);

	      if (start < 0) {
	        break;
	      }

	      int end = rawContent.indexOf("}}", start);

	      if (end < 0) {
	        break;
	      }

	      String text = rawContent.substring(start + 2, end);
	      String anchor = null;

	      // skip empty links
	      if (text.length() == 0) {
	        start = end + 1;
	        continue;
	      }

	      // skip special links
	      if (text.indexOf(":") != -1) {
	        start = end + 1;
	        continue;
	      }

	      // if there is anchor text, get only article title
	      int a;
	      if ((a = text.indexOf("|")) != -1) {
	        anchor = text.substring(a + 1, text.length());
	        text = text.substring(0, a);
	      }

	      if ((a = text.indexOf("#")) != -1) {
	        text = text.substring(0, a);
	      }

	      // ignore article-internal links, e.g., [[#section|here]]
	      if (text.length() == 0) {
	        start = end + 1;
	        continue;
	      }

	      if (anchor == null) {
	        anchor = text;
	      }
	      links.add(new Link(anchor, text));

	      start = end + 1;
	    }

	    return links;
	}
	
	// Duplicate the link data structure in WikipediaPage, since the constructor is not visible.
	// This is merely for backward compatibility and should be re-checked in subsequent versions
	// of Cloud9
	public static class Link {
	    private String anchor;
	    private String target;

	    private Link(String anchor, String target) {
	      this.anchor = anchor;
	      this.target = target;
	    }

	    public String getLabel() {
	      return anchor;
	    }

	    public String getTarget() {
	      return target;
	    }

	    public String toString() {
	      return String.format("[target: %s, anchor: %s]", target, anchor);
	    }
	  }
}
