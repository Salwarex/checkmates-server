package utmn.checkmates.server.game.session;

import utmn.checkmates.server.Application;
import utmn.checkmates.server.utility.Jsonable;
import utmn.checkmates.server.utility.logger.Logger;

public class SessionDto implements Jsonable {
    private int id;
    private String roomName;
    private boolean waitOpponent;

    public SessionDto(int id, String roomName, boolean waitOpponent) {
        this.id = id;
        this.roomName = roomName;
        this.waitOpponent = waitOpponent;
    }

    public SessionDto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public boolean isWaitOpponent() {
        return waitOpponent;
    }

    public void setWaitOpponent(boolean waitOpponent) {
        this.waitOpponent = waitOpponent;
    }

    @Override
    public String toJson() {
        Logger.log(this.getClass().getSimpleName(),
                "toJson",
                "Объект преобразовается в JSON"
        );
        return Application.getGson().toJson(this);
    }
}
