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

import java.util.LinkedHashSet;
import org.openrdf.model.URI;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class DocBMAData {

    private double[] maxSimColumn;
    private double sumMaxRow;
    private double sumMaxColumn;
    private double saved_sumMaxColumn;
    SimilarityMatrix simData;
    int[] concepts;

    /**
     *
     * @return
     */
    public double[] getMaxSimColumn() {
        return maxSimColumn;
    }

    /**
     *
     * @param conceptsURI
     * @param simData
     */
    public DocBMAData(LinkedHashSet<URI> conceptsURI, SimilarityMatrix simData) {
        this.concepts = new int[conceptsURI.size()];
        this.simData = simData;

        //maxSimRow = new double[conceptsURI.size()];
        maxSimColumn = new double[simData.getNbCol()];

        sumMaxColumn = 0;
        saved_sumMaxColumn = 0;
        sumMaxRow = 0;

        // Setting row sum and max
        int k = 0;
        for (URI concept : conceptsURI) {
            this.concepts[k++] = simData.getRowIDFromURI(concept);
        }

        for (int r_i : this.concepts) {
            //maxSimRow[pos] = simData.get(simData.getidColMaxForThisRow(r_i), r_i);
            //sumMaxRow += maxSimRow[pos];
            sumMaxRow += simData.get(simData.getidColMaxForThisRow(r_i), r_i);
        }

        // Setting col sum and max
        for (int c_i = 0; c_i < maxSimColumn.length; c_i++) {
            maxSimColumn[c_i] = simData.get(c_i, concepts[0]);
            for (int r_i : concepts) {
                if (simData.get(c_i, r_i) > maxSimColumn[c_i]) {
                    maxSimColumn[c_i] = simData.get(c_i, r_i);
                }
            }
            sumMaxColumn += maxSimColumn[c_i];
        }
    }

    /**
     *
     * @param delta
     */
    public void updateSumMaxRowValue(double delta) {
        sumMaxRow += delta;
//        sumMaxRow += 0;
//        for(int r_i : this.concepts) {                
//            sumMaxRow += simData.get(simData.getidColMaxForThisRow(r_i), r_i);
//        }
    }

    /**
     *
     * @return
     */
    public double BMA() {
        double bma = ((sumMaxColumn / simData.getNbValidCol()) + (sumMaxRow / concepts.length)) / 2;
        return bma;
    }

    /**
     *
     * @param conceptAddedOrRemoved
     * @param remove
     */
    public void updateSumMaxCol(int conceptAddedOrRemoved, boolean remove) {
        if (remove) {
            sumMaxColumn -= maxSimColumn[conceptAddedOrRemoved];
            saved_sumMaxColumn = maxSimColumn[conceptAddedOrRemoved];
        } else {
            sumMaxColumn += saved_sumMaxColumn;
        }
    }

    /**
     *
     */
    public void updateAllSumMax() {
        sumMaxRow = 0;
        for (int r_i : this.concepts) {
            sumMaxRow += simData.get(simData.getidColMaxForThisRow(r_i), r_i);
        }
        sumMaxColumn = 0;
        for (int c_i : simData.getValidConcepts()) {
            sumMaxColumn += maxSimColumn[c_i];
        }
    }
}
