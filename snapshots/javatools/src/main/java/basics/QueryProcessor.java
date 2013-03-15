package basics;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javatools.administrative.Announce;
import javatools.administrative.D;
import javatools.database.Database;
import javatools.datatypes.PeekIterator;

/**
 * This class is part of the YAGO converters (http://mpii.de/yago). It is
 * licensed under the Creative Commons Attribution License
 * (http://creativecommons.org/licenses/by/3.0) by the YAGO team
 * (http://mpii.de/yago).
 * 
 * If you use this class for scientific purposes, please cite our paper Fabian
 * M. Suchanek, Gjergji Kasneci and Gerhard Weikum
 * "Yago - A Core of Semantic Knowledge" (WWW 2007)
 * 
 * This class implements a simple query processor for reification graphs as
 * described in the Yago journal paper.
 * 
 * @author Fabian M. Suchanek
 */
public class QueryProcessor implements Closeable {

	// ----------------------------------------------------
	// General classes and helper routines
	// ----------------------------------------------------

	/** maximal elements of an SQL query to retrieve */
	public final int MAXSQLRES = 1000;
	
	/** Holds the database*/
	protected Database db;

	/** Represents an sql command plus a sequence of variables */
	public static class SQLCommand {

		/** Holds the command */
		public String command;

		/** Holds the variables in the order of the columns of the command */
		public List<String> variables;

		public SQLCommand(String command, List<String> variables) {
			super();
			this.command = command;
			this.variables = variables;
		}

		@Override
		public String toString() {
			return command;
		}
	}

	public QueryProcessor(Database db2) {
		db=db2;
	}

	/** Tells whether a string starts with "?" */
	public static boolean isVariable(String s) {
		return (s.startsWith("?"));
	}

	/** Tells whether a string starts with "#" */
	public static boolean isFactId(String s) {
		return (s.startsWith("#"));
	}

	/** Resolves a variable with respect to a mapping or returns the name itself */
	public static String variableValue(String variableName,
			Map<String, String> variableValues) {
		String res = variableValues.get(variableName);
		if (res == null)
			return (variableName);
		else
			return (res);
	}

	// ----------------------------------------------------------------------
	// Parsing
	// ----------------------------------------------------------------------

	/** Returns e.g. "facts0.arg1" for a variable */
	public static String sqlIdentifier(String entity,
			Map<String, String> variables, int table, String column) {
	    if(isFactId(entity)) return(" f"+(entity.charAt(1)-'0'-1)+".id");
		if (!isVariable(entity))
			return (entity);
		if (variables.containsKey(entity))
			return (variables.get(entity));
		String sqlId = " f" + table + "." + column;
		variables.put(entity, sqlId);
		return (sqlId);
	}

	/** Adds "arg1=arg2" to a collection of conditions */
	public static void addCondition(Collection<String[]> conditions,
			String arg1, String arg2) {
		if (arg1.equals(arg2))
			return;
		conditions.add(new String[] { arg1, arg2 });
	}

	/** Adds a condition of the form "x=y" for a fact template */
	public static void addSqlCondition(Fact template, int table,
			Map<String, String> variables, Collection<String[]> conditions) {
		addCondition(conditions, " f" + table + ".arg1", sqlIdentifier(
				template.arg1, variables, table, "arg1"));
		addCondition(conditions, " f" + table + ".arg2", sqlIdentifier(
				template.arg2, variables, table, "arg2"));
		addCondition(conditions, " f" + table + ".relation", sqlIdentifier(
				template.relation, variables, table, "relation"));

	}

	/** Returns an sqlcommand for a list of templates */
	public SQLCommand sqlFor(List<Fact> templates) throws Basics.InvalidTripleException {

		// Collect all the conditions and all variables
		Map<String, String> variables = new TreeMap<String, String>();
		int numTables = 0;
		Collection<String[]> conditions = new ArrayList<String[]>();
		for (Fact template : templates) {
			if (Basics.virtualRelations.containsKey(template.relation))
				continue;
			addSqlCondition(template, numTables, variables, conditions);
			numTables++;
		}
		if (numTables == 0)
			throw new Basics.InvalidTripleException("No non-virtual relation");

		// Build the SELECT part of the SQL statement, collect all variables in order
		StringBuilder result = new StringBuilder("SELECT ");
		List<String> vars=new ArrayList<String>();
		for (String variable : variables.keySet()) {
			vars.add(variable);
			result.append(variables.get(variable)).append(", ");
		}
		if (variables.size() == 0)
			result.append(1); // SELECT 1 FROM...
		else
			result.setLength(result.length() - 2);

		// Build the FROM part
		result.append(" FROM ");
		for (int i = 0; i < numTables; i++)
			result.append("facts f").append(i).append(", ");
		if (numTables > 0)
			result.setLength(result.length() - 2);

		// Build the WHERE part
		result.append(" WHERE\n");
		for (String[] condition : conditions) {
			if (!condition[0].startsWith(" "))
				condition[0] = db.format(condition[0]);
			if (!condition[1].startsWith(" "))
				condition[1] = db.format(condition[1]);
			result.append(condition[0]).append('=').append(condition[1])
					.append(" AND\n");
		}
		if (conditions.size() > 0)
			result.setLength(result.length() - 5);
		return (new SQLCommand(db.limit(result.toString(), MAXSQLRES),
				vars));
	}

