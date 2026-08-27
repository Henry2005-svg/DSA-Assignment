package tarumtresorts.boundary;

import tarumtresorts.control.*;
import tarumtresorts.entity.*;
import tarumtresorts.entity.Types.*;
import tarumtresorts.utility.*;
import java.time.*;

/** Author: <Your Name>. Walk-In boundary. */
public class WalkInBookingUI {
    private final WalkInBookingControl c;
    private final ConsoleInput in;

    public WalkInBookingUI(WalkInBookingControl c, ConsoleInput in) {
        this.c = c;
        this.in = in;
    }

    public void run() {
        while (true) {
            header("\tWALK-IN REGISTRATION AND STANDARD BOOKING");
            System.out.println(
                    "1. Display All Walk-In Requests\n2. Register Walk-In Guest\n3. Assign Ready Room to Next Guest\n4. Cancel Walk-In Request\n5. Generate Reports\n0. Return to Main Menu");
            int x = in.integer("Enter your choice: ", 0, 5);
            try {
                if (x == 1)
                    display();
                else if (x == 2)
                    register();
                else if (x == 3)
                    assign();
                else if (x == 4)
                    cancel();
                else if (x == 5)
                    report();
                else
                    return;
            } catch (Exception ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }
    }

    private void display() {
        header("DISPLAY ALL WALK-IN REQUESTS");
        System.out.println(
                "1. Display Waiting Queues\n2. Display Successfully Booked and Cancelled Requests\n3. Display Available Rooms\n0. Return");
        int x = in.integer("Enter your choice: ", 0, 3);
        if (x == 1) {
            RoomType[] t = RoomType.values();
            for (int i = 0; i < t.length; i++)
                printRequests(t[i] + " WAITING QUEUE", c.waiting(t[i]));
        } else if (x == 2) {
            Object[] all = c.history();
            printHistory("SUCCESSFULLY BOOKED REQUESTS", all, WalkInStatus.COMPLETED);
            printHistory("CANCELLED REQUESTS", all, WalkInStatus.CANCELLED);
        } else if (x == 3)
            printRooms(c.readyRooms());
    }

    private void register() {
        String name = in.line("Guest name: ");
        RoomType t = in.roomType();
        int g = in.integer("Number of guests: ", 1, 20), n = in.integer("Number of nights: ", 1, 365);
        WalkInRequest r = c.register(name, t, g, n);
        System.out.printf("Registered %s in %s queue at position %d. Arrival: %s%n", r.guestName, t,
                c.waiting(t).length, r.arrivalDateTime.format(Util.DT));
    }

    private void assign() {
        printRooms(c.readyRooms());
        String room = in.line("Ready room ID: ");
        if (in.confirm("Confirm assignment")) {
            Booking b = c.assign(room);
            System.out.printf("SUCCESS: %s assigned room %s. Confirmation %s; status CHECKED_IN.%n", b.guestName,
                    b.roomId, b.confirmationNumber);
        }
    }

    private void cancel() {
        String name = in.line("Guest name: ");
        LocalDateTime arrival = in.dateTime("Exact arrival");
        if (in.confirm("Confirm cancellation"))
            System.out.println(c.cancel(name, arrival));
    }

    private void report() {
        header("WALK-IN REPORT MENU");
        System.out.println(
                "1. Monthly Walk-In Request Summary Report\n2. Room-Type Demand and Queue Report\n0. Return");
        int x = in.integer("Enter your choice: ", 0, 2);
        if (x == 1)
            monthlyRequestSummaryReport();
        else if (x == 2)
            roomTypeDemandReport();
    }

    private void monthlyRequestSummaryReport() {
        int month = in.integer("Month (1-12): ", 1, 12);
        int year = in.integer("Year (2000-2100): ", 2000, 2100);
        WalkInRequest[] rows = c.monthlyRequests(month, year);
        int waiting = 0, completed = 0, cancelled = 0;
        header("MONTHLY WALK-IN REQUEST SUMMARY REPORT");
        System.out.printf("Period: %02d/%d%n", month, year);
        System.out.printf("%-4s| %-20s| %-17s| %-9s| %-7s| %-7s| %-10s%n", "No.", "Guest Name",
                "Arrival Date/Time", "Room Type", "Guests", "Nights", "Status");
        for (int i = 0; i < rows.length; i++) {
            WalkInRequest request = rows[i];
            System.out.printf("%-4d| %-20s| %-17s| %-9s| %-7d| %-7d| %-10s%n", i + 1, request.guestName,
                    request.arrivalDateTime.format(Util.DT), request.requestedRoomType, request.numberOfGuests,
                    request.numberOfNights, request.queueStatus);
            if (request.queueStatus == WalkInStatus.COMPLETED)
                completed++;
            else if (request.queueStatus == WalkInStatus.CANCELLED)
                cancelled++;
            else
                waiting++;
        }
        double completionRate = rows.length == 0 ? 0.0 : completed * 100.0 / rows.length;
        double cancellationRate = rows.length == 0 ? 0.0 : cancelled * 100.0 / rows.length;
        footer(String.format(
                "Total Walk-In Requests : %d%nCompleted Bookings     : %d%nCancelled Requests     : %d%nStill Waiting          : %d%nCompletion Rate        : %.2f%%%nCancellation Rate      : %.2f%%%n",
                rows.length, completed, cancelled, waiting, completionRate, cancellationRate));
    }

    private void roomTypeDemandReport() {
        int month = in.integer("Month (1-12): ", 1, 12);
        int year = in.integer("Year (2000-2100): ", 2000, 2100);
        WalkInRequest[] requests = c.monthlyRequests(month, year);
        RoomType[] types = { RoomType.STD, RoomType.DLX, RoomType.FAM, RoomType.STE };
        int[] total = new int[types.length], completed = new int[types.length], cancelled = new int[types.length],
                waiting = new int[types.length];
        for (int i = 0; i < requests.length; i++) {
            int typeIndex = requests[i].requestedRoomType.ordinal();
            total[typeIndex]++;
            if (requests[i].queueStatus == WalkInStatus.COMPLETED)
                completed[typeIndex]++;
            else if (requests[i].queueStatus == WalkInStatus.CANCELLED)
                cancelled[typeIndex]++;
            else
                waiting[typeIndex]++;
        }
        header("ROOM-TYPE DEMAND AND QUEUE REPORT");
        System.out.printf("Period: %02d/%d%n", month, year);
        System.out.printf("%-10s| %-15s| %-11s| %-11s| %-9s%n", "Room Type", "Total Requests", "Completed",
                "Cancelled", "Waiting");
        int maximumDemand = 0, maximumWaiting = 0, totalWaiting = 0;
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%-10s| %-15d| %-11d| %-11d| %-9d%n", types[i], total[i], completed[i],
                    cancelled[i], waiting[i]);
            if (total[i] > maximumDemand)
                maximumDemand = total[i];
            if (waiting[i] > maximumWaiting)
                maximumWaiting = waiting[i];
            totalWaiting += waiting[i];
        }
        StringBuilder mostRequested = new StringBuilder(), highestQueue = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            if (maximumDemand > 0 && total[i] == maximumDemand)
                appendType(mostRequested, types[i]);
            if (maximumWaiting > 0 && waiting[i] == maximumWaiting)
                appendType(highestQueue, types[i]);
        }
        footer(String.format(
                "Most Requested Room Type : %s%nHighest Waiting Queue     : %s%nTotal Guests Waiting      : %d%n",
                mostRequested.length() == 0 ? "-" : mostRequested.toString(),
                highestQueue.length() == 0 ? "-" : highestQueue.toString(), totalWaiting));
    }

    private void appendType(StringBuilder value, RoomType type) {
        if (value.length() > 0)
            value.append(" and ");
        value.append(type);
    }

    private void printRequests(String title, Object[] rows) {
        header(title);
        System.out.printf("%-4s %-15s %-17s %-6s %-6s %-10s%n", "Pos", "Guest", "Arrival", "Guests", "Nights",
                "Status");
        for (int i = 0; i < rows.length; i++) {
            WalkInRequest r = (WalkInRequest) rows[i];
            System.out.printf("%-4d %-15s %-17s %-6d %-6d %-10s%n", i + 1, r.guestName,
                    r.arrivalDateTime.format(Util.DT), r.numberOfGuests, r.numberOfNights, r.queueStatus);
        }
        footer(String.format("Total: " + rows.length));
    }

    private void overallWaitingQueue() {
        FrontDeskServiceUI.header("OVERALL WAITING QUEUE");

        System.out.printf(
                "%-4s| %-22s| %-17s| %-6s| %-6s| %-10s%n",
                "Pos", "Guest", "Arrival", "Guests", "Nights", "Status");

        RoomType[] roomTypes = RoomType.values();
        int position = 1;

        for (int typeIndex = 0; typeIndex < roomTypes.length; typeIndex++) {
            Object[] rows = c.waiting(roomTypes[typeIndex]);

            for (int i = 0; i < rows.length; i++) {
                WalkInRequest request = (WalkInRequest) rows[i];

                System.out.printf(
                        "%-4d| %-22s| %-17s| %-6d| %-6d| %-10s%n",
                        position++,
                        request.guestName,
                        request.arrivalDateTime.format(Util.DT),
                        request.numberOfGuests,
                        request.numberOfNights,
                        request.queueStatus);
            }
        }

        FrontDeskServiceUI.footer(String.format("Total Waiting Requests: %d%n", position - 1));
    }

    private void printHistory(String title, Object[] rows, WalkInStatus status) {
        FrontDeskServiceUI.header(title);
        int n = 0;
        System.out.printf("%-22s| %-17s| %-10s| %-10s%n", "Guest", "Arrival", "Room Type", "Status");
        for (int i = 0; i < rows.length; i++) {
            WalkInRequest r = (WalkInRequest) rows[i];
            if (r.queueStatus == status) {
                System.out.printf("%-22s| %-17s| %-10s| %-10s%n", r.guestName, r.arrivalDateTime.format(Util.DT),
                        r.requestedRoomType, r.queueStatus);
                n++;
            }
        }
        FrontDeskServiceUI.footer(String.format("Total: " + n + "\n"));
    }

    private void printRooms(Room[] a) {
        FrontDeskServiceUI.header("AVAILABLE ROOMS");
        System.out.printf("%-8s %-6s %-8s %-8s%n", "Room", "Type", "Capacity", "Status");
        for (int i = 0; i < a.length; i++)
            System.out.printf("%-8s %-6s %-8d %-8s%n", a[i].roomId, a[i].roomType, a[i].capacity, a[i].roomStatus);
        System.out.println("Total: " + a.length);
    }

    static void header(String title) {
        System.out.println("==============================================================");
        System.out.printf("%30s%n", title);
        System.out.println("==============================================================");
    }

    static void footer(String result) {
        System.out.println("==============================================================");
        System.out.println(result);
    }
}
