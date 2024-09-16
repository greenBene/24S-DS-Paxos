package click.greenbene.uni.ds.paxos;

import org.oxoo2a.sim4da.Message;
import org.oxoo2a.sim4da.Node;

public class Client extends Node {

    private static final int MAX_WAIT_IN_MS = 500;
    public static final String[] NAMES = {"Alice", "Bob", "Charly", "Danny", "Emil", "Franca"};

    private final int proposeId;

    public Client(int proposeId) {
        super(NAMES[proposeId]);
        this.proposeId = proposeId;
    }

    @Override
    protected void engage() {
        int sleepTime = (int)(Math.random() * MAX_WAIT_IN_MS);
        System.out.println(NodeName() + ": Sleeping for " + sleepTime + " milliseconds");
        sleep(sleepTime);

        Message propose = new Message(Messages.PROPOSE);
        propose.add("value", NodeName());
        System.out.println(NodeName() + ": Requesting to be the leader.");
        sendBlindly(propose, Proposer.PREFIX+proposeId);
    }
}
