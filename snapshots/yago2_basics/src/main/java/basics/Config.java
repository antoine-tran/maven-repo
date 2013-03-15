package basics;

import java.io.File;
import java.io.IOException;

import javatools.administrative.Announce;
import javatools.administrative.D;
import javatools.administrative.Parameters;
import javatools.database.Database;
import javatools.database.MySQLDatabase;
import javatools.database.OracleDatabase;
import javatools.database.PostgresDatabase;

/**
 * Initialization for YAGO
 * 
 * @author Fabian M. Suchanek
 * 
 */
public class Config {

	/** TRUE if we have initialized */
	public static boolean initialized = false;

	/** Initializes from the ini file (NULL=yago.ini) */
	public static void init(File iniFile) {
		if (initialized)
			return;
		if (iniFile == null) {
			iniFile = new File("yago.ini");
			if(!iniFile.exists()) {
				Announce.doing("Creating ini file",iniFile);
				try {
					iniFile.createNewFile();
				} catch (IOException e) {
					Announce.error("Cannot create file:",e.getMessage());
				}
				Announce.done();
			}
		}
		try {
			Announce.doing("Initializing from", iniFile);
			Parameters.init(iniFile);
			Announce.done();
			Announce.message("Current folder is",new File(".").getCanonicalFile());
			File basicsFolder = Parameters
					.getOrRequestFileParameter("yagoBasicsFolder",
							"Please enter the folder with the YAGO relation definitions");
			Basics.loadFrom(basicsFolder);
			Parameters.add("yagoBasicsFolder", basicsFolder.toString());
		} catch (IOException e) {
			Announce.error("Initialization failed:", e.getMessage());
		}
		initialized = true;
	}

	/** Returns the database */
	public static Database getDatabase() throws Exception {
		Config.init(null);
		Database database = null;
		String dbName = null;
		while (database == null) {
			dbName = Parameters.getOrRequest("databaseSystem",
					"Which Database are you using? (Oracle, MySQL, Postgres)")
					.toLowerCase();
			if (dbName.equals("oracle"))
				database = new OracleDatabase();
			if (dbName.equals("postgres"))
				database = new PostgresDatabase();
			if (dbName.equals("mysql"))
				database = new MySQLDatabase();
			Parameters.add("databaseSystem", dbName);
		}
		if (!database.jarAvailable()) {
			D.pl("You are missing the JDBC jar file for this database.");
			D.pl("You have to download it from the database provider.");
			D
					.pl("Then, restart the converter with the jar file as an argument.");
			D.pl("See readme_converters.txt for details.\n");
			D.exit();
		}
		try {
			String user = Parameters.getOrRequestAndAdd("databaseUser",
					"Please enter the database user name");
			String password = Parameters.getOrRequestAndAdd("databasePassword",
					"Please enter the database password");
			String host = Parameters.getOrRequestAndAdd("databaseHost",
					"Please enter the database host");
			if (host.length() == 0)
				host = null;
			Announce.doing("Connecting to database");
			if (dbName.equals("mysql")) {
				String schema = Parameters.getOrRequestAndAdd(
						"databaseDatabase", "Please enter the MySQL database");
				database = new MySQLDatabase(user, password, schema, host, null);
			}
			if (dbName.equals("postgres")) {
				String schema = Parameters.getOrRequestAndAdd("databaseSchema",
						"Please enter the Postgres database schema");
				database = new PostgresDatabase(user, password, schema, host,
						null);
			}
			if (dbName.equals("oracle")) {
				String sid = Parameters.getOrRequestAndAdd("databaseSID",
						"Please enter the Oracle database SID (service id)");
				database = new OracleDatabase(user, password, host, null, sid);
			}
			Announce.done();
			return (database);
		} catch (Exception e) {
			throw e;
		}
	}

}
