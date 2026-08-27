# TARUMT Resorts System - Stage 1 Implementation Plan

> Author: `<Your Name>`
>
> Course: BMCS2063 Data Structures and Algorithms
>
> This document is the required design checkpoint. It intentionally does not contain a submission-ready implementation. Add an author comment to every Java source file, acknowledge adapted ADTs in their interface files, disclose permitted AI assistance, and confirm the final work with the tutor.

## 1. Proposed NetBeans package tree

```text
G9_DSA_Assignment/
|-- nbproject/                         NetBeans-generated metadata
|-- src/
|   `-- tarumtresorts/
|       |-- adt/
|       |   |-- QueueInterface.java
|       |   |-- CircularArrayQueue.java
|       |   |-- LinearListInterface.java
|       |   |-- LinkedLinearList.java
|       |   |-- HashTableInterface.java
|       |   |-- SeparateChainingHashTable.java
|       |   |-- BinarySearchTreeInterface.java
|       |   |-- LinkedBinarySearchTree.java
|       |   |-- TrieInterface.java
|       |   |-- GuestNameTrie.java
|       |   |-- KeyValueEntry.java
|       |   `-- TrieMatch.java
|       |-- entity/
|       |   |-- Room.java
|       |   |-- WalkInRequest.java
|       |   |-- Booking.java
|       |   |-- HousekeepingStaff.java
|       |   |-- HousekeepingTask.java
|       |   |-- RoomStatusHistory.java
|       |   |-- RoomType.java
|       |   |-- RoomStatus.java
|       |   |-- WalkInStatus.java
|       |   |-- BookingStatus.java
|       |   |-- StaffStatus.java
|       |   |-- TaskStatus.java
|       |   `-- Priority.java
|       |-- control/
|       |   |-- SharedDataContext.java
|       |   |-- WalkInBookingControl.java
|       |   |-- HousekeepingControl.java
|       |   |-- FrontDeskControl.java
|       |   |-- WalkInReportControl.java
|       |   |-- HousekeepingReportControl.java
|       |   `-- FrontDeskReportControl.java
|       |-- boundary/
|       |   |-- MainMenuUI.java
|       |   |-- WalkInBookingUI.java
|       |   |-- HousekeepingTaskUI.java
|       |   |-- FrontDeskServiceUI.java
|       |   |-- WalkInReportUI.java
|       |   |-- HousekeepingReportUI.java
|       |   `-- FrontDeskReportUI.java
|       |-- data/
|       |   |-- DataFileControl.java
|       |   `-- DataInitializer.java
|       |-- utility/
|       |   |-- InputValidator.java
|       |   |-- IdGenerator.java
|       |   |-- DateTimeUtil.java
|       |   |-- TextUtil.java
|       |   `-- ExplicitSort.java
|       `-- client/
|           `-- TarumtResortsApplication.java
`-- data/
    |-- rooms.txt
    |-- walk_in_requests.txt
    |-- bookings.txt
    |-- housekeeping_staff.txt
    |-- housekeeping_tasks.txt
    `-- room_status_history.txt
