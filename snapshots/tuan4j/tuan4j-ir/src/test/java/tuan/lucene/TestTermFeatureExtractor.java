package tuan.lucene;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.Field;
import org.junit.Before;
import org.junit.Test;

public class TestTermFeatureExtractor {

	@Before
	public void setUpIndex() throws IOException {
		Directory dir = FSDirectory.open(new File("/Users/work/Downloads/lucene-test"));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = null;
        try {
        	writer = new IndexWriter(dir, iwc);
            
            Document doc = new Document();
            doc.add(new TextField("title", "gone with the wind", Field.Store.YES));
            doc.add(new TextField("text", "and they live happily ever", Field.Store.YES));
            writer.addDocument(doc);
            
            doc = new Document();
            doc.add(new TextField("title", "the wind and happiness", Field.Store.YES));
            doc.add(new TextField("text", "they gone in happiness ever", Field.Store.YES));
            writer.addDocument(doc);
        } finally {
        	if (writer != null) writer.close();
        }
	}

	@Test
	public void testFeatures() throws IOException {
		TermFeatureExtractor extractor = 
				new TermFeatureExtractor("/Users/work/Downloads/lucene-test", "title", "text");
		FileWriter writer = null;
		try {
			writer = new FileWriter("/Users/work/Downloads/lucene-1-1.txt");
			extractor.extractTFIDF(null, writer);
		} finally {
			if (writer != null) writer.close();
		}
	}
}
