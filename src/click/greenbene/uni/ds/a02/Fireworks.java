package click.greenbene.uni.ds.a02;

import org.oxoo2a.sim4da.Message;
import org.oxoo2a.sim4da.Node;
import org.oxoo2a.sim4da.Simulator;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Fireworks {

    public static final int NUMBER_OF_ACTOR_NODES = 20;
    public static final int MAX_NUMBER_OF_FIREWORK_TARGET_NODES = 5;
    public static final int MAX_SLEEP_TIME_IN_SECONDS = 5;
    public static final double LIKELIHOOD_TO_FORWARD_FIREWORK = 0.5;
    public static final double LIKELIHOOD_DECREASE = 0.5;

    enum STATE {
        ACTIVE,
        PASSIVE
    }

    public static class ActorNode extends Node {
        public static final String NAME_PREFIX = "ActorNode#";
        public static final Message FIREWORK_MESSAGE = new Message().add("name", "FIREWORK");

        private STATE currentState;
        private double forward_likelihood;

        public ActorNode(int id) {
            super(NAME_PREFIX + id);
            this.currentState = STATE.ACTIVE;
            this.forward_likelihood = LIKELIHOOD_TO_FORWARD_FIREWORK;
        }

        @Override
        protected void engage() {
            int sleepTime = (int)(Math.random() * MAX_SLEEP_TIME_IN_SECONDS * 1000);
            System.out.println(NodeName() + " sleeping for " + sleepTime + " milliseconds");
            sleep(sleepTime);
            sendFireworkToRandomSubset();
            currentState = STATE.PASSIVE;

            while (true) {
                Message m = receive();
                if (m.query("name").equals("FIREWORK")) {
                    receivedFireworks(m);
                }

            }
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
            }
        }

    }

    public static void main(String[] args) {
        ActorNode[] nodes = new ActorNode[NUMBER_OF_ACTOR_NODES];
        for(int i = 0; i < NUMBER_OF_ACTOR_NODES; i++) {
            nodes[i] = new ActorNode(i);
        }

        Simulator simulator = Simulator.getInstance();
        simulator.simulate(20);
    }

}
