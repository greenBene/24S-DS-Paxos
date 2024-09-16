package click.greenbene.uni.ds.paxos;

import org.oxoo2a.sim4da.Message;
import org.oxoo2a.sim4da.Node;

public class Learner extends Node {

    public static String PREFIX = "learner";

    public Learner (int id) {
        super(PREFIX + id);
    }

    @Override
    protected void engage() {
        while(true) {
            Message m = receive();
            String name = m.query("name");
            if (name.equals("DECIDE")) {
                System.out.println(NodeName() + ": Decided on value: " + m.query("value"));
            }
        }
    }
}
