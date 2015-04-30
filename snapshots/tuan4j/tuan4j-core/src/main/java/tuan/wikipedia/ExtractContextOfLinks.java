package tuan.wikipedia;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import tuan.io.FileUtility;

public class ExtractContextOfLinks {

	private static final Pattern ANCHOR = Pattern.compile("<a href=\"(.*?)\".*?>(.*?)</a>");
	private static final Pattern WHITE_SPACE = Pattern.compile("\\s+");

	public static void main(String[] args) throws IOException {
		Writer writer = null;
		try {
			writer = new FileWriter(args[1]);
			StringBuilder txt = new StringBuilder();
			for (String line : FileUtility.readLines(args[0])) {
				int idx1 = line.indexOf("<page>");
				int idx2 = line.indexOf("</doc></page>");
				if (idx1 >= 0) {
					txt = new StringBuilder();
				}
				txt.append(line);
				txt.append("\n");
				if (idx2 >= 0) {
					String raw = txt.toString();
					txt = null;
					// extract(raw, writer);
					safeExtract(raw, writer);
				}
			}
		} finally {
			writer.close();
		}
	}

	private static void extract(String raw, Writer writer) throws IOException {		

		// because we heavily rely on white spaces to detect context, we
		// must make sure there are spaces between the anchors
		// raw = raw.replace("<a href", " <a href");
		// raw = raw.replace("</a>", "</a> ");

		int i = raw.indexOf("id=\"");
		int j = raw.indexOf("\"",i+4);
		String docid = raw.substring(i+4,j);

		i = raw.indexOf("title=\"",j+1);
		j = raw.indexOf("\">",i+7);
		String title = raw.substring(i+7,j);

		i = j+1;
		j = raw.indexOf("</doc></page>", raw.length() - 20);

		// Every presence of an anchor triggering one emit
		List<Anchor> anchorOffsets = new ArrayList<Anchor>();

		// the number of words read so far. It is also the
		// position of the next anchor if there are no words
		// in between it and the previous anchor
		int wordCnt = 0;

		// We tokenize between the links, so "end" also
		// points to the beginning offset of the next chunk
		// of text
		int start = i+1, end = start;

		// We use a list of text buffers to cache the contexts
		List<ArrayList<String>> pre = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> pos = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> anchors = new ArrayList<ArrayList<String>>();

		// First scan: Get the offsets of the anchors, including starting
		// word offset and ending word offset of the anchors
		// System.out.println("Register anchor matcher....");
		Matcher anchorFinder = ANCHOR.matcher(raw);

		// System.out.println("Register whitespace matcher....");
		Matcher spaceFinder = WHITE_SPACE.matcher(raw);

		while (anchorFinder.find()) {
			start = anchorFinder.start();
			String target = anchorFinder.group(1);
			String anchor = anchorFinder.group(2);
			int anchorCnt = anchor.split("\\s+").length;


			// no. of spaces of the text before the two consecutive anchors. The
			// no. of words = tmoCnt+1
			int tmpCnt = 0;
			if (end < start) {
				int tmpEnd = end;
				while (spaceFinder.find(tmpEnd)) {
					if (spaceFinder.end() == start) {
						break;
					}
					int tmpStart = spaceFinder.start();
					if (tmpStart != end) {
						tmpCnt++;
					}
					tmpEnd = spaceFinder.end();
				}
			}
			Anchor ip = new Anchor(start + tmpCnt + 1, anchorCnt, target + "\t" + anchor);
			anchorOffsets.add(ip);
			end = anchorFinder.end();
			wordCnt = wordCnt + tmpCnt + 1 + anchorCnt;
		}

		// Second scan: Grab the context from the plain text
		if (anchorOffsets.size() == 0) {
			return;
		}

		int wordPos = -1;

		for (int k = 0; k < anchorOffsets.size(); k++) {
			pre.add(new ArrayList<String>());
			pos.add(new ArrayList<String>());
			anchors.add(new ArrayList<String>());
		}

		System.out.println("Pre size: " + pre.size() + ". Pos size: " + pos.size() + ". Anchor size: " + anchorOffsets.size());

		raw = raw.replaceAll("<a href=\"(.*?)\".*?>|</a>", "");
		spaceFinder = WHITE_SPACE.matcher(raw);
		int spaceBegin = i+1, spaceEnd = i+1;
		while (spaceFinder.find(spaceEnd)) {
			if (spaceFinder.end() == j) {
				break;
			}
			spaceBegin = spaceFinder.start();
			if (spaceBegin != 0) {
				wordPos++;
				String word = raw.substring(spaceEnd, spaceBegin);
				for (int k = 0; k < anchorOffsets.size(); k++) {
					IntPair a = anchorOffsets.get(k);
					int dist = wordPos - a.getLeft() - a.getRight();
					if (dist >= 0 && dist < 50) {
						pos.get(k).add(word);
					}
					dist = a.getLeft() - wordPos;
					if (dist >= 0 && dist < 50) {
						pre.get(k).add(word);
					}
					if (a.getLeft() <= wordPos && a.getLeft() 
							+
							a.getRight() > wordPos) {
						anchors.get(k).add(word);
					}
				}		
			}
			spaceEnd = spaceFinder.end();
		}



		// Finally emit the contexts
		for (int k = 0; k < anchorOffsets.size(); k++) {
			StringBuilder sb = new StringBuilder();
			sb.append(docid);
			sb.append("\t");
			sb.append(title);
			sb.append("\t");
			sb.append(anchorOffsets.get(k).text);
			sb.append("\t");
			for (String w : pre.get(k)) {
				sb.append(w);
				sb.append(' ');
			}
			sb.append("\t");
			for (String w : pos.get(k)) {
				sb.append(w);
				sb.append(' ');
			}
			writer.write(sb.toString());
			writer.write("\n");
		}
	}
	
