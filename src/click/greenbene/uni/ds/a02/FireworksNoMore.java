package click.greenbene.uni.ds.a02;

import org.oxoo2a.sim4da.Message;
import org.oxoo2a.sim4da.Node;
import org.oxoo2a.sim4da.Simulator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class FireworksNoMore {
    public static final int NUMBER_OF_ACTOR_NODES = 20;
    public static final int MAX_NUMBER_OF_FIREWORK_TARGET_NODES = 5;
    public static final int MAX_SLEEP_TIME_IN_SECONDS = 5;
    public static final double LIKELIHOOD_TO_FORWARD_FIREWORK = 0.5;
    public static final double LIKELIHOOD_DECREASE = 0.5;

    enum STATE {
        ACTIVE,
        PASSIVE
    }

    public static class ObserverNode extends Node {

        public static final Message GET_STATE = new Message().add("name", "GETSTATE");
        public static final Message TERMINATED = new Message().add("name", "TERMINATED");

        private int sentTotal = 0;
        private int receivedTotal = 0;
        private int passiveActors = 0;
        private int sentTotalPrevious = 0;
        private int receivedTotalPrevious = 0;

        public ObserverNode() {
            super("ObserverNode");
        }

        @Override
        protected void engage(){

            while (true) {
                System.out.println(NodeName() + " is starting next round to determine if system is terminated");

                sendGetStateMessageToAllActors();
                processSendStateMessageFromAllActors();

                if (isTerminated()) {
                    System.out.println(NodeName() + " detected that the system is terminated after " + sentTotal + " messages were send");
                    sendTerminateMessageToAllActors();
                    break;
                }

                sentTotalPrevious = sentTotal;
                receivedTotalPrevious = receivedTotal;
                sleep(200);
            }
        }

        private void sendGetStateMessageToAllActors() {
            for (int i = 0; i < NUMBER_OF_ACTOR_NODES; i++) {
                sendBlindly(new Message(GET_STATE).add("sender", NodeName()), ActorNode.NAME_PREFIX + i);
            }
        }

        private void processSendStateMessageFromAllActors() {
            Map<String, Boolean> received = new HashMap<>();

            sentTotal = 0;
            receivedTotal = 0;
            passiveActors = 0;

            for (int i = 0; i < NUMBER_OF_ACTOR_NODES; i++) {
                Message m = receive();
                String sender = m.query("sender");
                if (received.containsKey(sender)) {
                    System.out.println("Received unexpected message from " + sender);
                }
                received.put(sender, true);
                sentTotal += m.queryInteger("msgSent");
                receivedTotal += m.queryInteger("msgReceived");
                String state = m.query("state");
                passiveActors += (state.equals(STATE.PASSIVE.toString()) ? 1 : 0);
            }
        }

        private boolean isTerminated() {
            return passiveActors == NUMBER_OF_ACTOR_NODES &&
                    sentTotal == receivedTotal &&
                    sentTotal == sentTotalPrevious &&
                    receivedTotal == receivedTotalPrevious &&
                    sentTotal > 0;
        }

        private void sendTerminateMessageToAllActors() {
            for(int i = 0; i < NUMBER_OF_ACTOR_NODES; i++) {
                sendBlindly(new Message(TERMINATED), ActorNode.NAME_PREFIX + i);
            }
        }
    }

    public static class ActorNode extends Node {
        public static final String NAME_PREFIX = "ActorNode#";
        public static final Message FIREWORK_MESSAGE = new Message().add("name", "FIREWORK");
        public static final Message SEND_STATE = new Message().add("name", "SENDSTATE");

        private STATE currentState;
        private double forward_likelihood;
        private int messagesReceived = 0;
        private int messagesSent = 0;

        public ActorNode(int id) {
            super(NAME_PREFIX + id);
            this.currentState = STATE.ACTIVE;
            this.forward_likelihood = LIKELIHOOD_TO_FORWARD_FIREWORK;
        }

        @Override
        protected void engage() {
            int sleepTime = (int)(Math.random() * MAX_SLEEP_TIME_IN_SECONDS * 1000);
            System.out.println(NodeName() + " sleeping for " + sleepTime + " ms.");
            sleep(sleepTime);
            sendFireworkToRandomSubset();
            currentState = STATE.PASSIVE;

            while (true) {
                Message m = receive();
                if (m.query("name").equals("FIREWORK")) {
                    messagesReceived++;
                    receivedFireworks(m);
                }
                if (m.query("name").equals("GETSTATE")) {
                    receivedGetStateRequest(m);
                }
                if (m.query("name").equals("TERMINATED")) {
                    System.out.println(NodeName() + " is stopped.");
                    break;
                }
            }
        }

        private void receivedGetStateRequest(Message m) {
            String sender = m.query("sender");
            Message out = new Message(SEND_STATE);
            out.add("sender", NodeName());
            out.add("state", currentState.toString());
            out.add("msgReceived", messagesReceived);
            out.add("msgSent", messagesSent);
            sendBlindly(out, sender);
        }

        private void receivedFireworks(Message m) {
            currentState = STATE.ACTIVE;
            System.out.println(NodeName() + " was woken up by firework message from " + m.query("sender") + ".");

            if(Math.random() < this.forward_likelihood) {
                System.out.println(NodeName() + " sends firework message to random subset.");
                sendFireworkToRandomSubset();
            } else {
                System.out.println(NodeName() + " does not send firework message to random subset.");
            }
            this.forward_likelihood *= LIKELIHOOD_DECREASE;
            System.out.println(NodeName() + " new likelihood is now " + this.forward_likelihood + "%.");
            currentState = STATE.PASSIVE;
        }

        private void sendFireworkToRandomSubset() {
            List<String> targetNodeNames = new java.util.ArrayList<>(
                    IntStream.range(0, NUMBER_OF_ACTOR_NODES)
                            .boxed()
                            .map((x) -> NAME_PREFIX + x)
                            .toList());

            // Remove reference to itself
            targetNodeNames.remove(NodeName());
            // Randomise remaining node names
            Collections.shuffle(targetNodeNames);
            // Select between 0 and MAX_NUMBER_OF_FIREWORK_TARGET_NODES nodes to send Firework to
            List<String> targetNodes = targetNodeNames.subList(0, (int) (Math.random() * MAX_NUMBER_OF_FIREWORK_TARGET_NODES));
            // Send firework message to selected nodes
            for (String nodeName : targetNodes) {
                sendBlindly(new Message(FIREWORK_MESSAGE).add("sender", NodeName()), nodeName);
                messagesSent++;
            }
        }

    }

    public static void main(String[] args) {
        Node[] nodes = new Node[NUMBER_OF_ACTOR_NODES+1];
        for(int i = 0; i < NUMBER_OF_ACTOR_NODES; i++) {
            nodes[i] = new ActorNode(i);
        }
        nodes[NUMBER_OF_ACTOR_NODES] = new ObserverNode();

        Simulator simulator = Simulator.getInstance();
        simulator.simulate(15);
    }
}
