package tarumtresorts.control;

import java.time.*;
import tarumtresorts.adt.*;
import tarumtresorts.entity.*;
import tarumtresorts.entity.Types.*;

/**
 * Author: <Your Name>. Housekeeping state transitions and conflict
 * coordination.
 */
public class HousekeepingControl {
    private final SharedDataContext d;

    public HousekeepingControl(SharedDataContext d) {
        this.d = d;
    }

    private HousekeepingTask activeFor(String room) {
        HashTable.Entry<String, HousekeepingTask>[] e = d.tasks.entries();
        for (int i = 0; i < e.length; i++) {
            HousekeepingTask t = e[i].value;
            if (t.roomId.equals(room) && (t.taskStatus == TaskStatus.IPG || t.taskStatus == TaskStatus.PST))
                return t;
        }
        return null;
    }

    public HousekeepingTask createTask(String roomId, String staffId, Priority priority, String remarks) {
        Room room = d.rooms.get(roomId);
        HousekeepingStaff staff = d.staff.get(staffId);
        if (room == null || room.roomStatus != RoomStatus.DIR)
            throw new IllegalArgumentException("Only an existing DIR room can be cleaned.");
        if (activeFor(roomId) != null)
            throw new IllegalArgumentException("Room already has an active task.");
        if (staff == null || staff.staffStatus != StaffStatus.AVL || staff.currentRoomId != null)
            throw new IllegalArgumentException("Staff must be AVL.");
        String id = nextTaskId();
        HousekeepingTask task = new HousekeepingTask(id, LocalDate.now(), roomId, staffId,
                LocalTime.now().withSecond(0).withNano(0), null, TaskStatus.IPG, priority,
                remarks == null ? "" : remarks.trim());
        d.tasks.put(id, task);
        room.roomStatus = RoomStatus.CLN;
        staff.staffStatus = StaffStatus.BSY;
        staff.currentRoomId = roomId;
        d.persist();
        return task;
    }

    public HousekeepingTask complete(String roomId) {
        Room room = d.rooms.get(roomId);
        HousekeepingTask task = activeFor(roomId);
        if (room == null || room.roomStatus != RoomStatus.CLN || task == null || task.taskStatus != TaskStatus.IPG)
            throw new IllegalArgumentException("Room must be CLN with an IPG task.");
        HousekeepingStaff staff = d.staff.get(task.assignedStaffId);
        task.taskStatus = TaskStatus.COM;
        task.completionTime = LocalTime.now().withSecond(0).withNano(0);
        room.roomStatus = RoomStatus.INS;
        if (staff != null) {
            staff.staffStatus = StaffStatus.AVL;
            staff.currentRoomId = null;
        }
        d.persist();
        return task;
    }

    public void makeReady(String roomId) {
        Room r = d.rooms.get(roomId);
        if (r == null || r.roomStatus != RoomStatus.INS)
            throw new IllegalArgumentException("Only INS rooms can become RDY.");
        r.roomStatus = RoomStatus.RDY;
        d.persist();
    }

    public int makeAllReady() {
        HashTable.Entry<String, Room>[] e = d.rooms.entries();
        int n = 0;
        for (int i = 0; i < e.length; i++)
            if (e[i].value.roomStatus == RoomStatus.INS) {
                e[i].value.roomStatus = RoomStatus.RDY;
                n++;
            }
        if (n > 0)
            d.persist();
        return n;
    }

    public void handleLateCheckoutConflict(String roomId) {
        Room room = d.rooms.get(roomId);
        if (room == null || room.roomStatus != RoomStatus.LCO)
            return;
        HousekeepingTask task = activeFor(roomId);
        if (task != null && task.taskStatus == TaskStatus.IPG) {
            HousekeepingStaff s = d.staff.get(task.assignedStaffId);
            d.roomHistory.add(new RoomStatusHistory(roomId, RoomStatus.CLN, task.taskStatus, task.assignedStaffId,
                    task.startTime, LocalDateTime.now()));
            task.taskStatus = TaskStatus.PST;
            if (s != null) {
                s.staffStatus = StaffStatus.AVL;
                s.currentRoomId = null;
            }
            room.roomStatus = RoomStatus.LCO;
        }
    }

