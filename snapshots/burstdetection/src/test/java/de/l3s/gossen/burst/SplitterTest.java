package de.l3s.gossen.burst;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;

@Ignore
public class SplitterTest {

    @Test
    public void test() {
        String text = "a-b_c42-d42e";

        String pattern = "-|_|(?<=\\p{Alpha})(?=\\d)|(?<=\\d)(?=\\p{Alpha})";
        Splitter splitter = Splitter.onPattern(pattern);
        Iterable<String> split = splitter.split(text);
        for (String string : split) {
            System.out.println(string);
        }
        List<String> actual = Lists.newArrayList(split);
        System.out.println(Arrays.toString(text.split(pattern)));
        List<String> expected = Lists.newArrayList("a", "b", "c", "42", "d", "42", "e");
        //        assertEquals(expected, Arrays.asList(text.split(pattern)));
        assertEquals(expected, actual);
    }

    @Test
    public void testSimple() throws Exception {
        String pattern = "\\b";
        String s = "a quick brown fox";
        List<String> expected = Lists.newArrayList("", "a", " ", "quick", " ", "brown", " ", "fox");
        assertEquals("String#split", expected, Arrays.asList(s.split(pattern)));
        assertEquals("Splitter", expected,
                Lists.newArrayList(Splitter.onPattern(pattern).split(s)));
    }

}
