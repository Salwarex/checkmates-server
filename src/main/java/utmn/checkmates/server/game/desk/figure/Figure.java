package utmn.checkmates.server.game.desk.figure;

import java.util.Objects;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Figure figure = (Figure) object;
        return white == figure.white && type == figure.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, white);
    }
}
