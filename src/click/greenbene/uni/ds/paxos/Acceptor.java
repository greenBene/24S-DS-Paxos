package click.greenbene.uni.ds.paxos;

import org.oxoo2a.sim4da.Message;
import org.oxoo2a.sim4da.Node;

public class Acceptor extends Node {

    public static String PREFIX = "acceptor";

    private int highestProposalNumber = -1;
    private int acceptedProposalNumber = -1;
    private String value = "";

    public Acceptor(int id) {
        super(PREFIX + id);
    }

    @Override
    protected void engage() {
        while (true) {
            Message m = receive();
            String name = m.query("name");
            switch (name) {
                case "PREPARE":
                    onPrepare(m);
                    break;
                case "ACCEPT":
                    onAccept(m);
                    break;
            }
        }
    }

    private void onPrepare(Message message) {
        int proposal_number = message.queryInteger("n");
        String sender = message.query("sender");
        System.out.println(NodeName() + ": Received prepare proposal " + proposal_number + " from " + sender);
        if (proposal_number > highestProposalNumber) {
            Message promise = new Message(Messages.PROMISE);
            promise.add("sender", NodeName());
            promise.add("n", proposal_number);
            promise.add("highest", acceptedProposalNumber);
            promise.add("value", value);

            System.out.println(NodeName() + ": Sending promise ("
                    + proposal_number + ","
                    + acceptedProposalNumber + ", "
                    + value + ") to " + sender);

            highestProposalNumber = proposal_number;
            sendBlindly(promise, sender);
        } else {
            System.out.println(NodeName() + ": Ignoring prepare proposal " + proposal_number + " from " + sender);
        }
    }

    private void onAccept(Message message) {
        int proposal_number = message.queryInteger("n");
        String sender = message.query("sender");

        System.out.println(NodeName() + ": Received accept proposal " + proposal_number + " from " + sender);

        if (proposal_number >= highestProposalNumber) {
            highestProposalNumber = proposal_number;
            acceptedProposalNumber = proposal_number;
            value = message.query("value");

            System.out.println(NodeName() + ": Accepted proposal("
                    + proposal_number + ","
                    + value + ") from " + sender);

            Message accepted = new Message(Messages.ACCEPTED);
            accepted.add("sender", NodeName());
            accepted.add("n", proposal_number);

            sendBlindly(accepted, sender);
        } else {
            System.out.println(NodeName() + ": Ignoring accept proposal " + proposal_number + " from " + sender);
        }
    }
}
