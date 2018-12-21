package br.unisinos.simcop.builder.gui;

import br.unisinos.simcop.core.config.ISimcopConfig;
import br.unisinos.simcop.core.process.SimcopTask;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author tiago
 */
public class ProcessTreeModel extends DefaultTreeModel {

    private ISimcopConfig config;
    private DefaultMutableTreeNode preNode;
    private DefaultMutableTreeNode simNode;
    private DefaultMutableTreeNode posNode;

    public static void loadFor(ISimcopConfig config, JTree tree) {
        ProcessTreeModel model = new ProcessTreeModel(null);
        model.config = config;
        tree.setModel(model);
        model.refresh(tree);
    }

    public ProcessTreeModel(TreeNode root) {
        super(root);
    }

    public ProcessTreeModel(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }

    public void refresh(JTree tree) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("SimCoP Process");
        preNode = new DefaultMutableTreeNode("Pre Processing");
        simNode = new DefaultMutableTreeNode("Similarity Analysis");
        posNode = new DefaultMutableTreeNode("Post Processing");
        rootNode.add(preNode);
        rootNode.add(simNode);
        rootNode.add(posNode);

        for (SimcopTask task : config.getProcessTasks()) {
            switch (task.getType()) {
                case SimcopTask.INPUT_FILTER:
                    createNode(preNode, task);
                    break;

                case SimcopTask.INPUT_TRANSFORMATION:
                    createNode(preNode, task);
                    break;

                case SimcopTask.SEQUENCE_SIMILARITY:
                    DefaultMutableTreeNode seqSimNode = createNode(simNode, task);
                    if (task.hasSubTasks()) {
                        seqSimNode.setAllowsChildren(true);
                        for (SimcopTask subtask1 : task.getSubTasks()) {
                            DefaultMutableTreeNode attrNode = createNode(seqSimNode, subtask1);
                            if (subtask1.hasSubTasks()) {
                                attrNode.setAllowsChildren(true);
                                for (SimcopTask subtask2 : subtask1.getSubTasks()) {
                                    createNode(attrNode, subtask2);
                                }
                            }
                        }
                    }

                    break;

                case SimcopTask.OUTPUT_FILTER:
                    createNode(posNode, task);
                    break;

                case SimcopTask.OUTPUT_TRANSFORMATION:
                    createNode(posNode, task);
                    break;
            }

        }
        setRoot(rootNode);
        tree.expandPath(new TreePath(new Object[]{rootNode, preNode}));
        tree.expandPath(new TreePath(new Object[]{rootNode, simNode}));
        tree.expandPath(new TreePath(new Object[]{rootNode, posNode}));
    }

    private DefaultMutableTreeNode createNode(DefaultMutableTreeNode parent, SimcopTask task) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TaskNode(task), false);
        parent.add(node);
        return node;
    }
}
