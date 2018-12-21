/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LongMessage.java
 *
 * Created on 28/10/2013, 12:03:49
 */

package br.unisinos.simcop.builder.gui;

import java.awt.Dimension;

/**
 *
 * @author tiago
 */
public class LongMessage extends javax.swing.JDialog {

    /** Creates new form LongMessage */
    public LongMessage(java.awt.Frame parent, String title, String msg) {
        super(parent, true);
        initComponents();
        lbTitle.setText(title);
        edMsg.setText(msg);
        Dimension d = new Dimension(800, 250);
        setSize(d);
        setPreferredSize(d);
        setLocationRelativeTo(null);        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lbTitle = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btClose = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        edMsg = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SimCoP Config Builder");
        setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        setMinimumSize(new java.awt.Dimension(400, 250));

        lbTitle.setText("      ");
        jPanel1.add(lbTitle);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        btClose.setText("Close");
        btClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCloseActionPerformed(evt);
            }
        });
        jPanel2.add(btClose);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

        edMsg.setColumns(20);
        edMsg.setEditable(false);
        edMsg.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        edMsg.setRows(5);
        jScrollPane1.setViewportView(edMsg);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCloseActionPerformed
            dispose();
    }//GEN-LAST:event_btCloseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btClose;
    private javax.swing.JTextArea edMsg;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbTitle;
    // End of variables declaration//GEN-END:variables

}