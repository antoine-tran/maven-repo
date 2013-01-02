
package edu.stanford.nlp.ling;

import java.util.List;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.ErasureUtils;

/**
 * This class provides new customized annotations for more advanced tasks. It is
 * an extension of the Stanford CoreNLP's CoreAnnotations
 * 
 * @author Tuan Tran
 * @version 1.3.1.1
 * @since 23/05/12
 *
 */
public class CustomAnnotations {

	private CustomAnnotations() {
	} // only static members

	/**
	 * The CoreMap key for representing a group of consecutive tokens in an annotation.
	 * This key is useful, for instance to group a sequence of similarly named tokens in
	 * named entity annotation tasks. 
	 * @author tuan
	 */	
	public static class TokenGroupsAnnotation implements CoreAnnotation<List<CoreLabel>> {
		public Class<List<CoreLabel>> getType() {
			return ErasureUtils.<Class<List<CoreLabel>>> uncheckedCast(List.class);
		}
	}
}
