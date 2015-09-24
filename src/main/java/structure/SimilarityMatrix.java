/*
 * Copyright or © or Copr. école des mines d'Alès (2014) 
 * 
 * <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 * 
 * This software is a computer program whose purpose is to semantically
 * index entities of any type, given an annotated neighbourhood.
 * 
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.URI;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Lin_1998;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.metrics.ic.utils.IcUtils;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import tools.MeasuresConf;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class SimilarityMatrix {

    private ArrayList<URI> A0;
    private ArrayList<Integer> validConcepts;
    private ArrayList<Double> allICs;
    private double sumICs;
    private double lastRemovedIC;
    private ArrayList<URI> neighbours;
    private ArrayList<DocBMAData> BMADatas;
    private ArrayList<ArrayList<DocBMAData>> conceptIDToDocID;
    private double[][] sim_annotation_entities; // double[columns][rows]
    private Map<URI, Integer> columnIndex;
    private Map<URI, Integer> rowIndex;
    private int[] idColMaxForThisRow;
    private double[] BMAs;
    private ArrayList<Integer> modifiedIdColMax;
    private int lastRemovedConcept;
    
    SM_Engine engine = Index.getInstance().getEngineManager().getEngine();
    ICconf ICconf = MeasuresConf.getMeasure().getICconf();

    /**
     *
     * @return
     */
    public ArrayList<Integer> getValidConcepts() {
        return validConcepts;
    }

    /**
     *
     * @param id
     * @return
     */
    public double[] getCol(int id) {
        return sim_annotation_entities[id];
    }

    /**
     *
     * @param id
     * @return
     */
    public double getSumMaxCol(int id) {
        double sumMaxCol = 0;
        for (DocBMAData doc : BMADatas) {
            sumMaxCol += doc.getMaxSimColumn()[id];
        }
        return sumMaxCol;
    }

    /**
     *
     * @param valid
     */
    public void setValidConcepts(Set<URI> valid) {
        validConcepts.clear();
        for (URI u : valid) {
            validConcepts.add(columnIndex.get(u));
        }
        fullUpdateAllIdColMax();
    }

    /**
     *
     * @return
     */
    public ArrayList<URI> getA0() {
        return A0;
    }

    /**
     *
     * @return
     */
    public LinkedHashSet<URI> getValidConceptsURI() {
        LinkedHashSet<URI> columns = new LinkedHashSet();
        for (int c_i : validConcepts) {
            columns.add(A0.get(c_i));
        }
        return columns;
    }

    /**
     *
     * @param row
     * @return
     */
    public int getidColMaxForThisRow(int row) {
        return idColMaxForThisRow[row];
    }

    /**
     *
     * @param u
     * @return
     */
    public int getColIDFromURI(URI u) {
        return columnIndex.get(u);
    }

    /**
     *
     * @param u
     * @return
     */
    public int getRowIDFromURI(URI u) {
        return rowIndex.get(u);
    }

    /**
     *
     * @return
     */
    public int getNbCol() {
        return sim_annotation_entities.length;
    }

    /**
     *
     * @return
     */
    public int getNbValidCol() {
        return validConcepts.size();
    }

    /**
     *
     * @return
     */
    public int getNbRow() {
        return sim_annotation_entities[0].length;
    }

    /**
     *
     * @param col
     * @param row
     * @return
     */
    public double get(int col, int row) {
        return sim_annotation_entities[col][row];
    }

    /**
     *
     * @param col
     * @param row
     * @return
     */
    public double getByURIs(URI col, URI row) {
        return sim_annotation_entities[columnIndex.get(col)][rowIndex.get(row)];
    }

    /**
     *
     * @param unionConcepts
     * @param annotatedNeighbours
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public SimilarityMatrix(Set<URI> unionConcepts, ArrayList<LinkedHashSet<URI>> annotatedNeighbours) throws SLIB_Ex_Critic, SLIB_Exception {
        A0 = new ArrayList(unionConcepts); // Columns
        neighbours = new ArrayList(); // Rows
        modifiedIdColMax = new ArrayList();

        HashSet<URI> tmp = new HashSet();
        for (LinkedHashSet neighbour : annotatedNeighbours) {
            tmp.addAll(neighbour);
        }
        neighbours.addAll(tmp);

        this.validConcepts = new ArrayList();
        this.BMADatas = new ArrayList();
        this.allICs = new ArrayList();
        idColMaxForThisRow = new int[neighbours.size()];
        sim_annotation_entities = new double[A0.size()][neighbours.size()];
        columnIndex = new HashMap();
        rowIndex = new HashMap();
        conceptIDToDocID = new ArrayList();

        // Matrix init: all values at -1
        // Also filling columnIndex/rowIndex to fit with sim_annotation_entities
        for (int c_i = 0; c_i < this.A0.size(); c_i++) {
            URI conceptCol = A0.get(c_i);
            columnIndex.put(conceptCol, c_i);
            validConcepts.add(c_i);
            allICs.add(engine.getIC(ICconf, conceptCol));
            for (int r_i = 0; r_i < neighbours.size(); r_i++) {
                URI conceptRow = neighbours.get(r_i);
                rowIndex.put(conceptRow, r_i);
                sim_annotation_entities[c_i][r_i] = -1;
            }
        }

        init();

        // Init BMAData
        for (URI u : neighbours) {
            conceptIDToDocID.add(new ArrayList());
        }
        for (LinkedHashSet<URI> concepts : annotatedNeighbours) {
            BMADatas.add(new DocBMAData(concepts, this));
            for (URI concept : concepts) {
                conceptIDToDocID.get(rowIndex.get(concept)).add(BMADatas.get(BMADatas.size() - 1));
            }
        }
        BMAs = new double[BMADatas.size()];
        sumICs = 0;
        for(double ic : allICs) {
            sumICs += ic;
        }
    }

    private void init() throws SLIB_Ex_Critic, SLIB_Exception {

        // Computing symetry
        int[] rowIDToColID = new int[getNbRow()];
        for (int row = 0; row < getNbRow(); row++) {
            if (columnIndex.containsKey(neighbours.get(row))) {
                rowIDToColID[row] = columnIndex.get(neighbours.get(row));
            } else {
                rowIDToColID[row] = -1;
            }
        }
        int[] colIDToRowID = new int[getNbCol()];
        for (int column = 0; column < getNbCol(); column++) {
            if (rowIndex.containsKey(A0.get(column))) {
                colIDToRowID[column] = rowIndex.get(A0.get(column));
            } else {
                colIDToRowID[column] = -1;
            }
        }

        // Caching columns ancestors
        Set<URI>[] C_ancestors = new Set[validConcepts.size()];
        for (int column = 0; column < validConcepts.size(); column++) {
            C_ancestors[column] = engine.getAncestorsInc(A0.get(column));
        }

        // Filling matrix
        for (int row = 0; row < neighbours.size(); row++) {
            Set<URI> R_ancestors = engine.getAncestorsInc(neighbours.get(row));
            double ic_R = engine.getIC(ICconf, neighbours.get(row));
            int columnSim = rowIDToColID[row];
            for (int column = 0; column < validConcepts.size(); column++) {
                int rowSim = colIDToRowID[column];
                if (rowSim != -1 && columnSim != -1 && sim_annotation_entities[columnSim][rowSim] != -1) {
                    sim_annotation_entities[column][row] = sim_annotation_entities[columnSim][rowSim];
                } else {
                    double ic_C = engine.getIC(ICconf, A0.get(column));
                    double ic_MICA = IcUtils.searchMax_IC_MICA(neighbours.get(row), A0.get(column), R_ancestors, C_ancestors[column], engine.getIC_results(ICconf));
                    sim_annotation_entities[column][row] = Sim_pairwise_DAG_node_Lin_1998.sim(ic_R, ic_C, ic_MICA);
                    if (rowSim != -1 && columnSim != -1) {
                        sim_annotation_entities[columnSim][rowSim] = sim_annotation_entities[column][row];
                    }
                }
            }
        }
        updateAllIdColMax();
    }

    private void updateAllIdColMax() {
        for (int r_i = 0; r_i < this.getNbRow(); r_i++) {
            updateIdColMax(r_i, BMADatas, false);
        }
    }

    private void fullUpdateAllIdColMax() {
        for (int r_i = 0; r_i < this.getNbRow(); r_i++) {
            // New max
            idColMaxForThisRow[r_i] = validConcepts.get(0);
            for (int column : validConcepts) {
                if (sim_annotation_entities[column][r_i] > sim_annotation_entities[idColMaxForThisRow[r_i]][r_i]) {
                    idColMaxForThisRow[r_i] = column;
                }
            }
        }
        for (DocBMAData BMAData : BMADatas) {
            BMAData.updateAllSumMax();
        }
    }

    // Updates the idColMaxForThisRow for a given row and updates corresponding docs
    /**
     *
     * @param row
     * @param documents
     * @param updateBMA
     */
    public void updateIdColMax(int row, ArrayList<DocBMAData> documents, boolean updateBMA) {
        double delta = sim_annotation_entities[idColMaxForThisRow[row]][row];
        // New max
        idColMaxForThisRow[row] = validConcepts.get(0);
        for (int column : validConcepts) {
            if (sim_annotation_entities[column][row] > sim_annotation_entities[idColMaxForThisRow[row]][row]) {
                idColMaxForThisRow[row] = column;
            }
        }
        delta = sim_annotation_entities[idColMaxForThisRow[row]][row] - delta; // new - former
        // Impacted documents
        if (updateBMA) {
            for (DocBMAData BMAData : documents) {
                BMAData.updateSumMaxRowValue(delta);
            }
        }
    }

    /**
     *
     * @param toRemove
     * @throws SLIB_Exception
     */
    public void removeValidConcept(int toRemove) throws SLIB_Exception {
        lastRemovedConcept = toRemove;
        modifiedIdColMax.clear();
        validConcepts.remove((Integer) toRemove);
        for (int r_i = 0; r_i < idColMaxForThisRow.length; r_i++) {
            if (idColMaxForThisRow[r_i] == toRemove) {
                updateIdColMax(r_i, conceptIDToDocID.get(r_i), true);
                modifiedIdColMax.add(r_i); // Saves the concerned rows
            }
        }
        for (DocBMAData BMAData : BMADatas) {
            BMAData.updateSumMaxCol(toRemove, true);
        }
        // Update sumICs
        lastRemovedIC = allICs.get(toRemove);
        sumICs -= lastRemovedIC;
    }

    /**
     *
     * @throws SLIB_Exception
     */
    public void restoreLastRemovedConcept() throws SLIB_Exception {
        validConcepts.add(lastRemovedConcept);
        for (int r_i : modifiedIdColMax) {
            restoreIdColMax(r_i, conceptIDToDocID.get(r_i), true);
        }
        for (DocBMAData BMAData : BMADatas) {
            BMAData.updateSumMaxCol(lastRemovedConcept, false);
        }
        // Update sumICs
        sumICs += lastRemovedIC;
    }

    /**
     *
     * @param row
     * @param documents
     * @param updateBMA
     */
    public void restoreIdColMax(int row, ArrayList<DocBMAData> documents, boolean updateBMA) {
        double delta = sim_annotation_entities[idColMaxForThisRow[row]][row];
        idColMaxForThisRow[row] = lastRemovedConcept;
        delta = sim_annotation_entities[idColMaxForThisRow[row]][row] - delta; // new - former

        if (updateBMA) {
            for (DocBMAData BMAData : documents) {
                BMAData.updateSumMaxRowValue(delta);
            }
        }
    }
    
    public double getSumICs() {
//        double sum = 0;
//        for(URI c : getValidConceptsURI()) {
//            try {
//                sum += engine.getIC(ICconf, c);
//            } catch (SLIB_Exception ex) {
//                Logger.getLogger(SimilarityMatrix.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return sum;
        return sumICs;
    }

    /**
     *
     * @return
     */
    public double[] getBMA() {
        int k = 0;
        for (DocBMAData document : BMADatas) {
            BMAs[k++] = document.BMA();
        }
        return BMAs;
    }
}
