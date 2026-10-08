package com.mpapad.zoo.ui;

import com.mpapad.zoo.model.Animal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Renders animals as a plain-text table whose columns size themselves to their content. */
public final class AnimalTable {

    private static final String[] HEADERS =
            {"CODE", "NAME", "CLASS", "DIET", "MAX AGE", "WEIGHT (kg)", "WILD"};

    private AnimalTable() {
    }

    public static String render(List<Animal> animals) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADERS);
        for (Animal a : animals) {
            rows.add(new String[] {
                    String.valueOf(a.code()),
                    a.name(),
                    a.animalClass().label(),
                    a.diet().label(),
                    String.valueOf(a.maxAge()),
                    formatWeight(a.weightKg()),
                    a.wild() ? "yes" : "no"
            });
        }

        int[] widths = new int[HEADERS.length];
        for (String[] row : rows) {
            for (int c = 0; c < row.length; c++) {
                widths[c] = Math.max(widths[c], row[c].length());
            }
        }

        StringBuilder border = new StringBuilder("+");
        for (int w : widths) {
            border.append("-".repeat(w + 2)).append('+');
        }

        StringBuilder sb = new StringBuilder();
        sb.append(border).append('\n');
        for (int r = 0; r < rows.size(); r++) {
            sb.append('|');
            String[] row = rows.get(r);
            for (int c = 0; c < row.length; c++) {
                sb.append(' ').append(String.format("%-" + widths[c] + "s", row[c])).append(" |");
            }
            sb.append('\n');
            if (r == 0) {
                sb.append(border).append('\n');
            }
        }
        sb.append(border);
        return sb.toString();
    }

    /** Formats a weight without a trailing ".0" (e.g. 80.0 becomes "80", 2.5 stays "2.5"). */
    static String formatWeight(double weightKg) {
        return BigDecimal.valueOf(weightKg).stripTrailingZeros().toPlainString();
    }
}