```

The NetBeans source root is `src`. Package names are lowercase. ECB dependencies flow Boundary -> Control -> Entity/ADT/Data, while entities never depend on controls, boundaries, or ADTs.

## 2. Complete class responsibility table

| Class | Classification | Responsibility |
|---|---|---|
| `Room` | Entity | Stores the shared room identity, type, capacity, current state, occupant confirmation reference, and checkout time. |
| `WalkInRequest` | Entity | Stores a guest's queue request and WAITING/COMPLETED/CANCELLED state. |
| `Booking` | Entity | Stores confirmed and historical stay data. The same instance is referenced by the booking BST and used by all controls. |
| `HousekeepingStaff` | Entity | Stores staff availability and current room assignment. |
| `HousekeepingTask` | Entity | Stores cleaning-task assignment, timing, priority, remarks, and status. |
| `RoomStatusHistory` | Entity | Stores automatically captured pre-conflict state for audit/reversal. |
| Seven enums | Entity | Constrain valid type/status/priority codes and centralize file-code parsing. |
| `SharedDataContext` | Control | Owns every live ADT instance, exposes controlled references to controls, and ensures one shared Room/Booking object graph. |
| `WalkInBookingControl` | Control | Registers, queues, cancels, assigns rooms, creates bookings, and coordinates BST/Trie updates. |
| `HousekeepingControl` | Control | Creates/completes tasks, performs room transitions, handles late-checkout conflicts, and releases staff. |
| `FrontDeskControl` | Control | Routes unified guest searches, finds availability, applies late checkout, and processes checkout through shared data. |
| Three report controls | Control | Traverse/filter custom ADTs, copy references into basic arrays, explicitly sort, and compute totals. |
| Seven UI classes | Boundary | Loop menus, collect validated input through utilities, call controls, and format results. They never parse files or mutate ADTs/entities directly. |
| Four ADT interfaces | ADT | Define assignment-visible contracts and document adapted sources plus complexity. |
| Four ADT implementations | ADT | Implement circular queue, linked list, separate-chaining hash table, BST, and Trie without Java Collections. |
| `KeyValueEntry`, `TrieMatch` | ADT helper | Represent traversable key/value entries and lightweight Trie search references without duplicating Bookings. |
| `DataFileControl` | Data | Parse all six files at startup and safely write current records; no UI logic. |
| `DataInitializer` | Data | Seed exactly separated development data when a file is absent. |
| `InputValidator` | Utility | Validate menu ranges, names, integers, codes, dates/times, and confirmations. |
| `IdGenerator` | Utility | Generate unique task IDs and exactly eight-digit confirmation numbers, checking ADTs. |
| `DateTimeUtil` | Utility | Own stable file/display date-time formats and parsing. |
| `TextUtil` | Utility | Normalize guest names and render null display values as `-`. |
| `ExplicitSort` | Utility | Provide assignment-approved insertion-sort methods over basic arrays. |
| `TarumtResortsApplication` | Client/bootstrap | Construct context/data/controls/UIs, load once, run main menu, and save on confirmed exit. |

## 3. Entity field and type table

| Entity | Fields |
|---|---|
| `Room` | `String roomId`; `RoomType roomType`; `int capacity`; `RoomStatus roomStatus`; `String confirmationNumber`; `LocalDateTime checkoutTime` |
| `WalkInRequest` | `String guestName`; `LocalDateTime arrivalDateTime`; `RoomType requestedRoomType`; `int numberOfGuests`; `int numberOfNights`; `WalkInStatus queueStatus` |
| `Booking` | `String confirmationNumber`; `String guestName`; `String roomId`; `RoomType roomType`; `LocalDateTime checkInDateTime`; `LocalDateTime expectedCheckOutDateTime`; `LocalDateTime actualCheckOutDateTime`; `int numberOfGuests`; `int numberOfNights`; `BookingStatus bookingStatus` |
| `HousekeepingStaff` | `String staffId`; `String staffName`; `StaffStatus staffStatus`; `String currentRoomId` |
| `HousekeepingTask` | `String taskId`; `LocalDate taskDate`; `String roomId`; `String assignedStaffId`; `LocalTime startTime`; `LocalTime completionTime`; `TaskStatus taskStatus`; `Priority priority`; `String remarks` |
| `RoomStatusHistory` | `String roomId`; `RoomStatus previousRoomStatus`; `TaskStatus previousTaskStatus`; `String previousStaffId`; `LocalTime previousStartTime`; `LocalDateTime updateDateTime` |

Enum values are exactly: `RoomType {STD, DLX, FAM, STE}`, `RoomStatus {OCC, LCO, DIR, CLN, INS, RDY}`, `WalkInStatus {WAITING, COMPLETED, CANCELLED}`, `BookingStatus {CHECKED_IN, CHECKED_OUT, CANCELLED}`, `StaffStatus {AVL, BSY}`, `TaskStatus {IPG, PST, COM}`, and `Priority {LOW, MEDIUM, HIGH}`.

Capacity is derived/validated against room type: STD=2, DLX=3, FAM=4, STE=4. Setters that could bypass state rules should be package-private or avoided; controls perform transitions through narrow entity methods after validation.

## 4. ADT interface and implementation plan

### 4.1 Queue

```java
public interface QueueInterface<T> {
    boolean enqueue(T item);
    T dequeue();
    T getFront();
    boolean isEmpty();
    boolean isFull();
    int size();
    void clear();
}
```

`CircularArrayQueue<T>` uses `T[] elements`, `frontIndex`, `rearIndex`, and `count`. Wraparound uses modulo capacity. Fixed capacity is acceptable when sized above seed/demo demand; optional resizing must copy logical index `(frontIndex + i) % oldCapacity`. Enqueue, dequeue, and getFront are O(1); resize is O(n), giving amortized O(1) enqueue if resizing is used. Four instances serve STD, DLX, FAM, and STE. A temporary instance supports stable O(n) middle cancellation.

### 4.2 Linked linear list

```java
public interface LinearListInterface<T> {
    boolean add(T item);
    boolean add(int position, T item);
    T get(int position);
    T remove(int position);
    boolean replace(int position, T item);
    boolean contains(T item);
    boolean isEmpty();
    int size();
    void clear();
}
```

`LinkedLinearList<T>` stores `firstNode` and `count`. It holds request history and status history. Add-at-end may be O(n) with only a head or O(1) with an allowed tail pointer; indexed operations are O(n).

### 4.3 Hash table

```java
public interface HashTableInterface<K, V> {
    V put(K key, V value);
    V get(K key);
    V remove(K key);
    boolean containsKey(K key);
    boolean isEmpty();
    int size();
    void clear();
    KeyValueEntry<K, V>[] entries();
}
```

`SeparateChainingHashTable<K,V>` uses a basic array of custom linked nodes. Bucket index is `(key.hashCode() & 0x7fffffff) % table.length`. Average put/get/remove is O(1) with a suitable load factor and hash distribution; worst case is O(n) when keys collide. `entries()` creates a basic array for controlled traversal, not a Java Collection. Instances: shared `roomTable`, housekeeping `staffTable`, and `taskTable`.

### 4.4 Booking BST

```java
public interface BinarySearchTreeInterface<K extends Comparable<K>, V> {
    boolean insert(K key, V value);
    V search(K key);
    boolean isEmpty();
    int size();
    void clear();
    KeyValueEntry<K, V>[] inOrderEntries();
}
```

`LinkedBinarySearchTree` rejects duplicate confirmation keys. Insert/search average O(log n) when reasonably balanced and worst O(n) when skewed. In-order traversal is O(n). No balancing is claimed.

### 4.5 Guest-name Trie

```java
public interface TrieInterface {
    boolean insert(String normalizedName, String confirmationNumber);
    TrieMatch[] searchPrefix(String normalizedPrefix);
    boolean isEmpty();
    int size();
    void clear();
}
```

`GuestNameTrie` accepts normalized lowercase letters and spaces. Each terminal node stores a custom linked linear list of confirmation-number strings so duplicate guest names work. It stores no Booking copy. Prefix location is O(p); depth-first result collection is output-sensitive, described as O(p + k) for returned references (plus traversal needed to reach them). The chosen confirmation is resolved through the BST.

## 5. Shared-data ownership and module access

| Dataset/index | Owner | Walk-In | Housekeeping | Front Desk |
|---|---|---|---|---|
| `roomTable: roomId -> Room` | `SharedDataContext` | Search/transition RDY->OCC | Search/transition DIR->CLN->INS->RDY | Search/transition OCC->LCO and OCC/LCO->DIR |
| Four request queues | Walk-In context section | Full control | None | None |
| `requestHistory` | Walk-In context section | Full control/report | None | None |
| `bookingTree` | Shared context | Insert/search/traverse | None | Search/update/traverse |
| `guestTrie` | Shared context | Insert reference | None | Prefix search reference |
| `staffTable` | Housekeeping context section | None | Full control/report | No direct access |
| `taskTable` | Housekeeping context section | None | Full control/report | Only via `HousekeepingControl` notification/conflict method |
| `roomStatusHistory` | Housekeeping context section | None | Capture/traverse | None; communicates via housekeeping control |

`SharedDataContext` is constructed once and injected into controls. Data loading inserts each Room and Booking once. Loading a Booking inserts that same Booking reference into the BST, while the Trie stores only its confirmation-number reference.

## 6. Proposed method signatures

Signatures may return small result DTOs or entities for display; UIs must not receive mutable ADT internals.

### Controls

```java
// SharedDataContext
public HashTableInterface<String, Room> getRoomTable();
public QueueInterface<WalkInRequest> getQueue(RoomType type);
public LinearListInterface<WalkInRequest> getRequestHistory();
public BinarySearchTreeInterface<String, Booking> getBookingTree();
public TrieInterface getGuestTrie();
public HashTableInterface<String, HousekeepingStaff> getStaffTable();
public HashTableInterface<String, HousekeepingTask> getTaskTable();
public LinearListInterface<RoomStatusHistory> getRoomStatusHistory();

