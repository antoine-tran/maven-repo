package basics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.DataFormatException;

import javatools.administrative.Announce;
import javatools.filehandlers.FileLines;
import basics.Basics.InvalidTripleException;

/**
 * Class FactCollection
 * 
 * Represents a collection of facts, indexes them
 * 
 * @author Fabian M. Suchanek
 */
public class FactCollection implements Iterable<Fact> {

	private static final long serialVersionUID = -1L;

	/** Holds the facts */
	protected Set<Fact> facts;
	/** Maps first arg to relation to facts */
	protected Map<String, Map<String, List<Fact>>> index = Collections.synchronizedMap(new TreeMap<String, Map<String, List<Fact>>>());
	/** Maps relation to facts */
	protected Map<String, List<Fact>> relindex = Collections.synchronizedMap(new TreeMap<String, List<Fact>>());

	public synchronized boolean add(Fact fact) {
		if (facts.contains(fact)) {
			Announce.debug("Duplicate fact not added:",fact);
			return (false);
		}
		if (fact.arg1.equals(fact.arg2)) {
			Announce.debug("Identical arguments not added",fact);
			return (false);
		}
		if (!Basics.isRelation(fact.relation))
			Announce.warning("Undefined relation", fact);
		if (fact.polarity == false)
			return (remove(fact));
		if (Basics.isFunction(fact.relation)) {
			List<Fact> existing = get(fact.arg1, fact.relation);
			if (existing.size() > 0) {
				Fact exist = existing.get(0);
				if (exist.arg2.contains("#") && !fact.arg2.contains("#")) {
					Announce.debug("Removing existing fact",exist,"for more specific",fact);
					remove(exist);
				} else {
					Announce.debug("Functional fact already exists and was not added:",fact,existing);					
					return (false);
				}
			}
		}
		facts.add(fact);
		if (!index.containsKey(fact.arg1))
			index.put(fact.arg1, Collections.synchronizedMap(new TreeMap<String, List<Fact>>()));
		if (!index.get(fact.arg1).containsKey(fact.relation))
			index.get(fact.arg1).put(fact.relation, Collections.synchronizedList(new ArrayList<Fact>(1)));
		index.get(fact.arg1).get(fact.relation).add(fact);
		if (!relindex.containsKey(fact.relation))
			relindex.put(fact.relation, Collections.synchronizedList(new ArrayList<Fact>(1)));
		relindex.get(fact.relation).add(fact);
		return (true);
	}

	/** Empty list */
	protected static final List<Fact> EMPTY = new ArrayList<Fact>(0);

	/** Returns facts with matching first arg and relation */
	public List<Fact> get(String arg1, String relation) {
		if (!index.containsKey(arg1))
			return (EMPTY);
		if (!index.get(arg1).containsKey(relation))
			return (EMPTY);
		return (index.get(arg1).get(relation));
	}

	/** Returns facts with matching relation */
	public List<Fact> get(String relation) {
		if (!relindex.containsKey(relation))
			return (EMPTY);
		return (relindex.get(relation));
	}

	/** Returns facts about another fact (slow)*/
	public List<Fact> about(Fact me) {
      List<Fact> result=new ArrayList<Fact>(2);
      for(Fact f : facts) {
    	  if(f.arg1fact!=null && f.arg1fact.equals(me)) result.add(f);
      }
      return(result);
	}
	
	/**
	 * Returns second argument of first fact with matching first arg and
	 * relation
	 */
	public String getArg2(String arg1, String relation) {
		if (!index.containsKey(arg1))
			return (null);
		if (!index.get(arg1).containsKey(relation))
			return (null);
		List<Fact> facts = index.get(arg1).get(relation);
		if (facts.size() < 1)
			return (null);
		Fact fact = facts.get(0);
		return (fact.arg2);
	}

	/** Returns second argument of facts with matching first arg and relation */
	public List<String> getArg2s(String arg1, String relation) {
		if (!index.containsKey(arg1))
			return (new ArrayList<String>());
		if (!index.get(arg1).containsKey(relation))
			return (new ArrayList<String>());
		List<Fact> facts = index.get(arg1).get(relation);
		List<String> result = new ArrayList<String>(facts.size());
		for (Fact f : facts)
			result.add(f.arg2);
		return (result);
	}

	/** Returns first arguments of facts with matching second arg and relation */
	public List<String> getArg1s(String relation, String arg2) {
		if (!relindex.containsKey(relation))
			return (null);
		List<String> result = new ArrayList<String>();
		for (Fact fact : relindex.get(relation)) {
			if (fact.arg2.equals(arg2))
				result.add(fact.arg1);
		}
		return (result);
	}

	/** Loads from TSV file */
	public FactCollection(File tsvFile, String technique) throws IOException {
	  facts = Collections.synchronizedSet(new HashSet<Fact>());
		loadFrom(tsvFile, technique);
	}

	public FactCollection() {
		facts = Collections.synchronizedSet(new HashSet<Fact>());
	}

	public FactCollection(int capacity) {
		facts = Collections.synchronizedSet(new HashSet<Fact>(capacity));
	}

	/** Adds for positive facts, removes else */
	public synchronized boolean add(Iterable<Fact> facts) {
		boolean change = false;
		for (Fact f : facts)
			change |= add(f);
		return (change);
	}

	public synchronized boolean remove(Object f) {
		if (!facts.remove(f))
			return (false);
		Fact fact = (Fact) f;
		index.get(fact.arg1).get(fact.relation).remove(fact);
		relindex.get(fact.relation).remove(fact);
		return (true);
	}

