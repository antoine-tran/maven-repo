/**
 * ==================================
 * 
 * Copyright (c) 2010 Anh Tuan Tran
 *
 * URL: http://www.mpi-inf.mpg.de/~attran,
 *          http://www.l3s.de/~ttran
 *
 * Email: tranatuan24@gmail.com
 * ==================================
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 */
package edu.stanford.nlp.io;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * <p>This class implements the "Null Object Design Pattern" (see details in
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Wikipedia's 
 * article on Null Object pattern</a> or in Woolf, Bobby (1998). 
 * "Null Object". In Martin, Robert; Riehle, Dirk; 
 * Buschmann, Frank. <i>Pattern Languages of Program Design 3</i>. Addison-Wesley).</p>
 *  
 * <p>NOTE: This implementation makes use of singleton pattern to save heap memory and
 * to support the idea that a null object is unique. In addition, it can be serialized
 * to external storage. </p> 
 * 
 * @author tuan
 */
@SuppressWarnings("serial")
public class SerializableNull implements Serializable {

	//Singleton object of the class
	private static final SerializableNull nullObj = new SerializableNull();
	
	//singleton constructor
	private SerializableNull() {}
	
	//serialize method
	public static final void serialize(ObjectOutput output) throws IOException {
		output.writeObject(nullObj);
	}

	public static final SerializableNull obj() {
		return nullObj;
	}
}