// WalkInBookingControl
public WalkInRequest registerWalkIn(String guestName, RoomType type, int guests, int nights);
public WalkInRequest[] getWaitingRequests(RoomType type);
public WalkInRequest[] getHistoryByStatus(WalkInStatus status);
public Booking[] getConfirmedBookings();
public Room[] getReadyRooms(RoomType typeOrNull);
public OperationResult cancelRequest(String normalizedGuestName, LocalDateTime exactArrival);
public OperationResult assignReadyRoom(String roomId, boolean confirmed);
public int getQueuePosition(WalkInRequest request);

// HousekeepingControl
public Room[] getAllRooms();
public HousekeepingStaff[] getAllStaff();
public HousekeepingTask[] getAllTasks();
public Room[] getDirtyRoomsWithoutActiveTask();
public HousekeepingStaff[] getAvailableStaff();
public OperationResult createTask(String roomId, String staffId, Priority priority, String remarks);
public CleaningRoomView[] getCleaningRooms();
public OperationResult completeTaskForRoom(String roomId, boolean confirmed);
public Room[] getLateCheckoutRooms();
public OperationResult handleLateCheckoutConflict(String roomId);
public InspectedRoomView[] getInspectedRooms();
public OperationResult updateAllInspectedToReady(boolean confirmed);
public OperationResult updateInspectedRoomToReady(String roomId);
public void notifyGuestCheckout(String roomId);

