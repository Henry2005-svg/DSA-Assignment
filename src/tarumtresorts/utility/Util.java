package tarumtresorts.utility;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Author: <Your Name>. Input normalization, formatting, and explicit insertion
 * sorts.
 */
public final class Util {
    private Util() {
    }

    public static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String normalizeName(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public static boolean validName(String s) {
        return s != null && s.trim().matches("[A-Za-z]+(?:[ ]+[A-Za-z]+)*");
    }

    public static String show(Object o) {
        return o == null ? "-" : o.toString();
    }

    public static LocalDateTime parseDateTime(String s) {
        return LocalDateTime.parse(s.trim(), DT);
    }

    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            for (int i = 0; i < 30; i++) {
                System.out.println();
            }
        }
    }
}
