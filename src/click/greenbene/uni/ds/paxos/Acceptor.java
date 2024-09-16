package click.greenbene.uni.ds.paxos;

import org.oxoo2a.sim4da.Message;
import org.oxoo2a.sim4da.Node;

public class Acceptor extends Node {

    public static String PREFIX = "acceptor";

    private int highest_proposal_number = -1;
    private int accepted_proposal_number = -1;
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
        if (proposal_number > highest_proposal_number) {
            Message promise = new Message(Messages.PROMISE);
            promise.add("sender", NodeName());
            promise.add("n", proposal_number);
            promise.add("highest", accepted_proposal_number);
            promise.add("value", value);

            highest_proposal_number = proposal_number;
            sendBlindly(promise, sender);
        }

    }

    private void onAccept(Message message) {
        int proposal_number = message.queryInteger("n");
        if (proposal_number >= highest_proposal_number) {
            highest_proposal_number = proposal_number;
            accepted_proposal_number = proposal_number;
            value = message.query("value");
            String sender = message.query("sender");

            Message accepted = new Message(Messages.ACCEPTED);
            accepted.add("sender", NodeName());
            accepted.add("n", proposal_number);

            sendBlindly(accepted, sender);
        }
    }
}
