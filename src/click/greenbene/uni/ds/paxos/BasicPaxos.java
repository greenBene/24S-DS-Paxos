package click.greenbene.uni.ds.paxos;

import org.oxoo2a.sim4da.Message;
import org.oxoo2a.sim4da.Node;
import org.oxoo2a.sim4da.Simulator;

public class BasicPaxos {


    public static int PROPOSAL_NODES = 3;
    public static int ACCEPTOR_NODES = 7;
    public static int LEARNER_NODES = 5;
    public static int CLIENT_NODES = 3;

    public static String[] NAMES = {"Alice", "Bob", "Charly", "Danny", "Emil", "Franca"};


    public static class Client extends Node {
        private static int MAX_WAIT_IN_SECONDS = 0;

        private final int proposeId;
        public Client(String name, int proposeId) {
            super(name);
            this.proposeId = proposeId;
        }

        @Override
        protected void engage() {
            int sleepTime = (int)(Math.random() * MAX_WAIT_IN_SECONDS * 1000);
            System.out.println(NodeName() + " sleeping for " + sleepTime + " milliseconds");
            sleep(sleepTime);

            Message propose = new Message(Messages.PROPOSE);
            propose.add("value", NodeName());

            sendBlindly(propose, Proposer.PREFIX+proposeId);
        }
    }


    public static void main(String[] args) {
        Proposer[] proposers = new Proposer[PROPOSAL_NODES];
        for (int i = 0; i < PROPOSAL_NODES; i++) {
            proposers[i] = new Proposer(i);
        }

        Acceptor[] acceptors = new Acceptor[ACCEPTOR_NODES];
        for (int i = 0; i < ACCEPTOR_NODES; i++) {
            acceptors[i] = new Acceptor(i);
        }

        Learner[] learners = new Learner[LEARNER_NODES];
        for (int i = 0; i < LEARNER_NODES; i++) {
            learners[i] = new Learner(i);
        }

        Client[] clients = new Client[CLIENT_NODES];
        for (int i = 0; i < CLIENT_NODES; i++) {
            clients[i] = new Client(NAMES[i], i);
        }


        Simulator simulator = Simulator.getInstance();
        simulator.simulate(30);
    }
}
