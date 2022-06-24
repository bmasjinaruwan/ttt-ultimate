package dto;

/**
 *
 * @author Thushara Supun
 */
public class Player {

    private String playerID;
    private String name;
    private String sign;
    private int score;
    private int state;

    public Player() {
    }

    public Player(String playerID, String name, String sign, int score, int state) {
        setPlayerID(playerID);
        setName(name);
        setSign(sign);
        setScore(score);
        setState(state);
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Player{" + "playerID=" + playerID + ", name=" + name + ", sign=" + sign + ", score=" + score + ", state=" + state + '}';
    }

}
