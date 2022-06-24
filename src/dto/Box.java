package dto;

/**
 *
 * @author Thushara Supun
 */
public class Box {

    private String sign;
    private int LBNumber;
    private int boxNumber;
    private boolean state;

    public Box() {
    }

    public Box(String sign, int LBNumber, int boxNumber, boolean state) {
        setSign(sign);
        setLBNumber(LBNumber);
        setBoxNumber(boxNumber);
        setState(state);
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getLBNumber() {
        return LBNumber;
    }

    public void setLBNumber(int LBNumber) {
        this.LBNumber = LBNumber;
    }

    public int getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(int boxNumber) {
        this.boxNumber = boxNumber;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Box{" + "sign=" + sign + ", LBNumber=" + LBNumber + ", boxNumber=" + boxNumber + ", state=" + state + '}';
    }

}
