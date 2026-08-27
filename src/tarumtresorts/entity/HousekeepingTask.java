package tarumtresorts.entity;

import java.time.*;
import tarumtresorts.entity.Types.*;

/** Author: <Your Name>. */
public class HousekeepingTask {
    public final String taskId;
    public final LocalDate taskDate;
    public final String roomId, assignedStaffId;
    public final LocalTime startTime;
    public LocalTime completionTime;
    public TaskStatus taskStatus;
    public final Priority priority;
    public final String remarks;

    public HousekeepingTask(String id, LocalDate d, String room, String staff, LocalTime start, LocalTime completion,
            TaskStatus status, Priority p, String remarks) {
        taskId = id;
        taskDate = d;
        roomId = room;
        assignedStaffId = staff;
        startTime = start;
        completionTime = completion;
        taskStatus = status;
        priority = p;
        this.remarks = remarks;
    }
}
