package utmn.checkmates.server.network.packet;

import utmn.checkmates.server.Application;
import utmn.checkmates.server.utility.Jsonable;
import utmn.checkmates.server.utility.logger.Logger;


public abstract class Packet implements Jsonable {
    public Packet() {
        Logger.log(this.getClass().getSimpleName(),
                "Constructor",
                "Объект пакета инициализирован"
        );
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
