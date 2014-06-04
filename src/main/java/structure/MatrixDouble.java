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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Class used to represent a Matrix which can be used to store values associated
 * to pairs of elements.
 *
 * @param <C> Object to index Columns
 * @param <R> Object to index Rows
 *
 * @author Sebastien Harispe adapted for USI by Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class MatrixDouble<C, R> {

    private Map<C, Integer> columnIndex;
    private Map<R, Integer> rowIndex;
    private Double[][] matrix;
    private int columns_number;
    private int rows_number;

    /**
     *
     * @return
     */
    public Map<R, Integer> getRowIndex() {
        return rowIndex;
    }

    /**
     *
     * @return
     */
    public Map<C, Integer> getColumnIndex() {
        return columnIndex;
    }

    /**
     *
     * @param u
     * @return
     */
    public int getColIndex(C u) {
        if (columnIndex.containsKey(u)) {
            return columnIndex.get(u);
        } else {
            return -1;
        }
    }

    /**
     *
     * @param u
     * @return
     */
    public int getRowIndex(R u) {
        if (rowIndex.containsKey(u)) {
            return rowIndex.get(u);
        } else {
            return -1;
        }
    }

    /**
     *
     * @param c
     * @param r
     * @return
     */
    public double get(int c, int r) {
        return matrix[c][r];
    }

    /**
     *
     * @param matrix
     */
    public void setMatrix(Double[][] matrix) {
        this.matrix = matrix;
    }

    /**
     *
     * @param matrix
     */
    public void setMatrix(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                this.matrix[i][j] = matrix[i][j];
            }
        }
    }

    /**
     * Create a matrix filled with null values considering the given indexes.
     *
     * @param columResources the columns
     * @param rowResources the rows
     */
    public MatrixDouble(LinkedHashSet<C> columResources, LinkedHashSet<R> rowResources) {
        init(columResources, rowResources);
    }

    /**
     * Create a matrix filled with null values considering the given indexes
     *
     * @param columResources
     * @param rowResources
     * @param initValue default value
     */
    public MatrixDouble(LinkedHashSet<C> columResources, LinkedHashSet<R> rowResources, Double initValue) {
        init(columResources, rowResources);

        if (initValue != null) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    matrix[i][j] = initValue;
                }
            }
        }
    }

    private void init(LinkedHashSet<C> columResources, LinkedHashSet<R> rowResources) {
        columns_number = columResources.size();
        rows_number = rowResources.size();

        columnIndex = new HashMap<C, Integer>(columns_number);
        rowIndex = new HashMap<R, Integer>(rows_number);
        matrix = new Double[columns_number][rows_number];

        int id = 0;

        for (C rc : columResources) {
            columnIndex.put(rc, id);
            id++;
        }

        id = 0;
        for (R rc : rowResources) {
            rowIndex.put(rc, id);
            id++;
        }
    }

    /**
     * Return the column associated to the given element
     *
     * @param r the element
     * @return a tab corresponding to the values associated to the element
     */
    public Double[] getColumn(C r) {
        if (isInColumnIndex(r)) {
            return matrix[columnIndex.get(r)];
        }
        return null;
    }

    /**
     * Return the row associated to the given element
     *
     * @param r
     * @return
     */
    public Double[] getRow(R r) {

        if (!isInRowIndex(r)) {
            return null;
        }

        Double[] row = new Double[columns_number];
        int row_resource_id = rowIndex.get(r);

        for (int j = 0; j < columns_number; j++) {
            row[j] = matrix[j][row_resource_id];
        }
        return row;
    }

    /**
     *
     * @param colResource
     * @param rowResource
     * @return
     */
    public Double getValueCheckIndex(C colResource, R rowResource) {
        if (isInColumnIndex(colResource) && isInRowIndex(rowResource)) {
            return matrix[columnIndex.get(colResource)][rowIndex.get(rowResource)];
        }
        return null;
    }

    /**
     *
     * @param colResource
     * @param rowResource
     * @return
     * @throws SLIB_Ex_Critic
     */
    public Double getValue(C colResource, R rowResource) throws SLIB_Ex_Critic {
        try {
            return matrix[columnIndex.get(colResource)][rowIndex.get(rowResource)];
        } catch (Exception e) {
            throw new SLIB_Ex_Critic("Undefined index contains col index " + colResource + " " + isInColumnIndex(colResource) + "\ncontains row index " + rowResource + " " + isInRowIndex(rowResource) + " in matrix " + e.getMessage());
        }
    }

    /**
     *
     * @param colResource
     * @param rowResource
     * @param value
     */
    public void setValue(C colResource, R rowResource, Double value) {
        matrix[columnIndex.get(colResource)][rowIndex.get(rowResource)] = value;
    }

    /**
     *
     * @param r
     * @return
     */
    public boolean isInRowIndex(R r) {

        return rowIndex.keySet().contains(r);
    }

    /**
     *
     * @param r
     * @return
     */
    public boolean isInColumnIndex(C r) {
        return columnIndex.keySet().contains(r);
    }

    /**
     *
     * @return
     */
    public int getNbColumns() {
        return columns_number;
    }

    /**
     *
     * @return
     */
    public int getNbRows() {
        return rows_number;
    }

    /**
     *
     * @return
     */
    public boolean isSquare() {
        return rows_number == columns_number;
    }

    /**
     * @return the maximal value stored in the matrix
     */
    public Double getMax() {
        Double max = null;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] != null && (max == null || matrix[i][j] > max)) {
                    max = matrix[i][j];
                }
            }
        }
        return max;
    }

    /**
     * @param v 
     * @return the maximal value stored in the column of the given resource
     * @throws IllegalArgumentException if the given value cannot be associated
     * to a column
     */
    public Double getMaxColumn(C v) {

        if (!isInColumnIndex(v)) {
            throw new IllegalArgumentException("Unable to locate " + v + "in the column index");
        }

        Double[] columnScore = getColumn(v);
        Double max = null;
        for (int i = 0; i < rows_number; i++) {
            if (columnScore[i] != null && (max == null || max < columnScore[i])) {
                max = columnScore[i];
            }
        }
        return max;
    }

    /**
     * @param v 
     * @return the maximal value stored in the row of the given resource
     * @throws IllegalArgumentException if the given value cannot be associated
     * to a row
     */
    public Double getMaxRow(R v) {

        if (!isInRowIndex(v)) {
            throw new IllegalArgumentException("Unable to locate " + v + "in the row index");
        }

        Double[] rowScore = getRow(v);
        Double max = null;
        for (int i = 0; i < columns_number; i++) {
            if (rowScore[i] != null && (max == null || max < rowScore[i])) {
                max = rowScore[i];
            }
        }
        return max;
    }

    /**
     * Return the minimal value stored in the matrix
     *
     * @return null if the matrix is empty
     */
    public Double getMin() {

        Double min = null;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {

                if (matrix[i][j] != null && (min == null || matrix[i][j] < min)) {
                    min = matrix[i][j];
                }
            }
        }
        return min;
    }

    /**
     * Return the average of contained valued. Null values are excluded.
     *
     * @return null if the matrix is only composed of null value.
     */
    public Double getAverage() {
        Double sum = 0.;
        double count = 0.;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] != null) {
                    sum += matrix[i][j];
                    count++;
                }
            }
        }
        if (count == 0) {
            return null;
        }
        return sum / count;

    }

    /**
     * @return the underlying matrix
     */
    public Double[][] getMatrix() {
        return matrix;
    }

    @Override
    public String toString() {

        String out = "";
        for (C v : columnIndex.keySet()) {
            out += "\t" + v.toString();
        }

        out += "\n";

        for (R vj : rowIndex.keySet()) {

            out += vj.toString();

            for (C v : columnIndex.keySet()) {
                try {
                    out += "\t" + getValue(v, vj);
                } catch (SLIB_Ex_Critic e) {
                    throw new RuntimeException("Ooops an error occur in Matrix class");// no problem
                }
            }
            out += "\n";
        }
        out += "\n";

        return out;
    }

    /**
     *
     * @return
     */
    public Set<C> getColumnElements() {
        return columnIndex.keySet();
    }

    /**
     *
     * @return
     */
    public Set<R> getRowElements() {
        return rowIndex.keySet();
    }

    /**
     * Compute the sum of the values contained in the matrix. null cells are
     * skipped. If the matrix only contains null values, sum is equal to null.
     *
     * @return the sum of the values contained in the matrix.
     */
    public Double getSum() {
        double sum = 0;
        boolean touched = false;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {

                if (matrix[i][j] != null) {
                    sum += matrix[i][j];
                    touched = true;
                }
            }
        }
        if (touched) {
            return sum;
        } else {
            return null;
        }
    }

