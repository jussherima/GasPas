package com.gaspas.ui;

import com.googlecode.lanterna.graphics.TextGraphics;

public final class Banner {

    public static final int HEIGHT = 3;

    private static final String[] LINES = new String[] {
        "╔═╗  ╔═╗  ╔═╗  ╔═╗  ╔═╗  ╔═╗",
        "║ ╦  ╠═╣  ╚═╗  ╠═╝  ╠═╣  ╚═╗",
        "╚═╝  ╩ ╩  ╚═╝  ╩    ╩ ╩  ╚═╝",
    };

    private static final String TAGLINE = "─── passwords · secrets ───";

    public static int width() {
        int w = 0;
        for (String line : LINES) w = Math.max(w, line.length());
        return w;
    }

    public static String tagline() {
        return TAGLINE;
    }

    public static void draw(TextGraphics g, int x, int y) {
        for (int i = 0; i < LINES.length; i++) {
            g.putString(x, y + i, LINES[i]);
        }
    }

    private Banner() {}
}
