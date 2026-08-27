package tarumtresorts.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import tarumtresorts.adt.*;
import tarumtresorts.control.*;
import tarumtresorts.entity.*;
import tarumtresorts.entity.Types.*;
import tarumtresorts.utility.Util;

/** Author: <Your Name>. Six-file UTF-8 persistence outside boundary classes. */
public class DataFileControl {
    private final Path dir;

    public DataFileControl(Path dir) {
        this.dir = dir;
    }

    public void loadAll(SharedDataContext d) throws IOException {
        Files.createDirectories(dir);
        ensureDataFiles();
        loadRooms(d);
        loadRequests(d);
        loadBookings(d);
        loadStaff(d);
        loadTasks(d);
        loadHistory(d);
    }

    private void ensureDataFiles() throws IOException {
        ensure("rooms.txt", "roomId,roomType,capacity,roomStatus,confirmationNumber,checkoutTime");
        ensure("walk_in_requests.txt",
                "guestName,arrivalDateTime,requestedRoomType,numberOfGuests,numberOfNights,queueStatus");
        ensure("bookings.txt",
                "confirmationNumber,guestName,roomId,roomType,checkInDateTime,expectedCheckOutDateTime,actualCheckOutDateTime,numberOfGuests,numberOfNights,bookingStatus");
        ensure("housekeeping_staff.txt", "staffId,staffName,staffStatus,currentRoomId");
        ensure("housekeeping_tasks.txt",
                "taskId,taskDate,roomId,assignedStaffId,startTime,completionTime,taskStatus,priority,remarks");
        ensure("room_status_history.txt",
                "roomId,previousRoomStatus,previousTaskStatus,previousStaffId,previousStartTime,updateDateTime");
    }

