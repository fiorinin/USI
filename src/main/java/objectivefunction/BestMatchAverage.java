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

package objectivefunction;

import structure.SimilarityMatrix;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import tools.MapUtil;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class BestMatchAverage {

    private ArrayList<LinkedHashSet<URI>> neighbourhood;
    private ArrayList<Double> distances;
    private LinkedHashSet<URI> annotations;
    private double objectiveFunctionMargin;
    private boolean map;
    private Set<URI> trueAnnot;
    SimilarityMatrix similarityMatrix;

    /**
     *
     * @param trueAnnot
     */
    public void setTrueAnnot(Set<URI> trueAnnot) {
        this.trueAnnot = trueAnnot;
    }

    /**
     *
     * @return
     */
    public Set<URI> getAnnotations() {
        return annotations;
    }

    /**
     *
     * @param ann
     * @param neighb
     * @param d
     * @param ObjectiveFunctionMargin
     * @param map
     * @param simMatrix
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public BestMatchAverage(LinkedHashSet<URI> ann, ArrayList<LinkedHashSet<URI>> neighb, ArrayList<Double> d, double ObjectiveFunctionMargin, boolean map, SimilarityMatrix simMatrix) throws SLIB_Ex_Critic, SLIB_Exception {
        similarityMatrix = simMatrix;
        this.map = map;
        distances = d;
        neighbourhood = neighb;
        this.objectiveFunctionMargin = ObjectiveFunctionMargin;
        annotations = ann;
    }

    /**
     *
     * @return
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public double labelize() throws SLIB_Ex_Critic, SLIB_Exception {
        if (annotations.size() == 0) {
            return 0;
        }
        double score = computeScore();

        boolean best = true;
        while (best && similarityMatrix.getValidConcepts().size() > 1) {
            double betterScore = -Double.MAX_VALUE;
            int bestRemove = -1;
            ArrayList<Integer> validConcepts = new ArrayList();
            validConcepts.addAll(similarityMatrix.getValidConcepts());
            for (int idToRemove : validConcepts) {
                similarityMatrix.removeValidConcept(idToRemove);
//                System.out.print(similarityMatrix.getValidConceptsURI());
                double similarity = computeScore();
//                System.out.println(" = "+similarity);
                similarityMatrix.restoreLastRemovedConcept();

                if (bestRemove < 0 || similarity > betterScore) {
                    betterScore = similarity;
                    bestRemove = idToRemove;
                }
            }
//            if (((betterScore > score || (betterScore <= score && Math.abs(betterScore - score) / neighbourhood.size() <= objectiveFunctionMargin))) && bestRemove != -1) {
//            if (((betterScore > score || (betterScore <= score && Math.abs(betterScore - score) / score <= objectiveFunctionMargin))) && bestRemove != -1) {
            if (betterScore > score && bestRemove != -1) {
                similarityMatrix.removeValidConcept(bestRemove);
                score = betterScore;
            } else {
                best = false;
            }
        }
        annotations = similarityMatrix.getValidConceptsURI();
        return score;
    }

    /**
     *
     * @return
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public double labelizeQuick() throws SLIB_Ex_Critic, SLIB_Exception {
        if (annotations.size() == 0) {
            return 0;
        }
        double betterScore = computeScore();
        ArrayList<Integer> validConcepts = new ArrayList();
        validConcepts.addAll(similarityMatrix.getValidConcepts());
        LinkedHashMap<Integer, Double> nonOrderedConcepts = new LinkedHashMap();
        for (int concept : validConcepts) {
            double sumSimAllDocs = 0;
            double[] col = similarityMatrix.getCol(concept);
            for (double sim : col) {
                sumSimAllDocs += sim;
            }
            nonOrderedConcepts.put(concept, sumSimAllDocs);
        }
        Map<Integer, Double> orderedConcepts = MapUtil.sortByValueDESC(nonOrderedConcepts);
        for (int idToRemove : orderedConcepts.keySet()) {
            similarityMatrix.removeValidConcept(idToRemove);
            double similarity = computeScore();
            if (similarity > betterScore || (similarity <= betterScore && Math.abs(similarity - betterScore) / betterScore <= objectiveFunctionMargin)) {
                betterScore = similarity;
            } else {
                similarityMatrix.restoreLastRemovedConcept();
            }
        }
        annotations = similarityMatrix.getValidConceptsURI();
        return betterScore;
    }

    /**
     *
     * @return
     */
    public double computeScore() {
        double[] sims = similarityMatrix.getBMA();

        double similarity = 0;
        int i;
        for (i = 0; i < sims.length; i++) {
            double s = sims[i];
            similarity += s;
        }
        double cardA = similarityMatrix.getValidConcepts().size();
//        return (similarity / i) - objectiveFunctionMargin * cardA;
        return (similarity / i) - objectiveFunctionMargin * cardA - objectiveFunctionMargin * ((1/cardA) * similarityMatrix.getSumICs());
    }
}