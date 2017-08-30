/* ----------------------------------------------------------------------------
 * Copyright (C) 2015      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : ESA NanoSat MO Framework
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.nmf.ctt.services.com;

import esa.mo.com.impl.consumer.ArchiveConsumerServiceImpl;
import esa.mo.com.impl.consumer.ArchiveSyncConsumerServiceImpl;
import esa.mo.com.impl.provider.ArchivePersistenceObject;
import esa.mo.helpertools.helpers.HelperTime;
import esa.mo.tools.mowindow.MOWindow;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InterruptedIOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ccsds.moims.mo.com.archive.structures.ArchiveQuery;
import org.ccsds.moims.mo.com.archive.structures.CompositeFilter;
import org.ccsds.moims.mo.com.archive.structures.ExpressionOperator;
import org.ccsds.moims.mo.com.archivesync.body.GetTimeResponse;
import org.ccsds.moims.mo.com.archivesync.consumer.ArchiveSyncAdapter;
import org.ccsds.moims.mo.com.structures.ObjectType;
import org.ccsds.moims.mo.com.structures.ObjectTypeList;
import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.structures.Blob;
import org.ccsds.moims.mo.mal.structures.FineTime;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.LongList;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mc.parameter.ParameterHelper;

/**
 *
 * @author Cesar Coelho
 */
public class ArchiveSyncConsumerManagerPanel extends javax.swing.JPanel {

    private final ArchiveConsumerServiceImpl serviceCOMArchive;
    private final ArchiveSyncConsumerServiceImpl serviceCOMArchiveSync;

    /**
     * Creates new form ArchiveConsumerPanel
     *
     * @param serviceCOMArchive
     * @param serviceCOMArchiveSync
     */
    public ArchiveSyncConsumerManagerPanel(ArchiveConsumerServiceImpl serviceCOMArchive,
            ArchiveSyncConsumerServiceImpl serviceCOMArchiveSync) {
        initComponents();
        this.serviceCOMArchive = serviceCOMArchive;
        this.serviceCOMArchiveSync = serviceCOMArchiveSync;
    }

    protected class ArchiveSyncConsumerAdapter extends ArchiveSyncAdapter {

        private final ArchiveTablePanel archiveTablePanel = new ArchiveTablePanel(null, serviceCOMArchive);
        private ObjectType objType;
        private IdentifierList domain;
        private final Semaphore isOver = new Semaphore(0);
        private int n_objs_counter = 0;
        private final javax.swing.JPanel pnlTab = new javax.swing.JPanel();
        private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        private final Date date = new Date(System.currentTimeMillis());
        private final String functionName;

        ArchiveSyncConsumerAdapter(String stringLabel) {
            pnlTab.setOpaque(false);
            functionName = stringLabel;
            /*                    
            JLabel label = new JLabel(functionName + " (" + dateFormat.format(date) + ")" + " (" + n_objs_counter + ")");
            JLabel closeLabel = new JLabel("x");
            closeLabel.addMouseListener(new CloseMouseHandler(pnlTab));
            closeLabel.setFont(closeLabel.getFont().deriveFont(closeLabel.getFont().getStyle() | Font.BOLD));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            pnlTab.add(label, gbc);

            gbc.gridx++;
            gbc.weightx = 0;
            pnlTab.add(closeLabel, gbc);
             */

            this.refreshTabCounter();

            tabs.addTab("", archiveTablePanel);

            tabs.setTabComponentAt(tabs.getTabCount() - 1, pnlTab);
            tabs.setSelectedIndex(tabs.getTabCount() - 1);

        }

        private void refreshTabCounter() {
            JLabel label = new JLabel(functionName + " (" + dateFormat.format(date) + ")" + " (" + n_objs_counter + ")");
            JLabel closeLabel = new JLabel("x");
            closeLabel.addMouseListener(new CloseMouseHandler(this));
            closeLabel.setFont(closeLabel.getFont().deriveFont(closeLabel.getFont().getStyle() | Font.BOLD));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;

            gbc.gridx++;
            gbc.weightx = 0;

            pnlTab.removeAll();
            pnlTab.add(label, gbc);
            pnlTab.add(closeLabel, gbc);

            int index = tabs.indexOfTabComponent(archiveTablePanel);

            if (index == -1) {
                return;
            }

            tabs.setTabComponentAt(index, pnlTab);
//            pnlTab.revalidate();
//            pnlTab.repaint();  // not working
//            tabs.revalidate();
//            tabs.repaint();    // not working
//            label.repaint();
//            repaint();
        }