// FrontDeskControl
public GuestSearchResult[] searchGuest(String confirmationOrNamePrefix);
public Booking getBooking(String confirmationNumber);
public Room[] searchAvailableRooms(RoomType type, int guests);
public OperationResult applyLateCheckout(String confirmationNumber, LocalDateTime laterCheckout, boolean confirmed);
public OperationResult processCheckout(String confirmationNumber, boolean confirmed);

// Report controls
public WalkInMonthlyReport monthlyRequests(int month, int year, WalkInStatus status, RoomType type);
public RoomDemandReport roomTypeDemand(int month, int year);
public HousekeepingMonthlyReport monthlyTasks(int month, int year, TaskStatus status, Priority priority, RoomType type);
public StaffTaskReport monthlyStaffTasks(String staffId, int month, int year);
public BookingMonthlyReport monthlyBookings(int month, int year, RoomType type, BookingStatus status);
public RoomStatusReport roomStatusSummary(RoomStatus status, RoomType type, Integer floor);
```

`OperationResult` and report/view types are simple immutable result objects or nested control DTOs containing success flag, message, before/after display data, result rows, and totals. They are not entities or collections.

### Boundaries

```java
// MainMenuUI
public void run();
private void displayMenu();
private boolean confirmExit();

// WalkInBookingUI
public void run();
private void displayMenu();
private void displayRequestsMenu();
private void registerGuest();
private void assignRoom();
private void cancelRequest();
private void generateReports();

