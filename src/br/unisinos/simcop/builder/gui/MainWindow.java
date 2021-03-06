/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainWindow.java
 *
 * Created on 26/10/2013, 13:31:37
 */
package br.unisinos.simcop.builder.gui;

import br.unisinos.simcop.builder.repository.KnowClasses;
import br.unisinos.simcop.core.config.ISimcopConfig;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;

/**
 *
 * @author tiago
 */
public class MainWindow extends javax.swing.JFrame {

    private KnowClasses knowClasses;
    private MruList mruList;

    /** Creates new form MainWindow */
    public MainWindow() {
        Dimension d = new Dimension(1200, 700);
        setPreferredSize(d);
        setSize(d);
        setLocationRelativeTo(null);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlFooter = new javax.swing.JPanel();
        desktop = new javax.swing.JDesktopPane();
        mainMenu = new javax.swing.JMenuBar();
        mnuStore = new javax.swing.JMenu();
        mnuStoreNew = new javax.swing.JMenu();
        mnuStoreOpen = new javax.swing.JMenu();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuClose = new javax.swing.JMenuItem();
        mnuWindow = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Simcop Process Builder");

        pnlFooter.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        pnlFooter.setMaximumSize(new java.awt.Dimension(15, 15));
        pnlFooter.setMinimumSize(new java.awt.Dimension(15, 15));
        pnlFooter.setPreferredSize(new java.awt.Dimension(15, 15));

        javax.swing.GroupLayout pnlFooterLayout = new javax.swing.GroupLayout(pnlFooter);
        pnlFooter.setLayout(pnlFooterLayout);
        pnlFooterLayout.setHorizontalGroup(
            pnlFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 396, Short.MAX_VALUE)
        );
        pnlFooterLayout.setVerticalGroup(
            pnlFooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 11, Short.MAX_VALUE)
        );

        getContentPane().add(pnlFooter, java.awt.BorderLayout.PAGE_END);
        getContentPane().add(desktop, java.awt.BorderLayout.CENTER);

        mnuStore.setText("Config Store");

        mnuStoreNew.setText("New");
        mnuStore.add(mnuStoreNew);

        mnuStoreOpen.setText("Open");
        mnuStore.add(mnuStoreOpen);
        mnuStore.add(jSeparator1);

        mnuClose.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        mnuClose.setText("Exit");
        mnuClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCloseActionPerformed(evt);
            }
        });
        mnuStore.add(mnuClose);

        mainMenu.add(mnuStore);

        mnuWindow.setText("Window");
        mainMenu.add(mnuWindow);

        setJMenuBar(mainMenu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mnuCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCloseActionPerformed
        dispose();
}//GEN-LAST:event_mnuCloseActionPerformed

    public KnowClasses getKnowClasses() {
        return knowClasses;
    }

    public void setKnowClasses(KnowClasses knowClasses) {
        this.knowClasses = knowClasses;
        refreshMenus();
    }

    public void refreshMenus() {

        for (ISimcopConfig store : knowClasses.getForConfigStore()) {
            JMenuItem itemNew = new JMenuItem(store.getClass().getSimpleName());
            itemNew.addActionListener(new StoreAction(this, store, false));

            JMenuItem itemOpen = new JMenuItem(store.getClass().getSimpleName());
            itemOpen.addActionListener(new StoreAction(this, store, true));

            mnuStoreNew.add(itemNew);
            mnuStoreOpen.add(itemOpen);
        }
        mainMenu.updateUI();
    }

    public MruList getMruList() {
        return mruList;
    }

    public void setMruList(MruList mruList) {
        this.mruList = mruList;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JMenuItem mnuClose;
    private javax.swing.JMenu mnuStore;
    private javax.swing.JMenu mnuStoreNew;
    private javax.swing.JMenu mnuStoreOpen;
    private javax.swing.JMenu mnuWindow;
    private javax.swing.JPanel pnlFooter;
    // End of variables declaration//GEN-END:variables

    class StoreAction implements ActionListener {

        ISimcopConfig store;
        boolean open;
        MainWindow mainWindow;

        public StoreAction(MainWindow mainWindow, ISimcopConfig store, boolean open) {
            this.store = store.clone();
            this.open = open;
            this.mainWindow = mainWindow;
        }

        public void actionPerformed(ActionEvent e) {
            JInternalFrame window = null;
            if (open) {
                StoreOpen openWindow = new StoreOpen(mainWindow, store, mruList);
                openWindow.setVisible(true);
                if (openWindow.getStoreSelected() != null) {
                    ISimcopConfig configStore = openWindow.getStoreSelected();
                    window = new EditConfigStore(mainWindow, configStore, false);
                    mruList.store(configStore.getClass().getName(), configStore.getName(), configStore.getParameters());
                }
            } else {
                window = new EditConfigStore(mainWindow, store, true);
            }

            if (window != null) {
                window.setVisible(true);
                desktop.add(window);
                try {
                    window.setSelected(true);
                } catch (PropertyVetoException ex) {
                }
            }
        }
    }
}