	public void clear() {
		facts.clear();
		index.clear();
		relindex.clear();
	}

	/** TRUE if all facts hold in the union of the collections */
	public static boolean hold(List<Fact> formulae,
			FactCollection... factCollections) {
		for (Fact formula : formulae)
			if (!holds(formula, factCollections))
				return (false);
		return (true);
	}
	
	/** TRUE if the fact exists in the union of the collections */
	public static boolean holdsPositive(Fact formula,
			FactCollection... factCollections) {
		if (Basics.virtualHolds(formula))
			return (true);
		if (formula.arg1.equals("$")) {
			for (FactCollection fc : factCollections) {
				if (fc.getArg1s(formula.relation, formula.arg2).size() != 0)
					return (true);
			}
		} else if (formula.arg2.equals("$")) {
			for (FactCollection fc : factCollections) {
				if (fc.getArg2s(formula.arg1, formula.relation).size() != 0)
					return (true);
			}
		} else {
			for (FactCollection fc : factCollections) {
				if (fc.contains(formula))
					return (true);
			}
		}
		return (false);
	}

	private boolean contains(Fact formula) {
    return facts.contains(formula);
  }

  /** TRUE if the facts holds in the union of the collections */
	public static boolean holds(Fact formula, FactCollection... factCollections) {
		return (holdsPositive(formula, factCollections) == formula.polarity);
	}

	/** Builds facts from a template of the form "a1 r1 a2; b1 r2 b2; ..." */
	public static List<Fact> buildFacts(String factTemplates, String technique) throws InvalidTripleException {
		factTemplates = TermExtractor.stripQuotes(factTemplates);
		List<Fact> factList = new ArrayList<Fact>();
		for (String factTemplate : factTemplates.split(";")) {
			factTemplate = factTemplate.trim();
			if(factTemplate.length()==0) continue;
			factTemplate+=' ';
			String[] split = new String[3];
			int argNum = 0;
			int pos = 0;
			while (argNum < 3) {
				while (factTemplate.charAt(pos) == ' ')
					if (++pos >= factTemplate.length())
						throw new InvalidTripleException("Template must have 3 components",
								factTemplate);
				if (factTemplate.charAt(pos) == '"') {
					int endPos = factTemplate.indexOf('"', pos + 1);
					if (endPos == -1)
						throw new InvalidTripleException("Closing quote is missing in",
								factTemplate);
					split[argNum] = factTemplate.substring(pos, endPos + 1);
					pos = endPos + 1;
				} else if (factTemplate.charAt(pos) == '\'') {
					int endPos = factTemplate.indexOf('\'', pos + 1);
					if (endPos == -1)
						throw new InvalidTripleException("Closing quote is missing in",
								factTemplate);
					split[argNum] = factTemplate.substring(pos+1, endPos);
					pos = endPos + 1;
				} else {
					int endPos = factTemplate.indexOf(' ', pos + 1);
					split[argNum] = factTemplate.substring(pos, endPos);
					pos = endPos + 1;
				}
				argNum++;
			}
			if (pos != factTemplate.length())
				throw new InvalidTripleException("Too many components in template", factTemplate);
			Fact result = new Fact(split[0],split[1],split[2], technique);
			if (result.arg1.startsWith("#")) {
				if (result.arg1.length() != 2)
					throw new InvalidTripleException(
									"A template list can only contain template references of the form #x",
									factTemplate);
				int factId = result.arg1.charAt(1) - '0';
				if (factId < 1 || factId > factList.size())
					throw new InvalidTripleException(
									"#x in a template can only refer to preceding templates by their id, 1-based",
									factTemplate);
				result.arg1fact = factList.get(factId - 1);
				result.arg1="REF"+result.arg1fact.hashCode();
			}
			if (!Basics.isRelation(result.relation) && !result.relation.startsWith("?"))
				throw new InvalidTripleException("Undefined relation", result.relation, "in", factTemplate);
			// Test whether instantiation works
			try {
			result.instantiateSingle(new String[] { "test", "test", "test", "test" });
			} catch(DataFormatException e) {
				// This is OK
			} catch(NoSuchMethodException e) {
				throw new InvalidTripleException(e.getMessage());
			}
			factList.add(result);
		}
		return (factList);
	}

	/** Loads from TSV file */
	public void loadFrom(File tsvFile, String technique) throws IOException {
		Fact previous = null;
		for (String line : new FileLines(tsvFile, "Loading from " + tsvFile)) {
			String[] split = line.split("\t");
			if (split.length != 3)
				continue;
			if (split[0].length() == 0)
				add(previous = new Fact(previous, split[1].trim(), split[2].trim()));
			else
				add(previous = new Fact(split[0].trim(), split[1].trim(), split[2].trim(), technique));
		}
	}

	/** Loads from YAGO file, relation given by filename */
	public void loadResultsFrom(File tsvFile, String technique)
			throws IOException {
		String relation = tsvFile.getName();
		relation = relation.substring(0, relation.lastIndexOf('.'));
		for (String line : new FileLines(tsvFile, "Loading from " + tsvFile)) {
			String[] split = line.split("\t");
			if (split.length < 3)
				continue;
			add(new Fact(split[1], relation, split[2], technique));
		}
	}

  @Override
  public Iterator<Fact> iterator() {
    return facts.iterator();
  }

  public void removeAll(List<Fact> deleteMe) {  
    facts.removeAll(deleteMe);
  }

  public int size() {
    return facts.size();
  }
  
  public String toString() {
    return facts.toString();
  }
}
