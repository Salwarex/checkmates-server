package utmn.checkmates.server.game.session;

import utmn.checkmates.server.utility.logger.Logger;

public class StepModelManager {

    /**
     * Проверяет, доступен ли ход для фигуры с учетом правил шахмат и текущего состояния игры.
     * @param type тип фигуры
     * @param color цвет фигуры (0 - белые, 1 - черные)
     * @param from начальная позиция
     * @param to целевая позиция
     * @param gameState текущее состояние игры для проверки рокировок, взятий на проходе и т.д.
     * @return true если ход допустим
     */
    public static boolean isAvailable(FigureType type, int color, Position from, Position to, GameState gameState){
        if (from == null || to == null || from.equals(to)) {
            return false;
        }

        // ВНИМАНИЕ: в этой системе координат:
        // getRow() = file (вертикаль: a-h, 0-7)
        // getColumn() = rank (горизонталь: 1-8, 0-7)
        int fromFile = from.getRow();
        int fromRank = from.getColumn();
        int toFile = to.getRow();
        int toRank = to.getColumn();

        if (!isOnBoard(fromFile, fromRank) || !isOnBoard(toFile, toRank)) {
            return false;
        }

        Desk desk = gameState.getDesk();

        // Проверка: нельзя ходить на клетку со своей фигурой
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

    /**
     * Проверка хода для скользящих фигур (ладья, слон, ферзь)
     * @param fromFile вертикаль начальной позиции
     * @param fromRank горизонталь начальной позиции
     * @param toFile вертикаль целевой позиции
     * @param toRank горизонталь целевой позиции
     * @param desk игровое поле
     * @param checkOrthogonal проверять ортогональные направления
     * @param checkDiagonal проверять диагональные направления
     */
    private static boolean isSlidingPieceValid(int fromFile, int fromRank, int toFile, int toRank, Desk desk, boolean checkOrthogonal, boolean checkDiagonal) {
        int dFile = toFile - fromFile;
        int dRank = toRank - fromRank;

        // Валидация направления движения
        if (checkOrthogonal && checkDiagonal) {
            // Ферзь: ортогональ или диагональ
            if (fromFile != toFile && fromRank != toRank && Math.abs(dFile) != Math.abs(dRank)) {
                return false;
            }
        } else if (checkOrthogonal && !checkDiagonal) {
            // Ладья: только ортогональ (одна вертикаль ИЛИ одна горизонталь)
            if (fromFile != toFile && fromRank != toRank) {
                return false;
            }
        } else if (!checkOrthogonal && checkDiagonal) {
            // Слон: только диагональ
            if (Math.abs(dFile) != Math.abs(dRank) || dFile == 0) {
                return false;
            }
        }

        // Проверка, что путь свободен (исключая начальную и конечную клетки)
        int stepFile = Integer.signum(dFile);
        int stepRank = Integer.signum(dRank);

        int curFile = fromFile + stepFile;
        int curRank = fromRank + stepRank;

        while (curFile != toFile || curRank != toRank) {
            Desk.Square square = desk.getSquare(new Position(curFile, curRank));
            if (square == null || square.getFigure() != null) {
                return false; // Путь заблокирован
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

        // Обычный ход короля: одна клетка в любом направлении
        if (dFile <= 1 && dRank <= 1 && (dFile > 0 || dRank > 0)) {
            return true;
        }

        // Проверка рокировки
        return isCastlingValid(color, fromFile, fromRank, toFile, toRank, gameState);
    }

    private static boolean isCastlingValid(int color, int fromFile, int fromRank, int toFile, int toRank, GameState gameState) {
        Desk desk = gameState.getDesk();

        // Король должен находиться на e-вертикали (file = 4)
        if (fromFile != 4) {
            return false;
        }

        // Определение типа рокировки по целевой вертикали
        boolean isShort = (toFile == 6); // короткая: король на g-вертикали (file 6)
        boolean isLong = (toFile == 2);  // длинная: король на c-вертикали (file 2)

        if (!isShort && !isLong) {
            return false;
        }

        // Проверка прав на рокировку из GameState
        boolean castlingAllowed;
        if (color == 0) { // Белые
            castlingAllowed = isShort ? gameState.isWhiteShortCastling() : gameState.isWhiteLongCastling();
        } else { // Черные
            castlingAllowed = isShort ? gameState.isBlackShortCastling() : gameState.isBlackLongCastling();
        }

        if (!castlingAllowed) {
            return false;
        }

        // Король должен быть на начальной горизонтали
        int kingRank = (color == 0) ? 0 : 7; // белые: rank 0 (1-я горизонталь), черные: rank 7 (8-я)
        if (fromRank != kingRank || toRank != kingRank) {
            return false;
        }

        if (isShort) {
            // Короткая рокировка: проверка клеток на вертикалях f и g (files 5, 6)
            for (int f = 5; f <= 6; f++) {
                Desk.Square sq = desk.getSquare(new Position(f, kingRank));
                if (sq == null || sq.getFigure() != null) {
                    return false;
                }
            }
            // Проверка наличия ладьи на h-вертикали (file 7)
            Desk.Square rookSq = desk.getSquare(new Position(7, kingRank));
            if (rookSq == null || rookSq.getFigure() == null || rookSq.getFigure().getType() != FigureType.ROOK) {
                return false;
            }
        } else {
            // Длинная рокировка: проверка клеток на вертикалях b, c, d (files 1, 2, 3)
            for (int f = 1; f <= 3; f++) {
                Desk.Square sq = desk.getSquare(new Position(f, kingRank));
                if (sq == null || sq.getFigure() != null) {
                    return false;
                }
            }
            // Проверка наличия ладьи на a-вертикали (file 0)
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


        // Прямой ход по вертикали (файл не меняется)
        if (fromCol == toCol) {
            // Шаг на 1 клетку вперёд
            if (toRow == fromRow + direction) {
                Desk.Square sq = desk.getSquare(new Position(toRow, toCol));
                return sq != null && sq.getFigure() == null;
            }
            // Двойной шаг со стартовой позиции
            if (fromRow == startRow && toRow == fromRow + 2 * direction) {
                Position mid = new Position(fromRow + direction, fromCol);
                Desk.Square midSq = desk.getSquare(mid);
                Desk.Square toSq = desk.getSquare(new Position(toRow, toCol));
                return midSq != null && midSq.getFigure() == null &&
                        toSq != null && toSq.getFigure() == null;
            }
            return false;
        }

        // Взятие по диагонали
        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
            Desk.Square toSq = desk.getSquare(new Position(toRow, toCol));
            // Обычное взятие
            if (toSq != null && toSq.getFigure() != null) {
                Figure fig = toSq.getFigure();
                boolean isEnemy = (fig.isWhite() && color == 1) || (!fig.isWhite() && color == 0);
                if (isEnemy) return true;
            }
            // Взятие на проходе (en passant)
            Position ep = gameState.getAislePos();
            if (ep != null && ep.equals(new Position(toRow, toCol))) {
                // Взятая пешка: та же горизонталь (fromRow), но целевой файл (toCol)
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