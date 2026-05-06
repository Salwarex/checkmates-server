package utmn.checkmates.server.game.desk;

import utmn.checkmates.server.game.desk.figure.Figure;
import utmn.checkmates.server.game.desk.figure.FigureType;
import utmn.checkmates.server.game.GameState;

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

        return isCastlingValid(color, fromFile, fromRank, toFile, toRank, gameState);
    }

    private static boolean isCastlingValid(int color, int fromFile, int fromRank, int toFile, int toRank, GameState gameState) {
        Desk desk = gameState.getDesk();

        if (fromFile != 4) {
            return false;
        }

        boolean isShort = (toFile == 6);
        boolean isLong = (toFile == 2);

        if (!isShort && !isLong) {
            return false;
        }

        boolean castlingAllowed;
        if (color == 0) {
            castlingAllowed = isShort ? gameState.isWhiteShortCastling() : gameState.isWhiteLongCastling();
        } else {
            castlingAllowed = isShort ? gameState.isBlackShortCastling() : gameState.isBlackLongCastling();
        }

        if (!castlingAllowed) {
            return false;
        }

        int kingRank = (color == 0) ? 0 : 7;
        if (fromRank != kingRank || toRank != kingRank) {
            return false;
        }

        if (isShort) {
            for (int f = 5; f <= 6; f++) {
                Desk.Square sq = desk.getSquare(new Position(f, kingRank));
                if (sq == null || sq.getFigure() != null) {
                    return false;
                }
            }
            Desk.Square rookSq = desk.getSquare(new Position(7, kingRank));
            if (rookSq == null || rookSq.getFigure() == null || rookSq.getFigure().getType() != FigureType.ROOK) {
                return false;
            }
        } else {
            for (int f = 1; f <= 3; f++) {
                Desk.Square sq = desk.getSquare(new Position(f, kingRank));
                if (sq == null || sq.getFigure() != null) {
                    return false;
                }
            }
            Desk.Square rookSq = desk.getSquare(new Position(0, kingRank));
            if (rookSq == null || rookSq.getFigure() == null || rookSq.getFigure().getType() != FigureType.ROOK) {
                return false;
            }
        }

        return true;
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
}