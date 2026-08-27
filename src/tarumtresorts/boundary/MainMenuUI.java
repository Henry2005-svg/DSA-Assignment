package tarumtresorts.boundary;

import java.io.*;
import tarumtresorts.data.*;
import tarumtresorts.utility.*;

/** Author: <Your Name>. Application main-menu boundary. */
public class MainMenuUI {
    private final WalkInBookingUI walk;
    private final HousekeepingTaskUI housekeeping;
    private final FrontDeskServiceUI front;
    private final ConsoleInput in;
    private final DataFileControl files;
    private final tarumtresorts.control.SharedDataContext data;

    public MainMenuUI(
            WalkInBookingUI w, HousekeepingTaskUI h, FrontDeskServiceUI f, ConsoleInput in, DataFileControl files,
            tarumtresorts.control.SharedDataContext data) {
        walk = w;
        housekeeping = h;
        front = f;
        this.in = in;
        this.files = files;
        this.data = data;
    }

    public void run() {
        while (true) {
            WalkInBookingUI.header("TARUMT RESORTS SYSTEM");
            System.out.println(
                    "1. Walk-In Registration and Standard Booking\n2. Housekeeping and Task Log\n3. Front-Desk Service\n0. Exit");
            int x = in.integer("Enter your choice: ", 0, 3);
            if (x == 1)
                walk.run();
            else if (x == 2)
                housekeeping.run();
            else if (x == 3)
                front.run();
            else if (in.confirm("Confirm exit")) {
                try {
                    files.saveAll(data);
                    System.out.println("All six datasets saved. Goodbye.");
                } catch (IOException ex) {
                    System.out.println("Save failed: " + ex.getMessage());
                    if (!in.confirm("Exit without saving"))
                        continue;
                }
                return;
            }
        }
    }
}
