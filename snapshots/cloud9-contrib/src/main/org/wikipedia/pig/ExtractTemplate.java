/**
 * 
 */
package org.wikipedia.pig;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.TupleFactory;


/**
 * Extract a list of templates from Wikipedia raw page and have it
 * returned as a data bag
 * @author tuan
 *
 */
public class ExtractTemplate extends PageFunc<DataBag> {

	private BagFactory bags = BagFactory.getInstance();
	private TupleFactory tuples = TupleFactory.getInstance();
		
	private static final Pattern[] NOT_TEMPLATE_PATTERN = new Pattern[] {
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
		
		// Gotcha: A quick trick to avoid BBC player linkage ({{In Our Time|Anarchism|p0038x9t|Anarchism}}). 
		// Not work in all cases
		if (text.endsWith("|" + title)) {
			return true;
		}
		else return false;		
	}
	
	@Override
	public DataBag parse(long id, String title, String rawContent) {
					
		DataBag bag = bags.newDefaultBag();
		bag.add(tuples.newTupleNoCopy(Arrays.asList("test","anchor")));

		/*int start = 0;
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
			bag.add(tuples.newTupleNoCopy(Arrays.asList(text, anchor)));
			start = end + 1;
		}*/

		return bag;
	}

}
