package edu.umd.cloud9.collection.wikipedia;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

/** utility methods to complement the WikipediaPage format
 * @author tuan 
 * @deprecated please see EvalFunc's implementations ({@link org.wikimedia.pig.PageFunc}
 */
@Deprecated
public class WikipediaPageUtil  {

	static final Pattern[] NOT_TEMPLATE_PATTERN = new Pattern[] {
		Pattern.compile("R from.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("Redirect\\s.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("Cite.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("cite.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("Use\\s.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("pp-move-indef.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("File:\\s*.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("Related articles.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("lang\\s.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("lang-en.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("LSJ.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("OCLC.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("Main\\s.*|", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("IEP|.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("sep entry.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("Wayback\\s.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("See also\\s.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("inconsistent citations.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("Harvnb.*", Pattern.DOTALL | Pattern.MULTILINE), // Harvard citation no brackets
		Pattern.compile("Lookfrom\\s.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("Portal\\s.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("Reflist\\s.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("Sister project links.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("Link\\s.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("link\\s.*", Pattern.DOTALL | Pattern.MULTILINE),

		// WikiProject BBC
		Pattern.compile("WikiProject\\s.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("BBCNAV.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("Wikipedia:WikiProject\\s", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("User:Mollsmolyneux.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("subst:.*", Pattern.DOTALL | Pattern.MULTILINE), 
		Pattern.compile("BBC\\s.*", Pattern.DOTALL | Pattern.MULTILINE),
		Pattern.compile("BBC-.*stub.*", Pattern.DOTALL | Pattern.MULTILINE)
	};
	
	private static final boolean isNotTemplateQuote(String title, String text) {
		String qtext = Pattern.quote(text); 
		for (Pattern p : NOT_TEMPLATE_PATTERN) {
			if (p.matcher(qtext).matches()) return true;
		}
		if (text.endsWith("icon") && text.endsWith("sidebar")) {
			return true;
		}
		if (text.equalsIgnoreCase("good article") || text.equals("-")) {
			return true;
		}
		
		// A quick trick to avoid BBC player linkage. Might be a gotcha !!
		if (text.equals(title + "|")) {
			return true;
		}
		else return false;		
	}
	 
	public static List<Link> getTemplates(String title, String rawContent) {
		int start = 0;
		List<Link> links = Lists.newArrayList();
		rawContent = rawContent.replace('\n', ' ');
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
			if (isNotTemplateQuote(title, text)) {
				start = end + 1;
				continue;
			}

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
			if ((a = text.lastIndexOf("|")) != -1) {
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

	public static void main(String[] args) {
		//String s = "{{cite journal |first=Judith |last=Suissa |url=http://newhumanist.org.uk/1288/anarchy-in-the-classroom|title= Anarchy in the classroom |journal=[[The New Humanist]] |volume=120 |issue=5 |date=September–October 2005 |ref=harv}}";
		String s = "{{Cite journal\n|last=Williams\n|first=Leonard\n|date=September 2007\n |title=Anarchism Revived\n |journal=New Political Science\n |volume=29\n |issue=3\n |pages=297–312\n |doi=10.1080/07393140701510160\n |ref=harv\n}}";
		// String s = "File:HMS Hermes (R12) (Royal Navy aircraft carrier.jpg|";
		System.out.println(isNotTemplateQuote("", s));		
	}
}
