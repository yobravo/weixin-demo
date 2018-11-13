package com.yobravo.wx.testresource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MotivationalQuotes {
    public static List<String> quotes = new ArrayList<>();
    static {
        try {
            try (InputStream resourceAsStream = MotivationalQuotes.class.getClassLoader().getResourceAsStream("motivational_quote.txt");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    quotes.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getRandomQuote() {
        if (quotes.size() > 0) {
            Random random = new Random();
            int i = random.nextInt(quotes.size());
            return quotes.get(i);
        } else {
            return "Wish you a very good day!";
        }
    }
}
