package tarumtresorts.entity;

/** Author: <Your Name>. Required domain codes. */
public final class Types {
    private Types() {

    }

    public enum RoomType {
        STD(2), DLX(3), FAM(4), STE(4);

        public final int capacity;

        RoomType(int c) {
            capacity = c;
        }
    }

    public enum RoomStatus {
        OCC, LCO, DIR, CLN, INS, RDY
    }

    public enum WalkInStatus {
        WAITING, COMPLETED, CANCELLED
    }

    public enum BookingStatus {
        CHECKED_IN, CHECKED_OUT, CANCELLED
    }

    public enum StaffStatus {
        AVL, BSY
    }

    public enum TaskStatus {
        IPG, PST, COM
    }

    public enum Priority {
        LOW, MEDIUM, HIGH
    }
}
