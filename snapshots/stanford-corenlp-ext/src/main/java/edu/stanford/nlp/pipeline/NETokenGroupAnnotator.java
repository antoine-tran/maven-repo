/**
 * 
 */
package edu.stanford.nlp.pipeline;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomAnnotations.TokenGroupsAnnotation;
import edu.stanford.nlp.process.NETokenGroupProcessor;

/**
 * This class assumes an NER annotator has been invoked upfront, and the input
 * document contains a <code>List&lt;? extends CoreLabel&gt;</code> of named
 * entities. Furthermore, the ner should make use of a sequential model, such
 * as CRF or HMM. It then groups a sequence of similar tokens into one <code>
 * CoreLabel</code> object that semantically defines an entity or a common word
 * 
 * @author Tuan Tran
 * @version 1.3.1.1
 * @since 23/05/12
 *
 */
public class NETokenGroupAnnotator implements Annotator {
	
	private final NETokenGroupProcessor<CoreLabel> netgp;

	private boolean VERBOSE = false;

	public NETokenGroupAnnotator() {
		this(null, null);
	}
	
	public NETokenGroupAnnotator(String annotName, Properties props) {		
		netgp = new NETokenGroupProcessor<CoreLabel>();
	}

	public NETokenGroupAnnotator(NETokenGroupProcessor<CoreLabel> netgp) {
		this.netgp = netgp;
	}

	public NETokenGroupAnnotator(NETokenGroupProcessor<CoreLabel> netgp,
			boolean vERBOSE) {
		this.netgp = netgp;
		VERBOSE = vERBOSE;
	}

	@Override
	public void annotate(Annotation annotation) {
		if (VERBOSE) {
			System.err.println("Grouping tokens on named entites....");
		}
		if (annotation.has(TokenGroupsAnnotation.class)) {
			throw new RuntimeException("conflict to existing token groups in: " + 
					annotation);
		}
		List<CoreLabel> tokens = annotation.get(TokensAnnotation.class); 
		annotation.set(TokenGroupsAnnotation.class, netgp.process(tokens));
	}

}
