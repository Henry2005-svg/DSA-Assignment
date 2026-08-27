package tarumtresorts.boundary;

import tarumtresorts.adt.*;
import tarumtresorts.control.*;
import tarumtresorts.entity.*;
import tarumtresorts.entity.Types.*;
import tarumtresorts.utility.*;

/** Author: <Your Name>. Housekeeping boundary. */
public class HousekeepingTaskUI {
    private final HousekeepingControl c;
    private final ConsoleInput in;

    public HousekeepingTaskUI(HousekeepingControl c, ConsoleInput in) {
        this.c = c;
        this.in = in;
    }

    public void run() {
        while (true) {
            WalkInBookingUI.header("HOUSEKEEPING AND TASK LOG");
            System.out.println(
                    "1. Display Information\n2. Manage Tasks\n3. Check and Update Room Status\n4. Generate Reports\n0. Return to Main Menu");
            int x = in.integer("Enter your choice: ", 0, 4);
            try {
                if (x == 1)
                    display();
                else if (x == 2)
                    manage();
                else if (x == 3)
                    update();
                else if (x == 4)
                    report();
                else
                    return;
            } catch (Exception ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }
    }

    private void display() {
        WalkInBookingUI.header("DISPLAY INFORMATION");
        int x = in.integer("1. Display All Rooms\n2. Display All Staff\n3. Display All Tasks\n0. Return\nChoice: ", 0,
                3);
        if (x == 1) {
            RoomStatus[] s = RoomStatus.values();
            for (int i = 0; i < s.length; i++)
                rooms(c.roomsByStatus(s[i]), s[i] + " ROOMS");
        } else if (x == 2) {
            printAllStaff();
        } else if (x == 3)
            tasks();
    }

    private void manage() {
        WalkInBookingUI.header("MANAGE TASKS");
        int x = in.integer(
                "1. Add Housekeeping Task\n2. Complete Housekeeping Task\n3. Handle Late Checkout\n0. Return\nChoice: ",
                0, 3);
        if (x == 1) {
            rooms(c.roomsByStatus(RoomStatus.DIR), "DIR ROOMS");
            HousekeepingStaff[] staff = c.availableStaff();
            for (int i = 0; i < staff.length; i++)
                System.out.printf("%-5s %s%n", staff[i].staffId, staff[i].staffName);
            String room = in.line("Room ID: "), sid = in.line("Staff ID: ");
            Priority p = Priority.valueOf(in.line("Priority (LOW/MEDIUM/HIGH): ").toUpperCase());
            HousekeepingTask t = c.createTask(room, sid, p, in.line("Remarks: "));
            System.out.println("Task " + t.taskId + " IPG; room DIR -> CLN; staff AVL -> BSY.");
        } else if (x == 2) {
            rooms(c.roomsByStatus(RoomStatus.CLN), "CLN ROOMS");
            String room = in.line("Room ID: ");
            if (in.confirm("Confirm completion")) {
                HousekeepingTask t = c.complete(room);
                System.out.println("Task " + t.taskId + " COM; room CLN -> INS; staff -> AVL.");
            }
        } else if (x == 3)
            rooms(c.roomsByStatus(RoomStatus.LCO), "LATE CHECKOUT ROOMS");
    }

    private void update() {
        rooms(c.roomsByStatus(RoomStatus.INS), "INSPECTED ROOMS");
        int x = in.integer(
                "1. Update All Inspected Rooms to Ready\n2. Choose a Room to Update to Ready\n3. Return\nChoice: ", 1,
                3);
        if (x == 1 && in.confirm("Update all"))
            System.out.println("Updated: " + c.makeAllReady());
        else if (x == 2) {
            String id = in.line("Room ID: ");
            if (in.confirm("Update room")) {
                c.makeReady(id);
                System.out.println(id + " INS -> RDY");
            }
        }
    }

    private void report() {
        WalkInBookingUI.header("HOUSEKEEPING REPORTS");
        int choice = in.integer(
                "1. Housekeeping Task Report\n2. Staff Report\n0. Return\nChoice: ", 0,
                2);
        if (choice == 1)
            TaskReport();
        else if (choice == 2)
            staffTaskReport();
    }

    private void TaskReport() {
        int choice = in.integer(
                "1. Overall Task Report\n2. Monthly Task Report\n3. Return\nChoice: ", 1,
                3);
        if (choice == 1)
            printAllTasks();
        else if (choice == 2)
            monthlyTaskReport();
    }

    private void printAllTasks() {
        WalkInBookingUI.header("ALL TASKS");
        System.out.printf("%-9s| %-20s| %-7s| %-10s%n", "Task ID", "Date", "Room ID", "Staff ID", "Status");
        HashTable.Entry<String, HousekeepingTask>[] e = c.allTasks();
        for (int i = 0; i < e.length; i++) {
            HousekeepingTask s = e[i].value;
            System.out.printf("%-9s| %-20s| %-7s| %-10s%n", s.taskId, s.taskDate, s.roomId, s.assignedStaffId,
                    s.taskStatus);
        }
        WalkInBookingUI.footer(String.format("Total Staff: %d%n", e.length));
    }

    private void monthlyTaskReport() {
        int m = in.integer("Month: ", 1, 12), y = in.integer("Year: ", 2000, 2200), n = 0, completed = 0, ongoing = 0;
        HashTable.Entry<String, HousekeepingTask>[] e = c.allTasks();
        WalkInBookingUI.header("\tMONTHLY HOUSEKEEPING TASK REPORT");
        System.out.printf("%-7s| %-12s| %-8s| %-8s| %-7s| %-10s%n", "Task ID", "Date", "Room ID", "Staff ID", "Status",
                "Priority");
        for (int i = 0; i < e.length; i++) {
            HousekeepingTask t = e[i].value;
            if (t.taskDate.getMonthValue() == m && t.taskDate.getYear() == y) {
                System.out.printf("%-7s| %-12s| %-8s| %-8s| %-7s| %-10s%n", t.taskId, t.taskDate, t.roomId,
                        t.assignedStaffId, t.taskStatus, t.priority);
                n++;
                if (t.taskStatus == TaskStatus.COM)
                    completed++;
                else if (t.taskStatus == TaskStatus.IPG)
                    ongoing++;
            }
        }
        WalkInBookingUI.footer(String.format("Total: %d  Completed: %d  In Progress: %d%n", n, completed, ongoing));
    }

    private void staffTaskReport() {
        printAllStaff();
        String query = in.line("Enter staff ID or exact staff name: ");
        HousekeepingStaff staff = c.findStaff(query);
        System.out.printf("Selected Staff: %s - %s (%s)%n", staff.staffId, staff.staffName, staff.staffStatus);
        int period = in.integer("1. Overall Task Report\n2. One Month Task Report\n0. Return\nChoice: ", 0, 2);
        if (period == 0)
            return;
        Integer month = null, year = null;
        if (period == 2) {
            month = Integer.valueOf(in.integer("Month (1-12): ", 1, 12));
            year = Integer.valueOf(in.integer("Year (2000-2100): ", 2000, 2100));
        }
        HousekeepingTask[] rows = c.staffTasks(staff.staffId, month, year);
        WalkInBookingUI.header(period == 1 ? "STAFF OVERALL TASK REPORT" : "STAFF MONTHLY TASK REPORT");
        System.out.printf("Staff ID: %s  Staff Name: %s%n", staff.staffId, staff.staffName);
        if (period == 2)
            System.out.printf("Period: %02d/%d%n", month.intValue(), year.intValue());
        System.out.printf("%-8s| %-12s| %-8s| %-7s| %-10s%n", "Task ID", "Date", "Room ID", "Status", "Priority");
        for (int i = 0; i < rows.length; i++) {
            HousekeepingTask t = rows[i];
            System.out.printf("%-8s| %-12s| %-8s| %-7s| %-10s%n", t.taskId, t.taskDate, t.roomId, t.taskStatus,
                    t.priority);
        }
        WalkInBookingUI.footer(String.format("Total Tasks: %d%n", rows.length));
    }

    private void printAllStaff() {
        WalkInBookingUI.header("ALL STAFF");
        System.out.printf("%-9s| %-20s| %-7s| %-10s%n", "Staff ID", "Name", "Status", "Current Room");
        HashTable.Entry<String, HousekeepingStaff>[] e = c.allStaff();
        for (int i = 0; i < e.length; i++) {
            HousekeepingStaff s = e[i].value;
            System.out.printf("%-9s| %-20s| %-7s| %-10s%n", s.staffId, s.staffName, s.staffStatus,
                    Util.show(s.currentRoomId));
        }
        WalkInBookingUI.footer(String.format("Total Staff: %d%n", e.length));
    }

    private void rooms(Room[] a, String title) {
        WalkInBookingUI.header(title);
        System.out.printf("%-8s| %-15s| %-9s| %-7s| %-17s%n", "Room ID", "Room Type", "Capacity", "Status",
                "Check-out Time");
        for (int i = 0; i < a.length; i++)
            System.out.printf("%-8s| %-15s| %-9d| %-7s| %-17s%n", a[i].roomId, a[i].roomType, a[i].capacity,
                    a[i].roomStatus, Util.show(a[i].checkoutTime));
        WalkInBookingUI.footer(String.format("Total Rooms: %d%n%n", a.length));
    }

    private void tasks() {
        HashTable.Entry<String, HousekeepingTask>[] e = c.allTasks();
        WalkInBookingUI.header("ALL TASK");
        System.out.printf("%-8s| %-15s| %-7s| %-6s| %-5s| %-6s%n", "Task ID", "Task Date", "Room ID", "Staff ID",
                "Status", "Priority");
        for (int i = 0; i < e.length; i++) {
            HousekeepingTask t = e[i].value;
            System.out.printf("%-8s| %-15s| %-7s| %-6s| %-5s| %-6s%n", t.taskId, t.taskDate, t.roomId,
                    t.assignedStaffId, t.taskStatus, t.priority);
        }
        WalkInBookingUI.footer(String.format("Total Tasks: %d%n", e.length));
    }
}
