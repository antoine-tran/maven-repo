package org.wikipedia.pig;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

/**
 * A custom eval function that wraps the Pig's eval func
 * and allows user to handle the raw content of Wikipedia page
 */
public abstract class PageFunc<T> extends EvalFunc<T> {

	@Override	
	public final T exec(Tuple tuple) throws IOException {
		return parse(Long.parseLong((String) tuple.get(0)), (String)tuple.get(1), 
				(String)tuple.get(2));
	}
	
	public abstract T parse(long pageId, String pageTitle, String pageContent);
}