	/** Resolves MEANS and adds quotation marks where necessary */
	public static void resolveMeans(List<Fact> templates) {
		// We cannot use an iterator here, because we modify the list
		int numTemplates = templates.size();
		for (int i = 0; i < numTemplates; i++) {
			Fact template = templates.get(i);
			for (int a=1;a<3;a++) {
				String arg=TermExtractor.stripQuotes(template.getArg(a));
				if (arg.matches("\\d.*")
						|| isVariable(arg)
						|| arg.contains("_")
						|| isFactId(arg)) {
					continue;
				}
				if (Basics.ancestors(template.getDomainOfArg(a)).contains("yagoString")) {
					template.setArg(a, Normalize.string(arg));
					continue;
				}				
				String variable = "?"+template.getArg(a);
				Fact means=new Fact(Normalize.string(arg),"means",variable,"Means resolution");
				template.setArg(a,variable);
				templates.add(means);
			}
		}
	}

	/** Returns templates for a query */
	public static List<Fact> templatesFor(String input)
			throws Basics.InvalidTripleException {
		List<Fact> list = FactCollection.buildFacts(input, "query");
		resolveMeans(list);
		return (list);
	}

	// ------------------------------------------------------------------
	// Query solving
	// ------------------------------------------------------------------

	/** Checks the virtual relations in an answer */
	public static boolean checkVirtual(List<Fact> templates,
			Map<String, String> variables) throws Exception {
		for (Fact template : templates) {
			if (Basics.virtualRelations.containsKey(template.relation)) {
				Fact evalMe=new Fact(template);
				evalMe.arg1 = variableValue(template.arg1,
						variables);
				evalMe.arg2 = variableValue(template.arg2,
						variables);
				if (isVariable(evalMe.arg2) || isVariable(evalMe.arg1))
					throw new Basics.InvalidTripleException(
							"Unbound argument for virtual relation in " + template);
				if (!Basics.virtualHolds(evalMe))
					return (false);
			}
		}
		return (true);
	}

	/** Iterates over variable bindings for a list of templates */
	public PeekIterator<Map<String, String>> solutions(
			final List<Fact> templates) throws Basics.InvalidTripleException {

		final SQLCommand sql = sqlFor(templates);
		Announce.debug(templates,sql.command);
		return (new PeekIterator<Map<String, String>>() {

			ResultSet rs = null;

			public Map<String, String> internalNext() throws Exception {
				if (rs == null)
					rs = db.query(sql.command);
				Map<String, String> variables;
				do {
					if (!rs.next())
						return (null);
					variables = new TreeMap<String, String>();
					int i = 1;
					for (String var : sql.variables) {
						variables.put(var, rs.getString(i++));
					}
				} while (!checkVirtual(templates, variables));
				return variables;
			}

			@Override
			public void close() {
				Database.close(rs);
			}
		});
	}

	/** Returns up to n solutions for a query */
	public Set<Map<String, String>> solutions(List<Fact> templates, int n)
			throws Basics.InvalidTripleException {
		Set<Map<String, String>> solutions = new HashSet<Map<String, String>>();
		Iterator<Map<String, String>> it = solutions(templates);
		while (it.hasNext() && solutions.size() < n) {
			solutions.add(it.next());
		}
		return (solutions);
	}

	/** Returns the solutions for a query */
	public Iterator<Map<String, String>> solutions(String query)
			throws Basics.InvalidTripleException {
		return (solutions(FactCollection.buildFacts(query, "user")));
	}

	/** Returns up to n solutions for a query */
	public Set<Map<String, String>> solutions(String query, int n)
			throws Basics.InvalidTripleException {
		return (solutions(FactCollection.buildFacts(query, "user"), n));
	}

	public static void main(String[] args) throws Exception {
		Config.init(new File("yago.ini"));
		Database db = Config.getDatabase();
		if (db != null)
			new QueryProcessor(db).run();
	}

	/** Tests the Reification Query processor */
	public void run() throws Basics.InvalidTripleException {
		Announce.setLevel(Announce.Level.DEBUG);
		D.p("\nWelcome to the YAGO query processor\n");
		D.p("Example query: ");
		D.p("  \"Angela Merkel\" means ?x");
		D.p("  ?x type ?y");
		D.p("  #2 wasFoundIn ?f");
		while (true) {
			D
					.p("\n**** Enter query lines, followed by a blank line. Type blank line to quit.");
			String line = "";
			while (true) {
				String input = D.r().trim();
				if (input == null || input.length() == 0)
					break;
				line += "; " + input;
			}
			if (line.length() == 0)
				break;
			try {
				List<Fact> facts = templatesFor(line);
				D.p(facts);
				int count = 0;
				for (Map<String, String> solution : solutions(facts, 100)) {
					count++;
					D.p(solution);
				}
				if (count == 0) {
					D.println("No Results found");
				}
				D.println("");
			} catch (Exception e) {
				D.p(e.getMessage());
			}
		}
		Announce.doing("Closing database");
		db.close();
		Announce.done();
	}

	@Override
	public void close() throws IOException {
		db.close();
	}
}
