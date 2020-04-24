package sg.edu.appventure.examclock;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;

public class DigitalClock extends Parent {
    private final Digit[] digits;
    public final double width;

    public DigitalClock(Color onColor, Color offColor, Color borderColor) {
        digits = new Digit[7];
        for (int i = 0; i < 6; i++) {
            Digit digit = new Digit(onColor, offColor, borderColor);
            digit.setLayoutX(i * Digit.DIGIT_SPACE + ((i + 1) % 2) * Digit.DIGIT_SPACE / 4);
            digits[i] = digit;
            getChildren().add(digit);
        }
        width = 6 * Digit.DIGIT_SPACE;
        Group dots = new Group(
                new Circle(Digit.DIGIT_SPACE + Digit.DIGIT_WIDTH + Digit.DIGIT_SPACE / 4, 44 * Digit.DIGIT_HEIGHT / 108, Digit.DIGIT_WIDTH / 9, onColor),
                new Circle(Digit.DIGIT_SPACE + Digit.DIGIT_WIDTH + Digit.DIGIT_SPACE / 4, 64 * Digit.DIGIT_HEIGHT / 108, Digit.DIGIT_WIDTH / 9, onColor),
                new Circle((Digit.DIGIT_SPACE * 3) + Digit.DIGIT_WIDTH + Digit.DIGIT_SPACE / 4, 44 * Digit.DIGIT_HEIGHT / 108, Digit.DIGIT_WIDTH / 9, onColor),
                new Circle((Digit.DIGIT_SPACE * 3) + Digit.DIGIT_WIDTH + Digit.DIGIT_SPACE / 4, 64 * Digit.DIGIT_HEIGHT / 108, Digit.DIGIT_WIDTH / 9, onColor));
        getChildren().add(dots);
    }

    public void refreshClocks(int hours, int minutes, int seconds) {
        digits[0].showNumber(hours / 10);
        digits[1].showNumber(hours % 10);
        digits[2].showNumber(minutes / 10);
        digits[3].showNumber(minutes % 10);
        digits[4].showNumber(seconds / 10);
        digits[5].showNumber(seconds % 10);
    }
}

/**
 * Simple 7 segment LED style digit. It supports the numbers 0 through 9.
 */
final class Digit extends Parent {
    public static double DIGIT_SPACE = 30;
    public static double DIGIT_WIDTH = 0.675 * DIGIT_SPACE;
    public static double DIGIT_HEIGHT = 1.35 * DIGIT_SPACE;
    private static final boolean[][] DIGIT_COMBINATIONS = new boolean[][]{
            new boolean[]{true, false, true, true, true, true, true},
            new boolean[]{false, false, false, false, true, false, true},
            new boolean[]{true, true, true, false, true, true, false},
            new boolean[]{true, true, true, false, true, false, true},
            new boolean[]{false, true, false, true, true, false, true},
            new boolean[]{true, true, true, true, false, false, true},
            new boolean[]{true, true, true, true, false, true, true},
            new boolean[]{true, false, false, false, true, false, true},
            new boolean[]{true, true, true, true, true, true, true},
            new boolean[]{true, true, true, true, true, false, true}};
    private final Polygon[] polygons = new Polygon[]{
            new Polygon(2 * DIGIT_WIDTH / 54d, 0,
                    52 * DIGIT_WIDTH / 54d, 0,
                    42 * DIGIT_WIDTH / 54d, 10 * DIGIT_HEIGHT / 108d,
                    12 * DIGIT_WIDTH / 54d, 10 * DIGIT_HEIGHT / 108d),

            new Polygon(12 * DIGIT_WIDTH / 54d, 49 * DIGIT_HEIGHT / 108d,
                    42 * DIGIT_WIDTH / 54d, 49 * DIGIT_HEIGHT / 108d,
                    52 * DIGIT_WIDTH / 54d, 54 * DIGIT_HEIGHT / 108d,
                    42 * DIGIT_WIDTH / 54d, 59 * DIGIT_HEIGHT / 108d,
                    12f * DIGIT_WIDTH / 54d, 59f * DIGIT_HEIGHT / 108d,
                    2f * DIGIT_WIDTH / 54d, 54f * DIGIT_HEIGHT / 108d),

            new Polygon(12 * DIGIT_WIDTH / 54d, 98 * DIGIT_HEIGHT / 108d,
                    42 * DIGIT_WIDTH / 54d, 98 * DIGIT_HEIGHT / 108d,
                    52 * DIGIT_WIDTH / 54d, 108 * DIGIT_HEIGHT / 108d,
                    2 * DIGIT_WIDTH / 54d, 108 * DIGIT_HEIGHT / 108d),

            new Polygon(0 * DIGIT_WIDTH / 54d, 2 * DIGIT_HEIGHT / 108d,
                    10 * DIGIT_WIDTH / 54d, 12 * DIGIT_HEIGHT / 108d,
                    10 * DIGIT_WIDTH / 54d, 47 * DIGIT_HEIGHT / 108d,
                    0, 52 * DIGIT_HEIGHT / 108d),

            new Polygon(44 * DIGIT_WIDTH / 54d, 12 * DIGIT_HEIGHT / 108d,
                    54 * DIGIT_WIDTH / 54d, 2 * DIGIT_HEIGHT / 108d,
                    54 * DIGIT_WIDTH / 54d, 52 * DIGIT_HEIGHT / 108d,
                    44 * DIGIT_WIDTH / 54d, 47 * DIGIT_HEIGHT / 108d),

            new Polygon(0 * DIGIT_WIDTH / 54d, 56 * DIGIT_HEIGHT / 108d,
                    10 * DIGIT_WIDTH / 54d, 61 * DIGIT_HEIGHT / 108d,
                    10 * DIGIT_WIDTH / 54d, 96 * DIGIT_HEIGHT / 108d,
                    0, 106 * DIGIT_HEIGHT / 108d),

            new Polygon(44 * DIGIT_WIDTH / 54d, 61 * DIGIT_HEIGHT / 108d,
                    54 * DIGIT_WIDTH / 54d, 56 * DIGIT_HEIGHT / 108d,
                    54 * DIGIT_WIDTH / 54d, 106 * DIGIT_HEIGHT / 108d,
                    44 * DIGIT_WIDTH / 54d, 96 * DIGIT_HEIGHT / 108d)};
    private final Color onColor;
    private final Color offColor;

    public Digit(Color onColor, Color offColor, Color borderColor) {
        this.onColor = onColor;
        this.offColor = offColor;
        getChildren().addAll(polygons);
        showNumber(0);
        for (int i = 0; i < 7; i++) {
            polygons[i].setStroke(borderColor);
            polygons[i].setStrokeType(StrokeType.INSIDE);
            polygons[i].setStrokeWidth(0);
        }
    }

    public void showNumber(Integer num) {
        if (num < 0 || num > 9) num = 0; // default to 0 for non-valid numbers
        for (int i = 0; i < 7; i++) {
            polygons[i].setFill(DIGIT_COMBINATIONS[num][i] ? onColor : offColor);
            polygons[i].setStrokeWidth(DIGIT_COMBINATIONS[num][i] ? 1 : 0);
        }
    }
}