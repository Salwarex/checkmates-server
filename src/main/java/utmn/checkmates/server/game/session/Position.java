package utmn.checkmates.server.game.session;

import java.util.Objects;

public class Position {
    private final int row;
    private final int column;

    public Position(int column, int row) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Position position = (Position) object;
        return row == position.row && column == position.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, row);
    }

    @Override
    public String toString() {
        return "(" + column +
                ", " + row +
                ')';
    }

    public static Position getPositionByNotation(String notation){
        if(notation.equals("-")) return null;

        //todo: добавить проверку
        char columnRaw = notation.charAt(0);
        int row = Integer.parseInt(((Character) notation.charAt(1)).toString());
        int column = switch (columnRaw){
            case 'a' -> 0;
            case 'b' -> 1;
            case 'c' -> 2;
            case 'd' -> 3;
            case 'e' -> 4;
            case 'f' -> 5;
            case 'g' -> 6;
            case 'h' -> 7;
            default -> -1;
        };
        return new Position(column, row);
    }

    public static String getNotationByPosition(Position position){
        if (position == null) return "-";
        StringBuilder result = new StringBuilder();
        int row = position.getRow();
        int column = position.getColumn();

        char columnStr = switch (column){
            case 0 -> 'a';
            case 1 -> 'b';
            case 2 -> 'c';
            case 3 -> 'd';
            case 4 -> 'e';
            case 5 -> 'f';
            case 6 -> 'g';
            case 7 -> 'h';
            default -> '?';
        };

        result.append(columnStr).append(row);
        return result.toString();
    }

    public static void main(String[] args) {
        System.out.println(Position.getByByte((byte )0b000111));
    }

    public static Position getByByte(byte b){
        int row = (b >> 3) & 0b111;
        int column = b & 0b111;
        return new Position(column, row);
    }
}
