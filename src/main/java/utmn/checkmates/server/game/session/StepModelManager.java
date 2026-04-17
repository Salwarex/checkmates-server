package utmn.checkmates.server.game.session;

public class StepModelManager {

    /**
     * Проверяет, возможен ли ход для заданной фигуры с учётом правил шахмат
     * (без учёта состояния доски: шах, блокировка пути, наличие фигур для взятия)
     *
     * @param type тип фигуры
     * @param color цвет фигуры: 0 = белые (движутся к строке 8), 1 = чёрные (движутся к строке 1)
     * @param from начальная позиция
     * @param to конечная позиция
     * @return true если ход геометрически возможен по правилам шахмат
     */
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
                if (fromRow == toRow || fromCol == toCol) {
                    return true;
                }
                return rowDiff == colDiff;

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

    /**
     * Вспомогательный метод: проверка что позиция находится на доске
     */
    private static boolean isOnBoard(int row, int col) {
        return row >= 1 && row <= 8 && col >= 0 && col <= 7;
    }

    /**
     * Вспомогательный метод: проверка валидности хода пешки
     *
     * @param color 0 = белые (движение +1 по строке), 1 = чёрные (движение -1)
     */
    private static boolean isPawnMoveValid(int color, int fromRow, int fromCol, int toRow, int toCol) {
        int direction = (color == 0) ? 1 : -1;  // Направление движения
        int startRow = (color == 0) ? 2 : 7;     // Стартовая строка для двойного хода

        int colDiff = Math.abs(toCol - fromCol);

        if (fromCol == toCol) {
            if (toRow == fromRow + direction) {
                return true;
            }
            if (fromRow == startRow && toRow == fromRow + 2 * direction) {
                return true;
            }
            return false;
        }

        if (colDiff == 1 && toRow == fromRow + direction) {
            return true;
        }

        return false;
    }
}