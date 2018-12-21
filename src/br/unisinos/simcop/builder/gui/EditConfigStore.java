/*
 * CreateConfigStore.java
 *
 * Created on 26/10/2013, 15:37:24
 */
package br.unisinos.simcop.builder.gui;

import br.unisinos.simcop.Simcop;
import br.unisinos.simcop.Utils;
import br.unisinos.simcop.core.ContextPair;
import br.unisinos.simcop.core.ISimcopClass;
import br.unisinos.simcop.core.SimilarityResult;
import br.unisinos.simcop.core.config.ISimcopConfig;
import br.unisinos.simcop.core.process.SimcopTask;
import br.unisinos.simcop.data.model.Context;
import br.unisinos.simcop.data.model.ContextSequence;
import br.unisinos.simcop.data.model.Entity;
import br.unisinos.simcop.data.model.ExtendedData;
import br.unisinos.simcop.data.model.LocationDescription;
import br.unisinos.simcop.data.model.Situation;
import br.unisinos.simcop.data.model.TimeDescription;
import br.unisinos.simcop.data.source.ISequenceSource;
import java.awt.Component;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author tiago
 */
public class EditConfigStore extends javax.swing.JInternalFrame {

    private ISimcopConfig configStore;
    private MainWindow main;
    private SimcopTask editTask;
    private SimcopTask editSource;
    private boolean isNew;
    private List<SimcopTask> sources;
    private Entity editEntity;
    private Entity executeA;
    private Entity executeB;

    /**
     * Creates new form CreateConfigStore
     */
    public EditConfigStore(MainWindow main, ISimcopConfig configStore, boolean isNew) {
        initComponents();
        this.configStore = configStore;
        this.isNew = isNew;
        if (isNew) {
            setTitle("New " + configStore.getClass().getSimpleName());
        } else {
            setTitle(configStore.getName());
        }
        loadGUI();
        loadTabTestSequence();
        this.main = main;
        lbResult.setVisible(false);
        btExecute.setEnabled(false);
        lbStep.setText("");
    }

    private void loadTabTestSequence() {
        tabTestSequence.setModel(new SequencesTabModel(editEntity != null ? editEntity.getSequence() : null));
        tabTestSequence.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabTestSequence.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabTestSequence.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabTestSequence.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabTestSequence.getColumnModel().getColumn(3).setPreferredWidth(75);
        tabTestSequence.getColumnModel().getColumn(2).setCellRenderer(new MultRowCellRenderer());
        tabTestSequence.updateUI();
    }

    private void loadGUI() {
        loadStoreConnectionParameters();
        loadProcessTree();
        loadSources();
        loadAditionalInfo();
    }

