package tuan.lucene;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;

/** A wrapper for several exploring tasks for Lucene indices */


public class Explorer {

	private IndexReader index;

	public Explorer(String lucenePath) throws IOException {
		Directory dir = FSDirectory.open(new File(lucenePath));
		index = DirectoryReader.open(dir);
	}

	public void close() throws IOException {
		if (index != null) index.close();
	}

	public String getDocumentFullText(int docNo) throws IOException {
		Bits liveDocs = MultiFields.getLiveDocs(index);
		if (liveDocs == null || liveDocs.get(docNo)) {
			Document doc = index.document(docNo);
			StringBuilder sb = new StringBuilder();
			for (IndexableField field : doc.getFields()) {
				String name = field.name();
				sb.append(name);
				sb.append(":\n");
				sb.append(doc.get(name));
				sb.append("\n");
			}
			return sb.toString();
		} else return null;
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Options opts = new Options();
		OptionGroup optGrp = new OptionGroup();

		// Output stream and error stream
		Option outputStr =  OptionBuilder.withArgName("o").withLongOpt("output")
				.withDescription("When set, the system will redirect the output" +
						" stream to the file specified by this arguments")
						.hasArg()
						.create();
		opts.addOption(outputStr);

		Option errStr =  OptionBuilder.withArgName("e").withLongOpt("error")
				.withDescription("When set, the system will redirect the error" +
						" stream to the file specified by this arguments")
						.hasArg()
						.create();
		opts.addOption(errStr);

		// Option 1: load input lucene path
		Option lucenePath = OptionBuilder.withArgName("l").withLongOpt("lucene")
				.withDescription("Register lucene index path")
				.isRequired(true)
				.hasArg()				
				.create();
		optGrp.addOption(lucenePath);

		// Option 2: read document info
		Option docText = OptionBuilder.withArgName("d").withLongOpt("document")
				.withDescription("Show text of document with specified id")
				.hasArg()
				.create();
		optGrp.addOption(docText);

		opts.addOptionGroup(optGrp);

		Explorer explorer = null;
		String luceneLoc = null;

		// Parse the command line
		try {
			// check which script to be called
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(opts, args);


			// Print help message when command arguments are invalid
			if (cmd.getOptions().length == 0) {
				printHelp("arguments cannot be empty", opts);
				System.exit(-1);
			}			

			// Check the output / error configuration
			if (cmd.hasOption("output")) {
				String outStream = cmd.getOptionValue("output");
				PrintStream out = new PrintStream(new FileOutputStream(outStream));
				System.setOut(out);
			}

			if (cmd.hasOption("error")) {
				String errStream = cmd.getOptionValue("error");
				PrintStream err = new PrintStream(new FileOutputStream(errStream));
				System.setErr(err);
			}
			// Get mandatory argument values
			if (cmd.hasOption("lucene")) {
				luceneLoc = cmd.getOptionValue("lucene");
			} 
			else {
				printHelp("lucene index path has to be specified", opts);
				System.exit(-1);
			}

			// Run the individual task
			explorer = new Explorer(luceneLoc);

			// read document info
			if (cmd.hasOption("document")) {
				String docNo = cmd.getOptionValue("document");
				try {
					int i = Integer.parseInt(docNo);
					System.out.println(explorer.getDocumentFullText(i));
				} catch (NumberFormatException e) {
					printHelp("Document id must be an integer", opts);
					System.exit(-1);
				}
			}
			
			else {
				printHelp("command line syntax", opts);
				System.exit(-1);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				explorer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static final void printHelp(String msg, Options opts) {
		HelpFormatter help = new HelpFormatter();
		help.printHelp(msg, opts);
	}
}
