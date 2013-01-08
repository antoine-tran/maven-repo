package tuan.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneIndex {

	public static final IndexWriter createIndexWriter(Analyzer analyzer, String luceneIndexDir, OpenMode mode) throws IOException {
		Directory dir = FSDirectory.open(new File(luceneIndexDir));
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		iwc.setOpenMode(mode);
		return new IndexWriter(dir, iwc);
	}
}
