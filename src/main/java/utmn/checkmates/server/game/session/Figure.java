package utmn.checkmates.server.game.session;

public class Figure {
    private final FigureType type;
    private final boolean white;

    public Figure(FigureType type, boolean white) {
        this.type = type;
        this.white = white;
    }

    public FigureType getType() {
        return type;
    }

    public boolean isWhite() {
        return white;
    }
}
