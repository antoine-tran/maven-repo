/**
 * 
 */
package edu.umd.cloud9.example.ppr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import tuan.hadoop.io.IntFloatArrayListWritable;
import tuan.hadoop.io.IntFloatListWritable;

/**
 * Represents either an node in the graph (for graph structure
 * message passing), or a finger print of one random walk
 * @author tuan
 *
 */
public class FingerPrint implements Writable, Cloneable {

	public static enum Type {
		
		// a graph node for graph structure message passing
		STRUCTURE((byte) 0),			
				
		// a fingerprint path
		FINGERPRINT((byte) 2),
		
		// Fingerprint of a finished walk
		FINISHED((byte) 3),
		
		// individual personalized pagerank estimated by one random walk
		INDIVIDUAL_PPR((byte) 4);
		
		private byte val;		
		private Type(byte val) {
			this.val = val;
		}
	};
	private static final Type[] MAPPING = 
			new Type[]{Type.STRUCTURE, Type.FINGERPRINT, Type.FINISHED};
	private Type type;
	
	// id of the beginning node in the path
	private int beginId;		
	
	// id of the ending node in the path (equal to end if node)
	private int endId;			
	
	// length of the random walk (0 if node)
	private int length = 0;	
	
	// the adjacency list (null for finger print)
	private IntFloatListWritable adjacencyList;
	
	public FingerPrint() {}

	public static FingerPrint newInstance(DataInput in) throws IOException {
		FingerPrint obj = new FingerPrint();
		obj.readFields(in);
		return obj;
	}
	
	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public IntFloatListWritable getAdjacencyList() {
		return adjacencyList;
	}
	
	public int[] getAdjacencyIds() {
		return adjacencyList.indices();
	}

	public void setAdjacencyList(IntFloatListWritable lst) {
		this.adjacencyList = lst;
	}

	public Type getType() {
		return type;
	}

	public int getBeginId() {
		return beginId;
	}

	public int getEndId() {
		return endId;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	public void setBeginId(int beginId) {
		this.beginId = beginId;
	}

	public void setEndId(int endId) {
		this.endId = endId;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeByte(type.val);
		out.write(beginId);
		out.write(endId);
		if (type == Type.STRUCTURE) {
			adjacencyList.write(out);
		}
		else if (type == Type.FINGERPRINT) {
			out.writeByte(length);
		}	
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		byte b = in.readByte();
		type = MAPPING[b];
		beginId = in.readInt();
		endId = in.readInt();
		if (type == Type.STRUCTURE) {
			adjacencyList = new IntFloatArrayListWritable();
			adjacencyList.readFields(in);
		}
		else if (type == Type.FINGERPRINT) {
			length = in.readByte();
		}		
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
}
