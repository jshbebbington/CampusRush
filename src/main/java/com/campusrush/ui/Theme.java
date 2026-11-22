package com.campusrush.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public final class Theme {

    private static final String PIXEL_FONT = findPixelFont();

    public static final Color SKY = new Color(80, 199, 239);
    public static final Color SKY_LIGHT = new Color(178, 239, 232);
    public static final Color SUN = new Color(255, 214, 78);
    public static final Color HILL = new Color(41, 199, 142);
    public static final Color GRASS = new Color(27, 166, 79);
    public static final Color GRASS_DARK = new Color(24, 125, 65);
    public static final Color TRACK = new Color(195, 83, 89);
    public static final Color TRACK_LINE = new Color(255, 232, 218);
    public static final Color NAVY = new Color(25, 48, 74);
    public static final Color INK = new Color(20, 33, 48);
    public static final Color CREAM = new Color(255, 248, 224);
    public static final Color CORAL = new Color(239, 111, 89);
    public static final Color PURPLE = new Color(107, 82, 170);

    private Theme() {
    }

    public static Font pixelFont(int size, boolean bold) {
        return new Font(PIXEL_FONT,
                bold ? Font.BOLD : Font.PLAIN, size);
    }

    private static String findPixelFont() {
        String[] preferred = {
            "Press Start 2P", "Pixel Operator", "Monaco", "Menlo",
            Font.MONOSPACED
        };
        String[] installed = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String candidate : preferred) {
            for (String family : installed) {
                if (candidate.equalsIgnoreCase(family)) {
                    return family;
                }
            }
            if (Font.MONOSPACED.equals(candidate)) {
                return candidate;
            }
        }
        return Font.MONOSPACED;
    }

    public static void styleButton(JButton button, Color background,
                                   Color foreground) {
        button.setForeground(foreground);
        button.setBackground(background);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INK, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }
}
