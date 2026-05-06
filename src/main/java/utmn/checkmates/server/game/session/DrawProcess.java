package utmn.checkmates.server.game.session;

import utmn.checkmates.server.network.packet.output.DrawDecisionPacket;
import utmn.checkmates.server.network.tcp.SessionConnection;

public class DrawProcess {
    private final Session session;
    private final Player initiator;
    private boolean finished;
    private boolean agree;

    public DrawProcess(Session session, Player initiator) {
        this.session = session;
        this.initiator = initiator;
        finished = false;
        agree = false;
    }

    public Player getInitiator() {
        return initiator;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean result() throws GameRuleException {
        if(!finished) throw new GameRuleException("Ответ на запрос ещё не пришёл");
        return agree;
    }

    public void agree(SessionConnection connection) throws GameRuleException{
        if(connection.getPlayer().equals(initiator))
            throw new GameRuleException("Вы не можете ответить на запрос сдачи! Он был отправлен вами");
        agree = true; finished = true;
        session.end(new GameEnd(GameEndType.DRAW, -1));
    }

    public void disagree(SessionConnection connection) throws GameRuleException{
        if(connection.getPlayer().equals(initiator))
            throw new GameRuleException("Вы не можете ответить на запрос сдачи! Он был отправлен вами");
        agree = false; finished = true;
        session.broadcast(new DrawDecisionPacket(null, 1), connection.getAddress());
    }
}
