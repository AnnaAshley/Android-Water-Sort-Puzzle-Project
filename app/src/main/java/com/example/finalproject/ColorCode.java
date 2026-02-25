package com.example.finalproject;

public enum ColorCode {
    RED(0xFFFF0000),
    BLUE(0xFF0000FF),
    CYAN(0xFF00FFFF),
    GREEN(0xFF00FF00),
    YELLOW(0xFFFFFF00),
    PURPLE(0xFF800080);

    private final int colorValue;

    ColorCode(int colorValue) {
        this.colorValue = colorValue;
    }

    public int getColorValue() {
        return colorValue;
    }

    public static ColorCode fromColorValue(int value) {
        for (ColorCode code : values()) {
            if (code.colorValue == value) return code;
        }
        throw new IllegalArgumentException("Unknown color value: " + value);
    }
}

