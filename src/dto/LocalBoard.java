package dto;

import java.util.ArrayList;

/**
 *
 * @author Thushara Supun
 */
public class LocalBoard {

    private int boardNumber;
    private ArrayList<Box> boxs;
    private boolean state;
    private boolean available;

    public LocalBoard() {
    }

    public LocalBoard(int boardNumber, ArrayList<Box> boxs, boolean state, boolean available) {
        this.boardNumber = boardNumber;
        this.boxs = boxs;
        this.state = state;
        this.available = available;
    }

    public int getBoardNumber() {
        return boardNumber;
    }

    public void setBoardNumber(int boardNumber) {
        this.boardNumber = boardNumber;
    }

    public ArrayList<Box> getBoxs() {
        return boxs;
    }

    public void setBoxs(ArrayList<Box> boxs) {
        this.boxs = boxs;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean getAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        String boxsString = "";
        for (int i = 0; i < 9; i++) {
            boxsString += boxs.get(i).toString();
        }
        return "LocalBoard{" + "boardNumber=" + boardNumber + ", boxs=" + boxsString + ", state=" + state + ", available=" + available + '}';
    }

}
