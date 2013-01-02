/**
 * 
 */
package edu.stanford.nlp.process;

import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.OriginalTextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.StringUtils2;

/**
 * Groups a sequence of tokens into one object based on the named entity types
 * represented by the tokens.
 * @author Tuan Tran
 * @since 02/01/13
 * @param <IN> the type of tokens to be grouped
 */
public class NETokenGroupProcessor<IN extends CoreLabel> 
implements ListProcessor<CoreLabel, CoreLabel>{

	@Override
	public List<CoreLabel> process(List<? extends CoreLabel> tokens) {
		List<CoreLabel> groups = Generics.newArrayList();
		CoreLabel curGroup = null;
		String lastNe = null;	
		for (CoreLabel token : tokens) {
			String ne = token.get(NamedEntityTagAnnotation.class);

			if (ne.equals(lastNe)) {
				if ("PERSON".equals(ne) ||"LOCATION".equals(ne) ||
						"ORGANIZATION".equals(ne)) {					
					String word = StringUtils2.joinWithOriginalWhiteSpace(curGroup, token);	
					Integer offset = token.get(CharacterOffsetEndAnnotation.class);
					curGroup.set(TextAnnotation.class, word);
					curGroup.set(ValueAnnotation.class, word);
					curGroup.set(OriginalTextAnnotation.class, word);
					curGroup.set(CharacterOffsetEndAnnotation.class, offset);
				} else {
					groups.add(curGroup);
					curGroup = token;
				}
			} else {
				if (curGroup != null) groups.add(curGroup);
				if ("PERSON".equals(ne) ||"LOCATION".equals(ne) ||
						"ORGANIZATION".equals(ne)) {					
					curGroup = new CoreLabel(token);					
				} else {
					curGroup = token;
				}
			}
			lastNe = ne;
		}
		return groups;
	}
}
