/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import db.DBUtil;
import dto.Box;
import dto.GlobalBoard;
import dto.LocalBoard;
import dto.Player;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

/**
 *
 * @author Thushara Supun
 */
public class MPGamePlay extends javax.swing.JFrame {

    private final int lbCount = 9;
    private final int boxCount = 9;

    private ArrayList<Component> globalBoard;

    private ArrayList<Component> localBoard1;
    private ArrayList<Component> localBoard2;
    private ArrayList<Component> localBoard3;
    private ArrayList<Component> localBoard4;
    private ArrayList<Component> localBoard5;
    private ArrayList<Component> localBoard6;
    private ArrayList<Component> localBoard7;
    private ArrayList<Component> localBoard8;
    private ArrayList<Component> localBoard9;

    private ArrayList<ArrayList<Component>> localBoards;

    private Player player1;
    private Player player2;
    private String nextPlayerID;
    private String nextPlayerName;
    private String nextPlayerSign;
    private GlobalBoard gb;

    /**
     * Creates new form MPGamePlay
     */
    public MPGamePlay() {
        initComponents();

        globalBoard = getAllComponents(globalBoardPane);

        localBoard1 = getAllComponents(localBoard_1_Pane);
        localBoard2 = getAllComponents(localBoard_2_Pane);
        localBoard3 = getAllComponents(localBoard_3_Pane);
        localBoard4 = getAllComponents(localBoard_4_Pane);
        localBoard5 = getAllComponents(localBoard_5_Pane);
        localBoard6 = getAllComponents(localBoard_6_Pane);
        localBoard7 = getAllComponents(localBoard_7_Pane);
        localBoard8 = getAllComponents(localBoard_8_Pane);
        localBoard9 = getAllComponents(localBoard_9_Pane);

        localBoards = new ArrayList<>();
        localBoards.add(localBoard1);
        localBoards.add(localBoard2);
        localBoards.add(localBoard3);
        localBoards.add(localBoard4);
        localBoards.add(localBoard5);
        localBoards.add(localBoard6);
        localBoards.add(localBoard7);
        localBoards.add(localBoard8);
        localBoards.add(localBoard9);

        try {
            int gbState = checkGBState();
            switch (gbState) {
                case 0:
                    newMPGamePlay();
                    break;
                case 2:
                    restoreMPGamePlay();
                    break;
                default:
                    printDBErrorAndExit();
            }
        } catch (Exception ex) {
            printDBErrorAndExit(ex);
        }
    }

