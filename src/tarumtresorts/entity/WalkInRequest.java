package tarumtresorts.entity;

import java.time.LocalDateTime;
import tarumtresorts.entity.Types.*;

/** Author: <Your Name>. */
public class WalkInRequest {
    public final String guestName;
    public final LocalDateTime arrivalDateTime;
    public final RoomType requestedRoomType;
    public final int numberOfGuests, numberOfNights;
    public WalkInStatus queueStatus;

    public WalkInRequest(String n, LocalDateTime a, RoomType t, int g, int nights, WalkInStatus s) {
        guestName = n;
        arrivalDateTime = a;
        requestedRoomType = t;
        numberOfGuests = g;
        numberOfNights = nights;
        queueStatus = s;
    }
}
