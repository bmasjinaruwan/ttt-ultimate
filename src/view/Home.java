/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.sql.Connection;
import db.DBConnection;
import db.DBUtil;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Thushara Supun
 */
public class Home extends javax.swing.JFrame {

    /**
     * Creates new form Home
     */
    public Home() {
        initComponents();

        System.gc();

        try {
            checkDBAvailability();
            checkRestoreBtnEnability();
        } catch (Exception ex) {
            printDBErrorAndExit(ex);
        }
    }

    private void checkDBAvailability() throws Exception {
        Connection connection = DBConnection.getInstance().getConnection();
        if (connection != null) {
            String sql = "SELECT * FROM sqlite_master WHERE type='table' AND name ='gbState'";
            ResultSet rst = DBUtil.executeQuery(sql);
            if (!rst.next()) {
                createDBTables();
                initializeDBTables();
            }
        } else {
            printDBErrorAndExit(new Exception());
        }
    }

    private void createDBTables() throws Exception {
        ArrayList<String> sqls = new ArrayList<>();

        sqls.add(
                "CREATE TABLE IF NOT EXISTS gbState("
                + "board TEXT NOT NULL,"
                + "state INTEGER NOT NULL"
                + ");"
        );

        sqls.add(
                "CREATE TABLE IF NOT EXISTS lbState("
                + "board TEXT NOT NULL,"
                + "state INTEGER NOT NULL,"
                + "available INTEGER NOT NULL"
                + ");"
        );

        sqls.add(
                "CREATE TABLE IF NOT EXISTS gbValue("
                + "b0 TEXT NOT NULL,"
                + "b1 TEXT NOT NULL,"
                + "b2 TEXT NOT NULL,"
                + "b3 TEXT NOT NULL,"
                + "b4 TEXT NOT NULL,"
                + "b5 TEXT NOT NULL,"
                + "b6 TEXT NOT NULL,"
                + "b7 TEXT NOT NULL,"
                + "b8 TEXT NOT NULL,"
                + "b9 TEXT NOT NULL"
                + ");"
        );

        sqls.add(
                "CREATE TABLE IF NOT EXISTS playerState("
                + "playerID TEXT NOT NULL,"
                + "sign TEXT NOT NULL,"
                + "state INTEGER NOT NULL"
                + ");"
        );

        sqls.add(
                "CREATE TABLE IF NOT EXISTS playerValue("
                + "playerID TEXT NOT NULL,"
                + "name TEXT NOT NULL,"
                + "score INTEGER NOT NULL"
                + ");"
        );

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

    private void checkRestoreBtnEnability() throws Exception {
        String sql = "SELECT state from gbState where board = 'gb'";
        ResultSet rst = DBUtil.executeQuery(sql);

        int gbState = 0;

        if (rst.next()) {
            gbState = rst.getInt(1);
        }

        if (gbState == 0) {
            restoreBtn.setEnabled(false);
        } else {
            restoreBtn.setEnabled(true);
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
        singlePlayerBtn = new javax.swing.JButton();
        multiPlayerBtn = new javax.swing.JButton();
        restoreBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(600, 400));
        setResizable(false);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 48)); // NOI18N
        jLabel1.setForeground(java.awt.Color.blue);
        jLabel1.setText("Tic Tac Toe ");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 30, -1, -1));

        jLabel2.setFont(new java.awt.Font("Cantarell", 3, 24)); // NOI18N
        jLabel2.setForeground(java.awt.Color.green);
        jLabel2.setText("Ultimate");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 50, -1, -1));

        singlePlayerBtn.setText("Single Player");
        singlePlayerBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        singlePlayerBtn.setPreferredSize(new java.awt.Dimension(150, 40));
        singlePlayerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singlePlayerBtnActionPerformed(evt);
            }
        });
        jPanel1.add(singlePlayerBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 170, 150, 40));

        multiPlayerBtn.setText("MultiPlayer");
        multiPlayerBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        multiPlayerBtn.setPreferredSize(new java.awt.Dimension(150, 40));
        multiPlayerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiPlayerBtnActionPerformed(evt);
            }
        });
        jPanel1.add(multiPlayerBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 170, 150, 40));

        restoreBtn.setText("Restore");
        restoreBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        restoreBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreBtnActionPerformed(evt);
            }
        });
        jPanel1.add(restoreBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 290, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void singlePlayerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singlePlayerBtnActionPerformed
        JOptionPane.showMessageDialog(rootPane, "Single Player Game Play Currently Not Available");
    }//GEN-LAST:event_singlePlayerBtnActionPerformed

    private void multiPlayerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiPlayerBtnActionPerformed
        MPConfig mpConfig = new MPConfig();
        mpConfig.setTitle("Tic Tac Toe Ulimate ~ MultiPlayer Config");
        this.setVisible(false);
        mpConfig.setVisible(true);
    }//GEN-LAST:event_multiPlayerBtnActionPerformed

    private void restoreBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreBtnActionPerformed
        try {
            String sql = "SELECT state from gbState where board = 'gb'";
            ResultSet rst = DBUtil.executeQuery(sql);

            int gbState = 0;

            if (rst.next()) {
                gbState = rst.getInt(1);
            }

            if (gbState == 1) {
                singlePlayerBtn.doClick();
            } else if (gbState == 2) {
                MPGamePlay mpGamePlay = new MPGamePlay();
                mpGamePlay.setTitle("Tic Tac Toe Ulimate ~ MultiPlayer GamePlay");
                this.setVisible(false);
                mpGamePlay.setVisible(true);
            }
        } catch (Exception ex) {
            printDBErrorAndExit(ex);
        }
    }//GEN-LAST:event_restoreBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton multiPlayerBtn;
    private javax.swing.JButton restoreBtn;
    private javax.swing.JButton singlePlayerBtn;
    // End of variables declaration//GEN-END:variables
}
