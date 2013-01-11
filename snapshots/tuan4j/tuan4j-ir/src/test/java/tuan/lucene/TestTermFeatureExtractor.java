package tuan.lucene;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
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

	private static final String LUCENE_DIR_MAC = "/Users/work/Downloads/lucene-test";
	private static final String LUCENE_OUT_FILE_MAC = "/Users/work/Downloads/lucene-1-1.txt";
	
	private static final String LUCENE_DIR_DEBIAN = "/home/tuan/Downloads/lucene";
	private static final String LUCENE_OUT_FILE_DEBIAN = "/home/tuan/Downloads/lucene-1-1.txt";
	
	@Before
	public void setUpIndex() throws IOException {
		Directory dir = FSDirectory.open(new File(LUCENE_DIR_DEBIAN));
		CharArraySet chars = new CharArraySet(Version.LUCENE_40, new ArrayList<String>(), false);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40, chars);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = null;
        try {
        	writer = new IndexWriter(dir, iwc);
            
            Document doc = new Document();
            doc.add(new VectorTextField("title", "gone with the wind", Field.Store.YES));
            doc.add(new VectorTextField("text", "and they live happily ever", Field.Store.YES));
            writer.addDocument(doc);
            
            doc = new Document();
            doc.add(new VectorTextField("title", "the wind and happiness", Field.Store.YES));
            doc.add(new VectorTextField("text", "they gone in happiness ever", Field.Store.YES));
            writer.addDocument(doc);
        } finally {
        	if (writer != null) writer.close();
        }
	}

	@Test
	public void testFeatures() throws IOException {
		FeatureExtractor extractor = 
				new FeatureExtractor(LUCENE_DIR_DEBIAN, "text", "title");
		FileWriter writer = null;
		try {
			writer = new FileWriter(LUCENE_OUT_FILE_DEBIAN);			
			extractor.extractTFIDF(null, writer);
			System.out.println("Vocabulary lists: ");
			extractor.printVocabulary(System.out);
		} finally {
			if (writer != null) writer.close();
		}
	}
}
