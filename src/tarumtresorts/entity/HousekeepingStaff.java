package tarumtresorts.entity;

import tarumtresorts.entity.Types.*;

/** Author: <Your Name>. */
public class HousekeepingStaff {
    public final String staffId, staffName;
    public StaffStatus staffStatus;
    public String currentRoomId;

    public HousekeepingStaff(String id, String n, StaffStatus s, String room) {
        staffId = id;
        staffName = n;
        staffStatus = s;
        currentRoomId = room == null || room.trim().isEmpty() ? null : room;
    }
}
