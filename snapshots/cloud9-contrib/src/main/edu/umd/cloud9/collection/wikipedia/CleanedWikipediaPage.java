package edu.umd.cloud9.collection.wikipedia;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;

import tuan.hadoop.io.IntPair;

/**
 * The content of a Wikipedia page cleaned by WikiExtractor, 
 * with an offsets of the link anchors
 * @author tuan
 *
 */
public class CleanedWikipediaPage extends Text {

	/** 
	 * Static list of anchors sorted in order of their presence
	 * in the corresponding Wikipedia page 
	 */
	private transient IntPair[] anchors;
	
	@Override
	public void readFields(DataInput in) throws IOException {
		super.readFields(in);
		int size = in.readInt();
		anchors = new IntPair[size];
		for (int i=0; i<size; i++) {
			int l = in.readInt();
			int r = in.readInt();
			anchors[i] = new IntPair(l, r);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeInt(anchors.length);
		for (int i=0; i<anchors.length; i++) {
			out.writeInt(anchors[i].getLeft());
			out.writeInt(anchors[i].getRight());
		}
	}
	
	public IntPair anchor(int idx) {
		return anchors[idx];
	}
	
	public void extractAnchors() {
		String raw = toString();
		int i = raw.indexOf("\">\n");
		int j = raw.indexOf("</doc></page>", raw.length() - 20);
	}
	
	public static void main(String[] args) {
		/*Matcher m = ANCHOR.matcher("hello world <a href=\"http://yahoo.com/img1.jpg\" alt=\"\"> omg </a> fdsfjls hello world <a href=\"http://yahoo.com/img1.jpg\" alt=\"\"> omg </a>");
		while (m.find()) {
			System.out.println(m.start() + "\t" + m.end() + "\t" + m.group(1) + "\t" + m.group(2));	
		}
		  
		
		String s = "hello world <a href=\"http://yahoo.com/img1.jpg\" alt=\"\">"
				.replaceAll("<a href=\"(.*?)\".*?>", 
				"");
		
		System.out.println(s);*/
		
		Pattern wp = Pattern.compile("\\s+");
		Matcher m = wp.matcher("hello world   yahoo\t google \n sfso   fsdfh da");
		int count = 0;
		while (m.find()) {
			count++;
		}
		System.out.println(count);
		
		String t = "hello world <a href=\"http://yahoo.com/img1.jpg\" alt=\"\"> omg </a> fdsfjls hello world <a href=\"http://yahoo.com/img1.jpg\" alt=\"\"> omg </a>";
		System.out.println(t.replaceAll("<a href=\"(.*?)\".*?>|</a>", ""));
	}
}
