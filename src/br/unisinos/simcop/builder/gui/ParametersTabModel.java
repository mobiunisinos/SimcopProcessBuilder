package br.unisinos.simcop.builder.gui;

import br.unisinos.simcop.core.Parameter;
import br.unisinos.simcop.core.Parameters;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author tiago
 */
public class ParametersTabModel extends DefaultTableModel {

    private Parameters parameters;
    private List<Parameter> lstParameters;

    public static void forTable(JTable table, Parameters parameters) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(new ParametersTabModel(parameters));
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(2).setPreferredWidth(75);
        table.updateUI();


        table.addKeyListener(new TabParKeyListener(table));
    }

    public ParametersTabModel(Parameters parameters) {
        this.parameters = parameters;
        refreshList();
    }

    private void refreshList() {
        lstParameters = new ArrayList<Parameter>();
        for (String key : parameters.keySet()) {
            lstParameters.add(parameters.get(key));
        }
    }

    @Override
    public int getRowCount() {
        return lstParameters != null ? lstParameters.size() : 1;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Parameter";
            case 1:
                return "Value";
            case 2:
                return "Format";
        }
        return "";
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row >= 0 && row < lstParameters.size() && column >= 0 && column < 3) {
            Parameter par = lstParameters.get(row);
            switch (column) {
                case 0:
                    return par.getKey();
                case 1:
                    return par.getValue();
                case 2:
                    return par.getPattern();
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        if (row >= 0 && row < lstParameters.size() && column >= 0 && column < 3) {
            Parameter par = lstParameters.get(row);
            switch (column) {
                case 0:
                    par.setKey(aValue != null ? aValue.toString() : "");
                    break;
                case 1:
                    par.setValue(aValue != null ? aValue.toString() : "");
                    break;
                case 2:
                    par.setPattern(aValue != null ? aValue.toString() : "");
                    break;
            }
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    static class TabParKeyListener implements java.awt.event.KeyListener {

        private JTable table;
        private boolean active;

        public TabParKeyListener(JTable table) {
            this.table = table;
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            if (active) {
                ParametersTabModel model = (ParametersTabModel) table.getModel();
                int row = table.getSelectedRow();
                if (model.lstParameters != null && row >= 0 && row < model.lstParameters.size()) {

                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_DELETE:
                            model.lstParameters.remove(row);
                            break;
                        case KeyEvent.VK_ENTER:
                            Parameter current = model.lstParameters.get(row);
                            Parameter newPar = new Parameter();
                            if (current != null) {
                                newPar.setKey(current.getKey());
                                newPar.setPattern(current.getPattern());
                                newPar.setValue("");                                
                            }
                            model.lstParameters.add(row+1, newPar);
                            
                            break;
                    }
                    table.updateUI();
                }
            }

            if (e.getExtendedKeyCode() == KeyEvent.VK_CONTROL) {
                active = true;
            }
        }

        public void keyReleased(KeyEvent e) {
            if (e.getExtendedKeyCode() == KeyEvent.VK_CONTROL) {
                active = false;
            }
        }
    }
}
