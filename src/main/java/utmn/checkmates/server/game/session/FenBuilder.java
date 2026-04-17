package utmn.checkmates.server.game.session;

public class FenBuilder {
    private StringBuilder result;

    public FenBuilder(GameState state){
        addDesk(state.getDesk());
        addNextSide((state.getCurrentSide() == 1) ? 0 : (state.getCurrentSide() == 0) ? 1 : -1);
        addCastlings(state.isWhiteLongCastling(), state.isWhiteShortCastling(),
                state.isBlackLongCastling(), state.isBlackShortCastling());
        addAisle(state.getAislePos());
        addSubStep(state.getSubStep());
        addStep(state.getStep());
    }

    private void addDesk(Desk desk){
        StringBuilder deskStrBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            StringBuilder row = new StringBuilder();
            int currentEmptyLine = 0;
            for (int j = 0; j < 8; j++) {
                Desk.Square square = desk.getSquare(new Position(i, j));
                Figure figure = square.getFigure();
                if(figure == null) currentEmptyLine++;
                else{
                    if(currentEmptyLine > 0){
                        row.append(currentEmptyLine);
                        currentEmptyLine = 0;
                    }
                    row.append(figure.isWhite() ? figure.getType().getFenWhite() : figure.getType().getFenBlack());
                }
            }
            if(currentEmptyLine > 0) row.append(currentEmptyLine);
            deskStrBuilder.append(row).append('/');
        }

        String deskStr = deskStrBuilder.toString();
        if (deskStr.charAt(deskStr.length()-1)=='/'){
            deskStr = deskStr.replace(deskStr.substring(deskStr.length()-1), "");
        }
        result.append(deskStr).append(" ");
    }

    private void addNextSide(int side){
        result.append(side == 0 ? 'w' : side == 1 ? 'b' : '?').append(' ');
    }

    private void addCastlings(
            boolean whiteLongCastling,
            boolean whiteShortCastling,
            boolean blackLongCastling,
            boolean blackShortCastling
    ){
        StringBuilder castlingBuilder = new StringBuilder();
        castlingBuilder.append(whiteLongCastling ? 'Q' : '-');
        castlingBuilder.append(blackLongCastling ? 'q' : '-');
        castlingBuilder.append(whiteShortCastling ? 'K' : '-');
        castlingBuilder.append(blackShortCastling ? 'k' : '-');

        result.append(castlingBuilder).append(" ");
    }

    private void addAisle(Position aisle){
        result.append(Position.getNotationByPosition(aisle)).append(" ");
    }

    private void addSubStep(int subStep){
        result.append(subStep).append(" ");
    }

    private void addStep(int step){
        result.append(step);
    }

    public String toFen(){
        return result.toString();
    }

}
