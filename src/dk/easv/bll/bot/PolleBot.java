
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.*;

public class PolleBot implements IBot{

    private Random rand;
    private int opponent;
    private UCT uct;

    public PolleBot()
    {
        rand = new Random();
        uct = new UCT();
    }

    @Override
    public IMove doMove(IGameState state) {
        
        
        long endTime = System.currentTimeMillis() + 1000;
        
        
        Node rootNode = new Node(state);

        opponent = (state.getMoveNumber()+1)%2;
        //Spiller så længe den stadig har tid
        while(System.currentTimeMillis() < endTime)
        {

            //Finder god node
            Node promisingNode = selectPromisingNode(rootNode);

            if(!isGameOver(promisingNode.getState()))
            {
                expandNode(promisingNode);
            }

            Node nodeToExplore = promisingNode;
            if(!nodeToExplore.getChildren().isEmpty())
            {
                nodeToExplore = promisingNode.getRandomChild();
            }

            int rolloutResult = performRollout(nodeToExplore);
            backPropagation(nodeToExplore, rolloutResult);
        }
        Node winnerNode = rootNode.getChildWithMaxScore();
        return getMove(rootNode, winnerNode);
    }
    // finder en løsning til at finde en passende node
    private Node selectPromisingNode(Node rootNode)
    {
        Node promisingNode = rootNode;
        while(!promisingNode.getChildren().isEmpty())
        {
            promisingNode = uct.findBestNodeWithUCT(promisingNode);
        }
        return promisingNode;
    }

    private void expandNode(Node promisingNode)
    {
        List<IMove> availableMoves = promisingNode.getState().getField().getAvailableMoves();
        for(IMove move : availableMoves)
        {

            Node childNode = new Node(promisingNode.getState());
            childNode.setParent(promisingNode);
            promisingNode.getChildren().add(childNode);

            performMove(childNode.getState(), move.getX(), move.getY());
        }
    }
    
    private int performRollout(Node nodeToExplore)
    {
        Node tempNode = new Node(nodeToExplore);
        IGameState tempState = tempNode.getState();
        if(isGameOver(tempState) && (tempState.getMoveNumber()+1)%2 == opponent)
        {
            tempNode.getParent().setScore(Integer.MIN_VALUE);
            return opponent;
        }
        while(!isGameOver(tempState))
        {
            randomPlay(tempState);
        }
        return (tempState.getMoveNumber()+1)%2;

    }
    
    private void backPropagation(Node node, int value)
    {
        Node tempNode = node;
        while(tempNode != null)
        {
            tempNode.incrementVisit();
            if((tempNode.getState().getMoveNumber()+1)%2 == value)
            {
                tempNode.addScore(10);
            }
            tempNode = tempNode.getParent();
        }

    }
    
    private void randomPlay(IGameState state)
    {
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        IMove randomMove = availableMoves.get(rand.nextInt(availableMoves.size()));
        performMove(state, randomMove.getX(), randomMove.getY());
    }

    private class UCT {

        public double uctValue(int totalVisit, double nodeWinScore, int nodeVisit)
        {
            if (nodeVisit == 0)
            {
                return Integer.MAX_VALUE;
            }
            return (nodeWinScore / (double) nodeVisit) + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
        }
        
        public Node findBestNodeWithUCT(Node node)
        {
            int parentVisit = node.getNumberOfVisits();
            return Collections.max(
                node.getChildren(),
                Comparator.comparing(child -> uctValue(parentVisit,
                    child.getScore(), child.getNumberOfVisits())));
        }
        
    }
    
    private class Node {
        
        private Node parent;
        
        private IGameState state;
        private int score;
        private int numberOfVisits;
        
        private List<Node> children;
        
        public Node(Node node)
        {
            this(node.getState());
            
            this.children = new ArrayList();
            if(node.getParent() != null)
            {
                this.parent = node.getParent();
            }

            List<Node> childArray = node.getChildren();
            for(Node child : childArray)
            {
                this.children.add(new Node(child));
            }
            
            score = node.getScore();
            numberOfVisits = node.getNumberOfVisits();
        }
        
