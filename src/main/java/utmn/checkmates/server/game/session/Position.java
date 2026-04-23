package utmn.checkmates.server.game.session;

import java.util.Objects;

public class Position {
    private final int column;
    private final int row;

    public Position(int row, int column) {
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Position position = (Position) object;
        return column == position.column && row == position.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + row +
                ", " + column +
                ')';
    }

    public static Position getPositionByNotation(String notation){
        if(notation.equals("-")) return null;

        //todo: добавить проверку
        char columnRaw = notation.charAt(0);
        int column = Integer.parseInt(((Character) notation.charAt(1)).toString());
        int row = switch (columnRaw){
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
        return new Position(row, column);
    }

    public static String getNotationByPosition(Position position){
        if (position == null) return "-";
        StringBuilder result = new StringBuilder();
        int row = position.getColumn();
        int column = position.getRow();

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
        int column = (b >> 3) & 0b111;
        int row = b & 0b111;
        return new Position(row, column);
    }
}
