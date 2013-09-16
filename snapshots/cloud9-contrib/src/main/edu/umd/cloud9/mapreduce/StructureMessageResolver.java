package edu.umd.cloud9.mapreduce;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Reducer;


/**
 * A Hadoop reducer that accepts both content messages and one structure 
 * message, and update the title of all content messages with the 
 * info from the structure message.
 * CAVEAT: This reducer SHOULD ONLY be used when the number of reducer values 
 * is not too big, otherwise it will crash the heap memory
 * @author tuan
 */ 
public abstract class StructureMessageResolver<KEYIN, VALUEIN, KEYOUT, VALUEOUT> 
		extends Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
	
	@Override
	protected void reduce(KEYIN key, Iterable<VALUEIN> values, 
			Context context) throws IOException, InterruptedException {
		
		// before parsing the key
		setupTask(key, values, context);

		// the sentinel indicating whether we encounter the structure message
		// along the iterator
		VALUEIN smsg = null;
		
		for (VALUEIN value : values) {
			if (checkStructureMessage(key, value, context)) smsg = clone(value);		

			// items before the structure message in the iterator will
			// be copied and be emitted later
			else if (smsg == null) messageBeforeHits(key, value);
			
			// items after the structure message will be checked and emitted
			// immediately
			else messageAfterHits(context, key, smsg, value);
		}

		// No structure messages found
		if (smsg == null) flushWithoutHit(context);
				
		// second run: update the remaining messages with actual content from
		// the structure messages
		else {
			Iterable<VALUEIN> cache = tempValuesCache();
			if (cache != null) {
				for (VALUEIN v : cache) {
					messageAfterHits(context, key, smsg, v);
				}
			}
		}
	}	
	
	/** implement structure message checking logic. The output key might be updated
	 * right away 
	 * @throws InterruptedException 
	 * @throws IOException */
	public abstract boolean checkStructureMessage(KEYIN key, VALUEIN msg, Context context) 
			throws IOException, InterruptedException;
		
	/** Clone the value */
	public abstract VALUEIN clone(VALUEIN obj);
	
	/** set up operations before a reduce task */
	public abstract void setupTask(KEYIN key, Iterable<VALUEIN> values, Context context);
	
	/** clone the current message for possible subsequent emission */
	// public abstract VALUEIN clone(VALUEIN t);
	
	/** after the structure message in known, subsequent messages are all content messages.
	 * Apply some post-checking logics and emit them immediately if passed.
	 * NOTE: At this point, structure message cannot be null !! */
	public abstract void messageAfterHits(Context context, KEYIN key, VALUEIN structureMsg, 
		VALUEIN msg) throws IOException, InterruptedException;
	
	/** what to do if no structure messages found ? */
	public abstract void flushWithoutHit(Context context) throws IOException, 
		InterruptedException; 
	
	/** cache the content message arriving before the structure message (before the hit) */
	public abstract void messageBeforeHits(KEYIN key, VALUEIN value);
	
	/** access to the cache of messages arriving before the structure message (before
	 *  the hit), or null if no structure message found */
	public abstract Iterable<VALUEIN> tempValuesCache(); 
}
