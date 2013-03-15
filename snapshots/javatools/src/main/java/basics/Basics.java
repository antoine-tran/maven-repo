package basics;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

import javatools.administrative.Announce;
import javatools.administrative.Parameters;
import javatools.datatypes.Pair;
import javatools.parsers.Char;

/**
 * 
 * Class Basics
 * 
 * Provides basic infrastructure. Call Initialize() before use.
 * 
 * @author Fabian M. Suchanek
 */
public class Basics {

  /**
   * 
   * Class ReplacementComparator
   * 
   * Compares RegEx patterns. Makes simple patterns ome first
   * 
   * @author Fabian M. Suchanek
   */
  protected static class ReplacementComparator implements Comparator<Pair<Pattern, String>> {

    @Override
    public int compare(Pair<Pattern, String> o1, Pair<Pattern, String> o2) {

      if (o1.second().equals("NIL") && !o2.second().equals("NIL")) return (-1);
      if (!o1.second().equals("NIL") && o2.second().equals("NIL")) return (1);
      if (o1.first().matcher(o2.first().pattern()).matches()) return (1);
      if (o2.first().matcher(o1.first().pattern()).matches()) return (-1);
      String o1Pattern = o1.first().pattern();
      String o2Pattern = o2.first().pattern();

      /*
       * int o1wildcards = 0;
       * 
       * for (int i = 0; i < o1Pattern.length(); i++) if
       * (o1Pattern.charAt(i) == '*' || o1Pattern.charAt(i) == '+')
       * o1wildcards++; int o2wildcards = 0; for (int i = 0; i <
       * o2Pattern.length(); i++) if (o2Pattern.charAt(i) == '*' ||
       * o2Pattern.charAt(i) == '+') o2wildcards++; if (o1wildcards !=
       * o2wildcards) return (o1wildcards - o2wildcards); if
       * (o1Pattern.length() != o2Pattern.length()) return
       * (o1Pattern.length() - o2Pattern.length());
       */
      if (o1Pattern.length() != o2Pattern.length()) return (o2Pattern.length() - o1Pattern.length());
      return (o1Pattern.compareTo(o2Pattern));
    }
  }

  public static class InvalidTripleException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidTripleException(String m) {
      super(m);
    }

    public InvalidTripleException(Object... m) {
      super(concat(m));
    }

