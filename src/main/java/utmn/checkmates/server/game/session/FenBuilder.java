package utmn.checkmates.server.game.session;

import utmn.checkmates.server.utility.logger.Logger;

public class FenBuilder {
    private StringBuilder result = new StringBuilder();

    public FenBuilder(GameState state){
        addDesk(state.getDesk());
        addNextSide((state.getLastSide() == 1) ? 0 : (state.getLastSide() == 0) ? 1 : -1);
        addCastlings(state.isWhiteLongCastling(), state.isWhiteShortCastling(),
                state.isBlackLongCastling(), state.isBlackShortCastling());
        addAisle(state.getAislePos());
        addSubStep(state.getSubStep());
        addStep(state.getStep());
    }

    private void addDesk(Desk desk){
        //
        Logger.log("FenBuilder", "addDesk",
                "Добавляется...");
        //
        StringBuilder deskStrBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            StringBuilder column = new StringBuilder();
            int currentEmptyLine = 0;
            for (int j = 0; j < 8; j++) {
                Desk.Square square = desk.getSquare(new Position(i, j));
                Figure figure = square.getFigure();
                if(figure == null) currentEmptyLine++;
                else{
                    if(currentEmptyLine > 0){
                        column.append(currentEmptyLine);
                        currentEmptyLine = 0;
                    }
                    column.append(figure.isWhite() ? figure.getType().getFenWhite() : figure.getType().getFenBlack());
                }
            }
            if(currentEmptyLine > 0) column.append(currentEmptyLine);
            deskStrBuilder.append(column).append('/');
        }

        String deskStr = deskStrBuilder.toString();
        if (deskStr.endsWith("/")) {
            deskStr = deskStr.substring(0, deskStr.length() - 1);
        }
        result.append(deskStr).append(" ");
        //
        Logger.log("FenBuilder", "addStep",
                "Добавлена.");
        //
    }

    private void addNextSide(int side){
        //
        Logger.log("FenBuilder", "addNextSide",
                "Добавляется...");
        //
        result.append(side == 0 ? 'w' : side == 1 ? 'b' : '?').append(' ');
        //
        Logger.log("FenBuilder", "addNextSide",
                "Добавлена.");
        //
    }

    private void addCastlings(
            boolean whiteLongCastling,
            boolean whiteShortCastling,
            boolean blackLongCastling,
            boolean blackShortCastling
    ){
        //
        Logger.log("FenBuilder", "addCastlings",
                "Добавляется...");
        //
        StringBuilder castlingBuilder = new StringBuilder();

        if (whiteLongCastling) castlingBuilder.append('Q');
        if (whiteShortCastling) castlingBuilder.append('K');
        if (blackLongCastling) castlingBuilder.append('q');
        if (blackShortCastling) castlingBuilder.append('k');

        String castlingStr = castlingBuilder.length() > 0 ? castlingBuilder.toString() : "-";
        result.append(castlingStr).append(" ");
        //
        Logger.log("FenBuilder", "addCastlings",
                "Добавлена.");
        //
    }

    private void addAisle(Position aisle){
        //
        Logger.log("FenBuilder", "addAisle",
                "Добавляется...");
        //
        if(aisle != null) result.append(Position.getNotationByPosition(aisle)).append(" ");
        //
        Logger.log("FenBuilder", "addAisle",
                "Добавлена.");
        //
    }

    private void addSubStep(int subStep){
        //
        Logger.log("FenBuilder", "addSubStep",
                "Добавляется...");
        //
        result.append(subStep).append(" ");
        //
        Logger.log("FenBuilder", "addSubStep",
                "Добавлена.");
        //
    }

    private void addStep(int step){
        //
        Logger.log("FenBuilder", "addStep",
                "Добавляется...");
        //
        result.append(step);
        //
        Logger.log("FenBuilder", "addStep",
                "Добавлена.");
        //
    }

    public String toFen(){
        return result.toString();
    }

}
