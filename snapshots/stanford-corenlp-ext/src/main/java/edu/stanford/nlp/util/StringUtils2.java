package edu.stanford.nlp.util;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasOffset;
import edu.stanford.nlp.ling.HasWord;

public class StringUtils2 {

  /**
   * Don't let anyone instantiate this class.
   */
  private StringUtils2() {
  } 

  /**
   * Joins all the tokens together (more or less) according to their original whitespace.  It assumes all whitespace was " "
   * @param tokens list of tokens which implement {@link HasOffset} and {@link HasWord}
   * @return a string of the tokens with the appropriate amount of spacing
   */
  public static String joinWithOriginalWhiteSpace(CoreLabel... tokens) {
    if (tokens.length == 0) {
      return "";
    }

    CoreLabel lastToken = tokens[0];
    StringBuilder buffer = new StringBuilder(lastToken.word());

    for (int i = 1; i < tokens.length; i++) {
      CoreLabel currentToken = tokens[i];
      int numSpaces = currentToken.beginPosition() - lastToken.endPosition();
      if (numSpaces < 0) {
        numSpaces = 0;
      }

      buffer.append(StringUtils.repeat(' ', numSpaces) + currentToken.word());
      lastToken = currentToken;
    }

    return buffer.toString();
  }
}