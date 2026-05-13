package utmn.checkmates.server.game.desk;

import utmn.checkmates.server.game.desk.figure.Figure;
import utmn.checkmates.server.game.desk.figure.FigureType;
import utmn.checkmates.server.game.GameState;
import utmn.checkmates.server.utility.logger.Logger;

public class StepModelManager {

    public static boolean isAvailable(FigureType type, int color, Position from, Position to, GameState gameState){
        if (from == null || to == null || from.equals(to)) {
            return false;
        }

        int fromFile = from.getRow();
        int fromRank = from.getColumn();
        int toFile = to.getRow();
        int toRank = to.getColumn();

        if (!isOnBoard(fromFile, fromRank) || !isOnBoard(toFile, toRank)) {
            return false;
        }

        Desk desk = gameState.getDesk();

        Desk.Square toSquare = desk.getSquare(to);
        if (toSquare != null && toSquare.getFigure() != null) {
            Figure toFigure = toSquare.getFigure();
            boolean isSameColor = (toFigure.isWhite() && color == 0) || (!toFigure.isWhite() && color == 1);
            if (isSameColor) {
                return false;
            }
        }

        switch (type) {
            case KING:
                return isKingMoveValid(color, fromFile, fromRank, toFile, toRank, gameState);
            case QUEEN:
                return isSlidingPieceValid(fromFile, fromRank, toFile, toRank, desk, true, true);
            case ROOK:
                return isSlidingPieceValid(fromFile, fromRank, toFile, toRank, desk, true, false);
            case BISHOP:
                return isSlidingPieceValid(fromFile, fromRank, toFile, toRank, desk, false, true);
            case KNIGHT:
                return isKnightMoveValid(fromFile, fromRank, toFile, toRank);
            case PAWN:
                return isPawnMoveValid(color, fromFile, fromRank, toFile, toRank, gameState);
            default:
                return false;
        }
    }

    private static boolean isOnBoard(int file, int rank) {
        return file >= 0 && file <= 7 && rank >= 0 && rank <= 7;
    }

    private static boolean isSlidingPieceValid(int fromFile, int fromRank, int toFile, int toRank, Desk desk, boolean checkOrthogonal, boolean checkDiagonal) {
        int dFile = toFile - fromFile;
        int dRank = toRank - fromRank;

        if (checkOrthogonal && checkDiagonal) {
            if (fromFile != toFile && fromRank != toRank && Math.abs(dFile) != Math.abs(dRank)) {
                return false;
            }
        } else if (checkOrthogonal && !checkDiagonal) {
            if (fromFile != toFile && fromRank != toRank) {
                return false;
            }
        } else if (!checkOrthogonal && checkDiagonal) {
            if (Math.abs(dFile) != Math.abs(dRank) || dFile == 0) {
                return false;
            }
        }

        int stepFile = Integer.signum(dFile);
        int stepRank = Integer.signum(dRank);

        int curFile = fromFile + stepFile;
        int curRank = fromRank + stepRank;

        while (curFile != toFile || curRank != toRank) {
            Desk.Square square = desk.getSquare(new Position(curFile, curRank));
            if (square == null || square.getFigure() != null) {
                return false;
            }
            curFile += stepFile;
            curRank += stepRank;
        }

        return true;
    }

    private static boolean isKnightMoveValid(int fromFile, int fromRank, int toFile, int toRank) {
        int dFile = Math.abs(toFile - fromFile);
        int dRank = Math.abs(toRank - fromRank);
        return (dFile == 2 && dRank == 1) || (dFile == 1 && dRank == 2);
    }

    private static boolean isKingMoveValid(int color, int fromFile, int fromRank, int toFile, int toRank, GameState gameState) {
        int dFile = Math.abs(toFile - fromFile);
        int dRank = Math.abs(toRank - fromRank);

        if (dFile <= 1 && dRank <= 1 && (dFile > 0 || dRank > 0)) {
            return true;
        }

        return false;
    }

