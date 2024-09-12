package g5.elevator.model.events;


/**
 * Encapsulates events, will be used for displaying events using a java UI framework
 */
public class Event {
    private EventType eventType;
    private final int elevatorId;
    private final int secondary;
    private final String message;

    /**
     * Creates a non-elevator event instance
     * @param eventType the event type
     */
    public Event(EventType eventType) {
        this(eventType, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
    /**
     * Creates an elevator event instance
     * @param eventType the elevator event type
     * @param elevatorId the elevator ID
     */
    public Event(EventType eventType, int elevatorId) { this(eventType, elevatorId, Integer.MIN_VALUE); }
    /**
     * Creates an elevator event instance with some supplementary information
     * @param eventType the elevator event type
     * @param elevatorId the elevator ID
     * @param secondary secondary/supplementary information
     */
    public Event(EventType eventType, int elevatorId, int secondary) {
        this.eventType = eventType;
        this.elevatorId = elevatorId;
        this.secondary = secondary;
        this.message = null;
    }
    /**
     * Creates a general communication event instance with some supplementary information
     * @param eventType the elevator event type
     * @param message the message
     */
    public Event(EventType eventType, String message) {
        this.eventType = eventType;
        this.message = message;
        this.elevatorId = Integer.MAX_VALUE;
        this.secondary = Integer.MAX_VALUE;
    }
    /**
     * Creates an elevator communication event instance with some supplementary information
     * @param eventType the elevator event type
     * @param elevatorId elevator id
     * @param message the message
     */
    public Event(EventType eventType, int elevatorId, String message) {
        this.eventType = eventType;
        this.message = message;
        this.elevatorId = elevatorId;
        this.secondary = Integer.MAX_VALUE;
    }

    // Getters
    public EventType getEventType() { return eventType; }
    public int getElevatorId() { return elevatorId; }
    public int getSecondary() { return secondary; }

    @Override
    public String toString() {
        return
            eventType == EventType.ELEVATOR_ARRIVED ?                               String.format("Elevator %d arrived to floor %d.", elevatorId, secondary)
                    : eventType == EventType.ELEVATOR_TRAVERSED_FLOOR ?             String.format("Elevator %d traversed 1 floor.", elevatorId)
                    : eventType == EventType.ELEVATOR_IDLE_TOO_LONG ?               String.format("Elevator %d has been idle for too long and is now turning off...", elevatorId)
                    : eventType == EventType.ELEVATOR_DEPARTED ?                    String.format("Elevator %d departed for floor %d.", elevatorId, secondary)
                    : eventType == EventType.ELEVATOR_LOADING ?                     String.format("Elevator %d loading passenger (%s).", elevatorId, message)
                    : eventType == EventType.ELEVATOR_UNLOADING ?                   String.format("Elevator %d unloading passenger (%s).", elevatorId, message)
                    : eventType == EventType.ELEVATOR_DOOR_OPENING ?                String.format("Elevator %d opening its doors.", elevatorId)
                    : eventType == EventType.ELEVATOR_DOOR_STUCK ?                  String.format("Elevator %d doors stuck.", elevatorId)
                    : eventType == EventType.ELEVATOR_DOOR_OPEN ?                   String.format("Elevator %d opened its doors.", elevatorId)
                    : eventType == EventType.ELEVATOR_DOOR_CLOSING ?                String.format("Elevator %d closing its doors.", elevatorId)
                    : eventType == EventType.ELEVATOR_DOOR_CLOSED ?                 String.format("Elevator %d closed its doors.", elevatorId)
                    : eventType == EventType.ELEVATOR_RECEIVED_REQUEST ?            String.format("Elevator %d received a pickup request for floor %d.", elevatorId, secondary)
                    : eventType == EventType.ELEVATOR_STUCK ?                       String.format("Elevator %d is stuck.", elevatorId)
                    : eventType == EventType.ELEVATOR_SHUTDOWN ?                    String.format("Elevator %d has shutdown and will no longer serve requests.", elevatorId)
                    : eventType == EventType.SCHEDULER_RECEIVED_FLOOR_REQUEST ?     "Scheduler received a pickup request."
                    : eventType == EventType.SCHEDULER_RECEIVED_ELEVATOR_REQUEST ?  "Scheduler received an elevator event."
                    : eventType == EventType.SCHEDULER_MOVE_TO_IDLE ?               "Scheduler moving to idle."
                    : eventType == EventType.FORWARDED ?                            String.format("Forwarded \"%s\".", message)
                    : eventType == EventType.SENT ?                                 String.format("Sent \"%s\".", message)
                    : eventType == EventType.RECEIVED ?                             String.format("Received \"%s\".", message)
                    :                                                               String.format("Unknown event: %s. Please update Event.toString().", eventType);
    }

}
