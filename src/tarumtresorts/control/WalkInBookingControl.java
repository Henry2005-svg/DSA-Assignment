package tarumtresorts.control;

import java.time.*;
import java.util.Random;
import tarumtresorts.adt.*;
import tarumtresorts.entity.*;
import tarumtresorts.entity.Types.*;
import tarumtresorts.utility.Util;

/** Author: <Your Name>. Walk-in queue and atomic room-assignment use cases. */
public class WalkInBookingControl {
    private final SharedDataContext d;
    private final Random random = new Random();

    public WalkInBookingControl(SharedDataContext d) {
        this.d = d;
    }

    public WalkInRequest register(String name, RoomType type, int guests, int nights) {
        if (!Util.validName(name))
            throw new IllegalArgumentException("Name must contain letters and spaces only.");
        if (guests < 1 || guests > type.capacity)
            throw new IllegalArgumentException("Guest count exceeds capacity " + type.capacity);
        if (nights < 1)
            throw new IllegalArgumentException("Nights must be positive.");
        WalkInRequest r = new WalkInRequest(name.trim(), LocalDateTime.now().withSecond(0).withNano(0), type, guests,
                nights, WalkInStatus.WAITING);
        d.queue(type).enqueue(r);
        d.persist();
        return r;
    }

    public String cancel(String name, LocalDateTime arrival) {
        String norm = Util.normalizeName(name);
        RoomType[] types = RoomType.values();
        for (int t = 0; t < types.length; t++) {
            QueueInterface<WalkInRequest> q = d.queue(types[t]);
            CircularArrayQueue<WalkInRequest> temp = new CircularArrayQueue<WalkInRequest>(Math.max(1, q.size()));
            int n = q.size();
            WalkInRequest match = null;
            for (int i = 0; i < n; i++) {
                WalkInRequest r = q.dequeue();
                if (match == null && Util.normalizeName(r.guestName).equals(norm) && r.arrivalDateTime.equals(arrival))
                    match = r;
                else
                    temp.enqueue(r);
            }
            while (!temp.isEmpty())
                q.enqueue(temp.dequeue());
            if (match != null) {
                match.queueStatus = WalkInStatus.CANCELLED;
                d.requestHistory.add(match);
                d.persist();
                return "Cancelled " + match.guestName;
            }
        }
        throw new IllegalArgumentException("Waiting request not found.");
    }

    public Booking assign(String roomId) {
        Room room = d.rooms.get(roomId);
        if (room == null)
            throw new IllegalArgumentException("Room not found.");
        if (room.roomStatus != RoomStatus.RDY)
            throw new IllegalArgumentException("Room is not RDY.");
        QueueInterface<WalkInRequest> q = d.queue(room.roomType);
        WalkInRequest r = q.getFront();
        if (r == null)
            throw new IllegalArgumentException("No waiting guest for " + room.roomType);
        if (r.numberOfGuests > room.capacity)
            throw new IllegalArgumentException("Guest count exceeds room capacity.");
        String confirmation;
        do {
            confirmation = String.format("%08d", random.nextInt(100000000));
        } while (d.bookings.search(confirmation) != null);
        LocalDateTime in = LocalDateTime.now().withSecond(0).withNano(0), out = in.plusDays(r.numberOfNights);
        Booking b = new Booking(confirmation, r.guestName, room.roomId, room.roomType, in, out, null, r.numberOfGuests,
                r.numberOfNights, BookingStatus.CHECKED_IN);
        if (!d.bookings.insert(confirmation, b))
            throw new IllegalStateException("Booking insertion failed.");
        d.guestTrie.insert(Util.normalizeName(r.guestName), confirmation);
        q.dequeue();
        r.queueStatus = WalkInStatus.COMPLETED;
        d.requestHistory.add(r);
        room.roomStatus = RoomStatus.OCC;
        room.confirmationNumber = confirmation;
        room.checkoutTime = out;
        d.persist();
        return b;
    }

    public Object[] waiting(RoomType type) {
        return d.queue(type).toArray();
    }

    public Object[] history() {
        return d.requestHistory.toArray();
    }

    /**
     * Combines active queues with requestHistory, filters by month/year, and
     * explicitly insertion-sorts by arrival date/time.
     */
    public WalkInRequest[] monthlyRequests(int month, int year) {
        if (month < 1 || month > 12 || year < 2000 || year > 2100)
            throw new IllegalArgumentException("Invalid report month or year.");
        int capacity = d.requestHistory.size();
        RoomType[] types = RoomType.values();
        for (int i = 0; i < types.length; i++)
            capacity += d.queue(types[i]).size();
        WalkInRequest[] temporary = new WalkInRequest[capacity];
        int count = 0;
        for (int typeIndex = 0; typeIndex < types.length; typeIndex++) {
            Object[] waitingRows = d.queue(types[typeIndex]).toArray();
            for (int i = 0; i < waitingRows.length; i++) {
                WalkInRequest request = (WalkInRequest) waitingRows[i];
                if (request.arrivalDateTime.getMonthValue() == month && request.arrivalDateTime.getYear() == year)
                    temporary[count++] = request;
            }
        }
        Object[] historyRows = d.requestHistory.toArray();
        for (int i = 0; i < historyRows.length; i++) {
            WalkInRequest request = (WalkInRequest) historyRows[i];
            if (request.arrivalDateTime.getMonthValue() == month && request.arrivalDateTime.getYear() == year)
                temporary[count++] = request;
        }
        WalkInRequest[] result = new WalkInRequest[count];
        System.arraycopy(temporary, 0, result, 0, count);
        for (int i = 1; i < result.length; i++) {
            WalkInRequest current = result[i];
            int j = i - 1;
            while (j >= 0 && result[j].arrivalDateTime.isAfter(current.arrivalDateTime)) {
                result[j + 1] = result[j];
                j--;
            }
            result[j + 1] = current;
        }
        return result;
    }

    public Room[] readyRooms() {
        HashTable.Entry<String, Room>[] e = d.rooms.entries();
        Room[] a = new Room[e.length];
        int n = 0;
        for (int i = 0; i < e.length; i++)
            if (e[i].value.roomStatus == RoomStatus.RDY)
                a[n++] = e[i].value;
        Room[] out = new Room[n];
        System.arraycopy(a, 0, out, 0, n);
        sortRooms(out);
        return out;
    }

    private void sortRooms(Room[] a) {
        for (int i = 1; i < a.length; i++) {
            Room x = a[i];
            int j = i - 1;
            while (j >= 0 && a[j].roomId.compareTo(x.roomId) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = x;
        }
    }
}
