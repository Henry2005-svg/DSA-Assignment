package tarumtresorts.entity;

import java.time.*;
import tarumtresorts.entity.Types.*;

/** Author: <Your Name>. */
public class RoomStatusHistory {
    public final String roomId;
    public final RoomStatus previousRoomStatus;
    public final TaskStatus previousTaskStatus;
    public final String previousStaffId;
    public final LocalTime previousStartTime;
    public final LocalDateTime updateDateTime;

    public RoomStatusHistory(String room, RoomStatus rs, TaskStatus ts, String staff, LocalTime start,
            LocalDateTime update) {
        roomId = room;
        previousRoomStatus = rs;
        previousTaskStatus = ts;
        previousStaffId = staff;
        previousStartTime = start;
        updateDateTime = update;
    }
}
