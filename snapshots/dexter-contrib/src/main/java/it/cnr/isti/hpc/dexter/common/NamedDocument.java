package it.cnr.isti.hpc.dexter.common;

import java.io.File;

/**
 * A named document is just a flat document with identifier
 * @author tuan
 *
 */
public class NamedDocument extends FlatDocument {

	private String docname;
	
	public NamedDocument() {}

	public NamedDocument(String docname, String text) {
		super(text);
		this.docname = docname;
	}
	
	public NamedDocument(File file) {
		super(file);
		this.docname = file.getName();
	}
	
	/**
	 * @return the docname
	 */
	public String getDocname() {
		return docname;
	}

	/**
	 * @param docname the docname to set
	 */
	public void setDocname(String docname) {
		this.docname = docname;
	}
}