    public Room[] roomsByStatus(RoomStatus status) {
        HashTable.Entry<String, Room>[] e = d.rooms.entries();
        Room[] tmp = new Room[e.length];
        int n = 0;
        for (int i = 0; i < e.length; i++)
            if (e[i].value.roomStatus == status)
                tmp[n++] = e[i].value;
        Room[] out = new Room[n];
        System.arraycopy(tmp, 0, out, 0, n);
        return out;
    }

    public HousekeepingStaff[] availableStaff() {
        HashTable.Entry<String, HousekeepingStaff>[] e = d.staff.entries();
        HousekeepingStaff[] tmp = new HousekeepingStaff[e.length];
        int n = 0;
        for (int i = 0; i < e.length; i++)
            if (e[i].value.staffStatus == StaffStatus.AVL)
                tmp[n++] = e[i].value;
        HousekeepingStaff[] out = new HousekeepingStaff[n];
        System.arraycopy(tmp, 0, out, 0, n);
        return out;
    }

    public HashTable.Entry<String, HousekeepingTask>[] allTasks() {
        return d.tasks.entries();
    }

    public HashTable.Entry<String, HousekeepingStaff>[] allStaff() {
        return d.staff.entries();
    }

    /** Finds staff by exact ID or exact name using the custom staff hash table. */
    public HousekeepingStaff findStaff(String query) {
        if (query == null || query.trim().isEmpty())
            throw new IllegalArgumentException("Staff ID or name is required.");
        String value = query.trim();
        HousekeepingStaff byId = d.staff.get(value.toUpperCase());
        if (byId != null)
            return byId;
        HashTable.Entry<String, HousekeepingStaff>[] entries = d.staff.entries();
        for (int i = 0; i < entries.length; i++)
            if (entries[i].value.staffName.equalsIgnoreCase(value))
                return entries[i].value;
        throw new IllegalArgumentException("Staff not found. Enter an ID or exact name shown in the table.");
    }

    /**
     * Filters tasks assigned to one staff member and explicitly sorts by date,
     * start time, then task ID.
     */
    public HousekeepingTask[] staffTasks(String staffId, Integer month, Integer year) {
        if (d.staff.get(staffId) == null)
            throw new IllegalArgumentException("Staff not found.");
        if ((month == null) != (year == null))
            throw new IllegalArgumentException("Month and year must be supplied together.");
        if (month != null && (month < 1 || month > 12 || year < 2000 || year > 2100))
            throw new IllegalArgumentException("Invalid month or year.");
        HashTable.Entry<String, HousekeepingTask>[] entries = d.tasks.entries();
        HousekeepingTask[] temp = new HousekeepingTask[entries.length];
        int count = 0;
        for (int i = 0; i < entries.length; i++) {
            HousekeepingTask task = entries[i].value;
            if (task.assignedStaffId.equalsIgnoreCase(staffId)
                    && (month == null || (task.taskDate.getMonthValue() == month && task.taskDate.getYear() == year)))
                temp[count++] = task;
        }
        HousekeepingTask[] result = new HousekeepingTask[count];
        System.arraycopy(temp, 0, result, 0, count);
        for (int i = 1; i < result.length; i++) {
            HousekeepingTask current = result[i];
            int j = i - 1;
            while (j >= 0 && compareTask(result[j], current) > 0) {
                result[j + 1] = result[j];
                j--;
            }
            result[j + 1] = current;
        }
        return result;
    }

    private int compareTask(HousekeepingTask left, HousekeepingTask right) {
        int date = left.taskDate.compareTo(right.taskDate);
        if (date != 0)
            return date;
        int time = left.startTime.compareTo(right.startTime);
        return time != 0 ? time : left.taskId.compareTo(right.taskId);
    }

    private String nextTaskId() {
        int n = d.tasks.size() + 1;
        String id;
        do {
            id = String.format("T%04d", n++);
        } while (d.tasks.containsKey(id));
        return id;
    }
}
