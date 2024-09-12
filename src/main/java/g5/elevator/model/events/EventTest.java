package g5.elevator.model.events;
import static g5.elevator.model.events.EventType.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The EventTest class is responsible for conducting unit tests on the Event and EventType classes.
 *
 * @author Nabeel Azard
 * @version 1.0
 */

public class EventTest {
    private Event elevatorArrived;
    private Event traversedFloor;
    private Event elevatorDeparted;
    private Event elevatorLoading;
    private Event elevatorUnloading;
    private Event elevatorDoorOpening;
    private Event elevatorDoorOpen;
    private Event elevatorDoorClosing;
    private Event elevatorDoorClosed;
    private Event elevatorRecievedRequest;
    private Event schedulerRecievedRequest;

    @Before
    public void setUp() {
        elevatorArrived = new Event(EventType.ELEVATOR_ARRIVED, 1,2);
        traversedFloor = new Event(ELEVATOR_TRAVERSED_FLOOR);
        elevatorDeparted = new Event(ELEVATOR_DEPARTED, 21,4);
        elevatorLoading = new Event(ELEVATOR_LOADING);
        elevatorUnloading = new Event(ELEVATOR_UNLOADING, 775, 2);
        elevatorDoorOpening = new Event(ELEVATOR_DOOR_OPENING);
        elevatorDoorOpen = new Event(ELEVATOR_DOOR_OPEN, 87777, 4);
        elevatorDoorClosing = new Event(ELEVATOR_DOOR_CLOSING);
        elevatorDoorClosed = new Event(ELEVATOR_DOOR_CLOSED, 0);
        elevatorRecievedRequest = new Event(ELEVATOR_RECEIVED_REQUEST);
        schedulerRecievedRequest = new Event(SCHEDULER_RECEIVED_FLOOR_REQUEST, -1, 7);
    }

    @Test
    public void testEventType() {
        assertEquals(EventType.ELEVATOR_ARRIVED, elevatorArrived.getEventType());
        assertEquals(EventType.ELEVATOR_TRAVERSED_FLOOR, traversedFloor.getEventType());
        assertEquals(EventType.ELEVATOR_DEPARTED, elevatorDeparted.getEventType());
        assertEquals(EventType.ELEVATOR_LOADING, elevatorLoading.getEventType());
        assertEquals(EventType.ELEVATOR_UNLOADING, elevatorUnloading.getEventType());
        assertEquals(EventType.ELEVATOR_DOOR_OPENING, elevatorDoorOpening.getEventType());
        assertEquals(EventType.ELEVATOR_DOOR_OPEN, elevatorDoorOpen.getEventType());
        assertEquals(EventType.ELEVATOR_DOOR_CLOSING, elevatorDoorClosing.getEventType());
        assertEquals(EventType.ELEVATOR_DOOR_CLOSED,  elevatorDoorClosed.getEventType());
        assertEquals(EventType.ELEVATOR_RECEIVED_REQUEST,  elevatorRecievedRequest.getEventType());
        assertEquals(EventType.SCHEDULER_RECEIVED_FLOOR_REQUEST,  schedulerRecievedRequest.getEventType());

    }

    @Test
    public void testElevatorId() {
        assertEquals(1, elevatorArrived.getElevatorId());
        assertEquals(21, elevatorDeparted.getElevatorId());
        assertEquals(775, elevatorUnloading.getElevatorId());
        assertEquals(87777,  elevatorDoorOpen.getElevatorId());
        assertEquals(0, elevatorDoorClosed.getElevatorId());
        assertEquals(-1, schedulerRecievedRequest.getElevatorId());
    }

    @Test
    public void testSecondary(){
        assertEquals(2, elevatorArrived.getSecondary());
        assertEquals(4, elevatorDeparted.getSecondary());
        assertEquals(2, elevatorUnloading.getSecondary());
        assertEquals(4, elevatorDoorOpen.getSecondary());
        //assertEquals(5, elevatorDoorClosed.getSecondary());
        assertEquals(7, schedulerRecievedRequest.getSecondary());
    }

    @Test
    public void testToString() {
        String arrived = "Elevator 1 arrived to floor 2.";
        assertEquals(arrived, elevatorArrived.toString());
        String traversed = "Elevator -2147483648 traversed 1 floor.";
        assertEquals(traversed, traversedFloor.toString());
        String departed = "Elevator 21 departed for floor 4.";
        assertEquals(departed, elevatorDeparted.toString());
        String unloading = "Elevator 775 unloading passengers.";
        assertEquals(unloading, elevatorUnloading.toString());
        String doorOpen = "Elevator 87777 opened its doors.";
        assertEquals(doorOpen, elevatorDoorOpen.toString());
        String doorClosed = "Elevator 0 closed its doors.";
        assertEquals(doorClosed, elevatorDoorClosed.toString());
    }
}