// HousekeepingTaskUI
public void run();
private void displayMenu();
private void displayInformationMenu();
private void manageTasksMenu();
private void addTask();
private void completeTask();
private void handleLateCheckout();
private void updateRoomStatusMenu();
private void generateReports();

// FrontDeskServiceUI
public void run();
private void displayMenu();
private Booking selectGuestFromUnifiedSearch();
private void searchGuest();
private void searchAvailability();
private void handleLateCheckout();
private void processCheckout();
private void generateReports();

// Report UIs
public void run();
private void displayMenu();
private void printReport(/* matching report DTO */);
```

### Data and utility layer

```java
// DataFileControl
public void loadAll(SharedDataContext context);
public void saveAll(SharedDataContext context);
private void loadRooms(...); private void saveRooms(...);
private void loadWalkInRequests(...); private void saveWalkInRequests(...);
private void loadBookings(...); private void saveBookings(...);
private void loadStaff(...); private void saveStaff(...);
private void loadTasks(...); private void saveTasks(...);
private void loadRoomStatusHistory(...); private void saveRoomStatusHistory(...);

// DataInitializer
public void seedMissingData(SharedDataContext context);

// InputValidator
public int readIntInRange(String prompt, int min, int max);
public int readPositiveInt(String prompt);
public String readNonBlankName(String prompt);
public RoomType readRoomType(String prompt);
public LocalDateTime readDateTime(String prompt);
public boolean readConfirmation(String prompt);

// IdGenerator
public String nextUniqueConfirmation(BinarySearchTreeInterface<String, Booking> tree);
public String nextUniqueTaskId(HashTableInterface<String, HousekeepingTask> table);
```

## 7. File formats and sample lines

Use UTF-8, one record per line, comma-separated fields, a header row, ISO-like stable date formats, and an empty field for null. To prevent delimiter corruption, validate/reject commas and line breaks in free-text input (or implement documented CSV quoting consistently). Suggested formats:

| File | Header | Sample data line |
|---|---|---|
| `rooms.txt` | `roomId,roomType,capacity,roomStatus,confirmationNumber,checkoutTime` | `101,STD,2,RDY,,` |
| `walk_in_requests.txt` | `guestName,arrivalDateTime,requestedRoomType,numberOfGuests,numberOfNights,queueStatus` | `Aisha Rahman,2026-08-15T14:10:00,STD,2,2,WAITING` |
| `bookings.txt` | `confirmationNumber,guestName,roomId,roomType,checkInDateTime,expectedCheckOutDateTime,actualCheckOutDateTime,numberOfGuests,numberOfNights,bookingStatus` | `48392017,Daniel Tan,203,DLX,2026-08-14T15:00:00,2026-08-16T12:00:00,,2,2,CHECKED_IN` |
| `housekeeping_staff.txt` | `staffId,staffName,staffStatus,currentRoomId` | `S001,Nur Iman,AVL,` |
| `housekeeping_tasks.txt` | `taskId,taskDate,roomId,assignedStaffId,startTime,completionTime,taskStatus,priority,remarks` | `T0001,2026-08-15,105,S003,13:20:00,,IPG,HIGH,Guest arriving soon` |
| `room_status_history.txt` | `roomId,previousRoomStatus,previousTaskStatus,previousStaffId,previousStartTime,updateDateTime` | `105,CLN,IPG,S003,13:20:00,2026-08-15T13:32:00` |

Safe saving plan: write every dataset to a sibling `.tmp` file, close it successfully, then replace the target file. If replacement fails, preserve the original and report the error. Boundaries never parse or save files.

## 8. Pseudocode for integrated operations

### 8.1 Register and enqueue a WalkInRequest

```text
INPUT guestName, requestedType, guests, nights
normalize surrounding whitespace
IF name blank or contains unsupported delimiter/control characters: reject
IF type not STD/DLX/FAM/STE: reject
IF guests <= 0 OR guests > capacity(type): reject
IF nights <= 0: reject

