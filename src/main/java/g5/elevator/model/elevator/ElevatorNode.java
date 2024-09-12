package g5.elevator.model.elevator;

import g5.elevator.controllers.Updatable;
import g5.elevator.model.elevator.elevator_state.ElevatorIdleState;
import g5.elevator.defs.Defs;
import g5.elevator.model.elevator.elevator_comm_state.ElevatorCommState;
import g5.elevator.model.elevator.elevator_state.ElevatorState;
import g5.elevator.model.events.Event;
import g5.elevator.model.events.EventType;
import g5.elevator.model.instruction.Direction;
import g5.elevator.model.instruction.Instruction;
import g5.elevator.model.elevator.elevator_comm_state.ElevatorIdleCommState;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Elevator Sub-system which manages elevator-related behavior.
 * This class represents an elevator node that receives instructions from the scheduler system
 * and performs elevator operations accordingly.
 *
 * @author All
 */
public class ElevatorNode extends Thread {
    private static int nextId = 0;
    private final int id;
    private int currentFloor = 0;
    private float altitude = 0.0f;
    public float velocity = 0.0f; // no need for private attribute
    private ElevatorState state;
    private ElevatorCommState commState;
    public ArrayList<Integer> destinations = new ArrayList<>();;
    private final ArrayList<Event> log = new ArrayList<>();;
    private final ArrayList<Instruction> pendingInstructions = new ArrayList<>();;
    public DatagramSocket sSocket;
    public DatagramSocket rSocket;
    public boolean running = true;
    private final Updatable controller;
    private boolean registered = false;
    private boolean doorStuck = false;
    private boolean stuck = false;
    private ArrayList<Instruction> pickedupInstructions = new ArrayList<>();

