package utmn.checkmates.server.game.desk;

public class CastlingResult {
    private final Position newKingPos;
    private final Position newRookPos;
    public static final CastlingResult UNAVAILABLE = new CastlingResult(null, null, false);
    private final boolean _long;

    public CastlingResult(Position newKingPos, Position newRookPos, boolean _long) {
        this.newKingPos = newKingPos;
        this.newRookPos = newRookPos;
        this._long = _long;
    }

    public boolean isAvailable(){
        return newRookPos != null && newKingPos != null;
    }

    public boolean isLong() {
        return _long;
    }

    public Position getNewKingPos() {
        return newKingPos;
    }

    public Position getNewRookPos() {
        return newRookPos;
    }
}
