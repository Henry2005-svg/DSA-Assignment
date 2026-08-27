package tarumtresorts.utility;

import java.util.Scanner;
import java.time.*;
import tarumtresorts.entity.Types.*;

/** Author: <Your Name>. Shared validated console input. */
public class ConsoleInput {
    private final Scanner in;

    public ConsoleInput(Scanner s) {
        in = s;
    }

    public String line(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    public int integer(String prompt, int min, int max) {
        while (true)
            try {
                int n = Integer.parseInt(line(prompt));
                if (n < min || n > max)
                    throw new NumberFormatException();
                return n;
            } catch (NumberFormatException ex) {
                System.out.printf("Enter a number from %d to %d.%n", min, max);
            }
    }

    public boolean confirm(String prompt) {
        while (true) {
            String s = line(prompt + " (Y/N): ").toUpperCase();
            if (s.equals("Y"))
                return true;
            if (s.equals("N"))
                return false;
            System.out.println("Enter Y or N.");
        }
    }

    public RoomType roomType() {
        while (true)
            try {
                return RoomType.valueOf(line("Room type (STD/DLX/FAM/STE): ").toUpperCase());
            } catch (Exception ex) {
                System.out.println("Invalid room type.");
            }
    }

    public LocalDateTime dateTime(String prompt) {
        while (true)
            try {
                return Util.parseDateTime(line(prompt + " [yyyy-MM-dd HH:mm]: "));
            } catch (Exception ex) {
                System.out.println("Invalid date/time.");
            }
    }
}
