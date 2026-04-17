package utmn.checkmates.server.game.session;

public class GameState {
    private FenReader reader;
    private Desk desk;

    //Рокировки
    private boolean whiteLongCastling;
    private boolean whiteShortCastling;
    private boolean blackLongCastling;
    private boolean blackShortCastling;

    private int currentSide;
    private int subStep;
    private int step;
    private Position aislePos;

    public GameState(){
        this.reader = new FenReader("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public GameState(String fen){
        this.reader = new FenReader(fen);
    }

    public void updateFen(String fen){
        reader = new FenReader(fen);
        this.desk = new Desk(reader.getRows());
        this.whiteLongCastling = reader.castlingAvailable(false, false);
        this.whiteShortCastling = reader.castlingAvailable(true, false);
        this.blackLongCastling = reader.castlingAvailable(false, true);
        this.blackShortCastling = reader.castlingAvailable(true, true);
        this.currentSide = reader.currentSide();
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

    public int getCurrentSide() {
        return currentSide;
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
        if(from == null || to == null)
            throw new ServerSideException("Передано неверное значение аргументов позиций!");
        if(from.equals(to)) throw new GameRuleException("Ход в данную клетку невозможен: Вы не можете сходить на место, на котором уже находитесь!");

        if(from.getRow() > 7 || from.getColumn() > 7
                || to.getRow() > 7 || to.getColumn() > 7
                || from.getRow() < 0 || from.getColumn() < 0
                || to.getRow() < 0 || to.getColumn() < 0)
            throw new GameRuleException("Ход из данной клетки или в данную клетку невозможен: На вход передано значение, выходящее за пределы игровой доски (8x8)!");

        Desk.Square squareFrom = desk.getSquare(from);
        if(squareFrom == null)
            throw new ServerSideException("Данная клетка не была прогруженна на сервере!");

        Figure figure = squareFrom.getFigure();
        if(figure == null)
            throw new GameRuleException("Ход из данной клетки невозможен: В настоящий момент клетка пуста!");

        if((figure.isWhite() && currentSide == 1) ||
                (!figure.isWhite() && currentSide == 0))
            throw new GameRuleException("Ход невозможен: Сейчас не ваш ход!");

        Desk.Square squareTo = desk.getSquare(to);
        if(squareTo.getFigure() != null
                && ((squareTo.getFigure().isWhite() && squareFrom.getFigure().isWhite())
                || ((!squareTo.getFigure().isWhite() && !squareFrom.getFigure().isWhite()))))
            throw new GameRuleException("Ход в данную клетку невозможен! В данной клетке уже стоит другая фигура вашего цвета!");

        if(!StepModelManager.isAvailable(figure.getType(), figure.isWhite() ? 0 : 1, from, to)){
            throw new GameRuleException("Ход в данную клетку невозможен! Данная фигура имеет другую модель хождения.");
        }
    }

    public String getFen(){
        return new FenBuilder(this).toFen();
    }
}
