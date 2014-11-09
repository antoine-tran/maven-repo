package tuan.terrier;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;

public class HadoopFSRandomAccessFile implements RandomDataInput
{
	FSDataInputStream in;
	org.apache.hadoop.fs.FileSystem fs;
	String filename;

	public HadoopFSRandomAccessFile(org.apache.hadoop.fs.FileSystem _fs, String _filename) throws IOException
	{
		this.fs = _fs;
		this.filename = _filename;
		this.in = _fs.open(new Path(_filename));
	}

	public int read() throws IOException
	{
		return in.read();
	}

	public int read(byte b[], int off, int len) throws IOException
	{
		return in.read(in.getPos(), b, off, len);
	}

	public int readBytes(byte b[], int off, int len) throws IOException
	{
		return in.read(in.getPos(), b, off, len);
	}

	public void seek(long pos) throws IOException
	{
		in.seek(pos);
	}

	public long length() throws IOException
	{
		return fs.getFileStatus(new Path(filename)).getLen();
	}

	public void close() throws IOException
	{
		in.close();
	}
	
	// implementation from RandomAccessFile
	public final double readDouble() throws IOException {
		return in.readDouble();
	}

	public final int readUnsignedShort() throws IOException {
		return in.readUnsignedShort();
	}

	public final short readShort() throws IOException {
		return in.readShort();
	}

	public final int readUnsignedByte() throws IOException {
		return in.readUnsignedByte();
	}

	public final byte readByte() throws IOException {
		return in.readByte();
	}

	public final boolean readBoolean() throws IOException {
		return in.readBoolean();
	}
	
	public final int readInt() throws IOException {
	return in.readInt();
	}

	public final long readLong() throws IOException {
		return in.readLong();
	}
	
	public final float readFloat() throws IOException {
	return in.readFloat();
	}

	public final void readFully(byte b[]) throws IOException {
	in.readFully(b);
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
	in.readFully(b,off,len);
	}

	public int skipBytes(int n) throws IOException {
	return in.skipBytes(n);
	}

	public long getFilePointer() throws IOException
	{
		return in.getPos();
	}
	public final char readChar() throws IOException {
		return in.readChar();
	}

	public final String readUTF() throws IOException {
		return in.readUTF();
	}

	public final String readLine() throws IOException {
		return in.readLine();
	}
}
