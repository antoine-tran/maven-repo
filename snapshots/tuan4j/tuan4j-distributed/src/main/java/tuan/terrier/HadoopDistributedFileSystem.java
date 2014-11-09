package tuan.terrier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;

import tuan.terrier.Files.FSCapability;

/** Wrapper of access to an HDFS via Terrier's FileSystem API */
public class HadoopDistributedFileSystem implements FileSystem {
	
	public static final String HDFS_SCHEME = "hdfs";
	
	org.apache.hadoop.fs.FileSystem DFS;
	
	// When having no configuration, create new one
	public HadoopDistributedFileSystem() throws IOException {
		DFS = org.apache.hadoop.fs.FileSystem.get(new Configuration());
	}
	
	public HadoopDistributedFileSystem(Configuration conf) throws IOException {
		DFS = org.apache.hadoop.fs.FileSystem.get(conf);
	}
	
	public String name()
	{
		return HDFS_SCHEME;
	}

	/** capabilities of the filesystem */
	public byte capabilities() 
	{
		return FSCapability.READ | FSCapability.WRITE | FSCapability.RANDOM_READ 
			| FSCapability.STAT | FSCapability.DEL_ON_EXIT | FSCapability.LS_DIR;
	}
	public String[] schemes() { return new String[]{"dfs", "hdfs"}; }

	/** returns true if the path exists */
	public boolean exists(String filename) throws IOException
	{
		return DFS.exists(new Path(filename));
	}

	/** open a file of given filename for reading */
	public InputStream openFileStream(String filename) throws IOException
	{
		return DFS.open(new Path(filename));
	}
	/** open a file of given filename for writing */
	public OutputStream writeFileStream(String filename) throws IOException
	{
		return DFS.create(new Path(filename));
	}

	public boolean mkdir(String filename) throws IOException
	{
		return DFS.mkdirs(new Path(filename));
	}

	public RandomDataOutput writeFileRandom(String filename) throws IOException
	{
		throw new IOException("HDFS does not support random writing");
	}

	public RandomDataInput openFileRandom(String filename) throws IOException
	{
		return new HadoopFSRandomAccessFile(DFS, filename);
	}

	public boolean delete(String filename) throws IOException
	{
		return DFS.delete(new Path(filename), true);
	}

	public boolean deleteOnExit(String filename) throws IOException
	{
		return DFS.deleteOnExit(new Path(filename));
	}

	public String[] list(String path) throws IOException
	{
		final FileStatus[] contents = DFS.listStatus(new Path(path));
		if (contents == null)
			throw new FileNotFoundException("Cannot list path " + path);
		final String[] names = new String[contents.length];
		for(int i=0; i<contents.length; i++)
		{
			names[i] = contents[i].getPath().getName();
		}
		return names;
	}

	public String getParent(String path) throws IOException
	{
		return new Path(path).getParent().getName();
	}

	public boolean rename(String source, String destination) throws IOException
	{
		return DFS.rename(new Path(source), new Path(destination));
	} 

	public boolean isDirectory(String path) throws IOException
	{
		return DFS.getFileStatus(new Path(path)).isDir();
	}

	public long length(String path) throws IOException
	{
		return DFS.getFileStatus(new Path(path)).getLen();
	}

	public boolean canWrite(String path) throws IOException
	{
		return DFS.getFileStatus(new Path(path)).getPermission().getUserAction().implies(FsAction.WRITE);
	}

	public boolean canRead(String path) throws IOException
	{
		return DFS.getFileStatus(new Path(path)).getPermission().getUserAction().implies(FsAction.READ);
	}
}