arrival = current date/time
request = new WalkInRequest(name, arrival, type, guests, nights, WAITING)
queue = sharedContext.getQueue(type)
IF queue is full and does not resize: reject without creating persistent state
enqueue request
position = queue.size()
display queue type, arrival timestamp (needed for cancellation), and position
```

ADT/complexity: validation is O(1) relative to dataset size; circular enqueue and size are O(1), or amortized O(1) with resizing.

### 8.2 Assign ready room and create Booking

```text
INPUT roomId
room = roomTable.get(roomId)
IF missing OR room.status != RDY: reject with no changes
queue = queue selected by room.type
IF queue empty: reject with no changes
request = queue.getFront()
IF request.guests > room.capacity: reject with no changes
ASK confirmation; IF no: return with no changes

generate exactly 8-digit candidate
WHILE bookingTree.search(candidate) exists: generate another
checkIn = now
expectedOut = checkIn plus request.nights (using defined hotel checkout convention)
construct Booking(candidate, same guest details, room, CHECKED_IN)

Perform controlled transaction:
    dequeue request
    set request WAITING -> COMPLETED
    insert Booking into BST
    insert normalized guest name -> candidate into Trie
    add request to requestHistory
    set room RDY -> OCC
    set room.confirmationNumber = candidate
    set room.checkoutTime = expectedOut
IF an unexpected insertion fails:
    reverse already-applied steps using captured old values, or fail before mutation
save affected datasets after successful transaction
display request, room, and booking before/after states
```

ADT/complexity: room lookup average O(1), queue front/dequeue O(1), BST uniqueness/search and insert average O(log b), worst O(b), Trie insertion O(g), and history append O(1) with a tail pointer.

### 8.3 Create and complete HousekeepingTask

```text
CREATE:
traverse roomTable and display DIR rooms for which no IPG/PST task exists
IF none: return
INPUT displayed roomId
re-fetch room and verify DIR and still no active task
traverse staffTable and display AVL staff
INPUT staffId, priority, remarks
re-fetch staff and verify AVL with blank currentRoomId
generate unique taskId by probing taskTable
capture all old values
create IPG task with today and now
insert task; set room DIR -> CLN; set staff AVL -> BSY; set currentRoomId
save and show before/after state

COMPLETE:
traverse tasks and rooms; display CLN rooms with matching IPG task
INPUT one displayed roomId
re-fetch room, active task, and assigned staff
verify room CLN, task IPG, staff BSY, and same currentRoomId
ASK confirmation; IF no: return unchanged
capture old values
set task IPG -> COM and completionTime = now
set room CLN -> INS
set staff BSY -> AVL and currentRoomId = null
save and show before/after state
```

ADT/complexity: hash lookups average O(1), but finding an active task by room is O(t) because the required table is keyed by task ID. Report/display traversal is O(r+t+s). No duplicate active task is permitted.

### 8.4 Search guest and process checkout

```text
INPUT trimmed query
IF exactly eight digits:
    booking = bookingTree.search(query)
    IF missing: report not found
ELSE IF letters/spaces only and nonblank:
    prefix = normalize(query)
    matches = guestTrie.searchPrefix(prefix)
    for each confirmation reference:
        bookingTree.search(reference) and display guest, confirmation, room, status
    ask user to choose a displayed result
    booking = bookingTree.search(chosen confirmation)
ELSE reject invalid input

display complete Booking
FOR checkout:
    IF booking.status != CHECKED_IN: reject
    room = roomTable.get(booking.roomId)
    IF missing OR room.status not OCC/LCO: reject
    display booking and room; ASK confirmation
    IF no: return unchanged
    now = current date/time
    set booking CHECKED_IN -> CHECKED_OUT and actualCheckOutDateTime = now
    set room OCC/LCO -> DIR
    clear only room.confirmationNumber and room.checkoutTime
    keep booking in BST and reference in Trie
    call housekeepingControl.notifyGuestCheckout(roomId)
    save and show before/after state
