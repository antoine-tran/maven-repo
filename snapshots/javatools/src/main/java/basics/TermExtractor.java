package basics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

import javatools.administrative.Announce;
import javatools.parsers.DateParser;
import javatools.parsers.NumberParser;
import javatools.parsers.PlingStemmer;

/**
 * Class TermExtractor
 * 
 * Methods that extract entities from Wikipedia strings
 * 
 * @author Fabian M. Suchanek
 */
public abstract class TermExtractor {

  /** Extracts an entity from a string. Return NULL if this fails. */
  public String extractSingle(String s) {
    List<String> elements = extractList(s);
    if (elements != null) {
      return elements.get(0);
    } else {
      return null;
    }
  }

  /** Extracts multiple entities from a string. Return NULL if this fails. */
  public abstract List<String> extractList(String s);

  /** TRUE if the resulting entity has to be type checked */
  public boolean requiresTypecheck() {
    return (false);
  }

  /** Extracts a number form a string */
  public static TermExtractor forNumber = new TermExtractor() {

    @Override
    public List<String> extractList(String s) {
      List<String> result = NumberParser.getNumbers(NumberParser.normalize(s));
      if (result.size() == 0) {
        Announce.debug("No number found in", s);
      }
      return (result);
    }

  };

  /** Extracts a URL form a string */
  public static TermExtractor forUrl = new TermExtractor() {

    // also needs to match \ for yago-encoded stuff
    List<Pattern> urlPatterns = Arrays.asList(Pattern.compile("http[s]?://([-\\w\\./\\\\]+)"), Pattern.compile("(www\\.[-\\w\\./\\\\]+)"));

    @Override
    public List<String> extractList(String s) {
      // URL encode before matching - beacuse of unicode titles
      s = Normalize.string(s) + ' ';

      List<String> urls = new ArrayList<String>(3);

      boolean match = true;
      int pos = 0;

      while (match) {
        for (Pattern p : urlPatterns) {
          Matcher m = p.matcher(s);
          if (m.find(pos)) {
            String url = Normalize.string("http://" + m.group(1));
            urls.add(url);
            match = true;
            pos = m.end(1);
          } else {
            match = false;
          }
        }
      }

      if (urls.size() == 0) Announce.debug("Could not find URL in", s);
      return urls;
    }
  };

  /** Extracts a date form a string */
  public static TermExtractor forDate = new TermExtractor() {

    @Override
    public List<String> extractList(String s) {
      List<String> result = DateParser.getDates(DateParser.normalize(s));
      if (result.size() == 0) {
        Announce.debug("No date found in", s);
      }
      return (result);
    }

  };

  /** Extracts an entity form a string */
  public static TermExtractor forEntity = new TermExtractor() {

    @Override
    public List<String> extractList(String s) {
      return Arrays.asList(Normalize.entity(s));
    }

    @Override
    public boolean requiresTypecheck() {
      return (true);
    }
  };

  /** Extracts a YAGO string from a string */
  public static TermExtractor forString = new TermExtractor() {

    @Override
    public List<String> extractList(String s) {
      s = s.trim();
      List<String> result = new ArrayList<String>(3);
      for (String w : s.split(";|,?<br />|'''|''|, ?;|\"")) {
        w = w.trim();
        if (w.length() > 2 && !w.contains("{{") && !w.contains("[[")) result.add(Normalize.string(w));
      }
      if (result.size() == 0) Announce.debug("Could not find string in", s);
      return (result);
    }
  };
  
  /** Extracts a cleaned YAGO string form a part of text */
  public static TermExtractor forText = new TermExtractor() {

    @Override
    public List<String> extractList(String s) {    
      StringBuilder sb = new StringBuilder();
      
      int brackets = 0;
      
      for (int i = 0; i < s.length(); i++) {
        char current = s.charAt(i);
        
        if (current == '{') {
          brackets++;
        } else if (current == '}') {
          brackets--;
        } else if (brackets == 0) {
          sb.append(current);
        }
      }
       
      String clean = sb.toString().trim();
        
      clean = clean.replaceAll("\\s+", " ");
      clean = clean.replaceAll("\\[\\[[^\\]\n]+?\\|([^\\]\n]+?)\\]\\]", "$1");
      clean = clean.replaceAll("\\[\\[([^\\]\n]+?)\\]\\]", "$1");
      
      List<String> result = new ArrayList<String>(1);
      result.add(clean);
      
      return result;
    }
  };

  /** Extracts a language form a string */
  public static TermExtractor forLanguageCode = new TermExtractor() {

    @Override
    public List<String> extractList(String s) {
      String language = Basics.code2language.get(s);
      return (language == null ? new ArrayList<String>() : Arrays.asList(language));
    }
  };