    /**
     * Constructs an ElevatorNode object with default values.
     * Initializes elevator properties such as id, current floor, altitude, velocity, state, and data structures.
     */
    public ElevatorNode() {
        this(null);
    }
    public ElevatorNode(Updatable controller) {
        id = ElevatorNode.nextId++; // Why not just do: id = 0??
        // Because we need unique ids for each elevator, so we start with 0 and increment.
        // If you set it to 0, then all elevators have an id of 0, which defeats the point of an id. - Hamza
        this.controller = controller;
        try {
            sSocket = new DatagramSocket();
            rSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        rSocket.close();
        sSocket.close();
        running = false;
    }
    public boolean isRegistered() { return registered; }

    /**
     * Registers the elevator node with the Scheduler.
     * @return true if registration was successful, otherwise false.
     */
    private boolean register() {
        String msgString = String.format("elevator %d,register,%d", this.id, this.rSocket.getLocalPort());
        try {
            byte [] msg = msgString.getBytes();
            DatagramPacket registerFloorPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), Defs.SCHEDULER_PORT);
            this.sSocket.send(registerFloorPacket);
            addEvent(new Event(EventType.SENT, msgString));
            String res = awaitResponse();
            if(res.equals("OK")) {
                System.out.println("Registration complete!");
                return true;
            } else {
                System.out.println("Registration failed. Trying again...");
                return false;
            }
        } catch (SocketException e){
            if(e.getMessage().equals("Socket closed")) return false;
            throw new RuntimeException(e);
        } catch (UnknownHostException e){
            System.out.println(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    /**
     * Calls the update method on the controller, use with a UI
     */
    public void updateController() {
        if(controller == null) return;
        controller.update();
    }
    /**
     * Awaits response by the Scheduler, blocking method.
     * @return the response
     * @throws IOException if rSocket.receive throws an exception
     */
    private String awaitResponse() throws IOException {
        byte [] resByte = new byte[Defs.MSG_SIZE];
        DatagramPacket rPacket = new DatagramPacket(resByte, resByte.length);
        this.rSocket.receive(rPacket);
        String res = Defs.getMessage(rPacket.getData(), rPacket.getLength());
        addEvent(new Event(EventType.RECEIVED, res));
        return res;
    }
    public int getElevatorId() { return this.id; }
    public int getCurrentFloor() { return currentFloor; }
    public float getAltitude() { return altitude; }
    public float getVelocity() { return velocity; }
    public boolean isDoorStuck() { return doorStuck; }
    public boolean isStuck() { return stuck; }
    public ArrayList<Event> getLog() { return log; }
    public ArrayList<Integer> getDestinations() { return destinations; }
    public ElevatorState getElevatorState() { return state; }
    public ElevatorCommState getCommState() { return commState; }
    /**
     * Determines the pickup index for the given instruction.
     * This method calculates the index at which the pickup floor should be inserted into the destinations list.
     *
     * @param instruction the instruction containing pickup and destination floors
     * @return the index where the pickup floor should be inserted
     */
    public int getPickupIndex(Instruction instruction) {
        int index = 0;
        int pickup = instruction.getPickupFloor();
        Direction directionToPickup = instruction.getPickupFloor() - getCurrentFloor() > 0 ? Direction.UP : Direction.DOWN;
        int prevDestination = getCurrentFloor();
        Direction prevDirection = getDirection();
        for(int i = 0; i < destinations.size(); i++) {
            int curDestination = destinations.get(i);
            Direction curDirection = curDestination - prevDestination > 0 ? Direction.UP : Direction.DOWN;
            if(curDirection == instruction.getButtonDirection() && curDirection == directionToPickup) {
                if (pickup < curDestination) break;
            }

            // find if pickup is on the way
            int min = Math.min(prevDestination, curDestination);
            int max = Math.max(prevDestination, curDestination);
            if(min < pickup && pickup < max && curDirection != directionToPickup) {
                break;
            }
            index++;
            prevDirection = curDirection;
            prevDestination = curDestination;
        }

        return index;
    }

    /**
     * Calculates the elevator direction based on the next destination
     * @return null if the elevator isn't going anywhere, otherwise the direction the elevator needs to go to get to
     * its next destination
     */
    public Direction getDirection() {
        if(destinations.isEmpty()) return null;
        return getNextDestination() - currentFloor > 0 ? Direction.UP : Direction.DOWN;
    }

    /**
     * Calculates the current capacity
     * @return true
     */
    public int getCapacity() {
        return pickedupInstructions.size();
    }

    /**
     * Checks if there are no destinations
     * @return true if there are no destinations, otherwise false
     */
    public synchronized boolean destinationsEmpty(){
        return destinations.isEmpty();
    }
    /**
     * Adds a pickup instruction to the pending instructions list.
     *
     * @param instruction the pickup instruction to add
     */
    public synchronized void addPickup(Instruction instruction) {
        pendingInstructions.add(instruction);
        destinations.add(getPickupIndex(instruction), instruction.getPickupFloor());
        addEvent(new Event(EventType.ELEVATOR_RECEIVED_REQUEST, id, instruction.getPickupFloor()));
        updateController();
    }
    /**
     * Sets the state of the elevator.
     *
     * @param state the state to set
     */
    public void setState(ElevatorState state) {
        this.state = state;
        this.state.start();
    }
    public void setCommState(ElevatorCommState state) {
        this.commState = state;
        this.commState.start();
    }
    public void updateAltitude(float altitude) {
        this.altitude += altitude;
    }

    /**
     * Adds an event and prints that event to console
     * @param event the event to add
     */
    public void addEvent(Event event) {
        log.add(event);
        updateController();
        System.out.println(event);
        if(event.getEventType() == EventType.RECEIVED ||
                event.getEventType() == EventType.SENT ||
                event.getEventType() == EventType.FORWARDED
        ) return;
        sendEvent(event);
    }

    /**
     * Starts a stuck event
     */
    public void injectStuck() {
        System.out.println("stuck injected");
        stuck = true;
    }

    /**
     * Starts a door stuck event
     */
    public void injectDoorStuck() {
        System.out.println("door stuck injected");
        doorStuck = true;
    }


    public void resetDoorStuck() { doorStuck = false; }
    public void resetStuck() { stuck = false; }

    public void sendEvent(Event event) {
        // TODO: send event
        // 1. wrap event in a string
        // "elevator [id],event,[event.toString()]"
        String eventString = String.format("elevator %d, event, %s", id, event.toString());
        // 2. initialize packet with the wrapped event
        // 3. send packet to elevator
        try {
            byte[] eventData = eventString.getBytes();
            DatagramPacket eventPacket = new DatagramPacket(eventData, eventData.length, InetAddress.getLocalHost(), Defs.SCHEDULER_PORT);
            sSocket.send(eventPacket);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    /**
     * Traverses one floor
     * @param direction the direction (DOWN, UP)
     */
    public void traverse(Direction direction) {
        this.currentFloor += direction.ordinal()*2-1;
    }

    public Integer getNextDestination() {
        if (destinations.isEmpty()) return null;
        return destinations.get(0);
    }

    public synchronized void clearDestination() {
        if(destinations.isEmpty()) return;
        destinations.remove(0);
    }

    /**
     * Unloads passengers by removing the number that wanted to go to the current floor
     */
    public synchronized void unloadPassengers() {
        int i = 0;
        while(i < pickedupInstructions.size()) {
            Instruction instruction = pickedupInstructions.get(i);
            if(instruction.getDestinationFloor() == currentFloor) {
                addEvent(new Event(EventType.ELEVATOR_UNLOADING, id, instruction.toString()));
                pickedupInstructions.remove(i);
                continue;
            }
            i++;
        }
    }
    public synchronized void loadPassengers() {
        int i = 0;
        while(i < pendingInstructions.size()) {
            Instruction instruction = pendingInstructions.get(i);
            if (instruction.getPickupFloor() == currentFloor) {
                pickedupInstructions.add(instruction);
                addEvent(new Event(EventType.ELEVATOR_LOADING, id, instruction.toString()));
                if(!destinations.contains(instruction.getDestinationFloor())) {
                    Instruction temp = new Instruction(instruction.getTimestamp(), getDirection(), instruction.getDestinationFloor(), Integer.MAX_VALUE);
                    destinations.add(getPickupIndex(temp), instruction.getDestinationFloor());
                }
                pendingInstructions.remove(i);
                continue;
            }
            i++;
        }
    }
    @Override
    public void run() {
        System.out.printf("Elevator node %d Online\n", id);
        System.out.println("Registering");
        registered = register();
        if(!registered) {
            System.out.printf("Registration failed. Closing elevator node %d.\n", id);
            return;
        };
        System.out.println("\nListening");
        setCommState(new ElevatorIdleCommState(this));
        setState(new ElevatorIdleState(this));
    }

    public static void main(String[] args) {
        final int ELEVATOR_NUM = 4;


        for (int i = 0; i < ELEVATOR_NUM; i++) {
            ElevatorNode e = new ElevatorNode();
            e.start();
        }
    }
}
