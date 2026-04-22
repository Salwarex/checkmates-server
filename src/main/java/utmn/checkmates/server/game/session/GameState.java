package utmn.checkmates.server.game.session;

import utmn.checkmates.server.utility.logger.Logger;

public class GameState {
    private FenReader reader;
    private Desk desk;

    //Рокировки
    private boolean whiteLongCastling;
    private boolean whiteShortCastling;
    private boolean blackLongCastling;
    private boolean blackShortCastling;

    private int lastSide;
    private int subStep;
    private int step;
    private Position aislePos;

    public GameState(){
        updateFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public GameState(String fen){
        updateFen(fen);
    }

    public void updateFen(String fen){
        Logger.log("GameState", "updateFen", "Произведено обновление игрового состояния. FEN: %s"
                .formatted(fen));

        reader = new FenReader(fen);
        this.desk = new Desk(reader.getRows());
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

    public void move(Position from, Position to) throws GameRuleException, ServerSideException {
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

        if(from.getRow() > 7 || from.getColumn() > 7
                || to.getRow() > 7 || to.getColumn() > 7
                || from.getRow() < 0 || from.getColumn() < 0
                || to.getRow() < 0 || to.getColumn() < 0)
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

//        if((figure.isWhite() && lastSide == 0) ||
//                (!figure.isWhite() && lastSide == 1))
//        {
//            Logger.log("GameState", "move", "Движение прервано: Игрок попытался сходить не в свой ход. currentSide : %d, playerWhite : %b".formatted(lastSide, figure.isWhite()));
//            throw new GameRuleException("Ход невозможен: Сейчас не ваш ход!");
//        }

        Desk.Square squareTo = desk.getSquare(to);
        if(squareTo.getFigure() != null
                && ((squareTo.getFigure().isWhite() && squareFrom.getFigure().isWhite())
                || ((!squareTo.getFigure().isWhite() && !squareFrom.getFigure().isWhite()))))
        {
            Logger.log("GameState", "move", "Движение прервано: игрок попытался сходить в клетку, где стоит другая его фигура.");
            throw new GameRuleException("Ход в данную клетку невозможен! В данной клетке уже стоит другая фигура вашего цвета!");
        }

        if(!StepModelManager.isAvailable(figure.getType(), figure.isWhite() ? 0 : 1, from, to)){
            Logger.log("GameState", "move", "Движение прервано: Был произведен ход в клетку, которая не соответствует модели хождения данного типа фигуры");
            throw new GameRuleException("Ход в данную клетку невозможен! Данная фигура имеет другую модель хождения.");
        }

        squareFrom.setFigure(null);
        squareTo.setFigure(figure);
        Logger.log("GameState", "move", "Был совершен ход из %s в %s".formatted(from, to));
    }

    public String getFen(){
        return new FenBuilder(this).toFen();
    }
}
