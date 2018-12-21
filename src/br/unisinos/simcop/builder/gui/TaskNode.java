package br.unisinos.simcop.builder.gui;

import br.unisinos.simcop.Utils;
import br.unisinos.simcop.core.process.SimcopTask;

/**
 *
 * @author tiago
 */
public class TaskNode {

    private String label;
    private SimcopTask task;

    public TaskNode(SimcopTask task) {
        this.task = task;
        if (task != null) {
            label = task.getSequence() + ". ";
            if (task.getType() == SimcopTask.CONTEXT_SIMILARITY) {
                label = "Context: ";
            } else if (!Utils.isEmpty(task.getForAttribute())) {
                label = task.getForAttribute() + ": ";
            }
            label = label + (!Utils.isEmpty(task.getLabel()) ? task.getLabel() : task.getClassName());
        } else {
            label = "Not Available";
        }
    }

    @Override
    public String toString() {
        return label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public SimcopTask getTask() {
        return task;
    }

    public void setTask(SimcopTask task) {
        this.task = task;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskNode other = (TaskNode) obj;
        if (this.task != other.task && (this.task == null || !this.task.equals(other.task))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.task != null ? this.task.hashCode() : 0);
        return hash;
    }
}
