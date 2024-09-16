package click.greenbene.uni.ds.paxos;

import org.oxoo2a.sim4da.Message;

public class Messages {

    // Send to proposer nodes to initiate a run
    //  Payload {'value': suggested_value }
    public static Message PROPOSE = new Message().add("name", "PROPOSE");

    // Send to acceptor nodes to request that sending node can send value
    //  Payload: { 'n': proposal_number }
    public static Message PREPARE = new Message().add("name", "PREPARE");

    // Send to proposer node to indicate that no proposal number lower than requested will be accepted
    //  Payload: { 'n': proposal_number, 'highest': accepted_number, 'value': accepted_value }
    public static Message PROMISE = new Message().add("name", "PROMISE");

    // Send to acceptor nodes to request acceptance of proposed value
    //  Payload: { 'n': proposal_number, 'value': proposed_value }
    public static Message ACCEPT = new Message().add("name", "ACCEPT");

    // Send to proposer to acknowledge that proposed value was accepted.
    // Payload: { "n": proposal_number }
    public static Message ACCEPTED = new Message().add("name", "ACCEPTED");

    // Send to learner nodes to notify them that a value was accepted upon
    //  Payload: { 'value': value }
    public static Message DECIDE = new Message().add("name", "DECIDE");
}
