package tarumtresorts.test;
import tarumtresorts.adt.*;import tarumtresorts.control.*;import tarumtresorts.entity.*;import tarumtresorts.entity.Types.*;
/** Author: <Your Name>. Dependency-free normal, boundary, and invalid-case tests. */
public final class AdtAndIntegrationTest {
 private static int passed;private static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);passed++;}
 public static void main(String[] args){
  CircularArrayQueue<Integer> q=new CircularArrayQueue<Integer>(2);q.enqueue(1);q.enqueue(2);check(q.dequeue()==1,"queue FIFO");q.enqueue(3);check(q.dequeue()==2&&q.dequeue()==3,"queue wraparound");
  HashTable<String,Integer> h=new HashTable<String,Integer>();h.put("A",1);check(h.get("A")==1,"hash get");
  BinarySearchTree<String,Integer> tree=new BinarySearchTree<String,Integer>();check(tree.insert("2",2)&&tree.insert("1",1)&&tree.search("1")==1&&!tree.insert("1",9),"BST");
  GuestNameTrie guestTrie=new GuestNameTrie();guestTrie.insert("ah mei","12345678");guestTrie.insert("ahmad","87654321");check(guestTrie.searchPrefix("ah").length==2,"guest Trie prefix");
  SharedDataContext d=new SharedDataContext();d.rooms.put("101",new Room("101",RoomType.STD,2,RoomStatus.RDY,null,null));d.rooms.put("203",new Room("203",RoomType.DLX,3,RoomStatus.DIR,null,null));d.staff.put("S001",new HousekeepingStaff("S001","Nur Iman",StaffStatus.AVL,null));
  WalkInBookingControl walk=new WalkInBookingControl(d);WalkInRequest r=walk.register("Ali Tan",RoomType.STD,2,1);check(r.queueStatus==WalkInStatus.WAITING,"registration");check(walk.monthlyRequests(r.arrivalDateTime.getMonthValue(),r.arrivalDateTime.getYear()).length==1,"monthly walk-in filtering");Booking booking=walk.assign("101");check(booking.bookingStatus==BookingStatus.CHECKED_IN&&d.rooms.get("101").roomStatus==RoomStatus.OCC,"assignment transaction");
  HousekeepingControl hk=new HousekeepingControl(d);check(hk.findStaff("nur iman").staffId.equals("S001"),"staff exact-name hash traversal");FrontDeskControl fd=new FrontDeskControl(d,hk);check(fd.roomStatusSummary(null,null,0).length==2&&fd.roomStatusSummary(RoomStatus.DIR,RoomType.DLX,2).length==1,"room status summary filters");fd.checkout(booking.confirmationNumber);check(d.rooms.get("101").roomStatus==RoomStatus.DIR&&booking.bookingStatus==BookingStatus.CHECKED_OUT,"checkout transaction");
  HousekeepingTask task=hk.createTask("101","S001",Priority.HIGH,"test");check(task.taskStatus==TaskStatus.IPG&&d.rooms.get("101").roomStatus==RoomStatus.CLN,"task creation");check(hk.staffTasks("S001",null,null).length==1,"staff overall task report including BSY staff");check(hk.staffTasks("S001",Integer.valueOf(task.taskDate.getMonthValue()),Integer.valueOf(task.taskDate.getYear())).length==1,"staff monthly task report");hk.complete("101");hk.makeReady("101");check(d.rooms.get("101").roomStatus==RoomStatus.RDY,"cleaning lifecycle");
  boolean rejected=false;try{walk.register("Bad,Name",RoomType.STD,1,1);}catch(IllegalArgumentException ex){rejected=true;}check(rejected,"invalid name");System.out.println("PASS: "+passed+" assertions");
 }
}
