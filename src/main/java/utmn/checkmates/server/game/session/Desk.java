package utmn.checkmates.server.game.session;

import utmn.checkmates.server.utility.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Desk {
    private final List<Column> columns;
    private final static String START_FEN_NOTATION
            = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public Desk(List<Column> columns) {
        this.columns = columns;
        Logger.log("Desk", "Constructor", "Доска инициализирована! Количество строк : %d".formatted(columns.size()));
    }

    public static class Column {
        private final List<Square> squares;

        public Column(List<Square> squares){
            this.squares = squares;
            Logger.log("Desk.Row", "Constructor", "Строка инициализирована! Количество клеток : %d".formatted(squares.size()));
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

            //Logger.log("Desk.Square", "Constructor", "Клетка %s инициализирована! Фигура : %s".formatted(pos, figure == null ? "null" : figure.getType()));
            System.out.printf(
                    "[%s,%d,%d]" + (position.getColumn() == 7 ? "%n" : ""), figure != null ? (figure.isWhite() ? figure.getType().getFenWhite() : figure.getType().getFenBlack()) : "-",
                    position.getRow(),
                    position.getColumn());
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
            Logger.log("Desk", "setFigure", "Тип фигуры в клетке %s изменен на %s"
                    .formatted(pos, figure == null ? "null" : figure.getType()));
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
            Logger.log("Desk.PositionMatcher", "clear", "Кэш совмещения PositionMatcher очищается!");
            figureMap.clear();
        }
    }
}