    private void ensure(String file, String header) throws IOException {
        Path p = dir.resolve(file);
        if (!Files.exists(p)) {
            try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                w.write(header);
                w.newLine();
            }
        }
    }

    private String[] lines(String file) throws IOException {
        Path p = dir.resolve(file);
        LinearList<String> values = new LinearList<String>();
        try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null)
                values.add(line);
        }
        Object[] raw = values.toArray();
        String[] out = new String[raw.length];
        for (int i = 0; i < out.length; i++)
            out[i] = (String) raw[i];
        return out;
    }

    private void loadRooms(SharedDataContext d) throws IOException {
        String[] l = lines("rooms.txt");
        for (int i = 1; i < l.length; i++) {
            String[] f = l[i].split(",", -1);
            if (f.length >= 6)
                d.rooms.put(f[0], new Room(f[0], RoomType.valueOf(f[1]), Integer.parseInt(f[2]),
                        RoomStatus.valueOf(f[3]), f[4], dt(f[5])));
        }
    }

    private void loadRequests(SharedDataContext d) throws IOException {
        String[] l = lines("walk_in_requests.txt");
        for (int i = 1; i < l.length; i++) {
            String[] f = l[i].split(",", -1);
            if (f.length >= 6) {
                WalkInRequest r = new WalkInRequest(f[0], LocalDateTime.parse(f[1]), RoomType.valueOf(f[2]),
                        Integer.parseInt(f[3]), Integer.parseInt(f[4]), WalkInStatus.valueOf(f[5]));
                if (r.queueStatus == WalkInStatus.WAITING)
                    d.queue(r.requestedRoomType).enqueue(r);
                else
                    d.requestHistory.add(r);
            }
        }
    }

    private void loadBookings(SharedDataContext d) throws IOException {
        String[] l = lines("bookings.txt");
        for (int i = 1; i < l.length; i++) {
            String[] f = l[i].split(",", -1);
            if (f.length >= 10) {
                Booking b = new Booking(f[0], f[1], f[2], RoomType.valueOf(f[3]), LocalDateTime.parse(f[4]),
                        LocalDateTime.parse(f[5]), dt(f[6]), Integer.parseInt(f[7]), Integer.parseInt(f[8]),
                        BookingStatus.valueOf(f[9]));
                d.bookings.insert(b.confirmationNumber, b);
                d.guestTrie.insert(Util.normalizeName(b.guestName), b.confirmationNumber);
            }
        }
    }

    private void loadStaff(SharedDataContext d) throws IOException {
        String[] l = lines("housekeeping_staff.txt");
        for (int i = 1; i < l.length; i++) {
            String[] f = l[i].split(",", -1);
            if (f.length >= 4) {
                HousekeepingStaff staff = new HousekeepingStaff(f[0], f[1], StaffStatus.valueOf(f[2]), f[3]);
                d.staff.put(staff.staffId, staff);
            }
        }
    }

    private void loadTasks(SharedDataContext d) throws IOException {
        String[] l = lines("housekeeping_tasks.txt");
        for (int i = 1; i < l.length; i++) {
            String[] f = l[i].split(",", -1);
            if (f.length >= 9)
                d.tasks.put(f[0], new HousekeepingTask(f[0], LocalDate.parse(f[1]), f[2], f[3], LocalTime.parse(f[4]),
                        time(f[5]), TaskStatus.valueOf(f[6]), Priority.valueOf(f[7]), f[8]));
        }
    }

    private void loadHistory(SharedDataContext d) throws IOException {
        String[] l = lines("room_status_history.txt");
        for (int i = 1; i < l.length; i++) {
            String[] f = l[i].split(",", -1);
            if (f.length >= 6)
                d.roomHistory.add(new RoomStatusHistory(f[0], RoomStatus.valueOf(f[1]), TaskStatus.valueOf(f[2]),
                        empty(f[3]), time(f[4]), LocalDateTime.parse(f[5])));
        }
    }

    private LocalDateTime dt(String s) {
        return s == null || s.isEmpty() ? null : LocalDateTime.parse(s);
    }

    private LocalTime time(String s) {
        return s == null || s.isEmpty() ? null : LocalTime.parse(s);
    }

    private String empty(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    public void saveAll(SharedDataContext d) throws IOException {
        Files.createDirectories(dir);
        saveRooms(d);
        saveRequests(d);
        saveBookings(d);
        saveStaff(d);
        saveTasks(d);
        saveHistory(d);
    }

    private void write(String file, String header, String[] rows) throws IOException {
        Path target = dir.resolve(file), tmp = dir.resolve(file + ".tmp");
        try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            w.write(header);
            w.newLine();
            for (int i = 0; i < rows.length; i++) {
                w.write(rows[i]);
                w.newLine();
            }
        }
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void saveRooms(SharedDataContext d) throws IOException {
        HashTable.Entry<String, Room>[] e = d.rooms.entries();
        String[] a = new String[e.length];
        for (int i = 0; i < a.length; i++) {
            Room r = e[i].value;
            a[i] = r.roomId + "," + r.roomType + "," + r.capacity + "," + r.roomStatus + "," + val(r.confirmationNumber)
                    + "," + val(r.checkoutTime);
        }
        write("rooms.txt", "roomId,roomType,capacity,roomStatus,confirmationNumber,checkoutTime", a);
    }

    private void saveRequests(SharedDataContext d) throws IOException {
        int n = d.requestHistory.size() + d.standardQueue.size() + d.deluxeQueue.size() + d.familyQueue.size()
                + d.suiteQueue.size();
        String[] a = new String[n];
        int p = 0;
        RoomType[] types = RoomType.values();
        for (int t = 0; t < types.length; t++) {
            Object[] q = d.queue(types[t]).toArray();
            for (int i = 0; i < q.length; i++)
                a[p++] = request((WalkInRequest) q[i]);
        }
        Object[] h = d.requestHistory.toArray();
        for (int i = 0; i < h.length; i++)
            a[p++] = request((WalkInRequest) h[i]);
        write("walk_in_requests.txt",
                "guestName,arrivalDateTime,requestedRoomType,numberOfGuests,numberOfNights,queueStatus", a);
    }

    private String request(WalkInRequest r) {
        return safe(r.guestName) + "," + r.arrivalDateTime + "," + r.requestedRoomType + "," + r.numberOfGuests + ","
                + r.numberOfNights + "," + r.queueStatus;
    }

    private void saveBookings(SharedDataContext d) throws IOException {
        BinarySearchTree.Entry<String, Booking>[] e = d.bookings.inOrderEntries();
        String[] a = new String[e.length];
        for (int i = 0; i < a.length; i++) {
            Booking b = e[i].value;
            a[i] = b.confirmationNumber + "," + safe(b.guestName) + "," + b.roomId + "," + b.roomType + ","
                    + b.checkInDateTime + "," + b.expectedCheckOutDateTime + "," + val(b.actualCheckOutDateTime) + ","
                    + b.numberOfGuests + "," + b.numberOfNights + "," + b.bookingStatus;
        }
        write("bookings.txt",
                "confirmationNumber,guestName,roomId,roomType,checkInDateTime,expectedCheckOutDateTime,actualCheckOutDateTime,numberOfGuests,numberOfNights,bookingStatus",
                a);
    }

    private void saveStaff(SharedDataContext d) throws IOException {
        HashTable.Entry<String, HousekeepingStaff>[] e = d.staff.entries();
        String[] a = new String[e.length];
        for (int i = 0; i < a.length; i++) {
            HousekeepingStaff s = e[i].value;
            a[i] = s.staffId + "," + safe(s.staffName) + "," + s.staffStatus + "," + val(s.currentRoomId);
        }
        write("housekeeping_staff.txt", "staffId,staffName,staffStatus,currentRoomId", a);
    }

    private void saveTasks(SharedDataContext d) throws IOException {
        HashTable.Entry<String, HousekeepingTask>[] e = d.tasks.entries();
        String[] a = new String[e.length];
        for (int i = 0; i < a.length; i++) {
            HousekeepingTask t = e[i].value;
            a[i] = t.taskId + "," + t.taskDate + "," + t.roomId + "," + t.assignedStaffId + "," + t.startTime + ","
                    + val(t.completionTime) + "," + t.taskStatus + "," + t.priority + "," + safe(t.remarks);
        }
        write("housekeeping_tasks.txt",
                "taskId,taskDate,roomId,assignedStaffId,startTime,completionTime,taskStatus,priority,remarks", a);
    }

    private void saveHistory(SharedDataContext d) throws IOException {
        Object[] h = d.roomHistory.toArray();
        String[] a = new String[h.length];
        for (int i = 0; i < a.length; i++) {
            RoomStatusHistory x = (RoomStatusHistory) h[i];
            a[i] = x.roomId + "," + x.previousRoomStatus + "," + x.previousTaskStatus + "," + val(x.previousStaffId)
                    + "," + val(x.previousStartTime) + "," + x.updateDateTime;
        }
        write("room_status_history.txt",
                "roomId,previousRoomStatus,previousTaskStatus,previousStaffId,previousStartTime,updateDateTime", a);
    }

    private String safe(String s) {
        if (s == null)
            return "";
        return s.replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    private String val(Object o) {
        return o == null ? "" : safe(o.toString());
    }
}