    protected static String concat(Object... m) {
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < m.length; i++)
        buf.append(m[i]).append(' ');
      return (buf.toString());
    }
  }

  /** The person class */
  public static String PERSON = "wordnet_person_100007846";

  /** The entity class */
  public static String ENTITY = "wordnet_entity_100001740";

  /** The geographical entity class */
  public static String GEO_ENTITY = "yagoGeoEntity";

  /** The event class */
  public static String EVENT = "wordnet_event_100029378";

  /** Relations */
  public static String SUBPROPERTY_OF = "subpropertyOf";

  public static String SUBCLASS_OF = "subclassOf";

  /** The facts */
  public static FactCollection facts = new FactCollection(90000);

  /** The relations */
  public static Collection<String> relations;

  /** Maps patterns to replacements */
  public static SortedSet<Pair<Pattern, String>> wikiReplacements;

  /** Holds the wikiKeeps */
  public static SortedSet<Pair<Pattern, String>> wikiKeeps;

  /** Maps a closing bracket to an opening bracket */
  public static Map<String, String> closing2openingBracket;

  /** Maps yago class to type check pattern */
  public static Map<String, Pattern> typeChecks;

  /** Maps a relation to its potential splitting characters.
   * Used for multi-value infobox attributes
   */
  public static Map<String, List<String>> wikiSplits;

  /** Maps pattern to fact list */
  public static Map<Pattern, List<Fact>> wikiPatterns;
  
  /** Maps relations to their (evaluated) confidence */
  public static Map<String, Double> relationConfidence;

  /** Holds implications */
  public static List<Pair<List<Fact>, List<Fact>>> implications = null;

  /** Holds functional relations - precompiled to speed up, will be filled in Compile() */
  public static Set<String> functionalRelations = new HashSet<String>();

  /** Holds virtual relations */
  public static Map<String, VirtualRelation> virtualRelations = null;

  /** Maps a language code to the language name */
  public static Map<String, String> code2language;

  /** Maps a 3-letter language code to the language name */
  public static Map<String, String> tlc2language;

  /** Returns a regex pattern */
  public static Pattern makePatternOrAbort(String s) {
    try {
      return (Pattern.compile(makePureString(s)));
    } catch (Exception e) {
      Announce.error(e.getMessage(), "in", s);
    }
    return (null);
  }

  /** Removes quotes */
  public static String makePureString(String s) {
    return (Char.decodeBackslash(TermExtractor.stripQuotes(s)));
  }

  /**
   * Fills the internal maps from the facts
   */
  public synchronized static void Compile() {
    Announce.doing("Checking loaded facts");
    // Type checks
    typeChecks = new TreeMap<String, Pattern>();
    for (Fact f : facts.get("_hasTypeCheckPattern")) {
      typeChecks.put(f.arg1, makePatternOrAbort(f.arg2));
    }
    // Language Codes
    code2language = new TreeMap<String, String>();
    for (Fact f : facts.get("hasLanguageCode")) {
      code2language.put(TermExtractor.stripQuotes(f.arg2), f.arg1);
    }
    tlc2language = new TreeMap<String, String>();
    for (Fact f : facts.get("hasThreeLetterLanguageCode")) {
      tlc2language.put(TermExtractor.stripQuotes(f.arg2), f.arg1);
    }
    // Replacements
    wikiReplacements = new TreeSet<Pair<Pattern, String>>(new ReplacementComparator());
    for (Fact f : Basics.facts.get("_wikiReplace")) {
      wikiReplacements.add(new Pair<Pattern, String>(makePatternOrAbort(f.arg1), makePureString(f.arg2)));
    }
    // Keeps
    wikiKeeps = new TreeSet<Pair<Pattern, String>>(new ReplacementComparator());
    for (Fact f : Basics.facts.get("_wikiKeep")) {
      wikiKeeps.add(new Pair<Pattern, String>(makePatternOrAbort(f.arg1), makePureString(f.arg2)));
    }
    // Brackets
    closing2openingBracket = new TreeMap<String, String>();
    for (Fact f : Basics.facts.get("_wikiBrackets"))
      closing2openingBracket.put(makePureString(f.arg2), makePureString(f.arg1));
    // Splits
    wikiSplits = new HashMap<String, List<String>>();
    for (Fact f : Basics.facts.get("_wikiSplit")) {
      List<String> splits = wikiSplits.get(makePureString(f.arg1));
      if (splits == null) {
        splits = new LinkedList<String>();
        wikiSplits.put(makePureString(f.arg1), splits);
      }
      splits.add(makePureString(f.arg2));
    }
    // Patterns
    Announce.doing("Checking patterns");
    try {
      wikiPatterns = new HashMap<Pattern, List<Fact>>();

      List<Fact> patternFacts = new ArrayList<Fact>();

      if (!Parameters.getBoolean("extendedEditionOnly", false)) {
        patternFacts.addAll(Basics.facts.get("_wikiPattern"));
      }

      if (Parameters.getBoolean("extendedEdition", false)) {
        if (Parameters.getBoolean("extendedEditionStructureOnly", false)) {
          patternFacts.addAll(Basics.facts.get("_extendedStructureWikiPattern"));
        } else if (Parameters.getBoolean("extendedEditionContextOnly", false)) {
          patternFacts.addAll(Basics.facts.get("_extendedContextWikiPattern"));
        } else {
          patternFacts.addAll(Basics.facts.get("_extendedContextWikiPattern"));
          patternFacts.addAll(Basics.facts.get("_extendedStructureWikiPattern"));
          patternFacts.addAll(Basics.facts.get("_extendedWikiPattern"));
        }
      }
      
      for (Fact f : patternFacts)
        wikiPatterns.put(makePatternOrAbort(f.arg1), FactCollection.buildFacts(f.arg2, Normalize.string("wikipattern: " + f.arg1 + " => " + f.arg2)));
    } catch (InvalidTripleException e) {
      Announce.error(e.getMessage());
    }
    // relation confidence
    relationConfidence = new HashMap<String, Double>();
    for (Fact f : facts.get("_hasConfidence")) {
      relationConfidence.put(f.arg1, Double.parseDouble(f.arg2));
    }
    // Implications
    implications = new ArrayList<Pair<List<Fact>, List<Fact>>>();
    try {
      for (Fact f : facts.get("_implies")) {
        implications.add(new Pair<List<Fact>, List<Fact>>(FactCollection.buildFacts(f.arg1, "implication"), FactCollection.buildFacts(f.arg2, "implication")));
      }
    } catch (InvalidTripleException e) {
      Announce.error(e.getMessage());
    }

    Announce.done();

    // Virtual relations
    virtualRelations = new TreeMap<String, VirtualRelation>();
    for (Fact f : facts.get("_hasVirtualRelationImplementation")) {
      try {
        @SuppressWarnings("rawtypes")
        Class cls = Class.forName(TermExtractor.stripQuotes(f.arg2.substring(0, f.arg2.lastIndexOf('.'))));
        VirtualRelation n = (VirtualRelation) cls.getDeclaredField(TermExtractor.stripQuotes(f.arg2.substring(f.arg2.lastIndexOf('.') + 1)))
            .get(null);
        virtualRelations.put(f.arg1, n);
      } catch (Exception e) {
        Announce.error("Could not find virtual relation", f.arg2);
      }
    }

    // Relations
    relations = new TreeSet<String>();
    for (Fact f : facts.get("hasDomain"))
      relations.add(f.arg1);

    // Functional relations
    functionalRelations = new HashSet<String>();
    for (String relation : relations) {
      List<String> arg2s = facts.getArg2s(relation, "type");
      if (arg2s.contains("yagoFunction")) {
        functionalRelations.add(relation);
      }
    }

    // Check glosses
    Announce.doing("Checking glosses");
    for (Fact f : facts.get("hasGloss")) {
      if (!isRelation(f.arg1) && !isClass(f.arg1)) Announce.warning("Gloss refers to something that is not a class or relation:", f);
    }
    for (String relation : relations) {
      if (!relation.startsWith("_") && facts.get(relation, "hasGloss").size() == 0) Announce.warning("Relation", relation, "has no gloss");
    }
    Announce.done();
    Announce.done();
  }

  /** Loads stuff from folder. Adds it to existing repository. */
  public static void loadFrom(File inputFolder) throws IOException {
    if (inputFolder.isDirectory()) {
      Announce.doing("Loading facts in folder", inputFolder);
      if (new File(inputFolder, "relations.tsv").exists()) facts.loadFrom(new File(inputFolder, "relations.tsv"), "manual");
      for (File file : inputFolder.listFiles(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".tsv") && !name.equals("relations.tsv");
        }
      }))
        facts.loadFrom(file, "manual");
      Announce.done();
    } else {
      facts.loadFrom(inputFolder, "manual");
    }
    Compile();
  }

  /** TRUE if a fact with a virtual relation is true */
  public synchronized static boolean virtualHolds(Fact fact) {
    if (!virtualRelations.containsKey(fact.relation)) return (false);
    try {
      return (virtualRelations.get(fact.relation).holds(fact.arg1, fact.arg2));
    } catch (Exception e) {
      return (false);
    }
  }

  /** Returns the range of a relation */
  public static String range(String relation) {
    String ran = facts.getArg2(relation, "hasRange");
    return (ran == null ? ENTITY : ran);
  }

  /** Returns the preferred meaning */
  public static String preferredMeaning(String word) {
    return (facts.getArg2(word, "hasPreferredMeaning"));
  }

  public static List<String> allMeanings(String word) {
    return (facts.getArg2s(word, "means"));
  }

  /** Returns the domain of a relation */
  public static String domain(String relation) {
    String dom = facts.getArg2(relation, "hasDomain");
    return (dom == null ? ENTITY : dom);
  }

  /** TRUE for relations */
  public static boolean isRelation(String relation) {
    return (relation.equals("hasDomain") || facts.getArg2(relation, "hasDomain") != null);
  }

  /** TRUE for functions */
  public static boolean isFunction(String relation) {
    return functionalRelations.contains(relation);
  }

  /** TRUE for non-conceptual categories */
  public static boolean isNonConceptual(String w) {
    List<String> arg2s = facts.getArg2s(Normalize.string(w), "type");
    return (arg2s.contains("yagoNonConceptualWord"));
  }

  /** TRUE for classes */
  public static boolean isClass(String relation) {
    return (relation.equals(ENTITY) || facts.getArg2(relation, "subclassOf") != null);
  }

  /** Relation confidence (evaluated) */
  public static Double hasConfidence(String relation) {
    if (relationConfidence.containsKey(relation)) {
      return relationConfidence.get(relation);
    } else {
      // return default: 1.0
      return 1.0;
    }
  }
  
  /** Returns the first superclass of a class */
  public static String superclass(String cls) {
    return (facts.getArg2(cls, "subclassOf"));
  }

  /** Returns the superclasses of a class */
  public static List<String> superclasses(String cls) {
    return (facts.getArg2s(cls, "subclassOf"));
  }

  /** Fills the ancestors */
  protected static void ancestors(String cls, Set<String> set) {
    if (set.contains(cls)) return;
    set.add(cls);
    List<String> supercl = superclasses(cls);
    for (String superclass : supercl)
      ancestors(superclass, set);
  }

  /** Returns the ancestors of a class */
  public static Set<String> ancestors(String cls) {
    Set<String> set = new TreeSet<String>();
    ancestors(cls, set);
    return (set);
  }

  /** Returns the ancestors of a class in an ordered way */
  public static List<String> sortedAncestors(String cls) {
    List<String> closure = new LinkedList<String>();

    String current = cls;
    String parent = null;

    do {
      parent = facts.getArg2(current, "subclassOf");

      if (parent != null) {
        closure.add(parent);
        current = parent;
      }
    } while (parent != null);

    return closure;
  }

  /** Fills the ancestors */
  public static boolean hasAncestor(String subclass, String superclass) {
    if (subclass.equals(superclass)) return (true);
    List<String> supercl = superclasses(subclass);
    for (String next : supercl)
      if (hasAncestor(next, superclass)) return (true);
    return (false);
  }

  /** Returns the implications, instantiated with the entity */
  public static List<Pair<List<Fact>, List<Fact>>> instantiatedImplications(String entity) throws DataFormatException, NoSuchMethodException {
    String[] values = new String[] { entity };
    List<Pair<List<Fact>, List<Fact>>> instantiations = new ArrayList<Pair<List<Fact>, List<Fact>>>();
    for (Pair<List<Fact>, List<Fact>> implication : implications) {
      Pair<List<Fact>, List<Fact>> instantiation = new Pair<List<Fact>, List<Fact>>(new ArrayList<Fact>(), new ArrayList<Fact>());
      for (Fact f : implication.first())
        instantiation.first().add(f.instantiateSingle(values));
      for (Fact f : implication.second())
        instantiation.second().add(f.instantiateSingle(values));
      instantiations.add(instantiation);
    }
    return (instantiations);
  }

  /** Returns the mappings of files to relations */
  public static Map<String, String> wordnetFiles() {
    Map<String, String> result = new TreeMap<String, String>();
    for (Fact f : facts.get("_wordnetFile"))
      result.put(TermExtractor.stripQuotes(f.arg1), f.arg2);
    return (result);
  }

  /** TRUE if the entity is an instance of the class, as given by the facts */
  public static boolean is(String entity, String cls, FactCollection facts) {
    if (cls.equals(ENTITY)) return (true);
    for (String type : facts.getArg2s(entity, "type")) {
      if (ancestors(type).contains(cls)) return (true);
    }
    return (false);
  }

  /** Removes all facts */
  public static void deleteAll() {
    facts = new FactCollection();
  }

  /** Test */
  public static void main(String[] args) {
    Config.init(null);
  }
}