	private static void safeExtract(String raw, Writer writer) throws IOException {		

		// because we heavily rely on white spaces to detect context, we
		// must make sure there are spaces between the anchors
		// raw = raw.replace("<a href", " <a href");
		// raw = raw.replace("</a>", "</a> ");

		int i = raw.indexOf("id=\"");
		int j = raw.indexOf("\"",i+4);
		String docid = raw.substring(i+4,j);

		i = raw.indexOf("title=\"",j+1);
		j = raw.indexOf("\">",i+7);
		String title = raw.substring(i+7,j);

		i = j+1;
		j = raw.indexOf("</doc></page>", raw.length() - 20);

		// Every presence of an anchor triggering one emit
		List<Anchor> anchorOffsets = new ArrayList<Anchor>();

		// the number of words read so far. It is also the
		// position of the next anchor if there are no words
		// in between it and the previous anchor
		int wordCnt = 0;

		// We tokenize between the links, so "end" also
		// points to the beginning offset of the next chunk
		// of text
		int start = i+1, end = start;

		// We use a list of text buffers to cache the contexts
		List<ArrayList<String>> pre = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> pos = new ArrayList<ArrayList<String>>();
		List<ArrayList<String>> anchors = new ArrayList<ArrayList<String>>();

		// First scan: Get the offsets of the anchors, including starting
		// word offset and ending word offset of the anchors
		// System.out.println("Register anchor matcher....");
		Matcher anchorFinder = ANCHOR.matcher(raw);

		// System.out.println("Register whitespace matcher....");
		Matcher spaceFinder = WHITE_SPACE.matcher(raw);

		while (anchorFinder.find()) {
			start = anchorFinder.start();
			end = anchorFinder.end();
			String target = anchorFinder.group(1);
			String anchor = anchorFinder.group(2);

			Anchor ip = new Anchor(start, end, target + "\t" + anchor);
			anchorOffsets.add(ip);
		}

		// Second scan: Grab the context from the plain text
		if (anchorOffsets.size() == 0) {
			return;
		}

		int wordPos = -1;

		for (int k = 0; k < anchorOffsets.size(); k++) {
			pre.add(new ArrayList<String>());
			pos.add(new ArrayList<String>());
			anchors.add(new ArrayList<String>());
		}
		
		for (int k = 0; k < anchorOffsets.size(); k++) {
			int startO = anchorOffsets.get(k).getLeft();
			if (startO > 0) {
				String[] preText = raw.substring(0, startO).replaceAll("<a href=\"(.*?)\".*?>|</a>", "").split("\\s+");
				if (preText.length < 50) {
					for (String s : preText) pre.get(k).add(s);
				}
				else {
					for (int m = preText.length - 50; m < preText.length; m++) {
						pre.get(k).add(preText[m]);
					}
				}
			}
			int endO = anchorOffsets.get(k).getRight();
			if (endO > 0 && endO < j) {
				String[] posText = raw.substring(endO+1, j).replaceAll("<a href=\"(.*?)\".*?>|</a>", "").split("\\s+");
				if (posText.length < 50) {
					for (String s : posText) pos.get(k).add(s);
				}
				else {
					for (int m = 0; m < 50; m++) {
						pos.get(k).add(posText[m]);
					}
				}
			}
		}

		/*System.out.println("Pre size: " + pre.size() + ". Pos size: " + pos.size() 
				+ ". Anchor size: " + anchorOffsets.size());*/

		// raw = raw.replaceAll("<a href=\"(.*?)\".*?>|</a>", "");
		// spaceFinder = WHITE_SPACE.matcher(raw);
		
		/*int spaceBegin = i+1, spaceEnd = i+1;
		while (spaceFinder.find(spaceEnd)) {
			if (spaceFinder.end() == j) {
				break;
			}
			spaceBegin = spaceFinder.start();
			if (spaceBegin != 0) {
				wordPos++;
				String word = raw.substring(spaceEnd, spaceBegin);
				for (int k = 0; k < anchorOffsets.size(); k++) {
					IntPair a = anchorOffsets.get(k);
					int dist = wordPos - a.getLeft() - a.getRight();
					if (dist >= 0 && dist < 50) {
						pos.get(k).add(word);
					}
					dist = a.getLeft() - wordPos;
					if (dist >= 0 && dist < 50) {
						pre.get(k).add(word);
					}
					if (a.getLeft() <= wordPos && a.getLeft() 
							+
							a.getRight() > wordPos) {
						anchors.get(k).add(word);
					}
				}		
			}
			spaceEnd = spaceFinder.end();
		}*/



		// Finally emit the contexts
		for (int k = 0; k < anchorOffsets.size(); k++) {
			StringBuilder sb = new StringBuilder();
			sb.append(docid);
			sb.append("\t");
			sb.append(title);
			sb.append("\t");
			sb.append(anchorOffsets.get(k).text);
			sb.append("\t");
			for (String w : pre.get(k)) {
				sb.append(w);
				sb.append(' ');
			}
			if (pre.get(k).isEmpty()) {
				sb.append("[NULL]");
			}
			sb.append("\t");
			for (String w : pos.get(k)) {
				sb.append(w);
				sb.append(' ');
			}
			if (pos.get(k).isEmpty()) {
				sb.append("[NULL]");
			}
			writer.write(sb.toString());
			writer.write("\n");
		}
	}


	private static final class Anchor extends IntPair {
		String text;

		public Anchor(int l, int r, String t) {
			super(l,r);
			text = t;
		}
	}


	private static class IntPair {

		private int left, right;

		public IntPair() {}

		public IntPair(int left, int right) {
			this.left = left;
			this.right = right;
		}

		/**
		 * @return the left
		 */
		public int getLeft() {
			return left;
		}

		/**
		 * @param left the left to set
		 */
		public void setLeft(int left) {
			this.left = left;
		}

		/**
		 * @return the right
		 */
		public int getRight() {
			return right;
		}

		/**
		 * @param right the right to set
		 */
		public void setRight(int right) {
			this.right = right;
		}
	}
}
