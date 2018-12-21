/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unisinos.simcop.builder.gui;

import br.unisinos.simcop.data.model.Entity;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author tiago
 */
class EntitiesModel extends DefaultTableModel {
    private final String[] columns = new String[]{"UID", "Name"};
    List<Entity> entities;

    public EntitiesModel(List<Entity> entities) {
        this.entities = entities;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return columns != null ? columns.length : 1;
    }

    @Override
    public int getRowCount() {
        return entities != null ? entities.size() : 1;
    }

    @Override
    public String getColumnName(int column) {
        if (columns != null) {
            return columns[column];
        } else {
            return "";
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (entities != null && row >= 0 && row < entities.size()) {
            Entity entity = entities.get(row);
            if (entity != null) {
                switch (column) {
                    case 0:
                        return entity.getUid();
                    case 1:
                        return entity.getName();
                }
            }
        }
        return "";
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
    
}