    public final ArrayList<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        ArrayList<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container) {
                compList.addAll(getAllComponents((Container) comp));
            }
        }
        return compList;
    }

    private int checkGBState() throws Exception {
        String sql = "SELECT state FROM gbState WHERE board = 'gb'";
        ResultSet rst = DBUtil.executeQuery(sql);

        int gbState = 0;

        if (rst.next()) {
            gbState = rst.getInt("state");
        }

        return gbState;
    }

    private void clearDBTables() throws Exception {
        ArrayList<String> sqls = new ArrayList<>();

        sqls.add("DELETE FROM gbState;");
        sqls.add("DELETE FROM lbState;");
        sqls.add("DELETE FROM gbValue;");
        sqls.add("DELETE FROM playerState;");
        sqls.add("DELETE FROM playerValue;");

        for (String sql : sqls) {
            DBUtil.executeUpdate(sql);
        }
    }

    private void initializeDBTables() throws Exception {
        ArrayList<String> sqls = new ArrayList<>();

        sqls.add(
                "INSERT INTO gbState(board, state) VALUES ('gb', 0);"
        );

        sqls.add(
                "INSERT INTO lbState(board, state, available) VALUES"
                + "('lb1', 0, 0),"
                + "('lb2', 0, 0),"
                + "('lb3', 0, 0),"
                + "('lb4', 0, 0),"
                + "('lb5', 0, 0),"
                + "('lb6', 0, 0),"
                + "('lb7', 0, 0),"
                + "('lb8', 0, 0),"
                + "('lb9', 0, 0);"
        );

        sqls.add(
                "INSERT INTO gbValue(b0, b1, b2, b3, b4, b5, b6, b7, b8, b9) VALUES"
                + "('lb1', '', '', '', '', '', '', '', '', ''),"
                + "('lb2', '', '', '', '', '', '', '', '', ''),"
                + "('lb3', '', '', '', '', '', '', '', '', ''),"
                + "('lb4', '', '', '', '', '', '', '', '', ''),"
                + "('lb5', '', '', '', '', '', '', '', '', ''),"
                + "('lb6', '', '', '', '', '', '', '', '', ''),"
                + "('lb7', '', '', '', '', '', '', '', '', ''),"
                + "('lb8', '', '', '', '', '', '', '', '', ''),"
                + "('lb9', '', '', '', '', '', '', '', '', '');"
        );

        sqls.add(
                "INSERT INTO playerState(playerID, sign, state) VALUES"
                + "('player1', '', 0),"
                + "('player2', '', 0);"
        );

        sqls.add(
                "INSERT INTO playerValue(playerID, name, score) VALUES"
                + "('player1', '', 0),"
                + "('player2', '', 0);"
        );

        for (String sql : sqls) {
            DBUtil.executeUpdate(sql);
        }
    }

    private void newMPGamePlay() throws Exception {
        clearDBTables();
        initializeDBTables();

        this.player1 = new Player("player1", MPConfig.player1Name, "X", 0, 1);
        this.player2 = new Player("player2", MPConfig.player2Name, "O", 0, 0);

        ArrayList<LocalBoard> lbs = new ArrayList<>();

        for (int i = 0; i < lbCount; i++) {
            ArrayList<Box> boxs = new ArrayList<>();
            for (int j = 0; j < boxCount; j++) {
                boxs.add(j, new Box("", i + 1, j + 1, true));
            }
            lbs.add(i, new LocalBoard(i + 1, boxs, true, true));
        }
        this.gb = new GlobalBoard(lbs, 2);

        updateDBTables();
        publishPlayerNames();
        publishPlayerScores();
        setNextTurnPlayerID();
        setAndpublishNextTurnPlayerName();
        setAndpublishNextTurnPlayerSign();
        publishLBState();
        setGBValues();
        publishActiveLBsBoxState();
    }

    private void updateDBTables() throws Exception {
        updateGBStateTable();
        updateLBStateTable();
        updateGBValueTable();
        updatePlayerStateTable();
        updatePlayerValueTable();
    }

    private void updateGBStateTable() throws Exception {
        String sql = "UPDATE gbState SET state = 2 WHERE board = 'gb';";
        DBUtil.executeUpdate(sql);
    }

    private void updateLBStateTable() throws Exception {
        for (LocalBoard localBoard : this.gb.getLocalBoards()) {

            int state1 = 0;
            int state2 = 0;

            if (localBoard.getState()) {
                state1 = 1;
            } else {
                state1 = 0;
            }

            if (localBoard.getAvailable()) {
                state2 = 1;
            } else {
                state2 = 0;
            }

            String sql = "UPDATE lbState SET state = "
                    + state1
                    + ", available ="
                    + state2
                    + " WHERE board = 'lb"
                    + localBoard.getBoardNumber()
                    + "';";
            DBUtil.executeUpdate(sql);
        }
    }

    private void updateGBValueTable() throws Exception {
        for (LocalBoard localBoard : this.gb.getLocalBoards()) {
            for (Box box : localBoard.getBoxs()) {
                String sql = "UPDATE gbValue SET "
                        + "b" + box.getBoxNumber()
                        + "='" + box.getSign()
                        + "' WHERE b0 = 'lb"
                        + box.getLBNumber()
                        + "';";
                DBUtil.executeUpdate(sql);
            }
        }
    }

    private void updatePlayerStateTable() throws Exception {
        String sql1 = "UPDATE playerState SET "
                + "sign = '" + player1.getSign() + "'"
                + ", state = " + player1.getState()
                + " WHERE playerID='"
                + player1.getPlayerID()
                + "';";
        String sql2 = "UPDATE playerState SET "
                + "sign = '" + player2.getSign() + "'"
                + ", state = " + player2.getState()
                + " WHERE playerID='"
                + player2.getPlayerID()
                + "';";
        DBUtil.executeUpdate(sql1);
        DBUtil.executeUpdate(sql2);
    }

    private void updatePlayerValueTable() throws Exception {
        String sql1 = "UPDATE playerValue SET "
                + "name = '" + player1.getName()
                + "', score = " + player1.getScore()
                + " WHERE playerID='"
                + player1.getPlayerID()
                + "';";
        String sql2 = "UPDATE playerValue SET "
                + "name = '" + player2.getName()
                + "', score = " + player2.getScore()
                + " WHERE playerID='"
                + player2.getPlayerID()
                + "';";
        DBUtil.executeUpdate(sql1);
        DBUtil.executeUpdate(sql2);
    }

    private void restoreMPGamePlay() throws Exception {
        queryDBTables();
        publishPlayerNames();
        publishPlayerScores();
        setNextTurnPlayerID();
        setAndpublishNextTurnPlayerName();
        setAndpublishNextTurnPlayerSign();
        publishLBState();
        setGBValues();
        publishActiveLBsBoxState();
    }

    private void queryDBTables() throws Exception {
        int gbState = queryGBStateTable();
        int[] lbStates = queryLBStateTableState();
        int[] lbAvailable = queryLBStateTableAvailable();
        String[][] gbValues = queryGBValueTable();
        Player[] playerStates = queryPlayerStateTable();
        Player[] playerValues = queryPlayerValueTable();

        ArrayList<LocalBoard> lbs = new ArrayList<>();

        boolean state1 = false;
        boolean state2 = false;
        boolean state3 = false;

        for (int i = 0; i < lbCount; i++) {
            ArrayList<Box> boxs = new ArrayList<>();
            for (int j = 0; j < boxCount; j++) {
                if (gbValues[i][j].equals("X") || gbValues[i][j].equals("O")) {
                    state1 = false;
                } else {
                    state1 = true;
                }
                boxs.add(j, new Box(gbValues[i][j], i + 1, j + 1, state1));
            }
            if (lbStates[i] == 1) {
                state2 = true;
            } else {
                state2 = false;
            }
            if (lbAvailable[i] == 1) {
                state3 = true;
            } else {
                state3 = false;
            }
            lbs.add(i, new LocalBoard(i + 1, boxs, state2, state3));
        }
        this.gb = new GlobalBoard(lbs, gbState);

        this.player1 = new Player(
                playerStates[0].getPlayerID(),
                playerValues[0].getName(),
                playerStates[0].getSign(),
                playerValues[0].getScore(),
                playerStates[0].getState()
        );

        this.player2 = new Player(
                playerStates[1].getPlayerID(),
                playerValues[1].getName(),
                playerStates[1].getSign(),
                playerValues[1].getScore(),
                playerStates[1].getState()
        );
    }

    private int queryGBStateTable() throws Exception {
        String sql = "SELECT state FROM gbState;";
        ResultSet rst = DBUtil.executeQuery(sql);

        int gbState = 0;

        while (rst.next()) {
            gbState = rst.getInt("state");
        }

        return gbState;
    }

    private int[] queryLBStateTableState() throws Exception {
        String sql = "SELECT state FROM lbState;";
        ResultSet rst = DBUtil.executeQuery(sql);

        int count = 0;
        int[] lbState = new int[9];

        while (rst.next()) {
            lbState[count] = rst.getInt("state");
            count++;
        }

        return lbState;
    }

    private int[] queryLBStateTableAvailable() throws Exception {
        String sql = "SELECT available FROM lbState;";
        ResultSet rst = DBUtil.executeQuery(sql);

        int count = 0;
        int[] lbavailable = new int[9];

        while (rst.next()) {
            lbavailable[count] = rst.getInt("available");
            count++;
        }

        return lbavailable;
    }

    private String[][] queryGBValueTable() throws Exception {
        String sql = "SELECT b1, b2, b3, b4, b5, b6, b7, b8, b9 FROM gbValue;";
        ResultSet rst = DBUtil.executeQuery(sql);

        int count = 0;
        String[][] gbValue = new String[9][9];

        while (rst.next()) {
            gbValue[count][0] = rst.getString("b1");
            gbValue[count][1] = rst.getString("b2");
            gbValue[count][2] = rst.getString("b3");
            gbValue[count][3] = rst.getString("b4");
            gbValue[count][4] = rst.getString("b5");
            gbValue[count][5] = rst.getString("b6");
            gbValue[count][6] = rst.getString("b7");
            gbValue[count][7] = rst.getString("b8");
            gbValue[count][8] = rst.getString("b9");
            count++;
        }

        return gbValue;
    }

    private Player[] queryPlayerStateTable() throws Exception {
        String sql = "SELECT playerID, sign, state FROM playerState;";
        ResultSet rst = DBUtil.executeQuery(sql);

        int count = 0;
        Player[] playerStates = {new Player(), new Player()};

        while (rst.next()) {
            playerStates[count].setPlayerID(rst.getString("playerID"));
            playerStates[count].setSign(rst.getString("sign"));
            playerStates[count].setState(rst.getInt("state"));
            count++;
        }

        return playerStates;
    }

    private Player[] queryPlayerValueTable() throws Exception {
        String sql = "SELECT playerID, name, score FROM playerValue;";
        ResultSet rst = DBUtil.executeQuery(sql);

        int count = 0;
        Player[] playerValues = {new Player(), new Player()};

        while (rst.next()) {
            playerValues[count].setPlayerID(rst.getString("playerID"));
            playerValues[count].setName(rst.getString("name"));
            playerValues[count].setScore(rst.getInt("score"));
            count++;
        }

        return playerValues;
    }

    private void publishPlayerNames() throws Exception {
        player1NameTF.setText(MPConfig.player1Name);
        player2NameTF.setText(MPConfig.player2Name);
    }

    private void publishPlayerScores() throws Exception {
        player1ScoreTF.setText(Integer.toString(player1.getScore()));
        player2ScoreTF.setText(Integer.toString(player2.getScore()));
    }

    private void setNextTurnPlayerID() throws Exception {
        if (player1.getState() == 1 && player2.getState() == 0) {
            this.nextPlayerID = "player1";
        } else if (player1.getState() == 0 && player2.getState() == 1) {
            this.nextPlayerID = "player2";
        } else {
            printDBErrorAndExit();
        }
    }

    private void setAndpublishNextTurnPlayerName() throws Exception {
        if (player1.getState() == 1 && player2.getState() == 0) {
            this.nextPlayerName = "Player 1";
            nextTurnPlayerNameLbl.setText("Player 1");
        } else if (player1.getState() == 0 && player2.getState() == 1) {
            this.nextPlayerName = "Player 2";
            nextTurnPlayerNameLbl.setText("Player 2");
        } else {
            printDBErrorAndExit();
        }
    }

    private void setAndpublishNextTurnPlayerSign() throws Exception {
        if (player1.getState() == 1 && player2.getState() == 0) {
            this.nextPlayerSign = player1.getSign();
            nextTurnSignLbl.setText("(" + player1.getSign() + ")");
            nextTurnSignLbl.setForeground(Color.RED);
        } else if (player1.getState() == 0 && player2.getState() == 1) {
            this.nextPlayerSign = player2.getSign();
            nextTurnSignLbl.setText("(" + player2.getSign() + ")");
            nextTurnSignLbl.setForeground(Color.BLUE);
        } else {
            printDBErrorAndExit();
        }
    }

    private void swapAndSetPlayerState() throws Exception {
        if (player1.getState() == 1 && player2.getState() == 0) {
            player1.setState(0);
            player2.setState(1);
        } else if (player1.getState() == 0 && player2.getState() == 1) {
            player1.setState(1);
            player2.setState(0);
        } else {
            printDBErrorAndExit();
        }

        String sql1 = "UPDATE playerState SET "
                + " state = " + player1.getState()
                + " WHERE playerID='"
                + player1.getPlayerID()
                + "';";
        String sql2 = "UPDATE playerState SET "
                + " state = " + player2.getState()
                + " WHERE playerID='"
                + player2.getPlayerID()
                + "';";
        DBUtil.executeUpdate(sql1);
        DBUtil.executeUpdate(sql2);
    }

    private void swapAndPublishNextPlayerValues() throws Exception {
        swapAndSetPlayerState();
        setNextTurnPlayerID();
        setAndpublishNextTurnPlayerName();
        setAndpublishNextTurnPlayerSign();
    }

    private void setGBValues() throws Exception {
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (this.gb.getLocalBoards().get(i).getBoxs().get(j).getSign().equals("X")) {
                    this.localBoards.get(i).get(j).setForeground(Color.RED);
                } else if (this.gb.getLocalBoards().get(i).getBoxs().get(j).getSign().equals("O")) {
                    this.localBoards.get(i).get(j).setForeground(Color.BLUE);
                }
                Component component = this.localBoards.get(i).get(j);
                javax.swing.JButton button = (javax.swing.JButton) component;
                button.setText(this.gb.getLocalBoards().get(i).getBoxs().get(j).getSign());
            }
        }

    }

    private void publishLBState() throws Exception {
        for (int i = 0; i < lbCount; i++) {
            if (this.gb.getLocalBoards().get(i).getAvailable()) {
                if (this.gb.getLocalBoards().get(i).getState()) {
                    for (int j = 0; j < boxCount; j++) {
                        Component component = this.localBoards.get(i).get(j);
                        javax.swing.JButton button = (javax.swing.JButton) component;
                        button.setBackground(Color.WHITE);
                        button.setEnabled(true);
                        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                } else {
                    for (int j = 0; j < boxCount; j++) {
                        Component component = this.localBoards.get(i).get(j);
                        javax.swing.JButton button = (javax.swing.JButton) component;
                        button.setBackground(Color.LIGHT_GRAY);
                        button.setDisabledIcon(button.getIcon());
                        button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            } else {
                for (int j = 0; j < boxCount; j++) {
                    Component component = this.localBoards.get(i).get(j);
                    javax.swing.JButton button = (javax.swing.JButton) component;
                    button.setBackground(Color.LIGHT_GRAY);
                    button.setDisabledIcon(button.getIcon());
                    button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }

        }
    }

    private void publishActiveLBsBoxState() throws Exception {
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (this.gb.getLocalBoards().get(i).getAvailable()) {
                    if (this.gb.getLocalBoards().get(i).getState()) {
                        if (!this.gb.getLocalBoards().get(i).getBoxs().get(j).getState()) {
                            Component component = this.localBoards.get(i).get(j);
                            javax.swing.JButton button = (javax.swing.JButton) component;
                            button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                }
            }
        }
    }

    private void box_Btn_onAction(String boxBtnId) {
        try {
            int locationCode = Integer.parseInt(
                    boxBtnId.substring(4, 6)
            );

            int lbNumber = locationCode / 10;
            int boxNumber = locationCode % 10;

            if ((this.gb.getLocalBoards().get(lbNumber - 1).getState())
                    && (this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(boxNumber - 1).getState())) {
                if (this.player1.getState() == 1 && this.player2.getState() == 0) {
                } else if (this.player1.getState() == 0 && this.player2.getState() == 1) {
                }

                setBoxValues_boxBtnOnAction(lbNumber, boxNumber);
                publishBoxStateAndValues_boxBtnOnAction(lbNumber, boxNumber);
                updateDBBoxStateAndValues_boxBtnOnAction(lbNumber, boxNumber);

                setPublishAndUpdatePlayerNewScore(nextPlayerID, -5);

                boolean isGBWin = checkForGBWin(lbNumber);
                checkForLBWin(lbNumber);

                checkForLBFull(lbNumber);
                boolean isGBFull = checkForGBFull();

                setBoxState_boxBtnOnAction(lbNumber, boxNumber);
                setLBStateAndValues_boxBtnOnAction(lbNumber, boxNumber);
                publishLBStateAndBoxState_boxBtnOnAction();
                updateDBLBStateAndValues_boxBtnOnAction();

                if (isGBWin || isGBFull) {
                    MPGamePlayOver();
                }

                swapAndPublishNextPlayerValues();
            }
        } catch (Exception ex) {
            printDBErrorAndExit(ex);
        }
    }

    private void setBoxValues_boxBtnOnAction(int lbNumber, int boxNumber) throws Exception {
        this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(boxNumber - 1).setSign(nextPlayerSign);
    }

    private void setBoxState_boxBtnOnAction(int lbNumber, int boxNumber) throws Exception {
        this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(boxNumber - 1).setState(false);
    }

    private void publishBoxStateAndValues_boxBtnOnAction(int lbNumber, int boxNumber) throws Exception {
        Component component = this.localBoards.get(lbNumber - 1).get(boxNumber - 1);
        javax.swing.JButton button = (javax.swing.JButton) component;
        if (nextPlayerSign.equals("X")) {
            button.setForeground(Color.RED);
        } else if (nextPlayerSign.equals("O")) {
            button.setForeground(Color.BLUE);
        }
        button.setText(nextPlayerSign);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void updateDBBoxStateAndValues_boxBtnOnAction(int lbNumber, int boxNumber) throws Exception {
        Box box = this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(boxNumber - 1);

        String sql = "UPDATE gbValue SET "
                + "b" + box.getBoxNumber()
                + "='" + box.getSign()
                + "' WHERE b0 = 'lb"
                + box.getLBNumber()
                + "';";
        DBUtil.executeUpdate(sql);
    }

    private void setLBStateAndValues_boxBtnOnAction(int lbNumber, int boxNumber) throws Exception {
        if (this.gb.getLocalBoards().get(boxNumber - 1).getAvailable()) {
            for (int i = 0; i < lbCount; i++) {
                if ((boxNumber - 1) == i) {
                    this.gb.getLocalBoards().get(i).setState(true);
                } else {
                    this.gb.getLocalBoards().get(i).setState(false);
                }
            }
        } else {
            for (int i = 0; i < lbCount; i++) {
                if ((boxNumber - 1) == i) {
                    this.gb.getLocalBoards().get(i).setState(false);
                } else {
                    this.gb.getLocalBoards().get(i).setState(true);
                }
            }
        }
    }

    private void publishLBStateAndBoxState_boxBtnOnAction() throws Exception {
        publishLBState();
        publishActiveLBsBoxState();
    }

    private void updateDBLBStateAndValues_boxBtnOnAction() throws Exception {
        updateLBStateTable();
    }

    private void checkForLBWin(int lbNumber) throws Exception {
        Box box1 = this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(0);
        Box box2 = this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(1);
        Box box3 = this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(2);
        Box box4 = this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(3);
        Box box5 = this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(4);
        Box box6 = this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(5);
        Box box7 = this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(6);
        Box box8 = this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(7);
        Box box9 = this.gb.getLocalBoards().get(lbNumber - 1).getBoxs().get(8);

        ArrayList<Box> boxs = new ArrayList<>();
        boxs.add(0, box1);
        boxs.add(1, box2);
        boxs.add(2, box3);
        boxs.add(3, box4);
        boxs.add(4, box5);
        boxs.add(5, box6);
        boxs.add(6, box7);
        boxs.add(7, box8);
        boxs.add(8, box9);

        boolean isWinRow = checkRowsForLBWin(boxs);
        boolean isWinColumn = checkColumnsForLBWin(boxs);
        boolean isWinDiagonal = checkDiagonalsForLBWin(boxs);

        String playerName = null;
        if (this.player1.getPlayerID().equals(nextPlayerID)) {
            playerName = this.player1.getName();
        } else if (this.player2.getPlayerID().equals(nextPlayerID)) {
            playerName = this.player2.getName();
        }

        if (isWinRow) {
            setPublishAndUpdatePlayerNewScore(nextPlayerID, 100);

            this.gb.getLocalBoards().get(lbNumber - 1).setAvailable(false);
            String sql = "UPDATE lbState SET available = "
                    + "0"
                    + " WHERE board = 'lb"
                    + lbNumber
                    + "';";
            DBUtil.executeUpdate(sql);

            JOptionPane.showMessageDialog(
                    rootPane,
                    " You Have Earned 100 More Score for Winning a \n 'Row' of Local Board " + lbNumber + ".",
                    "Congratulations! " + playerName,
                    INFORMATION_MESSAGE
            );
        }
        if (isWinColumn) {
            setPublishAndUpdatePlayerNewScore(nextPlayerID, 100);

            this.gb.getLocalBoards().get(lbNumber - 1).setAvailable(false);
            String sql = "UPDATE lbState SET available = "
                    + "0"
                    + " WHERE board = 'lb"
                    + lbNumber
                    + "';";
            DBUtil.executeUpdate(sql);

            JOptionPane.showMessageDialog(
                    rootPane,
                    " You Have Earned 100 More Score for Winning a \n 'Column' of Local Board " + lbNumber + ".",
                    "Congratulations! " + playerName,
                    INFORMATION_MESSAGE
            );
        }
        if (isWinDiagonal) {
            setPublishAndUpdatePlayerNewScore(nextPlayerID, 100);

            this.gb.getLocalBoards().get(lbNumber - 1).setAvailable(false);
            String sql = "UPDATE lbState SET available = "
                    + "0"
                    + " WHERE board = 'lb"
                    + lbNumber
                    + "';";
            DBUtil.executeUpdate(sql);

            JOptionPane.showMessageDialog(
                    rootPane,
                    " You Have Earned 100 More Score for Winning a \n 'Diagonal' of Local Board " + lbNumber + ".",
                    "Congratulations! " + playerName,
                    INFORMATION_MESSAGE
            );
        }
    }

    private boolean checkRowsForLBWin(ArrayList<Box> boxs) throws Exception {
        boolean isWin = false;

        if (areAllSignsEqual(nextPlayerSign, boxs.get(0).getSign(), boxs.get(1).getSign(), boxs.get(2).getSign())) {
            if (!areAllBooleansFalse(boxs.get(0).getState(), boxs.get(1).getState(), boxs.get(2).getState())) {
                isWin = true;
            }
        } else if (areAllSignsEqual(nextPlayerSign, boxs.get(3).getSign(), boxs.get(4).getSign(), boxs.get(5).getSign())) {
            if (!areAllBooleansFalse(boxs.get(3).getState(), boxs.get(4).getState(), boxs.get(5).getState())) {
                isWin = true;
            }
        } else if (areAllSignsEqual(nextPlayerSign, boxs.get(6).getSign(), boxs.get(7).getSign(), boxs.get(8).getSign())) {
            if (!areAllBooleansFalse(boxs.get(6).getState(), boxs.get(7).getState(), boxs.get(8).getState())) {
                isWin = true;
            }
        }

        return isWin;
    }

    private boolean checkColumnsForLBWin(ArrayList<Box> boxs) throws Exception {
        boolean isWin = false;

        if (areAllSignsEqual(nextPlayerSign, boxs.get(0).getSign(), boxs.get(3).getSign(), boxs.get(6).getSign())) {
            if (!areAllBooleansFalse(boxs.get(0).getState(), boxs.get(3).getState(), boxs.get(6).getState())) {
                isWin = true;
            }
        } else if (areAllSignsEqual(nextPlayerSign, boxs.get(1).getSign(), boxs.get(4).getSign(), boxs.get(7).getSign())) {
            if (!areAllBooleansFalse(boxs.get(1).getState(), boxs.get(4).getState(), boxs.get(7).getState())) {
                isWin = true;
            }
        } else if (areAllSignsEqual(nextPlayerSign, boxs.get(2).getSign(), boxs.get(5).getSign(), boxs.get(8).getSign())) {
            if (!areAllBooleansFalse(boxs.get(2).getState(), boxs.get(5).getState(), boxs.get(8).getState())) {
                isWin = true;
            }
        }

        return isWin;
    }

    private boolean checkDiagonalsForLBWin(ArrayList<Box> boxs) throws Exception {
        boolean isWin = false;

        if (areAllSignsEqual(nextPlayerSign, boxs.get(0).getSign(), boxs.get(4).getSign(), boxs.get(8).getSign())) {
            if (!areAllBooleansFalse(boxs.get(0).getState(), boxs.get(4).getState(), boxs.get(8).getState())) {
                isWin = true;
            }
        } else if (areAllSignsEqual(nextPlayerSign, boxs.get(2).getSign(), boxs.get(4).getSign(), boxs.get(6).getSign())) {
            if (!areAllBooleansFalse(boxs.get(2).getState(), boxs.get(4).getState(), boxs.get(6).getState())) {
                isWin = true;
            }
        }

        return isWin;
    }

    private boolean checkForGBWin(int lbNumber) throws Exception {
        boolean isWinRow = checkRowsForGBWin(lbNumber);
        boolean isWinColumn = checkColumnsForGBWin(lbNumber);
        boolean isWinDiagonal = checkDiagonalsForGBWin(lbNumber);
        boolean isWin = false;

        String playerName = null;
        if (this.player1.getPlayerID().equals(nextPlayerID)) {
            playerName = this.player1.getName();
        } else if (this.player2.getPlayerID().equals(nextPlayerID)) {
            playerName = this.player2.getName();
        }

        if (isWinRow) {
            isWin = true;
            setPublishAndUpdatePlayerNewScore(nextPlayerID, 700);

            JOptionPane.showMessageDialog(
                    rootPane,
                    " You Have Earned 700 More Score for Winning a \n 'Row' of Global Board.",
                    "Congratulations! " + playerName,
                    INFORMATION_MESSAGE
            );
        }
        if (isWinColumn) {
            isWin = true;
            setPublishAndUpdatePlayerNewScore(nextPlayerID, 700);

            JOptionPane.showMessageDialog(
                    rootPane,
                    " You Have Earned 700 More Score for Winning a \n 'Column' of Global Board.",
                    "Congratulations! " + playerName,
                    INFORMATION_MESSAGE
            );
        }
        if (isWinDiagonal) {
            isWin = true;
            setPublishAndUpdatePlayerNewScore(nextPlayerID, 700);

            JOptionPane.showMessageDialog(
                    rootPane,
                    " You Have Earned 700 More Score for Winning a \n 'Diagonal' of Global Board.",
                    "Congratulations! " + playerName,
                    INFORMATION_MESSAGE
            );
        }

        return isWin;
    }

    private boolean checkRowsForGBWin(int lbNumber) throws Exception {
        boolean isWin = false;

        switch (lbNumber) {
            case 1:
            case 2:
            case 3:
                if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(0).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(2).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(0).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(2).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(0).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(5).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(0).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(5).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(0).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(8).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(0).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(8).getState()
                    )) {
                        isWin = true;
                    }
                }
                break;
            case 4:
            case 5:
            case 6:
                if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(3).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(2).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(3).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(2).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(3).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(5).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(3).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(5).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(3).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(8).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(3).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(8).getState()
                    )) {
                        isWin = true;
                    }
                }
                break;
            case 7:
            case 8:
            case 9:
                if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(6).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(2).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(6).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(2).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(6).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(5).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(6).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(5).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(6).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(8).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(6).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(8).getState()
                    )) {
                        isWin = true;
                    }
                }
                break;
            default:
                break;
        }

        return isWin;
    }

    private boolean checkColumnsForGBWin(int lbNumber) throws Exception {
        boolean isWin = false;

        switch (lbNumber) {
            case 1:
            case 4:
            case 7:
                if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(0).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(6).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(0).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(6).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(0).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(7).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(0).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(7).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(0).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(3).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(8).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(0).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(0).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(3).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(6).getBoxs().get(8).getState()
                    )) {
                        isWin = true;
                    }
                }
                break;
            case 2:
            case 5:
            case 8:
                if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(1).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(6).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(1).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(6).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(1).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(7).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(1).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(7).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(1).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(1).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(7).getBoxs().get(8).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(1).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(1).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(4).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(7).getBoxs().get(8).getState()
                    )) {
                        isWin = true;
                    }
                }
                break;
            case 3:
            case 6:
            case 9:
                if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(2).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(6).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(0).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(3).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(6).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(2).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(6).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(0).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(3).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(6).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(2).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(7).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(1).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(4).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(7).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(2).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(7).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(1).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(4).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(7).getState()
                    )) {
                        isWin = true;
                    }
                } else if (areAllSignsEqual(
                        nextPlayerSign,
                        this.gb.getLocalBoards().get(2).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(5).getBoxs().get(8).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(2).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(5).getSign(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(8).getSign()
                )) {
                    if (!areAllBooleansFalse(
                            this.gb.getLocalBoards().get(2).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(2).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(5).getBoxs().get(8).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(2).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(5).getState(),
                            this.gb.getLocalBoards().get(8).getBoxs().get(8).getState()
                    )) {
                        isWin = true;
                    }
                }
                break;
            default:
                break;
        }

        return isWin;
    }

    private boolean checkDiagonalsForGBWin(int lbNumber) throws Exception {
        boolean isWin = false;

        if (lbNumber == 1 || lbNumber == 5 || lbNumber == 9) {
            if (areAllSignsEqual(
                    nextPlayerSign,
                    this.gb.getLocalBoards().get(0).getBoxs().get(0).getSign(),
                    this.gb.getLocalBoards().get(0).getBoxs().get(4).getSign(),
                    this.gb.getLocalBoards().get(0).getBoxs().get(8).getSign(),
                    this.gb.getLocalBoards().get(4).getBoxs().get(0).getSign(),
                    this.gb.getLocalBoards().get(4).getBoxs().get(4).getSign(),
                    this.gb.getLocalBoards().get(4).getBoxs().get(8).getSign(),
                    this.gb.getLocalBoards().get(8).getBoxs().get(0).getSign(),
                    this.gb.getLocalBoards().get(8).getBoxs().get(4).getSign(),
                    this.gb.getLocalBoards().get(8).getBoxs().get(8).getSign()
            )) {
                if (!areAllBooleansFalse(
                        this.gb.getLocalBoards().get(0).getBoxs().get(0).getState(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(4).getState(),
                        this.gb.getLocalBoards().get(0).getBoxs().get(8).getState(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(0).getState(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(4).getState(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(8).getState(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(0).getState(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(4).getState(),
                        this.gb.getLocalBoards().get(8).getBoxs().get(8).getState()
                )) {
                    isWin = true;
                }
            }
        }

        if (lbNumber == 3 || lbNumber == 5 || lbNumber == 7) {
            if (areAllSignsEqual(
                    nextPlayerSign,
                    this.gb.getLocalBoards().get(2).getBoxs().get(2).getSign(),
                    this.gb.getLocalBoards().get(2).getBoxs().get(4).getSign(),
                    this.gb.getLocalBoards().get(2).getBoxs().get(6).getSign(),
                    this.gb.getLocalBoards().get(4).getBoxs().get(2).getSign(),
                    this.gb.getLocalBoards().get(4).getBoxs().get(4).getSign(),
                    this.gb.getLocalBoards().get(4).getBoxs().get(6).getSign(),
                    this.gb.getLocalBoards().get(6).getBoxs().get(2).getSign(),
                    this.gb.getLocalBoards().get(6).getBoxs().get(4).getSign(),
                    this.gb.getLocalBoards().get(6).getBoxs().get(6).getSign()
            )) {
                if (!areAllBooleansFalse(
                        this.gb.getLocalBoards().get(2).getBoxs().get(2).getState(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(4).getState(),
                        this.gb.getLocalBoards().get(2).getBoxs().get(6).getState(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(2).getState(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(4).getState(),
                        this.gb.getLocalBoards().get(4).getBoxs().get(6).getState(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(2).getState(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(4).getState(),
                        this.gb.getLocalBoards().get(6).getBoxs().get(6).getState()
                )) {
                    isWin = true;
                }
            }
        }

        return isWin;
    }

    public boolean areAllSignsEqual(String checkSign, String... otherSigns) throws Exception {
        for (String sign : otherSigns) {
            if (!sign.equals(checkSign)) {
                return false;
            }
        }
        return true;
    }

    public boolean areAllBooleansFalse(boolean... booleanValues) throws Exception {
        for (boolean value : booleanValues) {
            if (value == true) {
                return false;
            }
        }
        return true;
    }

    private void setPublishAndUpdatePlayerNewScore(String playerID, int score) throws Exception {
        int newScore = 0;
        if (player1.getPlayerID().equals(playerID)) {
            newScore = this.player1.getScore() + score;
        } else if (player2.getPlayerID().equals(playerID)) {
            newScore = this.player2.getScore() + score;
        } else {
            printDBErrorAndExit();
        }

        setPlayerNewScore(playerID, newScore);
        publishPlayerNewScore(playerID, newScore);
        updateDBPlayerNewScore(playerID, newScore);
    }

    private void setPlayerNewScore(String playerID, int newScore) throws Exception {
        if (player1.getPlayerID().equals(playerID)) {
            player1.setScore(newScore);
        } else if (player2.getPlayerID().equals(playerID)) {
            player2.setScore(newScore);
        }
    }

    private void publishPlayerNewScore(String playerID, int newScore) throws Exception {
        publishPlayerScores();
    }

    private void updateDBPlayerNewScore(String playerID, int newScore) throws Exception {
        String sql = "UPDATE playerValue SET "
                + " score = " + newScore
                + " WHERE playerID='"
                + playerID
                + "';";

        DBUtil.executeUpdate(sql);
    }

    private void checkForLBFull(int lbNumber) throws Exception {
        boolean isFull = true;

        for (int i = 0; i < boxCount; i++) {
            Component component = this.localBoards.get(lbNumber - 1).get(i);
            javax.swing.JButton button = (javax.swing.JButton) component;
            if (button.getText().equals("")) {
                isFull = false;
                break;
            }
        }

        if (isFull) {
            this.gb.getLocalBoards().get(lbNumber - 1).setAvailable(false);

            String sql = "UPDATE lbState SET available = "
                    + "0"
                    + " WHERE board = 'lb"
                    + lbNumber
                    + "';";
            DBUtil.executeUpdate(sql);
        }
    }

    private boolean checkForGBFull() throws Exception {
        boolean isFull = true;

        for (int i = 0; i < lbCount; i++) {
            if (this.gb.getLocalBoards().get(i).getAvailable()) {
                isFull = false;
                break;
            }
        }

        return isFull;
    }

    private void MPGamePlayOver() throws Exception {
        this.gb.setState(0);

        String sql = "UPDATE gbState SET state = 0 WHERE board = 'gb';";
        DBUtil.executeUpdate(sql);

        if (this.player1.getScore() > this.player2.getScore()) {
            JOptionPane.showMessageDialog(
                    rootPane,
                    " \n You Win the Game... \n ",
                    " Congratulations! " + this.player1.getName(),
                    INFORMATION_MESSAGE
            );
            homeBtn.doClick();
        } else if (this.player1.getScore() < this.player2.getScore()) {
            JOptionPane.showMessageDialog(
                    rootPane,
                    " \n You Win the Game... \n ",
                    " Congratulations! " + this.player2.getName(),
                    INFORMATION_MESSAGE
            );
            homeBtn.doClick();
        } else if (this.player1.getScore() == this.player2.getScore()) {
            JOptionPane.showMessageDialog(
                    rootPane,
                    " \n You Both Win the Game... \n ",
                    " Congratulations! " + this.player1.getName() + " & " + this.player2.getName(),
                    INFORMATION_MESSAGE
            );
            homeBtn.doClick();
        } else {
            printDBErrorAndExit();
        }
    }

    private void printDBErrorAndExit() throws Exception {
        int res = JOptionPane.showConfirmDialog(
                rootPane,
                " Something Went Wrong with Database. \n Try to Restart the Tic-Tac-Toe Ultimate.",
                "Error in Database",
                JOptionPane.OK_OPTION
        );
        if (res == JOptionPane.OK_OPTION) {
            System.exit(0);
        }
    }

    private void printDBErrorAndExit(Exception ex) {
        ex.printStackTrace();

        int res = JOptionPane.showConfirmDialog(
                rootPane,
                " Something Went Wrong with Database. \n Try to Restart the Tic-Tac-Toe Ultimate.",
                "Error in Database",
                JOptionPane.OK_OPTION
        );
        if (res == JOptionPane.OK_OPTION) {
            System.exit(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        globalBoardPane = new javax.swing.JPanel();
        localBoard_1_Pane = new javax.swing.JPanel();
        box_11_Btn = new javax.swing.JButton();
        box_12_Btn = new javax.swing.JButton();
        box_13_Btn = new javax.swing.JButton();
        box_14_Btn = new javax.swing.JButton();
        box_15_Btn = new javax.swing.JButton();
        box_16_Btn = new javax.swing.JButton();
        box_17_Btn = new javax.swing.JButton();
        box_18_Btn = new javax.swing.JButton();
        box_19_Btn = new javax.swing.JButton();
        localBoard_2_Pane = new javax.swing.JPanel();
        box_21_Btn = new javax.swing.JButton();
        box_22_Btn = new javax.swing.JButton();
        box_23_Btn = new javax.swing.JButton();
        box_24_Btn = new javax.swing.JButton();
        box_25_Btn = new javax.swing.JButton();
        box_26_Btn = new javax.swing.JButton();
        box_27_Btn = new javax.swing.JButton();
        box_28_Btn = new javax.swing.JButton();
        box_29_Btn = new javax.swing.JButton();
        localBoard_3_Pane = new javax.swing.JPanel();
        box_31_Btn = new javax.swing.JButton();
        box_32_Btn = new javax.swing.JButton();
        box_33_Btn = new javax.swing.JButton();
        box_34_Btn = new javax.swing.JButton();
        box_35_Btn = new javax.swing.JButton();
        box_36_Btn = new javax.swing.JButton();
        box_37_Btn = new javax.swing.JButton();
        box_38_Btn = new javax.swing.JButton();
        box_39_Btn = new javax.swing.JButton();
        localBoard_4_Pane = new javax.swing.JPanel();
        box_41_Btn = new javax.swing.JButton();
        box_42_Btn = new javax.swing.JButton();
        box_43_Btn = new javax.swing.JButton();
        box_44_Btn = new javax.swing.JButton();
        box_45_Btn = new javax.swing.JButton();
        box_46_Btn = new javax.swing.JButton();
        box_47_Btn = new javax.swing.JButton();
        box_48_Btn = new javax.swing.JButton();
        box_49_Btn = new javax.swing.JButton();
        localBoard_5_Pane = new javax.swing.JPanel();
        box_51_Btn = new javax.swing.JButton();
        box_52_Btn = new javax.swing.JButton();
        box_53_Btn = new javax.swing.JButton();
        box_54_Btn = new javax.swing.JButton();
        box_55_Btn = new javax.swing.JButton();
        box_56_Btn = new javax.swing.JButton();
        box_57_Btn = new javax.swing.JButton();
        box_58_Btn = new javax.swing.JButton();
        box_59_Btn = new javax.swing.JButton();
        localBoard_6_Pane = new javax.swing.JPanel();
        box_61_Btn = new javax.swing.JButton();
        box_62_Btn = new javax.swing.JButton();
        box_63_Btn = new javax.swing.JButton();
        box_64_Btn = new javax.swing.JButton();
        box_65_Btn = new javax.swing.JButton();
        box_66_Btn = new javax.swing.JButton();
        box_67_Btn = new javax.swing.JButton();
        box_68_Btn = new javax.swing.JButton();
        box_69_Btn = new javax.swing.JButton();
        localBoard_7_Pane = new javax.swing.JPanel();
        box_71_Btn = new javax.swing.JButton();
        box_72_Btn = new javax.swing.JButton();
        box_73_Btn = new javax.swing.JButton();
        box_74_Btn = new javax.swing.JButton();
        box_75_Btn = new javax.swing.JButton();
        box_76_Btn = new javax.swing.JButton();
        box_77_Btn = new javax.swing.JButton();
        box_78_Btn = new javax.swing.JButton();
        box_79_Btn = new javax.swing.JButton();
        localBoard_8_Pane = new javax.swing.JPanel();
        box_81_Btn = new javax.swing.JButton();
        box_82_Btn = new javax.swing.JButton();
        box_83_Btn = new javax.swing.JButton();
        box_84_Btn = new javax.swing.JButton();
        box_85_Btn = new javax.swing.JButton();
        box_86_Btn = new javax.swing.JButton();
        box_87_Btn = new javax.swing.JButton();
        box_88_Btn = new javax.swing.JButton();
        box_89_Btn = new javax.swing.JButton();
        localBoard_9_Pane = new javax.swing.JPanel();
        box_91_Btn = new javax.swing.JButton();
        box_92_Btn = new javax.swing.JButton();
        box_93_Btn = new javax.swing.JButton();
        box_94_Btn = new javax.swing.JButton();
        box_95_Btn = new javax.swing.JButton();
        box_96_Btn = new javax.swing.JButton();
        box_97_Btn = new javax.swing.JButton();
        box_98_Btn = new javax.swing.JButton();
        box_99_Btn = new javax.swing.JButton();
        nextTurnSignLbl = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        nextTurnPlayerNameLbl = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        player1ScoreTF = new javax.swing.JTextField();
        player1NameTF = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        player2NameTF = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        player2ScoreTF = new javax.swing.JTextField();
        homeBtn = new javax.swing.JButton();
        restartBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setPreferredSize(new java.awt.Dimension(900, 700));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 48)); // NOI18N
        jLabel1.setForeground(java.awt.Color.blue);
        jLabel1.setText("Tic Tac Toe ");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 20, -1, -1));

        jLabel2.setFont(new java.awt.Font("Cantarell", 3, 24)); // NOI18N
        jLabel2.setForeground(java.awt.Color.green);
        jLabel2.setText("Ultimate");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 40, -1, -1));

        globalBoardPane.setPreferredSize(new java.awt.Dimension(450, 450));
        globalBoardPane.setLayout(new java.awt.GridLayout(3, 3));

        localBoard_1_Pane.setBackground(java.awt.Color.black);
        localBoard_1_Pane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        localBoard_1_Pane.setForeground(java.awt.Color.white);
        localBoard_1_Pane.setPreferredSize(new java.awt.Dimension(150, 150));
        localBoard_1_Pane.setLayout(new java.awt.GridLayout(3, 3));

        box_11_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_11_Btn.setText("-");
        box_11_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_11_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_11_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_11_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_11_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_11_BtnActionPerformed(evt);
            }
        });
        localBoard_1_Pane.add(box_11_Btn);

        box_12_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_12_Btn.setText("-");
        box_12_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_12_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_12_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_12_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_12_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_12_BtnActionPerformed(evt);
            }
        });
        localBoard_1_Pane.add(box_12_Btn);

        box_13_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_13_Btn.setText("-");
        box_13_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_13_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_13_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_13_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_13_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_13_BtnActionPerformed(evt);
            }
        });
        localBoard_1_Pane.add(box_13_Btn);

        box_14_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_14_Btn.setText("-");
        box_14_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_14_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_14_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_14_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_14_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_14_BtnActionPerformed(evt);
            }
        });
        localBoard_1_Pane.add(box_14_Btn);

        box_15_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_15_Btn.setText("-");
        box_15_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_15_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_15_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_15_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_15_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_15_BtnActionPerformed(evt);
            }
        });
        localBoard_1_Pane.add(box_15_Btn);

        box_16_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_16_Btn.setText("-");
        box_16_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_16_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_16_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_16_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_16_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_16_BtnActionPerformed(evt);
            }
        });
        localBoard_1_Pane.add(box_16_Btn);

        box_17_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_17_Btn.setText("-");
        box_17_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_17_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_17_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_17_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_17_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_17_BtnActionPerformed(evt);
            }
        });
        localBoard_1_Pane.add(box_17_Btn);

        box_18_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_18_Btn.setText("-");
        box_18_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_18_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_18_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_18_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_18_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_18_BtnActionPerformed(evt);
            }
        });
        localBoard_1_Pane.add(box_18_Btn);

        box_19_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_19_Btn.setText("-");
        box_19_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_19_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_19_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_19_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_19_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_19_BtnActionPerformed(evt);
            }
        });
        localBoard_1_Pane.add(box_19_Btn);

        globalBoardPane.add(localBoard_1_Pane);

        localBoard_2_Pane.setBackground(java.awt.Color.black);
        localBoard_2_Pane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        localBoard_2_Pane.setForeground(java.awt.Color.white);
        localBoard_2_Pane.setPreferredSize(new java.awt.Dimension(150, 150));
        localBoard_2_Pane.setLayout(new java.awt.GridLayout(3, 3));

        box_21_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_21_Btn.setText("-");
        box_21_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_21_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_21_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_21_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_21_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_21_BtnActionPerformed(evt);
            }
        });
        localBoard_2_Pane.add(box_21_Btn);

        box_22_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_22_Btn.setText("-");
        box_22_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_22_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_22_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_22_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_22_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_22_BtnActionPerformed(evt);
            }
        });
        localBoard_2_Pane.add(box_22_Btn);

        box_23_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_23_Btn.setText("-");
        box_23_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_23_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_23_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_23_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_23_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_23_BtnActionPerformed(evt);
            }
        });
        localBoard_2_Pane.add(box_23_Btn);

        box_24_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_24_Btn.setText("-");
        box_24_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_24_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_24_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_24_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_24_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_24_BtnActionPerformed(evt);
            }
        });
        localBoard_2_Pane.add(box_24_Btn);

        box_25_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_25_Btn.setText("-");
        box_25_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_25_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_25_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_25_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_25_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_25_BtnActionPerformed(evt);
            }
        });
        localBoard_2_Pane.add(box_25_Btn);

        box_26_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_26_Btn.setText("-");
        box_26_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_26_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_26_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_26_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_26_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_26_BtnActionPerformed(evt);
            }
        });
        localBoard_2_Pane.add(box_26_Btn);

        box_27_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_27_Btn.setText("-");
        box_27_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_27_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_27_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_27_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_27_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_27_BtnActionPerformed(evt);
            }
        });
        localBoard_2_Pane.add(box_27_Btn);

        box_28_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_28_Btn.setText("-");
        box_28_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_28_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_28_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_28_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_28_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_28_BtnActionPerformed(evt);
            }
        });
        localBoard_2_Pane.add(box_28_Btn);

        box_29_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_29_Btn.setText("-");
        box_29_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_29_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_29_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_29_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_29_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_29_BtnActionPerformed(evt);
            }
        });
        localBoard_2_Pane.add(box_29_Btn);

        globalBoardPane.add(localBoard_2_Pane);

        localBoard_3_Pane.setBackground(java.awt.Color.black);
        localBoard_3_Pane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        localBoard_3_Pane.setForeground(java.awt.Color.white);
        localBoard_3_Pane.setPreferredSize(new java.awt.Dimension(150, 150));
        localBoard_3_Pane.setLayout(new java.awt.GridLayout(3, 3));

        box_31_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_31_Btn.setText("-");
        box_31_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_31_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_31_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_31_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_31_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_31_BtnActionPerformed(evt);
            }
        });
        localBoard_3_Pane.add(box_31_Btn);

        box_32_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_32_Btn.setText("-");
        box_32_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_32_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_32_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_32_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_32_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_32_BtnActionPerformed(evt);
            }
        });
        localBoard_3_Pane.add(box_32_Btn);

        box_33_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_33_Btn.setText("-");
        box_33_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_33_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_33_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_33_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_33_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_33_BtnActionPerformed(evt);
            }
        });
        localBoard_3_Pane.add(box_33_Btn);

        box_34_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_34_Btn.setText("-");
        box_34_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_34_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_34_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_34_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_34_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_34_BtnActionPerformed(evt);
            }
        });
        localBoard_3_Pane.add(box_34_Btn);

        box_35_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_35_Btn.setText("-");
        box_35_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_35_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_35_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_35_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_35_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_35_BtnActionPerformed(evt);
            }
        });
        localBoard_3_Pane.add(box_35_Btn);

        box_36_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_36_Btn.setText("-");
        box_36_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_36_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_36_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_36_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_36_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_36_BtnActionPerformed(evt);
            }
        });
        localBoard_3_Pane.add(box_36_Btn);

        box_37_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_37_Btn.setText("-");
        box_37_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_37_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_37_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_37_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_37_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_37_BtnActionPerformed(evt);
            }
        });
        localBoard_3_Pane.add(box_37_Btn);

        box_38_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_38_Btn.setText("-");
        box_38_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_38_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_38_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_38_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_38_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_38_BtnActionPerformed(evt);
            }
        });
        localBoard_3_Pane.add(box_38_Btn);

        box_39_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_39_Btn.setText("-");
        box_39_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_39_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_39_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_39_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_39_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_39_BtnActionPerformed(evt);
            }
        });
        localBoard_3_Pane.add(box_39_Btn);

        globalBoardPane.add(localBoard_3_Pane);

        localBoard_4_Pane.setBackground(java.awt.Color.black);
        localBoard_4_Pane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        localBoard_4_Pane.setForeground(java.awt.Color.white);
        localBoard_4_Pane.setPreferredSize(new java.awt.Dimension(150, 150));
        localBoard_4_Pane.setLayout(new java.awt.GridLayout(3, 3));

        box_41_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_41_Btn.setText("-");
        box_41_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_41_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_41_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_41_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_41_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_41_BtnActionPerformed(evt);
            }
        });
        localBoard_4_Pane.add(box_41_Btn);

        box_42_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_42_Btn.setText("-");
        box_42_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_42_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_42_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_42_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_42_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_42_BtnActionPerformed(evt);
            }
        });
        localBoard_4_Pane.add(box_42_Btn);

        box_43_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_43_Btn.setText("-");
        box_43_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_43_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_43_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_43_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_43_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_43_BtnActionPerformed(evt);
            }
        });
        localBoard_4_Pane.add(box_43_Btn);

        box_44_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_44_Btn.setText("-");
        box_44_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_44_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_44_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_44_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_44_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_44_BtnActionPerformed(evt);
            }
        });
        localBoard_4_Pane.add(box_44_Btn);

        box_45_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_45_Btn.setText("-");
        box_45_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_45_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_45_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_45_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_45_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_45_BtnActionPerformed(evt);
            }
        });
        localBoard_4_Pane.add(box_45_Btn);

        box_46_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_46_Btn.setText("-");
        box_46_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_46_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_46_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_46_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_46_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_46_BtnActionPerformed(evt);
            }
        });
        localBoard_4_Pane.add(box_46_Btn);

        box_47_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_47_Btn.setText("-");
        box_47_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_47_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_47_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_47_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_47_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_47_BtnActionPerformed(evt);
            }
        });
        localBoard_4_Pane.add(box_47_Btn);

        box_48_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_48_Btn.setText("-");
        box_48_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_48_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_48_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_48_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_48_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_48_BtnActionPerformed(evt);
            }
        });
        localBoard_4_Pane.add(box_48_Btn);

        box_49_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_49_Btn.setText("-");
        box_49_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_49_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_49_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_49_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_49_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_49_BtnActionPerformed(evt);
            }
        });
        localBoard_4_Pane.add(box_49_Btn);

        globalBoardPane.add(localBoard_4_Pane);

        localBoard_5_Pane.setBackground(java.awt.Color.black);
        localBoard_5_Pane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        localBoard_5_Pane.setForeground(java.awt.Color.white);
        localBoard_5_Pane.setPreferredSize(new java.awt.Dimension(150, 150));
        localBoard_5_Pane.setLayout(new java.awt.GridLayout(3, 3));

        box_51_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_51_Btn.setText("-");
        box_51_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_51_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_51_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_51_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_51_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_51_BtnActionPerformed(evt);
            }
        });
        localBoard_5_Pane.add(box_51_Btn);

        box_52_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_52_Btn.setText("-");
        box_52_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_52_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_52_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_52_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_52_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_52_BtnActionPerformed(evt);
            }
        });
        localBoard_5_Pane.add(box_52_Btn);

        box_53_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_53_Btn.setText("-");
        box_53_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_53_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_53_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_53_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_53_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_53_BtnActionPerformed(evt);
            }
        });
        localBoard_5_Pane.add(box_53_Btn);

        box_54_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_54_Btn.setText("-");
        box_54_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_54_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_54_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_54_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_54_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_54_BtnActionPerformed(evt);
            }
        });
        localBoard_5_Pane.add(box_54_Btn);

        box_55_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_55_Btn.setText("-");
        box_55_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_55_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_55_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_55_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_55_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_55_BtnActionPerformed(evt);
            }
        });
        localBoard_5_Pane.add(box_55_Btn);

        box_56_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_56_Btn.setText("-");
        box_56_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_56_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_56_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_56_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_56_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_56_BtnActionPerformed(evt);
            }
        });
        localBoard_5_Pane.add(box_56_Btn);

        box_57_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_57_Btn.setText("-");
        box_57_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_57_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_57_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_57_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_57_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_57_BtnActionPerformed(evt);
            }
        });
        localBoard_5_Pane.add(box_57_Btn);

        box_58_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_58_Btn.setText("-");
        box_58_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_58_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_58_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_58_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_58_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_58_BtnActionPerformed(evt);
            }
        });
        localBoard_5_Pane.add(box_58_Btn);

        box_59_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_59_Btn.setText("-");
        box_59_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_59_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_59_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_59_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_59_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_59_BtnActionPerformed(evt);
            }
        });
        localBoard_5_Pane.add(box_59_Btn);

        globalBoardPane.add(localBoard_5_Pane);

        localBoard_6_Pane.setBackground(java.awt.Color.black);
        localBoard_6_Pane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        localBoard_6_Pane.setForeground(java.awt.Color.white);
        localBoard_6_Pane.setPreferredSize(new java.awt.Dimension(150, 150));
        localBoard_6_Pane.setLayout(new java.awt.GridLayout(3, 3));

        box_61_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_61_Btn.setText("-");
        box_61_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_61_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_61_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_61_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_61_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_61_BtnActionPerformed(evt);
            }
        });
        localBoard_6_Pane.add(box_61_Btn);

        box_62_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_62_Btn.setText("-");
        box_62_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_62_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_62_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_62_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_62_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_62_BtnActionPerformed(evt);
            }
        });
        localBoard_6_Pane.add(box_62_Btn);

        box_63_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_63_Btn.setText("-");
        box_63_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_63_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_63_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_63_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_63_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_63_BtnActionPerformed(evt);
            }
        });
        localBoard_6_Pane.add(box_63_Btn);

        box_64_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_64_Btn.setText("-");
        box_64_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_64_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_64_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_64_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_64_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_64_BtnActionPerformed(evt);
            }
        });
        localBoard_6_Pane.add(box_64_Btn);

        box_65_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_65_Btn.setText("-");
        box_65_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_65_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_65_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_65_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_65_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_65_BtnActionPerformed(evt);
            }
        });
        localBoard_6_Pane.add(box_65_Btn);

        box_66_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_66_Btn.setText("-");
        box_66_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_66_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_66_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_66_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_66_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_66_BtnActionPerformed(evt);
            }
        });
        localBoard_6_Pane.add(box_66_Btn);

        box_67_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_67_Btn.setText("-");
        box_67_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_67_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_67_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_67_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_67_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_67_BtnActionPerformed(evt);
            }
        });
        localBoard_6_Pane.add(box_67_Btn);

        box_68_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_68_Btn.setText("-");
        box_68_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_68_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_68_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_68_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_68_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_68_BtnActionPerformed(evt);
            }
        });
        localBoard_6_Pane.add(box_68_Btn);

        box_69_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_69_Btn.setText("-");
        box_69_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_69_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_69_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_69_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_69_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_69_BtnActionPerformed(evt);
            }
        });
        localBoard_6_Pane.add(box_69_Btn);

        globalBoardPane.add(localBoard_6_Pane);

        localBoard_7_Pane.setBackground(java.awt.Color.black);
        localBoard_7_Pane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        localBoard_7_Pane.setForeground(java.awt.Color.white);
        localBoard_7_Pane.setPreferredSize(new java.awt.Dimension(150, 150));
        localBoard_7_Pane.setLayout(new java.awt.GridLayout(3, 3));

        box_71_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_71_Btn.setText("-");
        box_71_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_71_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_71_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_71_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_71_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_71_BtnActionPerformed(evt);
            }
        });
        localBoard_7_Pane.add(box_71_Btn);

        box_72_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_72_Btn.setText("-");
        box_72_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_72_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_72_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_72_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_72_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_72_BtnActionPerformed(evt);
            }
        });
        localBoard_7_Pane.add(box_72_Btn);

        box_73_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_73_Btn.setText("-");
        box_73_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_73_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_73_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_73_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_73_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_73_BtnActionPerformed(evt);
            }
        });
        localBoard_7_Pane.add(box_73_Btn);

        box_74_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_74_Btn.setText("-");
        box_74_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_74_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_74_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_74_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_74_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_74_BtnActionPerformed(evt);
            }
        });
        localBoard_7_Pane.add(box_74_Btn);

        box_75_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_75_Btn.setText("-");
        box_75_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_75_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_75_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_75_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_75_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_75_BtnActionPerformed(evt);
            }
        });
        localBoard_7_Pane.add(box_75_Btn);

        box_76_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_76_Btn.setText("-");
        box_76_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_76_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_76_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_76_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_76_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_76_BtnActionPerformed(evt);
            }
        });
        localBoard_7_Pane.add(box_76_Btn);

        box_77_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_77_Btn.setText("-");
        box_77_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_77_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_77_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_77_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_77_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_77_BtnActionPerformed(evt);
            }
        });
        localBoard_7_Pane.add(box_77_Btn);

        box_78_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_78_Btn.setText("-");
        box_78_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_78_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_78_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_78_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_78_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_78_BtnActionPerformed(evt);
            }
        });
        localBoard_7_Pane.add(box_78_Btn);

        box_79_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_79_Btn.setText("-");
        box_79_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_79_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_79_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_79_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_79_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_79_BtnActionPerformed(evt);
            }
        });
        localBoard_7_Pane.add(box_79_Btn);

        globalBoardPane.add(localBoard_7_Pane);

        localBoard_8_Pane.setBackground(java.awt.Color.black);
        localBoard_8_Pane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        localBoard_8_Pane.setForeground(java.awt.Color.white);
        localBoard_8_Pane.setPreferredSize(new java.awt.Dimension(150, 150));
        localBoard_8_Pane.setLayout(new java.awt.GridLayout(3, 3));

        box_81_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_81_Btn.setText("-");
        box_81_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_81_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_81_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_81_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_81_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_81_BtnActionPerformed(evt);
            }
        });
        localBoard_8_Pane.add(box_81_Btn);

        box_82_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_82_Btn.setText("-");
        box_82_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_82_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_82_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_82_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_82_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_82_BtnActionPerformed(evt);
            }
        });
        localBoard_8_Pane.add(box_82_Btn);

        box_83_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_83_Btn.setText("-");
        box_83_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_83_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_83_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_83_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_83_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_83_BtnActionPerformed(evt);
            }
        });
        localBoard_8_Pane.add(box_83_Btn);

        box_84_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_84_Btn.setText("-");
        box_84_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_84_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_84_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_84_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_84_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_84_BtnActionPerformed(evt);
            }
        });
        localBoard_8_Pane.add(box_84_Btn);

        box_85_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_85_Btn.setText("-");
        box_85_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_85_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_85_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_85_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_85_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_85_BtnActionPerformed(evt);
            }
        });
        localBoard_8_Pane.add(box_85_Btn);

        box_86_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_86_Btn.setText("-");
        box_86_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_86_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_86_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_86_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_86_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_86_BtnActionPerformed(evt);
            }
        });
        localBoard_8_Pane.add(box_86_Btn);

        box_87_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_87_Btn.setText("-");
        box_87_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_87_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_87_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_87_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_87_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_87_BtnActionPerformed(evt);
            }
        });
        localBoard_8_Pane.add(box_87_Btn);

        box_88_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_88_Btn.setText("-");
        box_88_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_88_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_88_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_88_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_88_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_88_BtnActionPerformed(evt);
            }
        });
        localBoard_8_Pane.add(box_88_Btn);

        box_89_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_89_Btn.setText("-");
        box_89_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_89_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_89_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_89_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_89_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_89_BtnActionPerformed(evt);
            }
        });
        localBoard_8_Pane.add(box_89_Btn);

        globalBoardPane.add(localBoard_8_Pane);

        localBoard_9_Pane.setBackground(java.awt.Color.black);
        localBoard_9_Pane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        localBoard_9_Pane.setForeground(java.awt.Color.white);
        localBoard_9_Pane.setPreferredSize(new java.awt.Dimension(150, 150));
        localBoard_9_Pane.setLayout(new java.awt.GridLayout(3, 3));

        box_91_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_91_Btn.setText("-");
        box_91_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_91_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_91_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_91_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_91_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_91_BtnActionPerformed(evt);
            }
        });
        localBoard_9_Pane.add(box_91_Btn);

        box_92_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_92_Btn.setText("-");
        box_92_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_92_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_92_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_92_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_92_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_92_BtnActionPerformed(evt);
            }
        });
        localBoard_9_Pane.add(box_92_Btn);

        box_93_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_93_Btn.setText("-");
        box_93_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_93_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_93_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_93_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_93_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_93_BtnActionPerformed(evt);
            }
        });
        localBoard_9_Pane.add(box_93_Btn);

        box_94_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_94_Btn.setText("-");
        box_94_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_94_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_94_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_94_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_94_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_94_BtnActionPerformed(evt);
            }
        });
        localBoard_9_Pane.add(box_94_Btn);

        box_95_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_95_Btn.setText("-");
        box_95_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_95_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_95_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_95_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_95_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_95_BtnActionPerformed(evt);
            }
        });
        localBoard_9_Pane.add(box_95_Btn);

        box_96_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_96_Btn.setText("-");
        box_96_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_96_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_96_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_96_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_96_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_96_BtnActionPerformed(evt);
            }
        });
        localBoard_9_Pane.add(box_96_Btn);

        box_97_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_97_Btn.setText("-");
        box_97_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_97_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_97_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_97_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_97_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_97_BtnActionPerformed(evt);
            }
        });
        localBoard_9_Pane.add(box_97_Btn);

        box_98_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_98_Btn.setText("-");
        box_98_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_98_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_98_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_98_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_98_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_98_BtnActionPerformed(evt);
            }
        });
        localBoard_9_Pane.add(box_98_Btn);

        box_99_Btn.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
        box_99_Btn.setText("-");
        box_99_Btn.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        box_99_Btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        box_99_Btn.setMargin(new java.awt.Insets(-2, -2, -2, -2));
        box_99_Btn.setPreferredSize(new java.awt.Dimension(50, 50));
        box_99_Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                box_99_BtnActionPerformed(evt);
            }
        });
        localBoard_9_Pane.add(box_99_Btn);

        globalBoardPane.add(localBoard_9_Pane);

        jPanel1.add(globalBoardPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 180, 450, 450));

        nextTurnSignLbl.setFont(new java.awt.Font("Cantarell", 1, 20)); // NOI18N
        nextTurnSignLbl.setText("(X)");
        jPanel1.add(nextTurnSignLbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(475, 140, -1, -1));

        jLabel4.setFont(new java.awt.Font("Cantarell", 0, 18)); // NOI18N
        jLabel4.setText("It's");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 140, -1, -1));

        nextTurnPlayerNameLbl.setFont(new java.awt.Font("Cantarell", 1, 20)); // NOI18N
        nextTurnPlayerNameLbl.setText("Player 1");
        jPanel1.add(nextTurnPlayerNameLbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(385, 140, -1, -1));

        jLabel6.setFont(new java.awt.Font("Cantarell", 0, 18)); // NOI18N
        jLabel6.setText("Turn!");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 140, -1, -1));

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Player 1 Name ");
        jLabel9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel9.setPreferredSize(new java.awt.Dimension(150, 20));
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 250, -1, -1));

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Player 1 Score");
        jLabel10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel10.setPreferredSize(new java.awt.Dimension(150, 20));
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 420, -1, -1));

        player1ScoreTF.setEditable(false);
        player1ScoreTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(player1ScoreTF, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 450, 180, 40));

        player1NameTF.setEditable(false);
        player1NameTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(player1NameTF, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 280, 180, 40));

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Player 2 Name ");
        jLabel11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel11.setPreferredSize(new java.awt.Dimension(150, 20));
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 250, -1, -1));

        player2NameTF.setEditable(false);
        player2NameTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(player2NameTF, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 280, 180, 40));

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Player 2 Score");
        jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel12.setPreferredSize(new java.awt.Dimension(150, 20));
        jPanel1.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 420, -1, -1));

        player2ScoreTF.setEditable(false);
        player2ScoreTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(player2ScoreTF, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 450, 180, 40));

        homeBtn.setText("Home");
        homeBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        homeBtn.setPreferredSize(new java.awt.Dimension(100, 30));
        homeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeBtnActionPerformed(evt);
            }
        });
        jPanel1.add(homeBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 620, -1, -1));

        restartBtn.setText("Reload");
        restartBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        restartBtn.setPreferredSize(new java.awt.Dimension(100, 30));
        restartBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restartBtnActionPerformed(evt);
            }
        });
        jPanel1.add(restartBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 620, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void box_11_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_11_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_11_BtnActionPerformed

    private void box_12_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_12_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_12_BtnActionPerformed

    private void box_13_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_13_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_13_BtnActionPerformed

    private void box_14_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_14_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_14_BtnActionPerformed

    private void box_15_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_15_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_15_BtnActionPerformed

    private void box_16_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_16_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_16_BtnActionPerformed

    private void box_17_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_17_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_17_BtnActionPerformed

    private void box_18_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_18_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_18_BtnActionPerformed

    private void box_19_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_19_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_19_BtnActionPerformed

    private void box_21_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_21_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_21_BtnActionPerformed

    private void box_22_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_22_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_22_BtnActionPerformed

    private void box_23_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_23_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_23_BtnActionPerformed

    private void box_24_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_24_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_24_BtnActionPerformed

    private void box_25_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_25_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_25_BtnActionPerformed

    private void box_26_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_26_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_26_BtnActionPerformed

    private void box_27_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_27_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_27_BtnActionPerformed

    private void box_28_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_28_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_28_BtnActionPerformed

    private void box_29_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_29_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_29_BtnActionPerformed

    private void box_31_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_31_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_31_BtnActionPerformed

    private void box_32_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_32_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_32_BtnActionPerformed

    private void box_33_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_33_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_33_BtnActionPerformed

    private void box_34_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_34_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_34_BtnActionPerformed

    private void box_35_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_35_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_35_BtnActionPerformed

    private void box_36_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_36_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_36_BtnActionPerformed

    private void box_37_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_37_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_37_BtnActionPerformed

    private void box_38_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_38_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_38_BtnActionPerformed

    private void box_39_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_39_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_39_BtnActionPerformed

    private void box_41_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_41_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_41_BtnActionPerformed

    private void box_42_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_42_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_42_BtnActionPerformed

    private void box_43_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_43_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_43_BtnActionPerformed

    private void box_44_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_44_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_44_BtnActionPerformed

    private void box_45_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_45_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_45_BtnActionPerformed

    private void box_46_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_46_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_46_BtnActionPerformed

    private void box_47_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_47_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_47_BtnActionPerformed

    private void box_48_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_48_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_48_BtnActionPerformed

    private void box_49_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_49_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_49_BtnActionPerformed

    private void box_51_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_51_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_51_BtnActionPerformed

    private void box_52_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_52_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_52_BtnActionPerformed

    private void box_53_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_53_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_53_BtnActionPerformed

    private void box_54_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_54_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_54_BtnActionPerformed

    private void box_55_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_55_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_55_BtnActionPerformed

    private void box_56_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_56_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_56_BtnActionPerformed

    private void box_57_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_57_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_57_BtnActionPerformed

    private void box_58_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_58_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_58_BtnActionPerformed

    private void box_59_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_59_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_59_BtnActionPerformed

    private void box_61_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_61_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_61_BtnActionPerformed

    private void box_62_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_62_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_62_BtnActionPerformed

    private void box_63_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_63_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_63_BtnActionPerformed

    private void box_64_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_64_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_64_BtnActionPerformed

    private void box_65_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_65_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_65_BtnActionPerformed

    private void box_66_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_66_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_66_BtnActionPerformed

    private void box_67_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_67_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_67_BtnActionPerformed

    private void box_68_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_68_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_68_BtnActionPerformed

    private void box_69_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_69_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_69_BtnActionPerformed

    private void box_71_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_71_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_71_BtnActionPerformed

    private void box_72_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_72_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_72_BtnActionPerformed

    private void box_73_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_73_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_73_BtnActionPerformed

    private void box_74_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_74_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_74_BtnActionPerformed

    private void box_75_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_75_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_75_BtnActionPerformed

    private void box_76_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_76_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_76_BtnActionPerformed

    private void box_77_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_77_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_77_BtnActionPerformed

    private void box_78_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_78_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_78_BtnActionPerformed

    private void box_79_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_79_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_79_BtnActionPerformed

    private void box_81_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_81_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_81_BtnActionPerformed

    private void box_82_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_82_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_82_BtnActionPerformed

    private void box_83_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_83_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_83_BtnActionPerformed

    private void box_84_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_84_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_84_BtnActionPerformed

    private void box_85_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_85_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_85_BtnActionPerformed

    private void box_86_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_86_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_86_BtnActionPerformed

    private void box_87_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_87_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_87_BtnActionPerformed

    private void box_88_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_88_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_88_BtnActionPerformed

    private void box_89_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_89_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_89_BtnActionPerformed

    private void box_91_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_91_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_91_BtnActionPerformed

    private void box_92_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_92_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_92_BtnActionPerformed

    private void box_93_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_93_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_93_BtnActionPerformed

    private void box_94_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_94_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_94_BtnActionPerformed

    private void box_95_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_95_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_95_BtnActionPerformed

    private void box_96_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_96_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_96_BtnActionPerformed

    private void box_97_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_97_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_97_BtnActionPerformed

    private void box_98_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_98_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_98_BtnActionPerformed

    private void box_99_BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_box_99_BtnActionPerformed
        for (int i = 0; i < lbCount; i++) {
            for (int j = 0; j < boxCount; j++) {
                if (evt.getSource() == (JButton) this.localBoards.get(i).get(j)) {
                    int boxNumber = ((i + 1) * 10) + (j + 1);
                    box_Btn_onAction("box_" + boxNumber + "_Btn");
                }
            }
        }
    }//GEN-LAST:event_box_99_BtnActionPerformed

    private void homeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeBtnActionPerformed
        Home home = new Home();
        home.setTitle("Tic Tac Toe Ulimate ~ Home");
        this.setVisible(false);
        home.setVisible(true);
    }//GEN-LAST:event_homeBtnActionPerformed

    private void restartBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restartBtnActionPerformed
        try {
            int res = JOptionPane.showConfirmDialog(
                    rootPane,
                    "Are You Sure to Restart Multiplayer Game Play ?",
                    "Restart Game?",
                    JOptionPane.OK_CANCEL_OPTION
            );
            if (res == JOptionPane.OK_OPTION) {
                newMPGamePlay();
            }
        } catch (Exception ex) {
            printDBErrorAndExit(ex);
        }
    }//GEN-LAST:event_restartBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton box_11_Btn;
    private javax.swing.JButton box_12_Btn;
    private javax.swing.JButton box_13_Btn;
    private javax.swing.JButton box_14_Btn;
    private javax.swing.JButton box_15_Btn;
    private javax.swing.JButton box_16_Btn;
    private javax.swing.JButton box_17_Btn;
    private javax.swing.JButton box_18_Btn;
    private javax.swing.JButton box_19_Btn;
    private javax.swing.JButton box_21_Btn;
    private javax.swing.JButton box_22_Btn;
    private javax.swing.JButton box_23_Btn;
    private javax.swing.JButton box_24_Btn;
    private javax.swing.JButton box_25_Btn;
    private javax.swing.JButton box_26_Btn;
    private javax.swing.JButton box_27_Btn;
    private javax.swing.JButton box_28_Btn;
    private javax.swing.JButton box_29_Btn;
    private javax.swing.JButton box_31_Btn;
    private javax.swing.JButton box_32_Btn;
    private javax.swing.JButton box_33_Btn;
    private javax.swing.JButton box_34_Btn;
    private javax.swing.JButton box_35_Btn;
    private javax.swing.JButton box_36_Btn;
    private javax.swing.JButton box_37_Btn;
    private javax.swing.JButton box_38_Btn;
    private javax.swing.JButton box_39_Btn;
    private javax.swing.JButton box_41_Btn;
    private javax.swing.JButton box_42_Btn;
    private javax.swing.JButton box_43_Btn;
    private javax.swing.JButton box_44_Btn;
    private javax.swing.JButton box_45_Btn;
    private javax.swing.JButton box_46_Btn;
    private javax.swing.JButton box_47_Btn;
    private javax.swing.JButton box_48_Btn;
    private javax.swing.JButton box_49_Btn;
    private javax.swing.JButton box_51_Btn;
    private javax.swing.JButton box_52_Btn;
    private javax.swing.JButton box_53_Btn;
    private javax.swing.JButton box_54_Btn;
    private javax.swing.JButton box_55_Btn;
    private javax.swing.JButton box_56_Btn;
    private javax.swing.JButton box_57_Btn;
    private javax.swing.JButton box_58_Btn;
    private javax.swing.JButton box_59_Btn;
    private javax.swing.JButton box_61_Btn;
    private javax.swing.JButton box_62_Btn;
    private javax.swing.JButton box_63_Btn;
    private javax.swing.JButton box_64_Btn;
    private javax.swing.JButton box_65_Btn;
    private javax.swing.JButton box_66_Btn;
    private javax.swing.JButton box_67_Btn;
    private javax.swing.JButton box_68_Btn;
    private javax.swing.JButton box_69_Btn;
    private javax.swing.JButton box_71_Btn;
    private javax.swing.JButton box_72_Btn;
    private javax.swing.JButton box_73_Btn;
    private javax.swing.JButton box_74_Btn;
    private javax.swing.JButton box_75_Btn;
    private javax.swing.JButton box_76_Btn;
    private javax.swing.JButton box_77_Btn;
    private javax.swing.JButton box_78_Btn;
    private javax.swing.JButton box_79_Btn;
    private javax.swing.JButton box_81_Btn;
    private javax.swing.JButton box_82_Btn;
    private javax.swing.JButton box_83_Btn;
    private javax.swing.JButton box_84_Btn;
    private javax.swing.JButton box_85_Btn;
    private javax.swing.JButton box_86_Btn;
    private javax.swing.JButton box_87_Btn;
    private javax.swing.JButton box_88_Btn;
    private javax.swing.JButton box_89_Btn;
    private javax.swing.JButton box_91_Btn;
    private javax.swing.JButton box_92_Btn;
    private javax.swing.JButton box_93_Btn;
    private javax.swing.JButton box_94_Btn;
    private javax.swing.JButton box_95_Btn;
    private javax.swing.JButton box_96_Btn;
    private javax.swing.JButton box_97_Btn;
    private javax.swing.JButton box_98_Btn;
    private javax.swing.JButton box_99_Btn;
    private javax.swing.JPanel globalBoardPane;
    private javax.swing.JButton homeBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel localBoard_1_Pane;
    private javax.swing.JPanel localBoard_2_Pane;
    private javax.swing.JPanel localBoard_3_Pane;
    private javax.swing.JPanel localBoard_4_Pane;
    private javax.swing.JPanel localBoard_5_Pane;
    private javax.swing.JPanel localBoard_6_Pane;
    private javax.swing.JPanel localBoard_7_Pane;
    private javax.swing.JPanel localBoard_8_Pane;
    private javax.swing.JPanel localBoard_9_Pane;
    private javax.swing.JLabel nextTurnPlayerNameLbl;
    private javax.swing.JLabel nextTurnSignLbl;
    private javax.swing.JTextField player1NameTF;
    private javax.swing.JTextField player1ScoreTF;
    private javax.swing.JTextField player2NameTF;
    private javax.swing.JTextField player2ScoreTF;
    private javax.swing.JButton restartBtn;
    // End of variables declaration//GEN-END:variables
}
