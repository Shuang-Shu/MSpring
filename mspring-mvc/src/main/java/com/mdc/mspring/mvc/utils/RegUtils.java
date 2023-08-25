package com.mdc.mspring.mvc.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mdc.mspring.mvc.exception.JsonParseException;

public class RegUtils {
    public static Map<String, String> parse(String patternString, String string) {
        Map<String, String> result = new HashMap<>();
        Pattern pattern = Pattern.compile("\\{(\\w+)\\}");
        List<String> keys = new ArrayList<>();
        Matcher keyMatcher = pattern.matcher(patternString);
        while (keyMatcher.find()) {
            keys.add(keyMatcher.group(1));
        }
        String formatString = patternString.replaceAll("\\{\\w+\\}", "(\\\\w+)");
        Pattern valPattern = Pattern.compile(formatString);
        Matcher valMatcher = valPattern.matcher(string);
        int idx = 1;
        try {
            if (valMatcher.find()) {
                for (String key : keys) {
                    result.put(key, valMatcher.group(idx++));
                }
            }
        } catch (RuntimeException e) {
            throw new JsonParseException("url pattern not match");
        }
        return result;
    }
}
