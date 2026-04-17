package utmn.checkmates.server.game.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Desk {
    private final List<Row> rows;
    private final static String START_FEN_NOTATION
            = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public Desk(List<Row> rows) {
        this.rows = rows;
    }

    public static class Row{
        private final List<Square> squares;

        public Row(List<Square> squares){
            this.squares = squares;
        }

        public Figure getFigureAt(int i){
            return squares.get(i).figure;
        }
    }

    public Square getSquare(Position position){
        return PositionMatcher.get(position);
    }

    public static class Square{
        private final Position pos;

        private Figure figure;

        public Square(Position position, Figure figure) {
            this.pos = position;
            this.figure = figure;
            if(!PositionMatcher.put(pos, this))
                throw new RuntimeException("В ходе генерации поля возникла дублирующая клетка!");
        }

        public Figure getFigure() {
            return figure;
        }

        public Position getPos() {
            return pos;
        }

        public void setFigure(Figure figure) {
            this.figure = figure;
        }
    }

    class PositionMatcher{
        private static Map<Position, Square> figureMap = new HashMap<>();

        public static boolean put(Position pos, Square figure){
            if(figureMap.containsKey(pos)) return false;
            figureMap.put(pos, figure);
            return true;
        }

        public static Square get(Position pos){
            if(!figureMap.containsKey(pos)) return null;
            return figureMap.get(pos);
        }

        public static void clear(){
            figureMap.clear();
        }
    }
}