        public synchronized void finalizeAdapter() {
            try {
                this.finalize();
            } catch (Throwable ex) {
                Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public synchronized int getSelectedIndex() {
            return archiveTablePanel.getSelectedRow();
        }

        protected void setObjType(ObjectType objType) {
            this.objType = objType;
        }

        protected void setDomain(IdentifierList domain) {
            this.domain = domain;
        }

        protected ObjectType getObjType() {
            return this.objType;
        }

        protected IdentifierList getDomain() {
            return this.domain;
        }

        @Override
        public void retrieveRangeAckReceived(org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader,
                Long interactionTicket, UInteger numberOfChunks, java.util.Map qosProperties) {
            // Later on, do something...
        }

        @Override
        public void retrieveRangeUpdateReceived(org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader,
                Blob chunk, UInteger index, java.util.Map qosProperties) {
            n_objs_counter++;
            refreshTabCounter();
            repaint();
        }

        @Override
        public void retrieveRangeResponseReceived(org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader,
                java.util.Map qosProperties) {
        }

        @Override
        public void retrieveRangeAckErrorReceived(org.ccsds.moims.mo.mal.transport.MALMessageHeader msgHeader,
                org.ccsds.moims.mo.mal.MALStandardError error, java.util.Map qosProperties) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, "retrieveRangeAckErrorReceived", error);
        }

        /*
        @Override
        public synchronized void retrieveResponseReceived(MALMessageHeader msgHeader,
                ArchiveDetailsList objDetails, ElementList objBodies, Map qosProperties) {
            ArchiveCOMObjectsOutput archiveObjectOutput = new ArchiveCOMObjectsOutput(domain, objType, objDetails, objBodies);
            archiveTablePanel.addEntries(archiveObjectOutput);
            n_objs_counter = n_objs_counter + objDetails.size();
            refreshTabCounter();
        }

        @Override
        public synchronized void countResponseReceived(MALMessageHeader msgHeader,
                LongList _LongList0, Map qosProperties) {
            JOptionPane.showMessageDialog(null, _LongList0.toString(),
                    "The count operation returned the following data!", JOptionPane.PLAIN_MESSAGE);
        }

        @Override
        public synchronized void queryResponseReceived(MALMessageHeader msgHeader, ObjectType objType,
                IdentifierList domain, ArchiveDetailsList objDetails, ElementList objBodies, Map qosProperties) {
            ArchiveCOMObjectsOutput archiveObjectOutput = new ArchiveCOMObjectsOutput(domain, objType, objDetails, objBodies);
            archiveTablePanel.addEntries(archiveObjectOutput);
            n_objs_counter = n_objs_counter + objDetails.size();
            refreshTabCounter();

            isOver.release();
        }

        @Override
        public synchronized void queryUpdateReceived(MALMessageHeader msgHeader, ObjectType objType,
                IdentifierList domain, ArchiveDetailsList objDetails, ElementList objBodies, Map qosProperties) {
            ArchiveCOMObjectsOutput archiveObjectOutput = new ArchiveCOMObjectsOutput(domain, objType, objDetails, objBodies);
            archiveTablePanel.addEntries(archiveObjectOutput);
            n_objs_counter = n_objs_counter + objDetails.size();
            refreshTabCounter();
            repaint();
        }

        @Override
        public synchronized void queryAckErrorReceived(MALMessageHeader msgHeader, MALStandardError error, Map qosProperties) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, "queryAckErrorReceived", error);
        }
         */
        protected void deleteAllInTable() {
            try {
                isOver.acquire();
            } catch (InterruptedException ex) {
                Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

            List<ArchivePersistenceObject> comObjects = archiveTablePanel.getAllCOMObjects();

            /*
            for (ArchivePersistenceObject comObject : comObjects) {
                LongList objIds = new LongList();
                objIds.add(comObject.getArchiveDetails().getInstId());
                try {
                    serviceCOMArchive.getArchiveStub().delete(comObject.getObjectType(), comObject.getDomain(), objIds);
                } catch (MALInteractionException ex) {
                    Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MALException ex) {
                    Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
             */
        }

        private JPanel getPanel() {
            return pnlTab;
        }

    }

