package click.greenbene.uni.ds.paxos;

import org.oxoo2a.sim4da.Message;
import org.oxoo2a.sim4da.Node;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Proposer extends Node {
    public static String PREFIX = "proposer";

    private int proposal_number;
    private String proposalValue;
    private List<String> chosenMajority;

    private int acceptedNumber = -1;
    private String acceptedValue = null;
    private int promiseCount = 0;
    private int acceptedCount = 0;

    public Proposer(int id) {
        super(PREFIX + id);
        proposal_number = id;
    }

    @Override
    protected void engage() {
        while (true) {
            Message m = receive();
            String name = m.query("name");
            switch (name) {
                case "PROPOSE":
                    onPropose(m);
                    break;
                case "PROMISE":
                    onPromise(m);
                    break;
                case "ACCEPTED":
                    onAccepted(m);
                    break;
            }
        }
    }


    private void onPropose(Message message) {
        proposalValue = message.query("value");
        promiseCount = 0;
        acceptedValue = "";
        acceptedNumber = -1;
        acceptedCount = 0;
        int proposeNumber = getNextProposalNumber();
        chosenMajority = getAcceptorMajority();

        Message prepare = new Message(Messages.PREPARE);
        prepare.add("n", proposeNumber);
        prepare.add("sender", NodeName());

        System.out.println(NodeName() + ": Start Proposal(" + proposeNumber + ","
                + proposalValue + ")");

        for(String acceptor : chosenMajority) {
            sendBlindly(prepare, acceptor);
        }
    }

    private void onPromise(Message message) {
        int n = message.queryInteger("n");
        int receivedHighestAcceptedNumber = message.queryInteger("highest");
        String receivedAcceptedValue = message.query("value");

        System.out.println(NodeName() + ": Receved promise(" + n + "," + receivedHighestAcceptedNumber + ","
                + receivedAcceptedValue + ")");
        if (n != proposal_number) {
            System.out.println(NodeName() + ": Ignore promise " + n);
            return;
        }
        if(receivedHighestAcceptedNumber > acceptedNumber) {
            acceptedNumber = receivedHighestAcceptedNumber;
            acceptedValue = receivedAcceptedValue;
        }
        promiseCount++;

        if(promiseCount == chosenMajority.size()) {
            System.out.println(NodeName() + ": Request Accepting of " + promiseCount);
            Message accept = new Message(Messages.ACCEPT);
            accept.add("n", proposal_number);
            accept.add("sender", NodeName());
            accept.add("value", (acceptedValue.isEmpty() ? proposalValue : acceptedValue));
            for (String acceptor : chosenMajority) {
                sendBlindly(accept, acceptor);
            }
        }
    }

    private void onAccepted(Message message) {
        int n = message.queryInteger("n");
        if (n != proposal_number) return;
        acceptedCount++;

        if(acceptedCount == chosenMajority.size()) {
            Message decide = new Message(Messages.DECIDE);
            decide.add("value", (acceptedValue.isEmpty() ? proposalValue : acceptedValue));
            for(String learner: getLearner()) {
                sendBlindly(decide, learner);
            }
        }

    }


    /**
     * Returns random majority set of acceptor nodes
     * @return List of acceptor nodes.
     */
    private List<String> getAcceptorMajority() {
        List<String> acceptorNodes =
            IntStream.range(0, BasicPaxos.ACCEPTOR_NODES)
                    .boxed()
                    .map((x) -> Acceptor.PREFIX + x)
                    .collect(Collectors.toList());

        Collections.shuffle(acceptorNodes);
        int majority = (BasicPaxos.ACCEPTOR_NODES/2) + 1;
        return acceptorNodes.subList(0, majority);
    }

    private List<String> getLearner() {
        return IntStream.range(0, BasicPaxos.LEARNER_NODES)
                        .boxed()
                        .map((x) -> Learner.PREFIX + x)
                        .toList();
    }


    /**
     * Returns the next unique proposal number.
     * @return Next proposal number
     */
    private int getNextProposalNumber() {
        proposal_number += BasicPaxos.PROPOSAL_NODES;
        return proposal_number;
    }
}
