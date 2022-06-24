package dto;

import java.util.ArrayList;

/**
 *
 * @author Thushara Supun
 */
public class GlobalBoard {

    private ArrayList<LocalBoard> localBoards;
    private int state;

    public GlobalBoard() {
    }

    public GlobalBoard(ArrayList<LocalBoard> localBoards, int state) {
        setLocalBoards(localBoards);
        setState(state);
    }

    public ArrayList<LocalBoard> getLocalBoards() {
        return localBoards;
    }

    public void setLocalBoards(ArrayList<LocalBoard> localBoards) {
        this.localBoards = localBoards;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        String localBoardsString = "";
        for (int i = 0; i < 9; i++) {
            localBoardsString += localBoards.get(i).toString();
        }
        return "GlobalBoard{" + "localBoards=" + localBoardsString + ", state=" + state + '}';
    }

}
