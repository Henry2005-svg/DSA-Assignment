package tarumtresorts.client;

import java.nio.file.*;
import java.util.Scanner;
import tarumtresorts.boundary.*;
import tarumtresorts.control.*;
import tarumtresorts.data.*;
import tarumtresorts.utility.*;

/** Author: G9 Henry, Eason, QiHuai. NetBeans and terminal entry point. */
public final class TarumtResortsApplication {
    private TarumtResortsApplication() {
    }

    public static void main(String[] args) {
        try {
            Path dataDir = Paths.get(args.length > 0 ? args[0] : "data");
            final SharedDataContext data = new SharedDataContext();
            final DataFileControl files = new DataFileControl(dataDir);
            files.loadAll(data);
            data.setPersistence(new Runnable() {
                public void run() {
                    try {
                        files.saveAll(data);
                    } catch (java.io.IOException ex) {
                        throw new IllegalStateException("Could not save data files: " + ex.getMessage(), ex);

                    }
                }
            });
            HousekeepingControl housekeeping = new HousekeepingControl(data);
            ConsoleInput input = new ConsoleInput(new Scanner(System.in));
            WalkInBookingControl walk = new WalkInBookingControl(data);
            FrontDeskControl front = new FrontDeskControl(data, housekeeping);
            new MainMenuUI(new WalkInBookingUI(walk, input), new HousekeepingTaskUI(housekeeping, input),
                    new FrontDeskServiceUI(front, input), input, files, data).run();
        } catch (Exception ex) {
            System.err.println("Startup failed: " + ex.getMessage());
            ex.printStackTrace();

        }
    }
}