    private void loadAditionalInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        edStoreName.setText(configStore.getName());
        edAuthor.setText(configStore.getAuthor());
        edCreated.setText(configStore.getCreated() != null ? sdf.format(configStore.getCreated()) : "Not Available");
        edDescription.setText(configStore.getDescription());
        edModified.setText(configStore.getModifiedBy() != null ? sdf.format(configStore.getModified()) : "Not Available");
        edModifiedBy.setText(configStore.getModifiedBy());

    }

    private void loadStoreConnectionParameters() {
        tabConfigStoreParameters.setVisible(configStore.hasParameters());
        if (tabConfigStoreParameters.isVisible()) {
            ParametersTabModel.forTable(tabConfigStoreParameters, configStore.getParameters());
        }
        tabConfigStoreParameters.setEnabled(isNew);

    }

    private void loadProcessTree() {
        ProcessTreeModel.loadFor(configStore, treeProcess);
        treeProcess.updateUI();
        setEditTask(null);
    }

    private void save() {
        try {
            if (editTask != null) {
                saveEditTask();
            }
            configStore.setName(edStoreName.getText());
            if (configStore.getCreated() == null) {
                configStore.setCreated(new Date());
            }
            configStore.setModified(new Date());
            configStore.setAuthor(edAuthor.getText());
            configStore.setModifiedBy(edModifiedBy.getText());
            configStore.setDescription(edDescription.getText());

            configStore.save();
            loadProcessTree();
            loadSources();
            loadAditionalInfo();

            if (isNew) {
                main.getMruList().store(configStore.getClass().getName(), configStore.getName(), configStore.getParameters());
                isNew = false;
            }

            JOptionPane.showMessageDialog(this, "Sucefuly Stored", "Saved", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            Logger.getLogger(EditConfigStore.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error saving data", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSources() {
        sources = new ArrayList<SimcopTask>();
        if (configStore != null) {
            for (String sourceName : configStore.getSequenceSources().keySet()) {
                SimcopTask task = configStore.getSequenceSources().get(sourceName);
                sources.add(task);
            }
        }
        Collections.sort(sources);
        lstSource.setModel(new SourceModel());
        lstSource.updateUI();
    }

    private void loadEditTask() {
        lbFor.setVisible(false);
        edFor.setVisible(false);
        if (editTask != null) {
            edTaskSequence.setText(Integer.toString(editTask.getSequence()));
            edTaskClassName.setText(editTask.getClassName());
            edTaskLabel.setText(editTask.getLabel());
            edTaskType.setText(editTask.getTypeName());
            ParametersTabModel.forTable(tabTaskParameters, editTask.getParameters());
            tabTaskParameters.setEnabled(!editTask.getParameters().isEmpty());
            btRemove.setEnabled(true);
            if (editTask.getType() == SimcopTask.ATTRIBUTE_SIMILARITY) {
                lbFor.setVisible(true);
                edFor.setVisible(true);
                edFor.setText(editTask.getForAttribute());
            }
        } else {
            edTaskSequence.setText("");
            edTaskClassName.setText("");
            edTaskLabel.setText("");
            edTaskType.setText("");
            tabTaskParameters.setEnabled(false);
            btRemove.setEnabled(false);
        }
    }

    private void saveEditTask() {
        editTask.setLabel(edTaskLabel.getText());
        editTask.setForAttribute(edFor.getText());
    }

    private void loadEditSource() {
        if (editSource != null) {
            edSourceSequence.setText(Integer.toString(editSource.getSequence()));
            edSourceClassName.setText(editSource.getClassName());
            edSourceLabel.setText(editSource.getLabel());
            edSourceType.setText(editSource.getTypeName());
            ParametersTabModel.forTable(tabSourceParameters, editSource.getParameters());
            tabSourceParameters.setEnabled(!editSource.getParameters().isEmpty());
            btRemove.setEnabled(true);
        } else {
            edSourceSequence.setText("");
            edSourceClassName.setText("");
            edSourceLabel.setText("");
            edSourceType.setText("");
            tabSourceParameters.setEnabled(false);
            btRemove.setEnabled(false);
        }
    }

    private void saveEditSource() {
        editSource.setLabel(edSourceLabel.getText());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        edStoreName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabConfigStoreParameters = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        tabs = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        pnlProcessMan = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        btInputFilter = new javax.swing.JButton();
        btInputTransformation = new javax.swing.JButton();
        btSequenceSimilarity = new javax.swing.JButton();
        btOutputTransformation = new javax.swing.JButton();
        btOutputFilter = new javax.swing.JButton();
        btContextSimilarity = new javax.swing.JButton();
        btAttributeSimilarity = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        treeProcess = new javax.swing.JTree();
        jPanel6 = new javax.swing.JPanel();
        pnlTask = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        edTaskSequence = new javax.swing.JTextField();
        edTaskClassName = new javax.swing.JTextField();
        btRemove = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        edTaskLabel = new javax.swing.JTextField();
        edTaskType = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        lbFor = new javax.swing.JLabel();
        edFor = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        tabTaskParameters = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        btAddSource = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        lstSource = new javax.swing.JList();
        jPanel8 = new javax.swing.JPanel();
        pnlTask1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        edSourceSequence = new javax.swing.JTextField();
        edSourceClassName = new javax.swing.JTextField();
        btRemoveSource = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        edSourceLabel = new javax.swing.JTextField();
        edSourceType = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tabSourceParameters = new javax.swing.JTable();
        jPanel12 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        btTestConnection = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        edEntitiesParameters = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        lbEntities = new javax.swing.JLabel();
        pnlTestSequences = new javax.swing.JPanel();
        pnlTestSequenceA = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tabTestEntities = new javax.swing.JTable();
        pnlTestSequenceB = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        edEntityUID = new javax.swing.JTextField();
        edEntityName = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        tabTestSequence = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        lbAuthor = new javax.swing.JLabel();
        lbCreated = new javax.swing.JLabel();
        lbLastModification = new javax.swing.JLabel();
        lbDescription = new javax.swing.JLabel();
        edAuthor = new javax.swing.JTextField();
        edModifiedBy = new javax.swing.JTextField();
        edCreated = new javax.swing.JLabel();
        edModified = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        edDescription = new javax.swing.JTextArea();
        lbModifiedBy = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        lbResult = new javax.swing.JLabel();
        edUidEA = new javax.swing.JTextField();
        edUidEB = new javax.swing.JTextField();
        edNameEA = new javax.swing.JTextField();
        edNameEB = new javax.swing.JTextField();
        btLoadA = new javax.swing.JButton();
        btLoadB = new javax.swing.JButton();
        btExecute = new javax.swing.JButton();
        lbStep = new javax.swing.JLabel();
        tabsResult = new javax.swing.JTabbedPane();
        jPanel19 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        edLog = new javax.swing.JTextArea();
        jPanel20 = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        tabResult = new javax.swing.JTable();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        edStoreName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edStoreNameFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 577;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 0, 0);
        jPanel1.add(edStoreName, gridBagConstraints);

        tabConfigStoreParameters.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"", null}
            },
            new String [] {
                "Parameter", "Value"
            }
        ));
        jScrollPane1.setViewportView(tabConfigStoreParameters);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 3, 15);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jButton1.setText("Store");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new java.awt.GridBagConstraints());

        jLabel2.setText("Connection:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        jPanel1.add(jLabel2, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        tabs.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        jPanel2.setLayout(new java.awt.BorderLayout());

        pnlProcessMan.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setText("Add Task:");

        btInputFilter.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        btInputFilter.setText("Input Filter");
        btInputFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInputFilterActionPerformed(evt);
            }
        });

        btInputTransformation.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        btInputTransformation.setText("Input Transformation");
        btInputTransformation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInputTransformationActionPerformed(evt);
            }
        });

        btSequenceSimilarity.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        btSequenceSimilarity.setText("Sequence Similarity");
        btSequenceSimilarity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSequenceSimilarityActionPerformed(evt);
            }
        });

        btOutputTransformation.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        btOutputTransformation.setText("Output Transformation");
        btOutputTransformation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOutputTransformationActionPerformed(evt);
            }
        });

        btOutputFilter.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        btOutputFilter.setText("Output Filter");
        btOutputFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOutputFilterActionPerformed(evt);
            }
        });

        btContextSimilarity.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        btContextSimilarity.setText("Context Similarity");
        btContextSimilarity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btContextSimilarityActionPerformed(evt);
            }
        });

        btAttributeSimilarity.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        btAttributeSimilarity.setText("Attribute Similarity");
        btAttributeSimilarity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAttributeSimilarityActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlProcessManLayout = new javax.swing.GroupLayout(pnlProcessMan);
        pnlProcessMan.setLayout(pnlProcessManLayout);
        pnlProcessManLayout.setHorizontalGroup(
            pnlProcessManLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProcessManLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btInputFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btInputTransformation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btSequenceSimilarity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btContextSimilarity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btAttributeSimilarity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btOutputTransformation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btOutputFilter)
                .addGap(357, 357, 357))
        );
        pnlProcessManLayout.setVerticalGroup(
            pnlProcessManLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProcessManLayout.createSequentialGroup()
                .addGroup(pnlProcessManLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btInputFilter)
                    .addComponent(btInputTransformation)
                    .addComponent(btSequenceSimilarity)
                    .addComponent(btContextSimilarity)
                    .addComponent(btOutputTransformation)
                    .addComponent(btOutputFilter)
                    .addComponent(btAttributeSimilarity))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.add(pnlProcessMan, java.awt.BorderLayout.NORTH);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setDividerSize(8);

        treeProcess.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeProcessValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(treeProcess);

        jSplitPane1.setLeftComponent(jScrollPane3);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel4.setText("Task Sequence:");

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel5.setText("Task Type:");

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel6.setText("Task Class Name:");

        edTaskSequence.setEditable(false);

        edTaskClassName.setEditable(false);

        btRemove.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        btRemove.setText("Remove");
        btRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoveActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel7.setText("Task Label:");

        edTaskType.setEditable(false);

        jLabel14.setFont(new java.awt.Font("Dialog", 2, 10)); // NOI18N
        jLabel14.setText("Hint: press CTRL+Enter to add a new parameter and CTRL+Delete to remove it.");

        lbFor.setText("For");

        javax.swing.GroupLayout pnlTaskLayout = new javax.swing.GroupLayout(pnlTask);
        pnlTask.setLayout(pnlTaskLayout);
        pnlTaskLayout.setHorizontalGroup(
            pnlTaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTaskLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlTaskLayout.createSequentialGroup()
                        .addComponent(edTaskSequence, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel7)
                        .addGap(3, 3, 3)
                        .addComponent(edTaskLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 386, Short.MAX_VALUE)
                        .addComponent(btRemove))
                    .addGroup(pnlTaskLayout.createSequentialGroup()
                        .addGroup(pnlTaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(edTaskClassName, javax.swing.GroupLayout.PREFERRED_SIZE, 569, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlTaskLayout.createSequentialGroup()
                                .addComponent(edTaskType, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lbFor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(edFor)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(pnlTaskLayout.createSequentialGroup()
                .addComponent(jLabel14)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnlTaskLayout.setVerticalGroup(
            pnlTaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTaskLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(edTaskSequence, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(edTaskLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btRemove))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(edTaskType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbFor)
                    .addComponent(edFor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(pnlTaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edTaskClassName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel14))
        );

        jPanel6.add(pnlTask, java.awt.BorderLayout.PAGE_START);

        tabTaskParameters.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null}
            },
            new String [] {
                "Parameter", "Value", "Pattern"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tabTaskParameters);

        jPanel6.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel6);

        jPanel2.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        tabs.addTab("Simcop Process", jPanel2);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel7.setPreferredSize(new java.awt.Dimension(1186, 40));
        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.LINE_AXIS));

        jLabel9.setText("Add Task:");
        jPanel7.add(jLabel9);

        btAddSource.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        btAddSource.setText("Sequence Source");
        btAddSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddSourceActionPerformed(evt);
            }
        });
        jPanel7.add(btAddSource);

        jPanel3.add(jPanel7, java.awt.BorderLayout.PAGE_START);

        jSplitPane2.setDividerLocation(200);

        lstSource.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lstSource.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstSource.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstSource.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstSourceValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(lstSource);

        jSplitPane2.setLeftComponent(jScrollPane5);

        jPanel8.setLayout(new java.awt.BorderLayout());

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel10.setText("Task Sequence:");

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel11.setText("Task Type:");

        jLabel12.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel12.setText("Task Class Name:");

        edSourceSequence.setEditable(false);

        edSourceClassName.setEditable(false);

        btRemoveSource.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        btRemoveSource.setText("Remove");
        btRemoveSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoveSourceActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel13.setText("Task Label:");

        edSourceType.setEditable(false);

        javax.swing.GroupLayout pnlTask1Layout = new javax.swing.GroupLayout(pnlTask1);
        pnlTask1.setLayout(pnlTask1Layout);
        pnlTask1Layout.setHorizontalGroup(
            pnlTask1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTask1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTask1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTask1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlTask1Layout.createSequentialGroup()
                        .addComponent(edSourceSequence, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13)
                        .addGap(3, 3, 3)
                        .addComponent(edSourceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 299, Short.MAX_VALUE)
                        .addComponent(btRemoveSource)
                        .addGap(97, 97, 97))
                    .addGroup(pnlTask1Layout.createSequentialGroup()
                        .addGroup(pnlTask1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(edSourceClassName, javax.swing.GroupLayout.PREFERRED_SIZE, 569, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(edSourceType, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(411, Short.MAX_VALUE))))
        );
        pnlTask1Layout.setVerticalGroup(
            pnlTask1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTask1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTask1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(edSourceSequence, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(edSourceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btRemoveSource))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlTask1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(edSourceType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(pnlTask1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edSourceClassName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.add(pnlTask1, java.awt.BorderLayout.PAGE_START);

        jPanel9.setLayout(new java.awt.BorderLayout());

        tabSourceParameters.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null}
            },
            new String [] {
                "Parameter", "Value", "Pattern"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(tabSourceParameters);

        jPanel9.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        jPanel12.setPreferredSize(new java.awt.Dimension(970, 10));
        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.LINE_AXIS));

        jLabel17.setFont(new java.awt.Font("Dialog", 2, 10)); // NOI18N
        jLabel17.setText("Hint: press CTRL+Enter to add a new parameter and CTRL+Delete to remove it.");
        jPanel12.add(jLabel17);

        jPanel9.add(jPanel12, java.awt.BorderLayout.PAGE_START);

        jTabbedPane1.addTab("Parameters", jPanel9);

        jPanel10.setLayout(new java.awt.BorderLayout());

        jPanel11.setPreferredSize(new java.awt.Dimension(970, 30));
        jPanel11.setLayout(new javax.swing.BoxLayout(jPanel11, javax.swing.BoxLayout.LINE_AXIS));

        btTestConnection.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        btTestConnection.setText("Test Connection");
        btTestConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btTestConnectionActionPerformed(evt);
            }
        });
        jPanel11.add(btTestConnection);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setMaximumSize(new java.awt.Dimension(5, 32767));
        jSeparator1.setPreferredSize(new java.awt.Dimension(5, 10));
        jPanel11.add(jSeparator1);

        jTextPane1.setEditable(false);
        jTextPane1.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        jTextPane1.setBorder(null);
        jTextPane1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jTextPane1.setText("Entities Query Parameters:\n( separated by semi-colons [ ; ] )");
        jScrollPane9.setViewportView(jTextPane1);

        jPanel11.add(jScrollPane9);
        jPanel11.add(edEntitiesParameters);

        jButton2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton2.setText("Load Entities");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel11.add(jButton2);

        lbEntities.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lbEntities.setText("0 Entities Found");
        jPanel11.add(lbEntities);

        jPanel10.add(jPanel11, java.awt.BorderLayout.PAGE_START);

        pnlTestSequences.setLayout(new javax.swing.BoxLayout(pnlTestSequences, javax.swing.BoxLayout.LINE_AXIS));

        pnlTestSequenceA.setBorder(javax.swing.BorderFactory.createTitledBorder("Entities"));
        pnlTestSequenceA.setPreferredSize(new java.awt.Dimension(371, 219));
        pnlTestSequenceA.setLayout(new java.awt.BorderLayout());

        tabTestEntities.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null}
            },
            new String [] {
                "UID", "Name"
            }
        ));
        tabTestEntities.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tabTestEntities.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabTestEntitiesMouseClicked(evt);
            }
        });
        jScrollPane7.setViewportView(tabTestEntities);

        pnlTestSequenceA.add(jScrollPane7, java.awt.BorderLayout.CENTER);

        pnlTestSequences.add(pnlTestSequenceA);

        pnlTestSequenceB.setBorder(javax.swing.BorderFactory.createTitledBorder("Sequence"));
        pnlTestSequenceB.setLayout(new java.awt.BorderLayout());

        jPanel13.setPreferredSize(new java.awt.Dimension(527, 60));
        jPanel13.setRequestFocusEnabled(false);

        jLabel15.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel15.setText("Entity UID:");

        jLabel16.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel16.setText("Entity Name:");

        jButton3.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton3.setText("Load Seq.");

        jButton4.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton4.setText("Attributes");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(27, 27, 27)
                        .addComponent(edEntityUID, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 330, Short.MAX_VALUE)
                        .addComponent(jButton4))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(edEntityName, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(edEntityUID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(edEntityName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlTestSequenceB.add(jPanel13, java.awt.BorderLayout.PAGE_START);

        tabTestSequence.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane8.setViewportView(tabTestSequence);

        pnlTestSequenceB.add(jScrollPane8, java.awt.BorderLayout.CENTER);

        pnlTestSequences.add(pnlTestSequenceB);

        jPanel10.add(pnlTestSequences, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Test", jPanel10);

        jPanel8.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        jTabbedPane1.getAccessibleContext().setAccessibleName("Parameters");

        jSplitPane2.setRightComponent(jPanel8);

        jPanel3.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        tabs.addTab("Sequence Sources", jPanel3);

        jLabel8.setText("Coming Soon...");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(394, 394, 394)
                .addComponent(jLabel8)
                .addContainerGap(824, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(138, 138, 138)
                .addComponent(jLabel8)
                .addContainerGap(545, Short.MAX_VALUE))
        );

        tabs.addTab("Ontologies", jPanel4);

        lbAuthor.setText("Author:");

        lbCreated.setText("Created At:");

        lbLastModification.setText("Last Modification:");

        lbDescription.setText("Description:");

        edAuthor.setText("jTextField1");

        edModifiedBy.setText("jTextField2");

        edCreated.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        edCreated.setText("jLabel6");

        edModified.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        edModified.setText("jLabel7");

        edDescription.setColumns(20);
        edDescription.setRows(5);
        jScrollPane2.setViewportView(edDescription);

        lbModifiedBy.setText("Modified By:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(lbAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(edAuthor, javax.swing.GroupLayout.DEFAULT_SIZE, 1162, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(lbCreated, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(edCreated))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(lbLastModification)
                        .addGap(18, 18, 18)
                        .addComponent(edModified))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(lbModifiedBy, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(edModifiedBy, javax.swing.GroupLayout.DEFAULT_SIZE, 1162, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(lbDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1162, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbAuthor)
                    .addComponent(edAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbCreated)
                    .addComponent(edCreated))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbLastModification)
                    .addComponent(edModified))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbModifiedBy)
                    .addComponent(edModifiedBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(lbDescription)
                        .addGap(121, 121, 121))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        tabs.addTab("Configuration Info", jPanel5);

        jPanel14.setLayout(new java.awt.BorderLayout());

        jPanel15.setLayout(new java.awt.BorderLayout());

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select context sequences to compare", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12))); // NOI18N

        jLabel18.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel18.setText("Entity A:");

        jLabel19.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel19.setText("Entity B:");

        jLabel20.setText("Result:");

        lbResult.setForeground(new java.awt.Color(255, 0, 51));
        lbResult.setText("Similarity/Distance = ");

        edUidEA.setEditable(false);
        edUidEA.setForeground(new java.awt.Color(51, 51, 255));

        edUidEB.setEditable(false);
        edUidEB.setForeground(new java.awt.Color(51, 51, 255));

        edNameEA.setEditable(false);
        edNameEA.setForeground(new java.awt.Color(51, 51, 255));

        edNameEB.setEditable(false);
        edNameEB.setForeground(new java.awt.Color(51, 51, 255));

        btLoadA.setText("Load");
        btLoadA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLoadAActionPerformed(evt);
            }
        });

        btLoadB.setText("Load");
        btLoadB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLoadBActionPerformed(evt);
            }
        });

        btExecute.setText("Analyze Similarity");
        btExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btExecuteActionPerformed(evt);
            }
        });

        lbStep.setText("Step:");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(btExecute)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbResult))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(edUidEA, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(edUidEB, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(edNameEB, javax.swing.GroupLayout.PREFERRED_SIZE, 512, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(edNameEA, javax.swing.GroupLayout.PREFERRED_SIZE, 512, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btLoadA)
                    .addComponent(btLoadB)
                    .addComponent(lbStep))
                .addContainerGap(571, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(edUidEA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(edNameEA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btLoadA))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(edUidEB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(edNameEB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btLoadB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btExecute, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel20)
                        .addComponent(lbResult, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lbStep)))
                .addContainerGap())
        );

        jPanel15.add(jPanel16, java.awt.BorderLayout.CENTER);

        jPanel14.add(jPanel15, java.awt.BorderLayout.PAGE_START);

        tabsResult.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        jPanel19.setLayout(new java.awt.BorderLayout());

        edLog.setEditable(false);
        edLog.setColumns(20);
        edLog.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        edLog.setRows(5);
        jScrollPane10.setViewportView(edLog);

        jPanel19.add(jScrollPane10, java.awt.BorderLayout.CENTER);

        tabsResult.addTab("Log", jPanel19);

        jPanel20.setLayout(new java.awt.BorderLayout());

        tabResult.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                ""
            }
        ));
        tabResult.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane11.setViewportView(tabResult);

        jPanel20.add(jScrollPane11, java.awt.BorderLayout.CENTER);

        tabsResult.addTab("Result: Context Pairs", jPanel20);

        jPanel14.add(tabsResult, java.awt.BorderLayout.CENTER);

        tabs.addTab("Execute Analysis", jPanel14);

        getContentPane().add(tabs, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void edStoreNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edStoreNameFocusLost
        configStore.setName(edStoreName.getText());
        setTitle(configStore.getName());
    }//GEN-LAST:event_edStoreNameFocusLost

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (!Utils.isEmpty(edStoreName.getText())) {
            save();
        } else {
            JOptionPane.showMessageDialog(this, "Please enter the name of this Configuration Storage", "Invalid Properties", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    private String forEdit;

    private ISimcopClass select(int type) {
        SelectClass dialog = new SelectClass(new javax.swing.JFrame(), type, main.getKnowClasses());
        dialog.setVisible(true);
        forEdit = dialog.getFor();
        return dialog.getSelected();
    }

    private SimcopTask createTask(int type, ISimcopClass simcopClass) {
        SimcopTask result = new SimcopTask(type);
        result.setClassName(simcopClass.getClass().getName());
        result.setInstance(simcopClass);
        result.setLabel(simcopClass.getClass().getSimpleName());
        result.setParameters(simcopClass.getDefaultParameters());
        return result;
    }

    private void add(SimcopTask task) {
        if (task.getType() == SimcopTask.SEQUENCE_SOURCE) {
            //TODO testar se nao precisa esta linha:configStore.addSource(title, (ISequenceSource) task.getInstance());
            loadSources();
            setEditSource(task);
        } else {
            configStore.add(task);
            configStore.updateTasksSequence();
            loadProcessTree();
            setEditTask(task);
        }
    }

    private void btInputFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInputFilterActionPerformed
        ISimcopClass selected = select(SelectClass.INPUT_FILTER);
        if (selected != null) {
            add(createTask(SimcopTask.INPUT_FILTER, selected));
        }
    }//GEN-LAST:event_btInputFilterActionPerformed

    private void btInputTransformationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInputTransformationActionPerformed
        ISimcopClass selected = select(SelectClass.INPUT_TRANSFORM);
        if (selected != null) {
            add(createTask(SimcopTask.INPUT_TRANSFORMATION, selected));
        }

    }//GEN-LAST:event_btInputTransformationActionPerformed

    private void btSequenceSimilarityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSequenceSimilarityActionPerformed
        ISimcopClass selected = select(SelectClass.SEQUENCE_SIMILARITY);
        if (selected != null) {
            SimcopTask newTask = createTask(SimcopTask.SEQUENCE_SIMILARITY, selected);
            SimcopTask oldTask = configStore.findSequenceSimilarityTask();
            if (oldTask != null) {
                configStore.getProcessTasks().remove(oldTask);
            }
            add(newTask);
        }

    }//GEN-LAST:event_btSequenceSimilarityActionPerformed

    private void btOutputTransformationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOutputTransformationActionPerformed
        ISimcopClass selected = select(SelectClass.OUTPUT_TRANSFORM);
        if (selected != null) {
            add(createTask(SimcopTask.OUTPUT_TRANSFORMATION, selected));
        }

    }//GEN-LAST:event_btOutputTransformationActionPerformed

    private void btOutputFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOutputFilterActionPerformed
        ISimcopClass selected = select(SelectClass.OUTPUT_FILTER);
        if (selected != null) {
            add(createTask(SimcopTask.OUTPUT_FILTER, selected));
        }

    }//GEN-LAST:event_btOutputFilterActionPerformed

    private void treeProcessValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeProcessValueChanged
        TreePath path = evt.getPath();
        if (path != null && path.getPathCount() > 0) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof TaskNode) {
                TaskNode taskNode = (TaskNode) node.getUserObject();
                setEditTask(taskNode.getTask());
            }
        }
    }//GEN-LAST:event_treeProcessValueChanged

    private void btRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoveActionPerformed
        if (editTask != null) {
            SimcopTask sequence;
            switch (editTask.getType()) {
                case SimcopTask.ATTRIBUTE_SIMILARITY:
                    sequence = configStore.findSequenceSimilarityTask();
                    if (sequence != null) {
                        SimcopTask ctx = sequence.findSubTask(SimcopTask.CONTEXT_SIMILARITY);
                        if (ctx != null) {
                            ctx.getSubTasks().remove(editTask);
                        }
                    }

                    break;
                case SimcopTask.CONTEXT_SIMILARITY:
                    sequence = configStore.findSequenceSimilarityTask();
                    if (sequence != null) {
                        sequence.getSubTasks().remove(editTask);
                    }

                    break;
                default:
                    configStore.getProcessTasks().remove(editTask);
            }
            configStore.updateTasksSequence();
            loadProcessTree();
            setEditTask(null);
        }

    }//GEN-LAST:event_btRemoveActionPerformed

    private void btAddSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddSourceActionPerformed
        ISimcopClass selected = select(SelectClass.SEQUENCE_SOURCE);
        if (selected != null) {
            ISequenceSource source = (ISequenceSource) selected;
            int sequence = configStore.getSequenceSources().size();
            String uid = "#" + source.getClass().getName() + ":" + sequence;
            SimcopTask task = configStore.addSource(uid, source);
            task.setSequence(sequence);
            add(task);
        }
    }//GEN-LAST:event_btAddSourceActionPerformed

    private void btRemoveSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoveSourceActionPerformed
        if (editSource != null) {
            configStore.getSequenceSources().remove(editSource.getUid());
            configStore.updateTasksSequence();
            setEditSource(null);
            updateUI();
        }

    }//GEN-LAST:event_btRemoveSourceActionPerformed

    private void lstSourceValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstSourceValueChanged
        int index = evt.getFirstIndex();
        if (index >= 0 && index < sources.size()) {
            setEditSource(sources.get(index));
        }
    }//GEN-LAST:event_lstSourceValueChanged

    private void btTestConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btTestConnectionActionPerformed
        if (editSource != null) {
            ISequenceSource source = null;
            StringBuilder sb = new StringBuilder();
            try {
                source = (ISequenceSource) editSource.createInstance();
            } catch (Exception e) {
                Utils.log(e);
                sb.append(e.getMessage());
            }
            if (source != null) {
                //connect
                source.resetErrors();
                boolean con = source.connectToSource();
                sb.append("* Connect: ").append(con ? "OK" : "Error:").append("\n");
                for (String error : source.getErrors()) {
                    sb.append("\t").append(error).append("\n");
                }

                //disconnect
                source.resetErrors();
                boolean dis = source.disconnectFromSource();
                sb.append("* Disconnect: ").append(dis ? "OK" : "Error:").append("\n");
                for (String error : source.getErrors()) {
                    sb.append("\t").append(error).append("\n");
                }
            } else {
                sb.append("Cannot initialize source instance");
            }

            //show
            LongMessage dialog = new LongMessage(new javax.swing.JFrame(), "Source Connection Test", sb.toString());
            dialog.setVisible(true);
        }
    }//GEN-LAST:event_btTestConnectionActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (editSource != null) {
            ISequenceSource source = null;
            StringBuilder sb = new StringBuilder();
            List<Entity> entities = new ArrayList<Entity>();
            try {
                source = (ISequenceSource) editSource.createInstance();
                if (source.connectToSource()) {
                    String[] parameters = null;
                    if (!Utils.isEmpty(edEntitiesParameters.getText())) {
                        parameters = edEntitiesParameters.getText().split(";");
                    }
                    entities = source.getListEntities(parameters);
                    source.disconnectFromSource();
                } else {
                    source = null;
                    sb.append("Cannot Connect: \n");
                    for (String error : source.getErrors()) {
                        sb.append("\t").append(error).append("\n");
                    }
                }
            } catch (Exception e) {
                Utils.log(e);
                sb.append(e.getMessage());
                for (String error : source.getErrors()) {
                    sb.append("\t").append(error).append("\n");
                }
                source = null;
            }
            if (entities != null) {
                lbEntities.setText(entities.size() + " Entities Found");
            } else {
                lbEntities.setText(" {null} Entities Found");
            }
            if (source != null) {
                tabTestEntities.setModel(new EntitiesModel(entities));
                tabTestEntities.getColumnModel().getColumn(0).setPreferredWidth(75);
                tabTestEntities.getColumnModel().getColumn(1).setPreferredWidth(300);
                tabTestEntities.updateUI();
            } else {
                sb.append("\nCannot initialize source instance. Parameters:\n");
                for (String pk : editSource.getParameters().keySet()) {
                    sb.append("\t").append(pk).append("=\"").append(editSource.getParameters().get(pk)).append("\"\n");
                }
                LongMessage dialog = new LongMessage(new javax.swing.JFrame(), "Source Load Entities Test", sb.toString());
                dialog.setVisible(true);
            }
        }

    }//GEN-LAST:event_jButton2ActionPerformed

    private Entity getSelectedEntity(int index) {
        EntitiesModel model = (EntitiesModel) tabTestEntities.getModel();
        return model.entities.get(index);
    }

    private void tabTestEntitiesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabTestEntitiesMouseClicked
        boolean proceed = editSource != null;
        Entity selected = null;
        editEntity = null;
        if (proceed) {
            int index = tabTestEntities.getSelectedRow();
            if (index >= 0) {
                selected = getSelectedEntity(index);
                proceed = selected != null;
            } else {
                proceed = false;
            }
        }


        if (proceed) {
            Object uid = selected.getUid();
            ISequenceSource source = null;
            StringBuilder sb = new StringBuilder();
            try {
                source = (ISequenceSource) editSource.createInstance();
                if (source.connectToSource()) {
                    //--
                    editEntity = source.loadEntityAndSequence(uid);
                    //---
                    source.disconnectFromSource();
                    loadTabTestSequence();
                } else {
                    source = null;
                    sb.append("Cannot Connect: \n");
                    for (String error : source.getErrors()) {
                        sb.append("\t").append(error).append("\n");
                    }
                }
            } catch (Exception e) {
                Utils.log(e);
                sb.append(e.getMessage());
                for (String error : source.getErrors()) {
                    sb.append("\t").append(error).append("\n");
                }
                source = null;
            }
            if (source == null) {
                sb.append("\nCannot initialize source instance. Parameters:\n");
                for (String pk : editSource.getParameters().keySet()) {
                    sb.append("\t").append(pk).append("=\"").append(editSource.getParameters().get(pk)).append("\"\n");
                }
                LongMessage dialog = new LongMessage(new javax.swing.JFrame(), "Source Load Context Sequence (entity.uid=" + uid + ") Test", sb.toString());
                dialog.setVisible(true);
            }
        }
    }//GEN-LAST:event_tabTestEntitiesMouseClicked

    private void btContextSimilarityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btContextSimilarityActionPerformed
        ISimcopClass selected = select(SelectClass.CONTEXT_SIMILARITY);
        if (selected != null) {
            add(createTask(SimcopTask.CONTEXT_SIMILARITY, selected));
        }

    }//GEN-LAST:event_btContextSimilarityActionPerformed

    private void btAttributeSimilarityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAttributeSimilarityActionPerformed
        ISimcopClass selected = select(SelectClass.ATTRIBUTE_SIMILARITY);
        if (selected != null) {
            SimcopTask task = createTask(SimcopTask.ATTRIBUTE_SIMILARITY, selected);
            task.setForAttribute(forEdit);
            add(task);
        }

    }//GEN-LAST:event_btAttributeSimilarityActionPerformed

    private void btLoadAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLoadAActionPerformed
        executeA = selectEntity();
        if (executeA != null) {
            edUidEA.setText(executeA.getUid() != null ? executeA.getUid().toString() : "null");
            edNameEA.setText(executeA.getName());
        }
        enableExecute();
    }//GEN-LAST:event_btLoadAActionPerformed

    private void btLoadBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLoadBActionPerformed
        executeB = selectEntity();
        if (executeB != null) {
            edUidEB.setText(executeB.getUid() != null ? executeB.getUid().toString() : "null");
            edNameEB.setText(executeB.getName());
        }
        enableExecute();

    }//GEN-LAST:event_btLoadBActionPerformed

    private void btExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btExecuteActionPerformed
        new Thread(new ExecuteSimcop()).start();
    }//GEN-LAST:event_btExecuteActionPerformed

    private void enableExecute() {
        btExecute.setEnabled(executeA != null && executeB != null);
    }

    private Entity selectEntity() {
        LoadEntities window = new LoadEntities(null, true, this);
        window.setVisible(true);
        return window.getSelected();
    }

    public ISimcopConfig getConfigStore() {
        return configStore;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAddSource;
    private javax.swing.JButton btAttributeSimilarity;
    private javax.swing.JButton btContextSimilarity;
    private javax.swing.JButton btExecute;
    private javax.swing.JButton btInputFilter;
    private javax.swing.JButton btInputTransformation;
    private javax.swing.JButton btLoadA;
    private javax.swing.JButton btLoadB;
    private javax.swing.JButton btOutputFilter;
    private javax.swing.JButton btOutputTransformation;
    private javax.swing.JButton btRemove;
    private javax.swing.JButton btRemoveSource;
    private javax.swing.JButton btSequenceSimilarity;
    private javax.swing.JButton btTestConnection;
    private javax.swing.JTextField edAuthor;
    private javax.swing.JLabel edCreated;
    private javax.swing.JTextArea edDescription;
    private javax.swing.JTextField edEntitiesParameters;
    private javax.swing.JTextField edEntityName;
    private javax.swing.JTextField edEntityUID;
    private javax.swing.JTextField edFor;
    private javax.swing.JTextArea edLog;
    private javax.swing.JLabel edModified;
    private javax.swing.JTextField edModifiedBy;
    private javax.swing.JTextField edNameEA;
    private javax.swing.JTextField edNameEB;
    private javax.swing.JTextField edSourceClassName;
    private javax.swing.JTextField edSourceLabel;
    private javax.swing.JTextField edSourceSequence;
    private javax.swing.JTextField edSourceType;
    private javax.swing.JTextField edStoreName;
    private javax.swing.JTextField edTaskClassName;
    private javax.swing.JTextField edTaskLabel;
    private javax.swing.JTextField edTaskSequence;
    private javax.swing.JTextField edTaskType;
    private javax.swing.JTextField edUidEA;
    private javax.swing.JTextField edUidEB;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JLabel lbAuthor;
    private javax.swing.JLabel lbCreated;
    private javax.swing.JLabel lbDescription;
    private javax.swing.JLabel lbEntities;
    private javax.swing.JLabel lbFor;
    private javax.swing.JLabel lbLastModification;
    private javax.swing.JLabel lbModifiedBy;
    private javax.swing.JLabel lbResult;
    private javax.swing.JLabel lbStep;
    private javax.swing.JList lstSource;
    private javax.swing.JPanel pnlProcessMan;
    private javax.swing.JPanel pnlTask;
    private javax.swing.JPanel pnlTask1;
    private javax.swing.JPanel pnlTestSequenceA;
    private javax.swing.JPanel pnlTestSequenceB;
    private javax.swing.JPanel pnlTestSequences;
    private javax.swing.JTable tabConfigStoreParameters;
    private javax.swing.JTable tabResult;
    private javax.swing.JTable tabSourceParameters;
    private javax.swing.JTable tabTaskParameters;
    private javax.swing.JTable tabTestEntities;
    private javax.swing.JTable tabTestSequence;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JTabbedPane tabsResult;
    private javax.swing.JTree treeProcess;
    // End of variables declaration//GEN-END:variables

    public SimcopTask getEditTask() {
        return editTask;
    }

    public void setEditTask(SimcopTask editTask) {
        if (this.editTask != null) {
            saveEditTask();
        }
        this.editTask = editTask;
        loadEditTask();
    }

    public SimcopTask getEditSource() {
        return editSource;
    }

    public void setEditSource(SimcopTask editSource) {
        if (this.editSource != null) {
            saveEditSource();
        }
        this.editSource = editSource;
        loadEditSource();
    }

    public List<SimcopTask> getSources() {
        if (sources == null) {
            sources = new ArrayList<SimcopTask>();
        }

        return sources;
    }

    class SourceModel implements ListModel {

        public int getSize() {
            return getSources() != null ? getSources().size() : 0;
        }

        public Object getElementAt(int index) {
            if (getSources() != null && index >= 0 && index < getSources().size()) {
                SimcopTask task = getSources().get(index);
                if (task != null) {
                    return !Utils.isEmpty(task.getLabel()) ? task.getLabel() : task.getClassName();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        public void addListDataListener(ListDataListener l) {
        }

        public void removeListDataListener(ListDataListener l) {
        }
    }

    class SequencesTabModel extends DefaultTableModel {

        private final String[] columns = new String[]{"Time", "Location", "Situation", "Extended Data"};
        private ContextSequence sequence;

        public SequencesTabModel(ContextSequence sequence) {
            this.sequence = sequence;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public int getRowCount() {
            return sequence != null ? sequence.size() : 1;
        }

        @Override
        public int getColumnCount() {
            return columns != null ? columns.length : 1;
        }

        @Override
        public String getColumnName(int column) {
            return columns != null ? columns[column] : "";
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (columns != null && sequence != null) {
                Context ctx = sequence.get(row);
                if (ctx != null) {
                    TimeDescription td = ctx.getTime();
                    LocationDescription ld = ctx.getLocation();
                    List<Situation> situations = ctx.getSituations();
                    ExtendedData ed = ctx.getExtendedData();

                    switch (column) {
                        case 0:
                            return td != null ? td.toString() : "<gap>";
                        case 1:
                            return ld != null ? ld.toString() : "<gap>";
                        case 2:
                            StringBuilder sitList = new StringBuilder();
                            if (Utils.isEmpty(situations)) {
                                sitList.append("<gap>");
                            } else {
                                for (Situation situation : situations) {
                                    sitList.append(situation.toString());
                                    sitList.append("\n");
                                }
                            }
                            return sitList.toString();
                        case 3:
                            return ed != null ? ld.toString() : "<gap>";
                    }

                }
            }
            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    }

    class MultRowCellRenderer implements TableCellRenderer {

        private int defaultRowHeight;

        public MultRowCellRenderer() {
            defaultRowHeight = tabTestSequence.getRowHeight();
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String text = value == null ? "" : value.toString();

            int height = defaultRowHeight;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    height += defaultRowHeight;
                }
            }

            table.setRowHeight(row, height);

            JTextArea result = new JTextArea(text);
            result.setEditable(false);
            result.setBorder(null);
            result.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            return result;
        }
    }

    class WindowPrintStream extends PrintStream {

        private JTextArea textArea;

        public WindowPrintStream(JTextArea area, OutputStream out) {
            super(out);
            textArea = area;
        }

        @Override
        public void println(String string) {
            textArea.append(string + "\n");
        }

        @Override
        public void print(String string) {
            textArea.append(string);
        }
    }

    class ResultTabModel extends DefaultTableModel {

        private final String[] columns = new String[]{"Index", "Context A", "Context B", "Calculated Value"};
        private SimilarityResult result;

        public ResultTabModel(SimilarityResult result) {
            this.result = result;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public int getRowCount() {
            return result != null ? result.size() : 1;
        }

        @Override
        public int getColumnCount() {
            return columns != null ? columns.length : 1;
        }

        @Override
        public String getColumnName(int column) {
            return columns != null ? columns[column] : "";
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (columns != null && result != null) {
                ContextPair pair = result.getContextPairs().get(row);
                if (pair != null) {
                    switch (column) {
                        case 0:
                            return new DecimalFormat("###,#00").format(pair.getIndex());
                        case 1:
                            return pair.getC1() != null ? pair.getC1().toString() : "<GAP>";
                        case 2:
                            return pair.getC2() != null ? pair.getC2().toString() : "<GAP>";
                        case 3:
                            return new DecimalFormat("###,##0.00######").format(pair.getCalculatedValue());
                    }

                }
            }
            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    }

    class ExecuteSimcop implements Runnable {
        /* ************************************************************************************************************************************************
         * 
         * BEGIN: EXECUTE THE SIMILARITY ANALYSIS
         *        (using a thread only for updates the log window in real time)
         * ************************************************************************************************************************************************/

        public void run() {
            lbStep.setText("");
            lbResult.setVisible(false);
            edLog.setText("");
            if (executeA != null && executeB != null) {
                Utils.logOutput = new WindowPrintStream(edLog, System.out);
                try {
                    tabsResult.setSelectedIndex(0);
                    //1. Create a SimcopProcess using the Configuration Store            
                    lbStep.setText("Step 1: Create a SimcopProcess using the Configuration Store");
                    Simcop simcop = new Simcop(configStore.createProcess());

                    //2. Do the Analysis 
                    lbStep.setText("Step 2: Analyzing");
                    SimilarityResult result = simcop.analyze(executeA.getSequence(), executeB.getSequence());

                    //3. Read the Result
                    lbStep.setText("Step 3: Reading result");
                    if (simcop.isDistanceFunction() != null) {
                        if (result != null) {
                            String type = simcop.isDistanceFunction() ? "Distance" : "Similarity";
                            lbResult.setText(type + ": " + new DecimalFormat("###,###,##0.00######").format(result.getCalculatedValue()));
                            tabResult.setModel(new ResultTabModel(result));
                            tabResult.getColumnModel().getColumn(0).setPreferredWidth(30);
                            tabResult.getColumnModel().getColumn(1).setPreferredWidth(400);
                            tabResult.getColumnModel().getColumn(1).setCellRenderer(new MultRowCellRenderer());
                            tabResult.getColumnModel().getColumn(2).setPreferredWidth(400);
                            tabResult.getColumnModel().getColumn(2).setCellRenderer(new MultRowCellRenderer());
                            tabResult.getColumnModel().getColumn(3).setPreferredWidth(100);


                            tabsResult.setSelectedIndex(1);
                        } else {
                            lbResult.setText("No result");
                        }
                    } else {
                        lbResult.setText("Warning: No Sequence Similarity Task defined in the process");
                    }

                } catch (Exception e) {
                    lbResult.setText(e.getClass().getSimpleName() + ": " + e.getMessage());
                    Utils.log(e);
                } finally {
                    lbResult.setVisible(true);
                }
                lbStep.setText("Done");
            } else {
                edLog.setText("Select entities to compare");
            }
        }
        /* ************************************************************************************************************************************************
         * 
         * END: EXECUTE THE SIMILARITY ANALYSIS
         * 
         * ************************************************************************************************************************************************/
    }
}
