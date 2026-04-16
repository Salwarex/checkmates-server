package utmn.checkmates.server.game.session;

public enum FigureType {
    KING("Король", "K", "k"),
    QUEEN("Ферзь", "Q", "q"),
    ROOK("Ладья", "R", "r"),
    KNIGHT("Конь", "N", "n"),
    BISHOP("Слон", "B", "b"),
    PAWN("Пешка", "P", "p");

    private final String displayName;
    private final String fenWhite;
    private final String fenBlack;

    FigureType(String displayName, String fenWhite, String fenBlack) {
        this.displayName = displayName;
        this.fenWhite = fenWhite;
        this.fenBlack = fenBlack;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFenWhite() {
        return fenWhite;
    }

    public String getFenBlack() {
        return fenBlack;
    }

    public static FigureType getByFen(String fen){
        for(FigureType type : FigureType.values()){
            if(type.getFenBlack().equalsIgnoreCase(fen)) return type;
        }
        return null;
    }
}
