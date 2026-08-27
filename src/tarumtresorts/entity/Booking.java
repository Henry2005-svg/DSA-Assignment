package tarumtresorts.entity;

import java.time.LocalDateTime;
import tarumtresorts.entity.Types.*;

/** Author: <Your Name>. */
public class Booking {
    public final String confirmationNumber, guestName, roomId;
    public final RoomType roomType;
    public final LocalDateTime checkInDateTime;
    public LocalDateTime expectedCheckOutDateTime, actualCheckOutDateTime;
    public final int numberOfGuests, numberOfNights;
    public BookingStatus bookingStatus;

    public Booking(String c, String n, String r, RoomType t, LocalDateTime in, LocalDateTime expected,
            LocalDateTime actual, int guests, int nights, BookingStatus s) {
        confirmationNumber = c;
        guestName = n;
        roomId = r;
        roomType = t;
        checkInDateTime = in;
        expectedCheckOutDateTime = expected;
        actualCheckOutDateTime = actual;
        numberOfGuests = guests;
        numberOfNights = nights;
        bookingStatus = s;
    }
}