    private static boolean isPawnMoveValid(int color, int fromRow, int fromCol, int toRow, int toCol, GameState gameState) {
        int direction = (color == 0) ? -1 : 1;
        int startRow = (color == 0) ? 6 : 1;

        Desk desk = gameState.getDesk();

        if (fromCol == toCol) {
            if (toRow == fromRow + direction) {
                Desk.Square sq = desk.getSquare(new Position(toRow, toCol));
                return sq != null && sq.getFigure() == null;
            }
            if (fromRow == startRow && toRow == fromRow + 2 * direction) {
                Position mid = new Position(fromRow + direction, fromCol);
                Desk.Square midSq = desk.getSquare(mid);
                Desk.Square toSq = desk.getSquare(new Position(toRow, toCol));
                return midSq != null && midSq.getFigure() == null &&
                        toSq != null && toSq.getFigure() == null;
            }
            return false;
        }

        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
            Desk.Square toSq = desk.getSquare(new Position(toRow, toCol));
            if (toSq != null && toSq.getFigure() != null) {
                Figure fig = toSq.getFigure();
                boolean isEnemy = (fig.isWhite() && color == 1) || (!fig.isWhite() && color == 0);
                if (isEnemy) return true;
            }
            Position ep = gameState.getAislePos();
            if (ep != null && ep.equals(new Position(toRow, toCol))) {
                Position captured = new Position(fromRow, toCol);
                Desk.Square capSq = desk.getSquare(captured);
                if (capSq != null && capSq.getFigure() != null) {
                    Figure cap = capSq.getFigure();
                    boolean isEnemyPawn = cap.getType() == FigureType.PAWN &&
                            ((cap.isWhite() && color == 1) || (!cap.isWhite() && color == 0));
                    if (isEnemyPawn) return true;
                }
            }
        }
        return false;
    }

    public static CastlingResult isCastlingValid(int color,
                                           int kingRow,
                                           int kingCol,
                                           int bishopRow,
                                           int bishopCol,
                                           boolean bl,
                                           boolean bs,
                                           boolean wl,
                                           boolean ws){
        int difference = Math.abs(bishopCol - kingCol);
        int bias = bishopRow - kingRow;

        Logger.log("GameState", "move", "Проверка на валидность рокировки!");

        if(bias != 0 || (difference < 3 || difference > 4)) {
            //
            Logger.log("GameState", "move", "Рокировка неудачна: Разница или отклонение больше допустимых: difference : %d, bias: %d".formatted(difference, bias));
            //
            return CastlingResult.UNAVAILABLE;
        }

        boolean isLong = difference == 4;

        if(color == 0){ //белые
            if(bishopRow != 7 || kingRow != 7) {
                //
                Logger.log("GameState", "move", "Рокировка неудачна: Белая рокировка происходит в 7 строке (текущая: %d)".formatted(bishopRow));
                //
                return CastlingResult.UNAVAILABLE;
            }

            if(isLong && wl) return isCastlingValidLong(7, kingCol, bishopCol);
            else if(!isLong && ws) return isCastlingValidShort(7, kingCol, bishopCol);
        }else{ //черные
            if(bishopRow != 0 || kingRow != 0) {
                //
                Logger.log("GameState", "move", "Рокировка неудачна: Белая рокировка происходит в 0 строке (текущая: %d)".formatted(bishopRow));
                //
                return CastlingResult.UNAVAILABLE;
            }

            if(isLong && bl) return isCastlingValidLong(0, kingCol, bishopCol);
            else if(!isLong && bs) return isCastlingValidShort(0, kingCol, bishopCol);
        }
        return CastlingResult.UNAVAILABLE;
    }

    private static boolean emptyLine(int row, int start, int end){
        for(int i = start + 1; i < end; i++){
            Desk.Square square = Desk.PositionMatcher.get(new Position(row, i));
            if(square != null && square.getFigure() != null) {
                //
                Logger.log("GameState", "move", "Рокировка неудачна: Найдена промежуточная фигура в позиции (%d, %d)".formatted(row, i));
                //
                return false;
            }
        }
        return true;
    }

    private static CastlingResult isCastlingValidLong(int row, int kingCol, int rookCol){
        if(emptyLine(row, kingCol, rookCol)){
            Position kingPos = new Position(row, 2);
            Position rookPos = new Position(row, 3);
            //
            Logger.log("GameState", "move", "Рокировка удачна: Длинная, kingPos: %s; rookPos: %s".formatted(kingPos.toString(), rookPos.toString()));
            //
            return new CastlingResult(kingPos, rookPos, true);
        }
        return CastlingResult.UNAVAILABLE;
    }

    private static CastlingResult isCastlingValidShort(int row, int kingCol, int rookCol){
        if(emptyLine(row, kingCol, rookCol)){
            Position kingPos = new Position(row, 6);
            Position rookPos = new Position(row, 5);
            //
            Logger.log("GameState", "move", "Рокировка удачна: Короткая, kingPos: %s; rookPos: %s".formatted(kingPos.toString(), rookPos.toString()));
            //
            return new CastlingResult(kingPos, rookPos, false);
        }
        return CastlingResult.UNAVAILABLE;
    }

}