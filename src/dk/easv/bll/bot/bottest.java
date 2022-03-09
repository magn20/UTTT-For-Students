package dk.easv.bll.bot;

import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

public class bottest implements  IBot{
    @Override
    public IMove doMove(IGameState state) {



        System.out.println("this is the availablemoves" + state.getField().getAvailableMoves() );
        System.out.println("this is the board: " + state.getField().getBoard());
        System.out.println("this is the macroboard: " + state.getField().getMacroboard());


        return null;
    }

    @Override
    public String getBotName() {
        return null;
    }
}
