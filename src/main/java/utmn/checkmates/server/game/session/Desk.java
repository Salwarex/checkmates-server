package utmn.checkmates.server.game.session;

import java.util.ArrayList;
import java.util.List;

public class Desk {
    private final List<Row> rows = new ArrayList<>();

    public Desk() {
    }

    private static class Row{
        private final List<Square> squares;

        public Row(List<Square> squares){
            this.squares = squares;
        }

        public Figure getFigureAt(int i){
            return squares.get(i).figure;
        }
    }

    private static class Square{
        private Figure figure;

        public Square(Figure figure) {
            this.figure = figure;
        }

        public Figure getFigure() {
            return figure;
        }

        public void setFigure(Figure figure) {
            this.figure = figure;
        }
    }
}


