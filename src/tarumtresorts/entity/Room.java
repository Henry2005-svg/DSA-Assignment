package tarumtresorts.entity;

import java.time.LocalDateTime;
import tarumtresorts.entity.Types.*;

/** Author: <Your Name>. */
public class Room {
    public final String roomId;
    public final RoomType roomType;
    public final int capacity;
    public RoomStatus roomStatus;
    public String confirmationNumber;
    public LocalDateTime checkoutTime;

    public Room(String id, RoomType type, int cap, RoomStatus status, String confirmation, LocalDateTime checkout) {
        roomId = id;
        roomType = type;
        capacity = cap;
        roomStatus = status;
        confirmationNumber = confirmation == null || confirmation.trim().isEmpty() ? null : confirmation;
        checkoutTime = checkout;
    }
}
