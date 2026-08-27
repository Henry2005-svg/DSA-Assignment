package tarumtresorts.control;

import tarumtresorts.adt.*;
import tarumtresorts.entity.*;
import tarumtresorts.entity.Types.*;

/** Author: <Your Name>. Single shared object graph for all modules. */
public class SharedDataContext {
    private Runnable persistence = new Runnable() {
        public void run() {
        }
    };
    public final HashTable<String, Room> rooms = new HashTable<String, Room>();
    public final HashTable<String, HousekeepingStaff> staff = new HashTable<String, HousekeepingStaff>();
    public final HashTable<String, HousekeepingTask> tasks = new HashTable<String, HousekeepingTask>();
    public final BinarySearchTree<String, Booking> bookings = new BinarySearchTree<String, Booking>();
    public final GuestNameTrie guestTrie = new GuestNameTrie();
    public final LinearList<WalkInRequest> requestHistory = new LinearList<WalkInRequest>();
    public final LinearList<RoomStatusHistory> roomHistory = new LinearList<RoomStatusHistory>();
    public final CircularArrayQueue<WalkInRequest> standardQueue = new CircularArrayQueue<WalkInRequest>(16),
            deluxeQueue = new CircularArrayQueue<WalkInRequest>(16),
            familyQueue = new CircularArrayQueue<WalkInRequest>(16),
            suiteQueue = new CircularArrayQueue<WalkInRequest>(16);

    public QueueInterface<WalkInRequest> queue(RoomType t) {
        switch (t) {
            case STD:
                return standardQueue;
            case DLX:
                return deluxeQueue;
            case FAM:
                return familyQueue;
            default:
                return suiteQueue;
        }
    }

    public void setPersistence(Runnable action) {
        if (action == null)
            throw new IllegalArgumentException("Persistence action is required.");
        persistence = action;
    }

    public void persist() {
        persistence.run();
    }
}