        public Node(IGameState state)
        {
            this.state = new GameState();
            String[][] board = new String[9][9];
            String[][] macroboard = new String[3][3];
            for(int i = 0; i < board.length; i++)
            {
                for(int j = 0; j < board[i].length; j++)
                {
                    board[i][j] = state.getField().getBoard()[i][j];
                }
            }
            for(int i = 0; i < macroboard.length; i++)
            {
                for(int j = 0; j < macroboard[i].length; j++)
                {
                    macroboard[i][j] = state.getField().getMacroboard()[i][j];
                }
            }
            
            this.state.getField().setBoard(board);
            this.state.getField().setMacroboard(macroboard);
            
            this.state.setMoveNumber(state.getMoveNumber());
            this.state.setRoundNumber(state.getRoundNumber());
           
            this.children = new ArrayList();
            
            this.score = 0;
            this.numberOfVisits = 0;
        }
        
        public List<Node> getChildren()
        {
            return children;
        }
        
        public Node getChildWithMaxScore()
        {
            return Collections.max(this.children, Comparator.comparing(c -> {
                return c.getNumberOfVisits();
            }));
        }
        
        public Node getRandomChild()
        {
            return children.get(rand.nextInt(children.size()));
        }
        
        public Node getParent()
        {
            return parent;
        }
        
        public void setParent(Node parent)
        {
            this.parent = parent;
        }
        
        public IGameState getState()
        {
            return state;
        }
        
        public void setScore(int score)
        {
            this.score = score;
        }
        
        public void addScore(int score)
        {
            this.score += score;
        }
        
        public int getScore()
        {
            return score;
        }
        
        public void incrementVisit()
        {
            numberOfVisits++;
        }
        
        public int getNumberOfVisits()
        {
            return numberOfVisits;
        }
        
    }
    
    private IMove getMove(Node parentNode, Node childNode)
    {
        String[][] parentBoard = parentNode.getState().getField().getBoard();
        String[][] childBoard = childNode.getState().getField().getBoard();
        for(int i = 0; i < parentBoard.length; i++)
        {
            for(int j = 0; j < parentBoard[i].length; j++)
            {
                if(!parentBoard[i][j].equals(childBoard[i][j]))
                {
                    return new Move(i,j);
                }
            }
        }
        return null;
    }
    
    private void performMove(IGameState state, int moveX, int moveY)
    {
        String[][] board = state.getField().getBoard();
        board[moveX][moveY] = state.getMoveNumber()%2 + "";
        state.getField().setBoard(board);
        updateMacroboard(state, moveX, moveY);
        state.setMoveNumber(state.getMoveNumber()+1);
    }
    
    private void updateMacroboard(IGameState state, int moveX, int moveY)
    {
       updateMicroboardState(state, moveX, moveY);
       updateMicroboardsAvailability(state, moveX, moveY);
    }
    
    private void updateMicroboardState(IGameState state, int moveX, int moveY)
    {
        String[][] macroboard = state.getField().getMacroboard();
        int startingXPosition = (moveX/3)*3;
        int startingYPosition = (moveY/3)*3;
        if(isWinOnMicroboard(state, startingXPosition, startingYPosition))
        {
            macroboard[moveX/3][moveY/3] = state.getMoveNumber()%2+"";
        }
        else if(isDrawOnMicroboard(state, startingXPosition, startingYPosition))
        {
            macroboard[moveX/3][moveY/3] = "-";
        }
        state.getField().setMacroboard(macroboard);
    }
    
    private void updateMicroboardsAvailability(IGameState state, int moveX, int moveY)
    {
       int aktivMicroboardX = moveX%3;
       int aktivMicroboardY = moveY%3;
       String[][] macroboard = state.getField().getMacroboard();
       if(macroboard[aktivMicroboardX][aktivMicroboardY].equals(IField.AVAILABLE_FIELD)
               || macroboard[aktivMicroboardX][aktivMicroboardY].equals(IField.EMPTY_FIELD))
       {
           setAvailableMicroboard(state, aktivMicroboardX, aktivMicroboardY);
       }
       else
       {
           setAllMicroboardsAvailable(state);
       }
    }
    