//    public double sumMaxSim(HashSet<R> Co, LinkedHashSet<C> restrictedDi, LinkedHashSet<C> Dj) {
//        double num = 0;
//        double den = 0;
//        
//        for(R c : Co) {
//            // Compute max with both di and dj
//            double maxn = getMaxRow(c,restrictedDi);
//            double maxd = getMaxRow(c,Dj);
//            num += maxn;
//            den += maxd;
//        }
//        
//        return num/den;
//    }
    /**
     *
     * @param r_id
     * @param restrictedCol
     * @return
     */
    public Double getMaxRow(int r_id, int[] restrictedCol) {
        Double max = null;
        if (restrictedCol.length == 0) {
            return null;
        } else {
            max = matrix[restrictedCol[0]][r_id];
        }
        for (int c_id : restrictedCol) {
            if (max < matrix[c_id][r_id]) {
                max = matrix[c_id][r_id];
            }
        }
        return max;
    }

    /**
     *
     * @param uris
     * @return
     */
    public int[] getRowFromURIs(LinkedHashSet<R> uris) {
        int[] ids = new int[uris.size()];
        int num = 0;
        for (R u : uris) {
            ids[num++] = rowIndex.get(u);
        }
        return ids;
    }

    /**
     *
     * @param restrictedRow
     * @param restrictedCol
     * @return
     */
    public Double getMin(LinkedHashSet<R> restrictedRow, LinkedHashSet<C> restrictedCol) {
        Double min = null;
        for (R r : restrictedRow) {
            Integer idr = rowIndex.get(r);
            for (C c : restrictedCol) {
                Integer idc = columnIndex.get(c);
                if (matrix[idc][idr] != null && (min == null || matrix[idc][idr] < min)) {
                    min = matrix[idc][idr];
                }
            }
        }
        return min;
    }

    /**
     *
     * @param c_id
     * @param restrictedRow
     * @return
     */
    public Double getMaxColumn(int c_id, int[] restrictedRow) {
        Double max = null;
        if (restrictedRow.length == 0) {
            return null;
        } else {
            max = matrix[c_id][restrictedRow[0]];
        }
        for (int r_id : restrictedRow) {
            if (max < matrix[c_id][r_id]) {
                max = matrix[c_id][r_id];
            }
        }
        return max;
    }

    /**
     *
     * @param uris
     * @return
     */
    public int[] getColFromURIs(Set<C> uris) {
        int[] ids = new int[uris.size()];
        int num = 0;
        for (C u : uris) {
            ids[num++] = columnIndex.get(u);
        }
        return ids;
    }

    /**
     *
     * @param r
     * @param k
     * @return
     * @throws SLIB_Ex_Critic
     */
    public ArrayList<C> getkMin(R r, int k) throws SLIB_Ex_Critic {
        ArrayList<C> vals = new ArrayList();
        Double[] row = getRow(r);
        ArrayList<Integer> selected = new ArrayList();

        while (vals.size() < k) {
            Double min = Double.MAX_VALUE;
            int minItem = -1;
            for (int i = 0; i < row.length; i++) {
                if (row[i] < min && !selected.contains(i) && !r.equals(getFromRowAndVal(r, row[i]))) {
                    min = row[i];
                    minItem = i;
                }
            }
            selected.add(minItem);
            C col = getFromRowAndVal(r, min);
            if (col != null) {
                vals.add(col);
            } else {
                throw new IllegalArgumentException("Unable to locate the column : " + r + " " + min);
            }
        }
        return vals;
    }

    private C getFromRowAndVal(R r, Double d) throws SLIB_Ex_Critic {
        for (C c : columnIndex.keySet()) {
            Double current = getValue(c, r);
            if (current == d) {
                return c;
            }
        }
        return null;
    }
}