    public class CloseMouseHandler implements MouseListener {

        private final ArchiveSyncConsumerAdapter adapter;

        CloseMouseHandler(ArchiveSyncConsumerAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void mouseClicked(MouseEvent evt) {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                Component component = tabs.getTabComponentAt(i);
                JPanel panel = adapter.getPanel();

                if (component == panel) {
                    tabs.remove(i);
                    adapter.finalizeAdapter();

                    try {
                        super.finalize();
                    } catch (Throwable ex) {
                        Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return;
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent me) {
        }

        @Override
        public void mouseReleased(MouseEvent me) {
        }

        @Override
        public void mouseEntered(MouseEvent me) {
        }

        @Override
        public void mouseExited(MouseEvent me) {
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

        jLabel6 = new javax.swing.JLabel();
        jButtonStoreAggregation = new javax.swing.JButton();
        jButtonGetAll = new javax.swing.JButton();
        jButtonQuery = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonRetrieve = new javax.swing.JButton();
        jButtonUpdate = new javax.swing.JButton();
        jButtonCount = new javax.swing.JButton();
        tabs = new javax.swing.JTabbedPane();
        homeTab = new javax.swing.JPanel();
        jButtonStoreConversions = new javax.swing.JButton();
        jButtonStoreActions = new javax.swing.JButton();
        jButtonStoreGroups = new javax.swing.JButton();
        TBoxStore = new javax.swing.JTextField();
        jButtonDeleteAll = new javax.swing.JButton();
        test_button = new javax.swing.JButton();

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Archive Manager");
        jLabel6.setToolTipText("");

        jButtonStoreAggregation.setText("getTime");
        jButtonStoreAggregation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStoreAggregationActionPerformed(evt);
            }
        });

        jButtonGetAll.setText("---");
        jButtonGetAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetAllActionPerformed(evt);
            }
        });