```

Complexity: exact confirmation search is average O(log b), worst O(b). Trie prefix lookup is O(p + k) plus BST resolution of each returned reference. Checkout room lookup is average O(1).

## 9. Validation matrix

| Area | Validation | Where enforced | Failure behavior |
|---|---|---|---|
| Menus | Integer within displayed range | `InputValidator` + Boundary | Explain range and reprompt; no state change |
| Names | Trimmed, nonblank, supported letters/spaces; no file delimiter/newline | Boundary utility, rechecked by Control | Reject safely |
| Room type/status | Exact enum code | Parser/InputValidator | Reject input or report bad file line |
| Counts | Guests/nights positive | Boundary and Control | Reject safely |
| Capacity | Guests <= selected/actual room capacity | Registration and assignment controls | Keep request/room unchanged |
| Confirmation | Exactly eight digits and absent from BST | `IdGenerator` + assignment control | Regenerate; never overwrite |
| Existence | Room/staff/task/booking is present in relevant ADT | Controls | Clear not-found result |
| Request cancellation | Normalized name + exact arrival; WAITING only | Walk-In control | Restore queue order if absent |
| Queue capacity | Not full, or resize succeeds | Queue/control | No partial registration |
| Task uniqueness | No IPG/PST task for room | Housekeeping control traversal | Reject duplicate |
| Staff assignment | Staff AVL and currentRoomId blank | Housekeeping control | Reject without task creation |
| Cleaning start | Room DIR only | Housekeeping control | Reject |
| Cleaning completion | Room CLN + task IPG + matching BSY staff | Housekeeping control | Reject |
| Inspection | Room INS only | Housekeeping control | Reject |
| Late checkout | Booking CHECKED_IN, room OCC, new time later | Front Desk control | Reject; no history/task changes |
| Checkout | Booking CHECKED_IN; room OCC/LCO | Front Desk control | Reject |
| Transitions | Only listed state transitions | Controls/entity transition methods | Reject invalid transition |
| Confirmation prompts | Assignment, cancellation/status changes, completion, exit | Boundaries | `No` means unchanged |
| File input | Field count, enum/date/number validity, referential consistency | Data layer | Report line; do not insert corrupt record |
| Free text | Commas/newlines rejected or consistently quoted | Validator/Data layer | Prevent malformed files |

Cancellation preservation algorithm: for each of four queues, remember original size; dequeue exactly that many elements; move the first exact match to history as CANCELLED; enqueue every other item to a temporary queue; then restore all items in FIFO order. Total O(n) time and O(n) auxiliary queue space across requests.

## 10. Test and demonstration plan

### ADT unit tests

1. Circular queue: empty behavior, fill, wraparound, FIFO order, clear, and optional resize preserving order.
2. Linked list: add/get/remove first-middle-last, size, and clear.
3. Hash table: insert/update/search/remove, deliberately colliding keys, traversal count, and missing key.
4. BST: root/left/right insert, duplicate rejection, search hit/miss, sorted traversal, and skewed-tree demonstration.
5. Trie: exact and prefix search, spaces/case normalization, duplicate guest names, multiple confirmations, and no match.

### Integrated demo sequence

1. **Startup/Main Menu:** load 20 rooms, 15 requests, 15 bookings, 6 staff, 12 tasks, and dynamic history; show malformed/missing-file behavior separately.
2. **Walk-In:** register a valid STD guest; demonstrate over-capacity rejection; display queue position; cancel a middle request and prove FIFO order; assign an RDY room; verify request COMPLETED, Booking CHECKED_IN in BST/Trie, and Room OCC.
3. **Front Desk (late checkout):** unified-search the new booking by 8-digit number and by name prefix; apply later checkout; verify Booking time and Room LCO; demonstrate conflict call to Housekeeping.
4. **Front Desk (checkout):** check out the same OCC/LCO booking; verify actual time, CHECKED_OUT, Room DIR, and cleared room-only confirmation/time; prove historical BST/Trie search still works.
5. **Housekeeping:** list DIR room; select AVL staff; create IPG task and verify Room CLN/Staff BSY; complete task and verify COM/INS/AVL; update inspected room to RDY.
6. **Shared visibility:** return to Walk-In and Front Desk and show the same room immediately as RDY.
7. **Reports:** run all six monthly reports with empty, partial, and populated filters; verify explicit chronological/count sorting and totals.
8. **Persistence:** confirm exit, restart, and verify all six datasets and references/state are restored.
9. **Main Menu:** enter invalid choices in each menu, return from each module, decline exit once, then confirm exit.

### Transaction/failure tests

- Missing room/staff/task/booking, duplicate confirmation/task, full queue, wrong states, no active task, and declined confirmation cause zero mutations.
- Simulate save failure: preserve original file, report error, and decide whether in-memory changes remain pending for retry (document chosen policy).
- Late-checkout conflict captures history, postpones IPG task, releases staff, and leaves room LCO; later checkout makes room DIR.

## 11. Assumptions and conflicts to resolve before coding

1. **Academic-integrity constraint:** the supplied prompt requires planning first and explicit approval before implementing one class or small method group. Therefore no full application is generated at this checkpoint.
2. **Checkout time convention:** `numberOfNights` alone does not define the expected checkout clock time. Proposed rule: expected checkout is check-in plus `numberOfNights` days, unless the assignment requires a fixed noon checkout. Tutor confirmation is needed.
3. **Walk-in persistence reconstruction:** WAITING records load into the matching queues in file order; COMPLETED/CANCELLED records load into `requestHistory`. File order must therefore preserve FIFO order.
4. **Initial consistency:** seed Bookings, occupied Rooms, Trie references, staff assignments, and active tasks must agree. The loader should validate cross-references after parsing.
5. **History reversal details:** `RoomStatusHistory` has no previous completion time, priority, remarks, or task ID. It is sufficient only for the stated late-checkout conflict state; the exact automatic reversal scenario should be confirmed with the tutor.
6. **`PST` active definition:** the prompt calls IPG/PST active when preventing duplicate tasks, but only specifically pauses IPG during late checkout. Proposed rule: both block new tasks until the existing postponed task is explicitly resumed or otherwise resolved. No resume menu is specified, so tutor guidance is required.
7. **Report inclusion:** monthly walk-in demand needs both active WAITING queues and history because requests are split across ADTs. The report combines references into a basic array without Java Collections.
8. **Safe save timing:** proposed save after each successful transaction and again on confirmed exit. If the course expects exit-only saving, adjust centrally in `DataFileControl`.
9. **Hash-table traversal API:** `entries()` is necessary for reports because no Java Collection conversion is allowed. Confirm this method aligns with the team-assessed ADT rubric.
10. **Team-assessed ADT:** the team must identify which custom collection ADT is submitted for assessment and cite any course/adapted source in that interface. This cannot be chosen accurately without team/tutor input.
11. **Seed totals:** the suggested 68 fixed records include 20 rooms + 15 requests + 15 bookings + 6 staff + 12 tasks. Seed data should not imply every COMPLETED request maps one-to-one with all bookings unless timestamps/names are designed consistently.
12. **Atomicity:** text files do not provide database transactions. Controls validate fully before mutation, capture old values for rollback, and the data layer uses temporary-file replacement; true multi-file atomic commit is outside the assignment scope.

## Approval checkpoint

The next permitted step is **one class or one small method group only**. A sensible first choice is `QueueInterface<T>` plus `CircularArrayQueue<T>` and its focused tests, because the Walk-In workflow depends on it and its correctness can be defended independently.

Before continuing, select the single class or small method group to implement and confirm any relevant assumptions above. Keep your own notes so you can explain each algorithm, validation rule, ADT operation, and time complexity during assessment.
