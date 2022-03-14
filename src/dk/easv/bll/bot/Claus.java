package dk.easv.bll.bot;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import dk.easv.bll.field.Field;
import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.List;

/**
 *
 * @author KristianUrup
 */
public class Claus implements IBot
{
    IField Ifield;
    private String player;
    private static final String BOTNAME = "Claus";
    Field field;

    // Moves {row, col} in order of preferences. {0, 0} at top-left corner


    @Override
    public IMove doMove(IGameState state) {
        if (checkMacroBoard(state) != null) {
            return checkMacroBoard(state);
        }
        int[][]preferredMoves = {
                {1,1}, //Center
                {0, 0}, {2, 2}, {0, 2}, {2, 0},  //Corners ordered across
                {0, 1}, {2, 1}, {1, 0}, {1, 2}, //Outer Middles ordered across
        };
        //Find macroboard to play in

        for (int[] move :preferredMoves)
        {

            if(state.getField().getMacroboard()[move[0]][move[1]].equals(IField.AVAILABLE_FIELD))
            {
                //find move to play
                for (int[] selectedMove : preferredMoves)
                {
                    int x = move[0]*3 + selectedMove[0];
                    int y = move[1]*3 + selectedMove[1];
                    if(state.getField().getBoard()[x][y].equals(IField.EMPTY_FIELD))
                    {
                        return new Move(x,y);
                    }
                }
            }
        }
        return state.getField().getAvailableMoves().get(0);
    }
    /**
     * Makes a turn. Edit this method to make your bot smarter.
     * A bot that uses a local prioritised list algorithm, in order to win any local board,
     * and if all boards are available for play, it'll run a on the macroboard,
     * to select which board to play in.
     *
     * @return The selected move we want to make.
     */

    public IMove checkMacroBoard(IGameState state)
    {
        for (int i = 0; i < state.getField().getMacroboard().length; i++) {
            for (int j = 0; j < state.getField().getMacroboard()[0].length; j++) {
                if (state.getField().getMacroboard()[i][j].equals(IField.AVAILABLE_FIELD)) {
                    int boardX = i+i*3;
                    int boardY = j+j*3;
                    if (state.getField().getBoard()[boardX][boardY].equals(IField.EMPTY_FIELD)) {
                        return new Move(boardX,boardY);
                    }
                }
            }
        }
        return null;
    }

    public IMove checkMicroBoard(IGameState state)
    {
        String [][] board = state.getField().getBoard();
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        //Find macroboard to play in
        for(IMove newMove: availableMoves)
        {
            int localX = newMove.getX() % 3;
            int localY = newMove.getY() % 3;
            int startX = newMove.getX() - (localX);
            int startY = newMove.getY() - (localY);

            //check col
            for (int i = startY; i < startY + 3; i++) {
                if (!board[newMove.getX()][i].equals(player))
                    break;
                if (i == startY + 3 - 1) return newMove;
            }

            //check row
            for (int i = startX; i < startX + 3; i++) {
                if (!board[i][newMove.getY()].equals(player))
                    break;
                if (i == startX + 3 - 1) return newMove;
            }

            //check diagonal
            if (localX == localY) {
                //we're on a diagonal
                int y = startY;
                for (int i = startX; i < startX + 3; i++) {
                    if (!board[i][y++].equals(player))
                        break;
                    if (i == startX + 3 - 1) return newMove;
                }
            }

            //check anti diagonal
            if (localX + localY == 3 - 1) {
                int less = 0;
                for (int i = startX; i < startX + 3; i++) {
                    if (!board[i][(startY + 2)-less++].equals(player))
                        break;
                    if (i == startX + 3 - 1) return newMove;
                }
            }
        }
        return null;
    }



    @Override
    public String getBotName()
    {
        return BOTNAME;
    }

}