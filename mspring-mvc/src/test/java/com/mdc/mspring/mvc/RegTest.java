package com.mdc.mspring.mvc;

import com.mdc.mspring.mvc.utils.RegUtils;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegTest {
    @Test
    public void testRegBasic() {
        String pattern = "/hello/{name}";
        String url = "/hello/mdc";
        System.out.println(RegUtils.parse(pattern, url));
        String url2 = "/hello/123";
        System.out.println(RegUtils.parse(pattern, url2));
    }

    @Test
    public void testMatch() {
        String pattern = "/hello/{name}";
        String url = "/hello/mdc";
        Pattern valPattern = Pattern.compile(RegUtils.formatPatternString(pattern));
        Matcher valMatcher = valPattern.matcher(url);
        System.out.println(valMatcher.matches());
    }
}