        jButtonQuery.setText("---");
        jButtonQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonQueryActionPerformed(evt);
            }
        });

        jButtonDelete.setText("---");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jButtonRetrieve.setText("retrieveRange");
        jButtonRetrieve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRetrieveActionPerformed(evt);
            }
        });

        jButtonUpdate.setText("retrieveRangeAgain");
        jButtonUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateActionPerformed(evt);
            }
        });

        jButtonCount.setText("---");
        jButtonCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCountActionPerformed(evt);
            }
        });

        tabs.setToolTipText("");
        tabs.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tabs.setMaximumSize(new java.awt.Dimension(800, 600));
        tabs.setMinimumSize(new java.awt.Dimension(800, 300));
        tabs.setPreferredSize(new java.awt.Dimension(800, 300));
        tabs.setRequestFocusEnabled(false);
        tabs.addTab("Home", homeTab);

        jButtonStoreConversions.setText("---");
        jButtonStoreConversions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStoreConversionsActionPerformed(evt);
            }
        });

        jButtonStoreActions.setText("---");
        jButtonStoreActions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStoreActionsActionPerformed(evt);
            }
        });

        jButtonStoreGroups.setText("---");
        jButtonStoreGroups.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStoreGroupsActionPerformed(evt);
            }
        });

        TBoxStore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TBoxStoreActionPerformed(evt);
            }
        });

        jButtonDeleteAll.setText("---");
        jButtonDeleteAll.setEnabled(false);
        jButtonDeleteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteAllActionPerformed(evt);
            }
        });

        test_button.setText("---");
        test_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                test_buttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonGetAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonRetrieve))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDeleteAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                    .addComponent(jButtonCount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonQuery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonStoreActions, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonStoreConversions, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                    .addComponent(jButtonStoreGroups, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(test_button, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                    .addComponent(jButtonStoreAggregation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TBoxStore, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(tabs, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 957, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonRetrieve)
                    .addComponent(jButtonStoreAggregation)
                    .addComponent(jButtonUpdate)
                    .addComponent(jButtonStoreGroups)
                    .addComponent(TBoxStore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonQuery)
                    .addComponent(jButtonCount))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonGetAll)
                    .addComponent(jButtonDeleteAll)
                    .addComponent(jButtonDelete)
                    .addComponent(jButtonStoreActions)
                    .addComponent(jButtonStoreConversions)
                    .addComponent(test_button))
                .addContainerGap(438, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGap(129, 129, 129)
                    .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonStoreAggregationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStoreAggregationActionPerformed

    }//GEN-LAST:event_jButtonStoreAggregationActionPerformed

    private void jButtonGetAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetAllActionPerformed
        try {
            GetTimeResponse response = serviceCOMArchiveSync.getArchiveSyncStub().getTime();
            MOWindow genObjType = new MOWindow(response, false);
        } catch (MALInteractionException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonGetAllActionPerformed


    private void jButtonQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonQueryActionPerformed


    }//GEN-LAST:event_jButtonQueryActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed

        ArchivePersistenceObject comObject = ((ArchiveTablePanel) tabs.getSelectedComponent()).getSelectedCOMObject();

        LongList objIds = new LongList();
        objIds.add(comObject.getObjectId());

        /*
        try {
            serviceCOMArchive.getArchiveStub().delete(comObject.getObjectType(), comObject.getDomain(), objIds);
        } catch (MALInteractionException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
        ((ArchiveTablePanel) tabs.getSelectedComponent()).removeSelectedEntry();

    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonRetrieveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRetrieveActionPerformed
        ArchiveSyncConsumerAdapter adapter = new ArchiveSyncConsumerAdapter("Retrieve Range...");

        FineTime from = new FineTime(0);
        MOWindow windowFrom = new MOWindow(from, true);
        try {
            from = (FineTime) windowFrom.getObject();
        } catch (InterruptedIOException ex) {
            return;
        }

        FineTime until = HelperTime.getTimestamp();
        MOWindow windowUntil = new MOWindow(until, true);
        try {
            until = (FineTime) windowUntil.getObject();
        } catch (InterruptedIOException ex) {
            return;
        }

        // Select Parameter Definitions by default
        ObjectTypeList objTypes = new ObjectTypeList();
        objTypes.add(ParameterHelper.PARAMETERDEFINITION_OBJECT_TYPE);
        MOWindow genObjType = new MOWindow(objTypes, true);
        try {
            objTypes = (ObjectTypeList) genObjType.getObject();
        } catch (InterruptedIOException ex) {
            return;
        }

        try {
            serviceCOMArchiveSync.getArchiveSyncStub().retrieveRange(from, until, objTypes, adapter);
        } catch (MALInteractionException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonRetrieveActionPerformed

    @SuppressWarnings("unchecked")
    private void jButtonUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateActionPerformed

        /*
        ArchivePersistenceObject comObject = ((ArchiveTablePanel) tabs.getSelectedComponent()).getSelectedCOMObject();
        MOWindow objBodyWindow = new MOWindow(comObject.getObject(), true);
        ArchiveDetailsList archiveDetailsList = new ArchiveDetailsList();
        archiveDetailsList.add(comObject.getArchiveDetails());
        ElementList finalObject;
         */

    }//GEN-LAST:event_jButtonUpdateActionPerformed

    private void jButtonCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCountActionPerformed

        /*
        // Object Type
        ObjectType objType = AggregationHelper.AGGREGATIONDEFINITION_OBJECT_TYPE;
        MOWindow genObjType = new MOWindow(objType, true);
        try {
            objType = (ObjectType) genObjType.getObject();
        } catch (InterruptedIOException ex) {
            return;
        }

        // Archive Query
        ArchiveQuery archiveQuery = ArchiveSyncConsumerManagerPanel.generateArchiveQuery();
        ArchiveQueryList archiveQueryList = new ArchiveQueryList();
        archiveQueryList.add(archiveQuery);
        MOWindow genArchiveQueryList = new MOWindow(archiveQueryList, true);
        try {
            archiveQueryList = (ArchiveQueryList) genArchiveQueryList.getObject();
        } catch (InterruptedIOException ex) {
            return;
        }

        // Composite Filter
        CompositeFilterSetList compositeFilters = new CompositeFilterSetList();
        CompositeFilterSet compositeFilterSet = new CompositeFilterSet();
        CompositeFilterList compositeFilterList = new CompositeFilterList();
        compositeFilterList.add(ArchiveSyncConsumerManagerPanel.generateCompositeFilter());
        compositeFilterSet.setFilters(compositeFilterList);
        compositeFilters.add(compositeFilterSet);
        MOWindow genFilter = new MOWindow(compositeFilters, true);
        try {
            compositeFilters = (CompositeFilterSetList) genFilter.getObject();
        } catch (InterruptedIOException ex) {
            return;
        }

        ArchiveConsumerAdapter adapter = new ArchiveConsumerAdapter("Count...");
        adapter.setObjType(objType);

        try {
            serviceCOMArchive.getArchiveStub().count(objType, archiveQueryList, compositeFilters, adapter);
        } catch (MALInteractionException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        JDialog frame = new JDialog();
        int n = JOptionPane.showConfirmDialog(frame, "Would you like to automatically query and get the objects?", "Query?", JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {
            try {
                serviceCOMArchive.getArchiveStub().query(true, objType, archiveQueryList, compositeFilters, adapter);
            } catch (MALInteractionException ex) {
                Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MALException ex) {
                Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         */

    }//GEN-LAST:event_jButtonCountActionPerformed

    private void jButtonStoreConversionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStoreConversionsActionPerformed

        /*
//        archiveDetailsList = new ArchiveDetailsList();
//        archiveDetailsList.add(serviceCOMArchive.generateArchiveDetails(new Long(0)));
        ArchiveDetailsList archiveDetailsList = HelperArchive.generateArchiveDetailsList(null, null, serviceCOMArchive.getConnectionDetails());
        objType = ConversionHelper.DISCRETECONVERSION_OBJECT_TYPE;
        DiscreteConversionDetailsList objList1 = new DiscreteConversionDetailsList();
        objList1.add(this.generateDiscreteConversionDetails());

        try {
            outObjId = serviceCOMArchive.getArchiveStub().store(Boolean.TRUE, objType, serviceCOMArchive.getConnectionDetails().getDomain(), archiveDetailsList, objList1);
        } catch (MALInteractionException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

//        archiveDetailsList = new ArchiveDetailsList();
//        archiveDetailsList.add(serviceCOMArchive.generateArchiveDetails(new Long(0)));
        archiveDetailsList = HelperArchive.generateArchiveDetailsList(null, null, serviceCOMArchive.getConnectionDetails());
        objType = ConversionHelper.LINECONVERSION_OBJECT_TYPE;
        LineConversionDetailsList objList2 = new LineConversionDetailsList();
        objList2.add(this.generateLineConversionDetails());

        try {
            outObjId = serviceCOMArchive.getArchiveStub().store(Boolean.TRUE, objType, serviceCOMArchive.getConnectionDetails().getDomain(), archiveDetailsList, objList2);
        } catch (MALInteractionException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

//        archiveDetailsList = new ArchiveDetailsList();
//        archiveDetailsList.add(serviceCOMArchive.generateArchiveDetails(new Long(0)));
        archiveDetailsList = HelperArchive.generateArchiveDetailsList(null, null, serviceCOMArchive.getConnectionDetails());
        objType = ConversionHelper.POLYCONVERSION_OBJECT_TYPE;
        PolyConversionDetailsList objList3 = new PolyConversionDetailsList();
        objList3.add(this.generatePolyConversionDetails());

        try {
            outObjId = serviceCOMArchive.getArchiveStub().store(Boolean.TRUE, objType, serviceCOMArchive.getConnectionDetails().getDomain(), archiveDetailsList, objList3);
        } catch (MALInteractionException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

//        archiveDetailsList = new ArchiveDetailsList();
//        archiveDetailsList.add(serviceCOMArchive.generateArchiveDetails(new Long(0)));
        archiveDetailsList = HelperArchive.generateArchiveDetailsList(null, null, serviceCOMArchive.getConnectionDetails());
        objType = ConversionHelper.RANGECONVERSION_OBJECT_TYPE;
        RangeConversionDetailsList objList4 = new RangeConversionDetailsList();
        objList4.add(this.generateRangeConversionDetails());

        try {
            outObjId = serviceCOMArchive.getArchiveStub().store(Boolean.TRUE, objType, serviceCOMArchive.getConnectionDetails().getDomain(), archiveDetailsList, objList4);
        } catch (MALInteractionException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
         */

    }//GEN-LAST:event_jButtonStoreConversionsActionPerformed

    private void jButtonStoreActionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStoreActionsActionPerformed


    }//GEN-LAST:event_jButtonStoreActionsActionPerformed

    private void jButtonStoreGroupsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStoreGroupsActionPerformed
        /*
        // Object Type
        ObjectType objType = new ObjectType(new UShort(4), new UShort(8), new UOctet((short) 1), new UShort(1));

        // Domain
        IdentifierList domain = serviceCOMArchive.getConnectionDetails().getDomain();

        // Archive details
        ArchiveDetailsList archiveDetailsList = HelperArchive.generateArchiveDetailsList(null, null, serviceCOMArchive.getConnectionDetails());
        MOWindow genArchiveDetailsList = new MOWindow(archiveDetailsList, true);
        try {
            archiveDetailsList = (ArchiveDetailsList) genArchiveDetailsList.getObject();
        } catch (InterruptedIOException ex) {
            return;
        }

        GroupDetails group = new GroupDetails();
        GroupDetailsList groupList = new GroupDetailsList();

//        group.setName(new Identifier("Group1"));
        group.setDescription("A group of the 3 first Parameter Definitions.");
        ObjectType objTypeParameterDef = new ObjectType(new UShort(4), new UShort(2), new UOctet((short) 1), new UShort(1));
        group.setObjectType(objTypeParameterDef);
        group.setDomain(domain);
        LongList objIds = new LongList();
        objIds.add(new Long(1));
        objIds.add(new Long(2));
        objIds.add(new Long(3));

        group.setInstanceIds(objIds);

        groupList.add(group);

        // Actually you have to use the Action service to store the definiton
        try {
            serviceCOMArchive.getArchiveStub().store(false, objType, domain, archiveDetailsList, groupList);
        } catch (MALInteractionException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MALException ex) {
            Logger.getLogger(ArchiveSyncConsumerManagerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
    }//GEN-LAST:event_jButtonStoreGroupsActionPerformed

    private void TBoxStoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TBoxStoreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TBoxStoreActionPerformed

    private void jButtonDeleteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteAllActionPerformed

    }//GEN-LAST:event_jButtonDeleteAllActionPerformed

    public static ArchiveQuery generateArchiveQuery() {
        // ArchiveDetails
        ArchiveQuery archiveQuery = new ArchiveQuery();
        archiveQuery.setDomain(null);
        archiveQuery.setNetwork(null);
        archiveQuery.setProvider(null);
        archiveQuery.setRelated(new Long(0));
        archiveQuery.setSource(null);
        archiveQuery.setStartTime(null);
        archiveQuery.setEndTime(null);
        archiveQuery.setSortOrder(null);
        archiveQuery.setSortFieldName(null);

        return archiveQuery;
    }

    public static CompositeFilter generateCompositeFilter() {
        CompositeFilter compositeFilter = new CompositeFilter();
        compositeFilter.setFieldName("name");
        compositeFilter.setType(ExpressionOperator.fromNumericValue(ExpressionOperator.EQUAL_NUM_VALUE));
        compositeFilter.setFieldValue(new Identifier("AggregationUpdate"));

        return compositeFilter;
    }


    private void test_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_test_buttonActionPerformed


    }//GEN-LAST:event_test_buttonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField TBoxStore;
    private javax.swing.JPanel homeTab;
    private javax.swing.JButton jButtonCount;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonDeleteAll;
    private javax.swing.JButton jButtonGetAll;
    private javax.swing.JButton jButtonQuery;
    private javax.swing.JButton jButtonRetrieve;
    private javax.swing.JButton jButtonStoreActions;
    private javax.swing.JButton jButtonStoreAggregation;
    private javax.swing.JButton jButtonStoreConversions;
    private javax.swing.JButton jButtonStoreGroups;
    private javax.swing.JButton jButtonUpdate;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JButton test_button;
    // End of variables declaration//GEN-END:variables
}