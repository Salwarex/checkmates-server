package utmn.checkmates.server.game.process;

public enum GameEndType {
    MATE(0, "МАТ"),
    DRAW(1, "НИЧЬЯ"),
    RESIGN(2, "СДАЧА"),
    TIMER(3, "ТАЙМЕР"),
    DISCONNECTION(4, "ОТКЛЮЧЕНИЕ"),
    SERVER(5, "ТЕХНИЧЕСКОЕ ЗАВЕРШЕНИЕ")
    ;
    private final int code;
    private final String name;

    GameEndType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
