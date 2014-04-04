package org.wikipedia.pig.load;

import static java.lang.String.valueOf;

import java.io.IOException;
import java.util.Arrays;

import org.apache.pig.ResourceSchema;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema;
import org.mortbay.log.Log;

import pignlproc.markup.AnnotatingMarkupParser;
import pignlproc.markup.Annotation;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage;
import edu.umd.cloud9.collection.wikipedia.WikipediaPageUtil;
import edu.umd.cloud9.collection.wikipedia.WikipediaPageUtil.Link;

/**
 * This is a UDF loader that pipelines records in Wikipedia XML dump to Pig tuple, using
 * WikipediaPageInputFormat. Besides columns defined in Page schema 
 * (http://www.mediawiki.org/wiki/Manual:Page_table), it parses a number of additional
 * data:
 * - outgoing links
 * - paragraphs
 * - headers
 * - anchor texts
 * - templates
 * @author tuan
 */
public class FullWikipediaLoader extends LiteWikipediaLoader {

	@SuppressWarnings("unchecked")
	@Override
	public Tuple getNext() throws IOException {
		boolean hasNext;
		try {
			hasNext = reader.nextKeyValue();
			if (hasNext) {
				WikipediaPage page = reader.getCurrentValue();
				String id = page.getDocid();
				String title = page.getTitle();
								
				boolean isArticle = page.isArticle();
				boolean isDisamb = page.isDisambiguation();
				boolean isRedirect = page.isRedirect();
				
				// load custom WikiModel from PigNLPRoc
				String lang = page.getLanguage();
				AnnotatingMarkupParser parser = new AnnotatingMarkupParser(lang);
				String raw = page.getRawXML();
				String text = parser.parse(raw);
				String length = valueOf(text.length());
				
				// load headers
				DataBag headers = bags.newDefaultBag();
				for (Annotation headr : parser.getHeaderAnnotations()) {
					headers.add(tuples.newTupleNoCopy(Arrays.asList(headr.value, headr.begin, 
							headr.end)));
				}
				
				// load links
				DataBag links = bags.newDefaultBag();
				for (Annotation link : parser.getWikiLinkAnnotations()) {
					links.add(tuples.newTupleNoCopy(Arrays.asList(link.value, link.label, 
							link.begin, link.end)));
				}
				
				// load paragraphs
				DataBag paragraphs = bags.newDefaultBag();
				for (Annotation par : parser.getWikiLinkAnnotations()) {
					paragraphs.add(tuples.newTupleNoCopy(Arrays.asList(par.value, par.begin, 
							par.end)));
				}
				
				// load templates 
				DataBag templates = bags.newDefaultBag();
				for (Link t : WikipediaPageUtil.getTemplates(title, raw)) {
					Log.info("Page " + title + " has template: "+ t.getLabel() + "\t" + t.getTarget());				
					templates.add(tuples.newTupleNoCopy(Arrays.asList(t.getTarget(), t.getLabel())));
				}
								
				return tuples.newTupleNoCopy(Arrays.asList(id, (isArticle) ? "0" : "118", title,
						text, valueOf(isRedirect), length, valueOf(isDisamb), headers, links, 
						paragraphs, templates));
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		return null;
	}

	@Override
	protected void defineSchema() throws FrontendException {
		Schema schema = new Schema();

		// canonical fields in Wikipedia SQL dump
		schema.add(new FieldSchema("page_id", DataType.INTEGER));
		schema.add(new FieldSchema("page_namespace", DataType.INTEGER));
		schema.add(new FieldSchema("page_title", DataType.CHARARRAY));
		schema.add(new FieldSchema("text", DataType.CHARARRAY));
		schema.add(new FieldSchema("page_is_redirect", DataType.BOOLEAN));
		schema.add(new FieldSchema("page_len", DataType.INTEGER));

		// Added fields
		schema.add(new FieldSchema("page_is_disamb", DataType.BOOLEAN));
		
		// register headers
		Schema headerSchema = new Schema();
		headerSchema.add(new FieldSchema("tagname", DataType.CHARARRAY));
		headerSchema.add(new FieldSchema("begin", DataType.INTEGER));
		headerSchema.add(new FieldSchema("end", DataType.INTEGER));
		Schema headerSchemaWrapper = new Schema(new FieldSchema("th", headerSchema));
		schema.add(new FieldSchema("headers", headerSchemaWrapper, DataType.MAP));
		
		// register links
		Schema linkSchema = new Schema();
		linkSchema.add(new FieldSchema("target", DataType.CHARARRAY));
		linkSchema.add(new FieldSchema("anchor", DataType.CHARARRAY));
		linkSchema.add(new FieldSchema("begin", DataType.INTEGER));
		linkSchema.add(new FieldSchema("end", DataType.INTEGER));
		Schema linkSchemaWrapper = new Schema(new FieldSchema("tl", linkSchema));
		schema.add(new FieldSchema("links", linkSchemaWrapper, DataType.BAG));
		
		// register paragraphs
		Schema paragraphSchema = new Schema();
		paragraphSchema.add(new FieldSchema("tagname", DataType.CHARARRAY));
		paragraphSchema.add(new FieldSchema("begin", DataType.INTEGER));
		paragraphSchema.add(new FieldSchema("end", DataType.INTEGER));
		Schema paragraphSchemaWrapper = new Schema(new FieldSchema("tp", paragraphSchema));
		schema.add(new FieldSchema("paragraph", paragraphSchemaWrapper, DataType.MAP));
		
		// register templates
		Schema templateSchema = new Schema();
		templateSchema.add(new FieldSchema("target", DataType.CHARARRAY));
		templateSchema.add(new FieldSchema("label", DataType.CHARARRAY));
		Schema templateSchemaWrapper = new Schema(new FieldSchema("tt", templateSchema));
		schema.add(new FieldSchema("template", templateSchemaWrapper, DataType.MAP));
		
		this.schema = new ResourceSchema(schema);
	}
}
