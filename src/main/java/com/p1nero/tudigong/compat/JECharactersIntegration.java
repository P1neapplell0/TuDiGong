package com.p1nero.tudigong.compat;

import java.util.Locale;

public final class JECharactersIntegration {
    private JECharactersIntegration() {
    }

    public static boolean match(String text, String query) {
        return text.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }
}