  /** Extracts a wiki link form a string */
  public static TermExtractor forWikiLink = new TermExtractor() {

    Pattern wikipediaLink = Pattern.compile("\\[\\[([^\\|\\]]+)(?:\\|([^\\]]+))?\\]\\]");

    @Override
    public boolean requiresTypecheck() {
      return (true);
    }

    @Override
    public List<String> extractList(String s) {
      List<String> links = new LinkedList<String>();

      Matcher m = wikipediaLink.matcher(s);
      while (m.find()) {
        String result = m.group(1);
        if (result.contains(":") || result.contains("#")) continue;
        if (result.contains(" and ") || result.contains("#")) continue;
        if (s.substring(m.end()).startsWith(" [[")) continue; // It was an adjective
        if (result.matches("\\d+")) continue; // It's the year in which sth happened
        links.add(Normalize.entity(result));
      }

      if (links.size() == 0) Announce.debug("Could not find wikilink in", s);
      return links;
    }

  };

  /** Extracts a wordnet class form a string */
  public static TermExtractor forClass = new TermExtractor() {

    @Override
    public List<String> extractList(String s) {
      List<String> result = new ArrayList<String>(3);
      for (String word : s.split(",")) {
        word = word.trim().replace("[", "").replace("]]", "");
        if (word.length() < 4) continue;
        String meaning = Basics.preferredMeaning(Normalize.string(word));
        if (meaning == null) meaning = Basics.preferredMeaning(Normalize.string(PlingStemmer.stem(word)));
        if (meaning == null) continue;
        if (Basics.isClass(meaning)) result.add(meaning);
      }
      if (result.size() == 0) Announce.debug("Could not find class in", s);
      return (result);
    }

  };

  /** Pattern used in fact templates */
  protected static Pattern convertPattern = Pattern.compile("(?s)@(\\w+)\\((.*)\\)");

  /**
   * Converts expressions like "@Date($1)". Throws noSuchMethodException for
   * unknown conversions. Throws DataFormatException if conversion fails.
   */
  public static String convertSingle(String input, boolean[] requiresTypecheck) throws DataFormatException, NoSuchMethodException {
    List<String> result = convertList(input, requiresTypecheck);
    if (result.size() == 0) {
      return null;
    } else {
      return result.get(0);
    }
  }

  /**
   * Converts expressions like "@Date($1)". Throws noSuchMethodException for
   * unknown conversions. Throws DataFormatException if conversion fails. This
   * method supports enumeration in the input string
   * 
   * @throws DataFormatException
   * @throws NoSuchMethodException
   */
  public static List<String> convertList(String input, boolean[] requiresTypecheck) throws DataFormatException, NoSuchMethodException {
    List<String> results = new ArrayList<String>(3);

    Matcher m = convertPattern.matcher(input);
    if (!m.matches()) {
      results.add(input);
      return (results);
    }
    if (m.group(1).startsWith("yago")) {
      String cls = m.group(1);
      Pattern typeCheck = Basics.typeChecks.get(cls);
      if (typeCheck == null) typeCheck = Basics.typeChecks.get("yago" + cls);
      if (typeCheck == null) throw new NoSuchMethodException("Unknown target class of conversion " + m.group(1) + " in " + input);
      String obj = m.group(2);
      if (Basics.ancestors(cls).contains("yagoString")) {
        m = typeCheck.matcher(obj);
        if (m.find()) {
          results.add(Normalize.string(m.group()));
          return results;
        }
      } else {
        m = typeCheck.matcher(NumberParser.normalize(obj));
        if (m.find()) {
          results.add(Normalize.string(m.group()));
          return results;
        }
      }
      throw new DataFormatException("Could not convert " + obj + " to " + cls);
    }
    String normalizerName = "for" + m.group(1);
    try {
      TermExtractor normalizer = (TermExtractor) TermExtractor.class.getDeclaredField(normalizerName).get(null);
      requiresTypecheck[0] |= normalizer.requiresTypecheck();
      results = normalizer.extractList(m.group(2));
      if (results.size() == 0) throw new DataFormatException("Term extractor failed " + normalizerName + " " + m.group(2));
      return results;
    } catch (DataFormatException e) {
      throw e;
    } catch (Exception e) {
      throw new NoSuchMethodException("Cannot invoke term extractor " + normalizerName);
    }
  }

  /** Removes quotes if any */
  public static String stripQuotes(String s) {
    if (s.startsWith("\"")) s = s.substring(1);
    if (s.endsWith("\"")) s = s.substring(0, s.length() - 1);
    return (s);
  }

}
