package utmn.checkmates.server.game.session;

public class StepModelManager {

    public static boolean isAvailable(FigureType type, int color, Position from, Position to){
        if (from == null || to == null || from.equals(to)) {
            return false;
        }

        int fromRow = from.getRow();
        int fromCol = from.getColumn();
        int toRow = to.getRow();
        int toCol = to.getColumn();

        if (!isOnBoard(fromRow, fromCol) || !isOnBoard(toRow, toCol)) {
            return false;
        }

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        switch (type) {
            case KING:
                return rowDiff <= 1 && colDiff <= 1 && (rowDiff > 0 || colDiff > 0);

            case QUEEN:
                return (fromRow == toRow || fromCol == toCol) || (rowDiff == colDiff);

            case ROOK:
                return fromRow == toRow || fromCol == toCol;

            case BISHOP:
                return rowDiff == colDiff && rowDiff > 0;

            case KNIGHT:
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);

            case PAWN:
                return isPawnMoveValid(color, fromRow, fromCol, toRow, toCol);

            default:
                return false;
        }
    }

    private static boolean isOnBoard(int row, int col) {
        return row >= 0 && row <= 7 && col >= 0 && col <= 7;
    }

    private static boolean isPawnMoveValid(int color, int fromRow, int fromCol, int toRow, int toCol) {
        int direction = (color == 0) ? 1 : -1;

        int startRow = (color == 0) ? 6 : 1;

        if (fromCol == toCol) {
            if (toRow == fromRow + direction) {
                return true;
            }
            if (fromRow == startRow && toRow == fromRow + 2 * direction) {
                return true;
            }
            return false;
        }

        int colDiff = Math.abs(toCol - fromCol);
        return colDiff == 1 && toRow == fromRow + direction;
    }
}