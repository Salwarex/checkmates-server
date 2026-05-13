package utmn.checkmates.server.game.desk;

import utmn.checkmates.server.game.desk.figure.Figure;

import java.util.Objects;

public class SquareSnapshot {
    private final Figure type;
    private final Position position;

    public SquareSnapshot(Desk.Square square) {
        this.type = square.getFigure();
        this.position = square.getPos();
    }

    public Figure getFigure() {
        return type;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        SquareSnapshot that = (SquareSnapshot) object;
        return Objects.equals(type, that.type) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, position);
    }
}
