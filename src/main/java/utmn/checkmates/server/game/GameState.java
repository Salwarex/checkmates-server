package utmn.checkmates.server.game;

import utmn.checkmates.server.game.desk.*;
import utmn.checkmates.server.game.desk.figure.Figure;
import utmn.checkmates.server.game.desk.figure.FigureType;
import utmn.checkmates.server.game.desk.fen.FenBuilder;
import utmn.checkmates.server.game.desk.fen.FenReader;
import utmn.checkmates.server.game.exception.GameRuleException;
import utmn.checkmates.server.game.exception.ServerSideException;
import utmn.checkmates.server.game.process.GameEnd;
import utmn.checkmates.server.game.process.GameEndType;
import utmn.checkmates.server.game.session.Session;
import utmn.checkmates.server.network.tcp.SessionConnection;
import utmn.checkmates.server.utility.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private Session session;
    private FenReader reader; //обработчик Fen-записи
    private Desk desk; //доска

    private boolean whiteLongCastling; //возможность длинных белых рокировок
    private boolean whiteShortCastling; //возможность коротких белых рокировок
    private boolean blackLongCastling; //возможность длинных чёрных рокировок
    private boolean blackShortCastling; //возможность коротких чёрных рокировок

    private int lastSide; //крайняя ходившая сторона
    private int subStep; //номер полухода
    private int step; //номер хода
    private Position aislePos; //пропущенная клетка при взятии на проходе
    private Desk.Square aisleOriginFigure; // клетка, которая будет очищена при взятии на проходе

    private boolean check = false; //шах
    private List<SquareSnapshot> checkSquaresSnaps = new ArrayList<>();

    public GameState(Session session){
        this.session = session;
        updateFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public GameState(Session session, String fen){
        this.session = session;
        updateFen(fen);
    }

    public void updateFen(String fen){
        Logger.log("GameState", "updateFen", "Произведено обновление игрового состояния. FEN: %s"
                .formatted(fen));

        reader = new FenReader(fen);
        this.desk = new Desk(reader.getColumns());
        this.whiteLongCastling = reader.castlingAvailable(false, false);
        this.whiteShortCastling = reader.castlingAvailable(true, false);
        this.blackLongCastling = reader.castlingAvailable(false, true);
        this.blackShortCastling = reader.castlingAvailable(true, true);
        this.lastSide = reader.currentSide();
        this.aislePos = reader.getPositionBetweenTwoSquaresPawnStep();
        this.subStep = reader.getSubStep();
        this.step = reader.getStep();
    }

    public Desk getDesk() {
        return desk;
    }

    public boolean isWhiteLongCastling() {
        return whiteLongCastling;
    }

    public boolean isWhiteShortCastling() {
        return whiteShortCastling;
    }

    public boolean isBlackLongCastling() {
        return blackLongCastling;
    }

    public boolean isBlackShortCastling() {
        return blackShortCastling;
    }

    public int getLastSide() {
        return lastSide;
    }

    public int getSubStep() {
        return subStep;
    }

    public int getStep() {
        return step;
    }

    public Position getAislePos() {
        return aislePos;
    }

    public void move(SessionConnection connection, Position from, Position to) throws GameRuleException, ServerSideException {
        Logger.log("GameState", "move", "Инициировано движение фигуры. ");
        if(from == null || to == null)
        {
            Logger.log("GameState", "move", "Движение прервано: один из входных параметров равен null");
            throw new ServerSideException("Передано неверное значение аргументов позиций!");
        }
        if(from.equals(to)) {
            Logger.log("GameState", "move", "Движение прервано: Игрок попытался сходить в ту же клетку");
            throw new GameRuleException("Ход в данную клетку невозможен: Вы не можете сходить на место, на котором уже находитесь!");
        }

        if(from.getColumn() > 7 || from.getRow() > 7
                || to.getColumn() > 7 || to.getRow() > 7
                || from.getColumn() < 0 || from.getRow() < 0
                || to.getColumn() < 0 || to.getRow() < 0)
        {
            Logger.log("GameState", "move", "Движение прервано: Ход указан на клетку, выходящую за пределы доски");
            throw new GameRuleException("Ход из данной клетки или в данную клетку невозможен: На вход передано значение, выходящее за пределы игровой доски (8x8)!");
        }

        Desk.Square squareFrom = desk.getSquare(from);
        if(squareFrom == null)
        {
            Logger.log("GameState", "move", "Движение прервано: Клетка отсутствует на сервере!");
            throw new ServerSideException("Данная клетка не была прогружена на сервере!");
        }

        Figure figure = squareFrom.getFigure();
        if(figure == null)
        {
            Logger.log("GameState", "move", "Движение прервано: В изначальной клетке отсутствует фигура!");
            throw new GameRuleException("Ход из данной клетки невозможен: В настоящий момент клетка пуста!");
        }

        if((figure.isWhite() && connection.getPlayer().getColor() == 1)
                || (!figure.isWhite() && connection.getPlayer().getColor() == 0)){
            Logger.log("GameState", "move", "Движение прервано: Игрок попытался сходить не своей фигурой. Цвет фигуры, которой походили белый? %b; Сторона игрока: %d"
                    .formatted(figure.isWhite(), connection.getPlayer().getColor()));
            throw new GameRuleException("Ход невозможен: Сейчас не ваш ход!");
        }

        if((figure.isWhite() && lastSide == 1) || (!figure.isWhite() && lastSide == 0)) {
            Logger.log("GameState", "move", "Движение прервано: Игрок попытался сходить не в свой ход. Цвет фигуры, которой походили белый? %b; Последняя сторона, которой походили: %d"
                    .formatted(figure.isWhite(), lastSide));
            throw new GameRuleException("Ход невозможен: Сейчас не ваш ход!");
        }

        Desk.Square squareTo = desk.getSquare(to);
        boolean notDefaultMove = false;

        //рокировка
        Figure figureFrom = squareFrom.getFigure();
        Figure figureTo = squareTo.getFigure();
        Position posFrom = squareFrom.getPos();
        Position posTo = squareTo.getPos();

        if(figureFrom != null && figureTo != null && figureFrom.isWhite() == figureTo.isWhite()
                && figureFrom.getType() == FigureType.KING && figureTo.getType() == FigureType.ROOK
        ){
            CastlingResult check = StepModelManager.isCastlingValid(figure.isWhite() ? 0 : 1,
                    posFrom.getRow(),
                    posFrom.getColumn(),
                    posTo.getRow(),
                    posTo.getColumn(),
                    blackLongCastling,
                    blackShortCastling,
                    whiteLongCastling,
                    whiteShortCastling
            );
            if(check.isAvailable()){
                notDefaultMove = true;

                //перемещаем ладью
                desk.getSquare(check.getNewRookPos()).setFigure(squareTo.getFigure());
                squareTo.setFigure(null);

                //устанавливаем новую позицию для ходившего короля - дальше она применится
                squareTo = desk.getSquare(check.getNewKingPos());

                Logger.log("GameState", "move", "Произведена рокировка!");

                boolean white = figure.isWhite();
                boolean _long = check.isLong();

                //убираем флаги доступности рокировок
                if(white && _long) whiteLongCastling = false;
                else if(!white && _long) blackLongCastling = false;
                else if(white) whiteShortCastling = false;
                else blackShortCastling = false;
            }
        }

        if(!notDefaultMove && !StepModelManager.isAvailable(figure.getType(), figure.isWhite() ? 0 : 1, from, to, this)){
            Logger.log("GameState", "move", "Движение прервано: Был произведен ход в клетку, которая не соответствует модели хождения данного типа фигуры");
            throw new GameRuleException("Ход в данную клетку невозможен! Данная фигура имеет другую модель хождения.");
        }

        //не помню че это такое, мб удалить можно
//        if (figure.getType() == FigureType.PAWN && squareTo.getFigure() == null) {
//            Position ep = this.aislePos;
//            if (ep != null && ep.equals(to)) {
//                Position capturedPos = new Position(from.getRow(), to.getColumn());
//                Desk.Square capturedSquare = desk.getSquare(capturedPos);
//                if (capturedSquare != null && capturedSquare.getFigure() != null) {
//                    capturedSquare.setFigure(null);
//                }
//            }
//        }

        //проверка на мат
        if(squareTo.getFigure() != null && squareTo.getFigure().getType() == FigureType.KING){
            Logger.log("GameState", "move", "ПРОИЗВЕДЕН МАТ");
            session.end(new GameEnd(GameEndType.MATE, figure.isWhite() ? 0 : 1));
        }

        squareFrom.setFigure(null);
        squareTo.setFigure(figure);

        //взятие на проходе
        if (figure.getType() == FigureType.PAWN && Math.abs(to.getRow() - from.getRow()) == 2) {
            this.aislePos = new Position((from.getRow() + to.getRow()) / 2, from.getColumn());
            this.aisleOriginFigure = squareTo;

            Logger.log("GameState", "move", "Установлена следующая позиция в качестве aislePos: %s"
                    .formatted(aislePos.toString()));
        } else {
            if(squareTo.getPos().equals(aislePos)){
                aisleOriginFigure.setFigure(null);
                Logger.log("GameState", "move", "ПРОВЕДЕНО ВЗЯТИЕ НА ПРОХОДЕ!");
            }

            this.aislePos = null;
            this.aisleOriginFigure = null;

            Logger.log("GameState", "move", "aisle-позиция обнулена!");
        }

        //пешки -> ферзи
        if(figure.getType() == FigureType.PAWN && //фигура - пешка
                ((figure.isWhite() && (squareTo.getPos().getRow() == 0)) //белая - на первой строке черных
                                || (!figure.isWhite() && (squareTo.getPos().getRow() == 7))) //чёрная - на первой строке белых
        ){
            squareTo.setFigure(new Figure(FigureType.QUEEN, figure.isWhite()));
            Logger.log("GameState", "move", "ПЕШКА повышена до ФЕРЗЯ!");
        }

        //проверка шаха
        check = StepModelManager.isAvailable(figure.getType(), figure.isWhite() ? 0 : 1, to,
                figure.isWhite() ? Desk.PositionMatcher.getBlackKingPos() : Desk.PositionMatcher.getWhiteKingPos(), this);
        checkSquaresSnaps.add(squareTo.snapshot());
        if(!check){ //сохранение шаха при следующих ходах
            List<SquareSnapshot> updatedList = new ArrayList<>();
            for(SquareSnapshot squareSnapshot : checkSquaresSnaps){
                Desk.Square square = Desk.PositionMatcher.get(squareSnapshot.getPosition());
                boolean hasCheck = false;

                //шаховая клетка не изменилась - проверяем, существует ли шах
                if(square != null && square.snapshot().equals(squareSnapshot)){
                    Figure snapFigure = squareSnapshot.getFigure();
                    hasCheck = StepModelManager.isAvailable(snapFigure.getType(), snapFigure.isWhite() ? 0 : 1, squareSnapshot.getPosition(),
                            snapFigure.isWhite() ? Desk.PositionMatcher.getBlackKingPos() : Desk.PositionMatcher.getWhiteKingPos(), this);
                }

                if(hasCheck) updatedList.add(square.snapshot());

                check = check || hasCheck;
            }
            checkSquaresSnaps = updatedList;
        }

        Logger.log("GameState", "move", "Шах: %b".formatted(check));

        this.lastSide = lastSide == 0 ? 1 : 0;

        Logger.log("GameState", "move", "Был совершен ход из %s в %s".formatted(from, to));
    }

    public boolean isCheck() {
        return check;
    }

    public String getFen(){
        return new FenBuilder(this).toFen();
    }

}