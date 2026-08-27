package tarumtresorts.boundary;

import java.time.*;
import tarumtresorts.adt.*;
import tarumtresorts.control.*;
import tarumtresorts.entity.*;
import tarumtresorts.entity.Types.*;
import tarumtresorts.utility.*;

/** Author: <Your Name>. Front-Desk boundary. */
public class FrontDeskServiceUI {
    private final FrontDeskControl c;
    private final ConsoleInput in;

    public FrontDeskServiceUI(FrontDeskControl c, ConsoleInput in) {
        this.c = c;
        this.in = in;
    }

    public void run() {
        while (true) {
            WalkInBookingUI.header("FRONT-DESK SERVICE");
            System.out.println(
                    "1. Search Guest\n2. Search Room Availability\n3. Handle Late Checkout\n4. Process Guest Checkout\n5. Generate Reports\n0. Return to Main Menu");
            int x = in.integer("Enter your choice: ", 0, 5);
            try {
                if (x == 1)
                    print(search());
                else if (x == 2)
                    availability();
                else if (x == 3)
                    late();
                else if (x == 4)
                    checkout();
                else if (x == 5)
                    reports();
                else
                    return;
            } catch (Exception ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }
    }

    private Booking[] search() {
        Booking[] a = c.search(in.line("Enter confirmation number or guest name to search: "));
        if (a.length == 0)
            System.out.println("No matching guest.");
        return a;
    }

    private Booking choose() {
        Booking[] a = search();
        print(a);
        if (a.length == 0)
            return null;
        if (a.length == 1)
            return a[0];
        String confirmation = in.line("Select confirmation number: ");
        for (int i = 0; i < a.length; i++)
            if (a[i].confirmationNumber.equals(confirmation))
                return a[i];
        throw new IllegalArgumentException("Select a displayed confirmation.");
    }

    private void availability() {
        RoomType t = in.roomType();
        int guests = in.integer("Number of guests: ", 1, 20);
        Room[] a = c.availability(t, guests);
        WalkInBookingUI.header("ROOM AVAILABILITY");
        for (int i = 0; i < a.length; i++)
            System.out.printf("%-6s %-4s %-3d %-4s%n", a[i].roomId, a[i].roomType, a[i].capacity, a[i].roomStatus);
        System.out.println("Total: " + a.length);
    }

    private void late() {
        Booking b = choose();
        if (b == null)
            return;
        LocalDateTime later = in.dateTime("New expected checkout");
        if (in.confirm("Confirm late checkout")) {
            LocalDateTime old = b.expectedCheckOutDateTime;
            c.lateCheckout(b.confirmationNumber, later);
            System.out.printf("Booking %s checkout %s -> %s; room OCC -> LCO.%n", b.confirmationNumber,
                    old.format(Util.DT), later.format(Util.DT));
        }
    }

    private void checkout() {
        Booking b = choose();
        if (b != null && in.confirm("Confirm guest checkout")) {
            c.checkout(b.confirmationNumber);
            System.out.printf("Booking %s -> CHECKED_OUT; room %s -> DIR.%n", b.confirmationNumber, b.roomId);
        }
    }

    private void reports() {
        int x = in.integer("1. Booking Report\n2. Room Status Summary Report\n0. Return\nChoice: ", 0, 2);
        if (x == 1) {
            bookingReport();
        } else if (x == 2)
            roomStatusSummary();
    }

    private void bookingReport() {
        int choice = in.integer("1.Monthly Booking Report\n2.Overall Booking Report\n3.Return\nChoice: ", 1, 3);
        if (choice == 1) {
            monthlyBookingReport();
        } else if (choice == 2) {
            overallBookingReport();
        } else {
            return;
        }
    }

    private void overallBookingReport() {
        int total = 0, inCount = 0, out = 0, cancel = 0;
        BinarySearchTree.Entry<String, Booking>[] e = c.allBookings();
        header("OVERALL BOOKING REPORT");
        System.out.printf("%-13s| %-20s| %-8s| %-10s| %-11s| %-16s| %-12s%n", "Confirmation", "Guest Name", "Room ID",
                "Room Type", "Status", "Check-in", "Check-out");
        for (int i = 0; i < e.length; i++) {
            Booking b = e[i].value;
            System.out.printf("%-13s| %-20s| %-8s| %-10s| %-11s| %-16s| %-12s%n", b.confirmationNumber, b.guestName,
                    b.roomId,
                    b.roomType, b.bookingStatus, b.checkInDateTime.format(Util.DT),
                    b.expectedCheckOutDateTime.format(Util.DT));
            total++;
            if (b.bookingStatus == BookingStatus.CHECKED_IN)
                inCount++;
            else if (b.bookingStatus == BookingStatus.CHECKED_OUT)
                out++;
            else
                cancel++;
        }
        footer(String.format("Total: %d | Checked-in: %d | Checked-out: %d | Cancelled: %d%n", total, inCount, out,
                cancel));
    }

    private void monthlyBookingReport() {
        int m = in.integer("Month: ", 1, 12), y = in.integer("Year: ", 2000, 2200), inCount = 0, out = 0,
                cancel = 0, total = 0;
        BinarySearchTree.Entry<String, Booking>[] e = c.allBookings();
        header("MONTHLY BOOKING REPORT");
        System.out.printf("%-13s| %-20s| %-8s| %-10s| %-11s| %-16s| %-12s%n", "Confirmation", "Guest Name", "Room ID",
                "Room Type", "Status", "Check-in", "Check-out");

        for (int i = 0; i < e.length; i++) {
            Booking b = e[i].value;
            if (b.checkInDateTime.getMonthValue() == m && b.checkInDateTime.getYear() == y) {
                System.out.printf("%-13s| %-20s| %-8s| %-10s| %-11s| %-16s| %-12s%n", b.confirmationNumber, b.guestName,
                        b.roomId,
                        b.roomType, b.bookingStatus, b.checkInDateTime.format(Util.DT),
                        b.expectedCheckOutDateTime.format(Util.DT));
                total++;
                if (b.bookingStatus == BookingStatus.CHECKED_IN)
                    inCount++;
                else if (b.bookingStatus == BookingStatus.CHECKED_OUT)
                    out++;
                else
                    cancel++;
            }
        }
        footer(String.format("Total: %d  Checked-in: %d  Checked-out: %d  Cancelled: %d%n", total, inCount, out,
                cancel));
    }

    private void roomStatusSummary() {
        String statusInput = in.line("Status (ALL/OCC/LCO/DIR/CLN/INS/RDY): ").toUpperCase();
        RoomStatus status = null;
        if (!statusInput.equals("ALL") && !statusInput.isEmpty())
            try {
                status = RoomStatus.valueOf(statusInput);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid room status filter.");
            }
        String typeInput = in.line("Room type (ALL/STD/DLX/FAM/STE): ").toUpperCase();
        RoomType type = null;
        if (!typeInput.equals("ALL") && !typeInput.isEmpty())
            try {
                type = RoomType.valueOf(typeInput);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid room type filter.");
            }
        int floor = in.integer("Floor (0 for ALL, 1-9): ", 0, 9);
        Room[] rooms = c.roomStatusSummary(status, type, floor);
        int occ = 0, lco = 0, dir = 0, cln = 0, ins = 0, rdy = 0;
        WalkInBookingUI.header("ROOM STATUS SUMMARY REPORT");
        System.out.printf("Filters: Status=%s  Type=%s  Floor=%s%n", status == null ? "ALL" : status,
                type == null ? "ALL" : type, floor == 0 ? "ALL" : String.valueOf(floor));
        System.out.println("--------------------------------------------------------------");
        System.out.printf("%-8s| %-6s| %-10s| %-8s| %-6s%n", "Room ID", "Type", "Capacity", "Status", "Floor");

        for (int i = 0; i < rooms.length; i++) {
            Room room = rooms[i];
            System.out.printf("%-8s| %-6s| %-10d| %-8s| %-6c%n", room.roomId, room.roomType, room.capacity,
                    room.roomStatus,
                    room.roomId.charAt(0));
            switch (room.roomStatus) {
                case OCC:
                    occ++;
                    break;
                case LCO:
                    lco++;
                    break;
                case DIR:
                    dir++;
                    break;
                case CLN:
                    cln++;
                    break;
                case INS:
                    ins++;
                    break;
                case RDY:
                    rdy++;
                    break;
            }
        }
        WalkInBookingUI.footer(
                String.format("OCC: %d  LCO: %d  DIR: %d  CLN: %d  INS: %d  RDY: %d \nTotal rooms displayed: %d%n",
                        occ, lco, dir, cln, ins, rdy, rooms.length));
    }

    private void print(Booking[] a) {
        for (int i = 0; i < a.length; i++) {
            Booking b = a[i];
            System.out.printf("%-8s %-20s Room %-5s %-4s %-11s In %s Out %s%n", b.confirmationNumber, b.guestName,
                    b.roomId, b.roomType, b.bookingStatus, b.checkInDateTime.format(Util.DT),
                    b.expectedCheckOutDateTime.format(Util.DT));
        }
    }

    static void header(String title) {
        System.out.println(
                "==========================================================================================================");
        System.out.printf("%50s%n", title);
        System.out.println(
                "==========================================================================================================");
    }

    static void footer(String result) {
        System.out.println(
                "==========================================================================================================");
        System.out.println(result);
    }
}
