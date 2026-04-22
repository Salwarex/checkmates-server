package utmn.checkmates.server.game.session;

import java.util.ArrayList;
import java.util.List;

public class FenReader {
    private final String fen;
    private final String[] rows;

    private final String nextSide;
    private final String castlings;
    private final String pawnTwoSquares;
    private final int subStep; //не было взятия или хода пешкой
    private final int step; //после каждого чёрного

    //0,1,2,3,4,5,6,7
    //abcdefgh
    //rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1

    public FenReader(String fen) {
        //todo: ввести проверки строки
        Desk.PositionMatcher.clear();

        this.fen = fen;
        String[] firstSplit = fen.split(" ");
        this.rows = firstSplit[0].split("/");
        this.nextSide = firstSplit[1];
        this.castlings = firstSplit[2];
        this.pawnTwoSquares = firstSplit[3];
        this.subStep = Integer.parseInt(firstSplit[4]);
        this.step = Integer.parseInt(firstSplit[5]);
    }

    public int getSubStep() {
        return subStep;
    }

    public int getStep() {
        return step;
    }

    public Position getPositionBetweenTwoSquaresPawnStep(){
        return Position.getPositionByNotation(pawnTwoSquares);
    }

    public boolean castlingAvailable(boolean shortCastling, boolean black){
        String check;
        if(shortCastling) check = "k";
        else check = "q";

        if(!black) check = check.toUpperCase();

        return castlings.contains(check);
    }

    public int currentSide(){
        return nextSide.equalsIgnoreCase("b") ? 1 : 0;
    }

    public String getFen() {
        return fen;
    }

    List<Desk.Row> getRows(){
        List<Desk.Row> rows = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            List<Desk.Square> squares = getSquares(i);
            Desk.Row row = new Desk.Row(squares);
            rows.add(row);
        }
        return rows;
    }

    List<Desk.Square> getSquares(int rowIndex){
        String fenState = rows[rowIndex];
        List<Desk.Square> squares = new ArrayList<>();
        int actualIndex = 0;

        for (int i = 0; i < fenState.length(); i++) {
            char character = fenState.charAt(i);
            String c = Character.toString(character);

            if(Character.isDigit(character)){
                byte b = Byte.parseByte(c);
                for(int j = 0; j < b; j++){
                    squares.add(new Desk.Square(new Position(rowIndex, actualIndex), null));
                    actualIndex++;
                }
            } else {
                FigureType type = FigureType.getByFen(c);
                boolean white = Character.isUpperCase(character);
                Figure figure = new Figure(type, white);
                squares.add(new Desk.Square(new Position(rowIndex, actualIndex), figure));
                actualIndex++;
            }
        }
        return squares;
    }
}
