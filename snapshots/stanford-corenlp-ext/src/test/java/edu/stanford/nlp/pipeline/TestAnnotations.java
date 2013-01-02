package edu.stanford.nlp.pipeline;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;

public class TestAnnotations {

	@Test
	public void testNETokenAnnotations() {
		Properties props = new Properties();		
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, nerannot");
		props.put("customAnnotatorClass.nerannot", "edu.stanford.nlp.pipeline.NETokenGroupAnnotator");
		Annotation text = new Annotation("Kosgi Santosh sent an email to Stanford University. He didn't get a reply.");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);		
		StringBuilder annotText = new StringBuilder();

		// run all Annotators on this text
		pipeline.annotate(text);

		// get all annotated sentences
		List<CoreLabel> phrases = 
				text.get(CustomAnnotations.TokenGroupsAnnotation.class);

		// In-memory hash
		Set<String> index = new HashSet<String>();

		// for each sentence, do the following:
		// 0 - build the annotated text

		for (CoreLabel phrase : phrases) {
			String word = phrase.get(TextAnnotation.class);
			String ne = phrase.get(NamedEntityTagAnnotation.class);
			String key = word + ne;
			if (index.contains(key)) continue;
			else index.add(key);

			// 1 - run Okkam to disambiguate the entity
			// 2 - integrate the entity URI into the database
			if ("PERSON".equals(ne) || "LOCATION".equals(ne) || 
					"ORGANIZATION".equals(ne)) {
				annotText.append(annotate(word, ne));
				annotText.append(" ");
			} else {
				annotText.append(word).append(" ");
			}
		}
		
		System.out.println(annotText);
	}

	/** this internal method invokes OKKAM services to disambiguated a given
	 * named entity, then hook up with a corresponding entity in the GLOCAL
	 * database 
	 * @throws ServerException */
	private String annotate(String label, String type) {
		StringBuilder sb = new StringBuilder();
		sb.append("<ENAMEX type='");
		sb.append(type);
		sb.append("'");					
		sb.append(" uri=''");		
		sb.append(" score='NaN'");				
		sb.append("'");
		sb.append(">");
		sb.append(label);
		sb.append("</ENAMEX>");
		return sb.toString();		
	}
}