    private void setAvailableMicroboard(IGameState state, int aktivMicroboardX, int aktivMicroboardY)
    {
        String[][] macroboard = state.getField().getMacroboard();
        for(int x = 0; x < macroboard.length; x++)
           {
               for(int y = 0; y < macroboard[x].length; y++)
               {
                   if(x == aktivMicroboardX && y == aktivMicroboardY)
                   {
                       macroboard[x][y] = IField.AVAILABLE_FIELD;
                   }
                   else if(macroboard[x][y].equals(IField.AVAILABLE_FIELD))
                   {
                       macroboard[x][y] = IField.EMPTY_FIELD;
                   }
               }
           }
        state.getField().setMacroboard(macroboard);
    }
    
    private void setAllMicroboardsAvailable(IGameState state)
    {
        String[][] macroboard = state.getField().getMacroboard();
        for(int x = 0; x < 3; x++)
           {
               for(int y = 0; y < 3; y++)
               {
                   if(macroboard[x][y].equals(IField.EMPTY_FIELD))
                   {
                       macroboard[x][y] = IField.AVAILABLE_FIELD;
                   }
               }
           }
    }
    
    private boolean isWinOnMicroboard(IGameState state, int startX, int startY)
    {
        String[][] board = state.getField().getBoard();
        return isWin(board, startX, startY);
    }
    
    private boolean isDrawOnMicroboard(IGameState state, int startX, int startY)
    {
        boolean isDraw = true;
        String[][] board = state.getField().getBoard();
        for(int x = startX; x < startX+3; x++)
        {
            for(int y = startY; y < startY+3; y++)
            {
                if(board[x][y].equals(IField.EMPTY_FIELD))
                {
                    isDraw = false;
                }
            }
        }
        return isDraw;
    }
    
    private boolean isGameOver(IGameState state)
    {
        String[][] macroboard = state.getField().getMacroboard();
        return isWin(macroboard, 0, 0) || isDraw(state);
    }
    
    private boolean isWin(IGameState state)
    {
        String[][] macroboard = state.getField().getMacroboard();
        return isWin(macroboard, 0, 0);
    }
    
    private boolean isDraw(IGameState state)
    {
        String[][] macroboard = state.getField().getMacroboard();
        for(int x = 0; x < macroboard.length; x++)
        {
            for(int y = 0; y < macroboard[x].length; y++)
            {
                if(macroboard[x][y].equals(IField.EMPTY_FIELD) || macroboard[x][y].equals(IField.AVAILABLE_FIELD))
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isWin(String[][] board, int startX, int startY)
    {
        for(int x = startX; x < startX+3; x++)
        {
            if(isHorizontalWin(board, x, startY))
            {
                return true;

            }
            for(int y = startY; y < startY+3; y++)
            {
                
                if(isVerticalWin(board, startX, y))
                {
                    return true;
                }
            }
        }
        return isDiagonalWin(board, startX, startY);
    }
    
    private boolean isHorizontalWin(String[][] board, int startX, int startY)
    {
        return ((board[startX][startY].equals("0") || board[startX][startY].equals("1"))
                    && board[startX][startY].equals(board[startX][startY+1])
                    && board[startX][startY+1].equals(board[startX][startY+2]));
    }
    
    private boolean isVerticalWin(String[][] board, int startX, int startY)
    {
        return ((board[startX][startY].equals("0") || board[startX][startY].equals("1"))
                    && board[startX][startY].equals(board[startX+1][startY])
                    && board[startX+1][startY].equals(board[startX+2][startY]));
    }
    
    private boolean isDiagonalWin(String[][] board, int startX, int startY)
    {
        if((board[startX][startY].equals("0") || board[startX][startY].equals("1"))
                && board[startX][startY].equals(board[startX+1][startY+1])
                && board[startX+1][startY+1].equals(board[startX+2][startY+2]))
        {
            return true;
        }
        else if((board[startX][startY+2].equals("0") || board[startX][startY+2].equals("1"))
                && board[startX][startY+2].equals(board[startX+1][startY+1])
                && board[startX+1][startY+1].equals(board[startX+2][startY]))
        {
            return true;
        }
        return false;
    }
    @Override
    public String getBotName() {
        return "PolleBot";
    }
    
}
