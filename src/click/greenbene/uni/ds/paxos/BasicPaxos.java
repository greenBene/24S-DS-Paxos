package click.greenbene.uni.ds.paxos;

import org.oxoo2a.sim4da.Simulator;

public class BasicPaxos {


    public static int PROPOSAL_NODES = 4;
    public static int ACCEPTOR_NODES = 7;
    public static int LEARNER_NODES = 5;
    public static int CLIENT_NODES = 4;

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
            if(i >= Client.NAMES.length) {
                throw new IllegalArgumentException("Not enough names available. Please add more");
            }
            clients[i] = new Client(i);
        }

        Simulator simulator = Simulator.getInstance();
        simulator.simulate(30);
    }
}
