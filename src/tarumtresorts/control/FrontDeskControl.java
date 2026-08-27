package tarumtresorts.control;

import java.time.*;
import tarumtresorts.adt.*;
import tarumtresorts.entity.*;
import tarumtresorts.entity.Types.*;
import tarumtresorts.utility.Util;

/**
 * Author: <Your Name>. Unified BST/Trie search, availability, late checkout and
 * checkout.
 */
public class FrontDeskControl {
    private final SharedDataContext d;
    private final HousekeepingControl housekeeping;

    public FrontDeskControl(SharedDataContext d, HousekeepingControl h) {
        this.d = d;
        housekeeping = h;
    }

    public Booking[] search(String input) {
        String q = input == null ? "" : input.trim();
        if (q.matches("\\d{8}")) {
            Booking b = d.bookings.search(q);
            return b == null ? new Booking[0] : new Booking[] { b };
        }
        if (!Util.validName(q))
            throw new IllegalArgumentException("Enter exactly 8 digits or letters/spaces.");
        GuestNameTrie.Match[] m = d.guestTrie.searchPrefix(Util.normalizeName(q));
        Booking[] out = new Booking[m.length];
        int n = 0;
        for (int i = 0; i < m.length; i++) {
            Booking b = d.bookings.search(m[i].confirmationNumber);
            if (b != null)
                out[n++] = b;
        }
        if (n == out.length)
            return out;
        Booking[] trim = new Booking[n];
        System.arraycopy(out, 0, trim, 0, n);
        return trim;
    }

    public Room[] availability(RoomType type, int guests) {
        if (guests < 1 || guests > type.capacity)
            throw new IllegalArgumentException("Invalid guest count for type.");
        HashTable.Entry<String, Room>[] e = d.rooms.entries();
        Room[] tmp = new Room[e.length];
        int n = 0;
        for (int i = 0; i < e.length; i++) {
            Room r = e[i].value;
            if (r.roomStatus == RoomStatus.RDY && r.roomType == type && r.capacity >= guests)
                tmp[n++] = r;
        }
        Room[] out = new Room[n];
        System.arraycopy(tmp, 0, out, 0, n);
        return out;
    }

    public void lateCheckout(String confirmation, LocalDateTime later) {
        Booking b = d.bookings.search(confirmation);
        if (b == null || b.bookingStatus != BookingStatus.CHECKED_IN)
            throw new IllegalArgumentException("Booking must be CHECKED_IN.");
        Room r = d.rooms.get(b.roomId);
        if (r == null || r.roomStatus != RoomStatus.OCC)
            throw new IllegalArgumentException("Room must be OCC.");
        if (later == null || !later.isAfter(b.expectedCheckOutDateTime))
            throw new IllegalArgumentException("New checkout must be later.");
        b.expectedCheckOutDateTime = later;
        r.checkoutTime = later;
        r.roomStatus = RoomStatus.LCO;
        housekeeping.handleLateCheckoutConflict(r.roomId);
        d.persist();
    }

    public void checkout(String confirmation) {
        Booking b = d.bookings.search(confirmation);
        if (b == null || b.bookingStatus != BookingStatus.CHECKED_IN)
            throw new IllegalArgumentException("Booking must be CHECKED_IN.");
        Room r = d.rooms.get(b.roomId);
        if (r == null || (r.roomStatus != RoomStatus.OCC && r.roomStatus != RoomStatus.LCO))
            throw new IllegalArgumentException("Room must be OCC or LCO.");
        b.actualCheckOutDateTime = LocalDateTime.now().withSecond(0).withNano(0);
        b.bookingStatus = BookingStatus.CHECKED_OUT;
        r.roomStatus = RoomStatus.DIR;
        r.confirmationNumber = null;
        r.checkoutTime = null;
        d.persist();
    }

    public BinarySearchTree.Entry<String, Booking>[] allBookings() {
        return d.bookings.inOrderEntries();
    }

    /**
     * Traverses the shared room hash table, applies optional filters, and sorts by
     * room ID using insertion sort.
     */
    public Room[] roomStatusSummary(RoomStatus status, RoomType type, int floor) {
        if (floor < 0 || floor > 9)
            throw new IllegalArgumentException("Floor must be 0 (ALL) or 1-9.");
        HashTable.Entry<String, Room>[] entries = d.rooms.entries();
        Room[] temp = new Room[entries.length];
        int count = 0;
        for (int i = 0; i < entries.length; i++) {
            Room room = entries[i].value;
            int roomFloor = Character.digit(room.roomId.charAt(0), 10);
            if ((status == null || room.roomStatus == status) && (type == null || room.roomType == type)
                    && (floor == 0 || roomFloor == floor))
                temp[count++] = room;
        }
        Room[] result = new Room[count];
        System.arraycopy(temp, 0, result, 0, count);
        for (int i = 1; i < result.length; i++) {
            Room current = result[i];
            int j = i - 1;
            while (j >= 0 && result[j].roomId.compareTo(current.roomId) > 0) {
                result[j + 1] = result[j];
                j--;
            }
            result[j + 1] = current;
        }
        return result;
    }
}
