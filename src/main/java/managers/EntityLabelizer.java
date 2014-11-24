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

package managers;

import structure.ConcentricClusterSet;
import structure.Index;
import structure.Point;
import structure.PointDist;
import structure.SimilarityMatrix;
import objectivefunction.BestMatchAverage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.openrdf.model.URI;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class EntityLabelizer {

    private Set<URI> A0 = new LinkedHashSet();
    private int filter;
    private boolean includeAncestors = false;
    private int neighboursNumberMin;
    private int spread;
    private double ObjectiveFunctionMargin;
    private double similarityThreshold;
    private boolean map;
    private double weightCoeff;
    private LinkedHashMap<String, Double> neighbours = new LinkedHashMap();
    private LinkedHashMap<String, Double> extendedNeighbours = new LinkedHashMap();
    private double finalScore = 0;

    /**
     *
     * @param filter
     */
    public void setFilter(int filter) {
        this.filter = filter;
    }

    /**
     *
     * @param spread
     */
    public void setSpread(int spread) {
        this.spread = spread;
    }

    /**
     *
     * @param similarityThreshold
     */
    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    /**
     *
     * @param weightCoeff
     */
    public void setWeightCoeff(double weightCoeff) {
        this.weightCoeff = weightCoeff;
    }

    /**
     *
     * @param map
     */
    public void setMap(boolean map) {
        this.map = map;
    }

    /**
     *
     * @return
     */
    public double getScore() {
        return finalScore;
    }

    /**
     *
     * @return
     */
    public LinkedHashMap<String, Double> getNeighbours() {
        return neighbours;
    }

    public void setNeighboursNumberMin(int neighboursNumberMin) {
        this.neighboursNumberMin = neighboursNumberMin;
    }

    public void setIncludeAncestors(boolean includeAncestors) {
        this.includeAncestors = includeAncestors;
    }

    /**
     *
     * @param clusterSizeMin
     * @param ObjectiveFunctionMargin
     */
    public EntityLabelizer(int clusterSizeMin, double ObjectiveFunctionMargin) {
        this.neighboursNumberMin = clusterSizeMin;
        this.ObjectiveFunctionMargin = ObjectiveFunctionMargin;
    }

    /**
     *
     * @return
     * @throws SLIB_Ex_Critic
     */
    public int prepare() throws SLIB_Ex_Critic {
        neighbours.clear();
        ConcentricClusterSet ccs = ConcentricClusterSet.getInstance();
        // Set clusters
        ArrayList<PointDist> distances = ccs.getPoints();
        Iterator it = distances.iterator();

        // Clustering
        neighbours = new LinkedHashMap();
//        if (!radius) {
        while (it.hasNext()) {
            if (neighbours.size() >= neighboursNumberMin) {
                if (spread > neighboursNumberMin && extendedNeighbours.size() < spread - neighboursNumberMin) {
                    PointDist e = (PointDist) it.next();
                    Point p = e.getP();
                    extendedNeighbours.put(p.getLabel(), e.getDist());
                } else {
                    break;
                }
            } else {
                PointDist e = (PointDist) it.next();
                Point p = e.getP();
                neighbours.put(p.getLabel(), e.getDist());
            }
        }
        int neighbors = neighbours.size();
        return neighbors;
    }

    /**
     *
     * @return
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public Set<URI> getBaseAnnotation() throws SLIB_Ex_Critic, SLIB_Exception {
        LinkedHashMap<URI, Double> tf = new LinkedHashMap();

        EngineOverlay em = Index.getInstance().getEngineManager();
        LinkedHashMap<String, Double> loopOn = new LinkedHashMap();
        loopOn.putAll(neighbours);
        if (extendedNeighbours.size() > 0) {
            loopOn.putAll(extendedNeighbours);
        }
        for (Entry<String, Double> e : loopOn.entrySet()) {
            Set<URI> c = Index.getInstance().getEntityById(e.getKey()).getConcepts();
            for (URI u : c) {
                if (tf.containsKey(u)) {
                    tf.put(u, tf.get(u) + 1);
                } else {
                    tf.put(u, 1.0);
                }
            }
        }
        A0.clear();
        if (filter == 0) {
            A0.addAll(tf.keySet());
        } else if (filter == 1) {
            for (Entry<URI, Double> e : tf.entrySet()) {
                if (e.getValue() > 2) {
                    A0.add(e.getKey());
                }
            }
        } else if (filter == 2) {
            LinkedHashSet<URI> tfcopy = new LinkedHashSet();
            tfcopy.addAll(tf.keySet());
            ArrayList<LinkedHashSet<URI>> pseudoDocument = new ArrayList();
            pseudoDocument.add(tfcopy);
            SimilarityMatrix matrix = new SimilarityMatrix(tfcopy, pseudoDocument);
            for (URI URIAnnot : tf.keySet()) {
                int nb = 0;
                boolean first = true;
                for (Entry<String, Double> document : neighbours.entrySet()) {
                    if (!first) {
                        Set<URI> docConcepts = Index.getInstance().getEntityById(document.getKey()).getConcepts();
                        boolean closeEnough = false;
                        for (URI concept : docConcepts) {
                            if (matrix.getByURIs(concept, URIAnnot) > similarityThreshold) {
                                closeEnough = true;
                            }
                        }
                        if (closeEnough) {
                            nb++;
                        }
                    }
                    first = false;
                }
                if (nb >= 2) {
                    A0.add(URIAnnot);
                }
            }
        }
        if (includeAncestors) {
            for(URI u : A0) {
                A0.addAll(Index.getInstance().getEngineManager().getEngine().getAncestorsInc(u));
            }
        }
        return A0;
    }

    /**
     *
     * @return
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public Set<URI> labelize() throws SLIB_Ex_Critic, SLIB_Exception {
        // Initial annotation
        LinkedHashSet<URI> annotation = new LinkedHashSet();
        annotation.addAll(getBaseAnnotation());
        // Annotated

        ArrayList<LinkedHashSet<URI>> neighbourhood = new ArrayList();
        ArrayList<Double> distances = new ArrayList();
        for (Entry<String, Double> c : neighbours.entrySet()) {
            LinkedHashSet<URI> entityURIs = new LinkedHashSet();
            entityURIs.addAll(Index.getInstance().getEntityById(c.getKey()).getConcepts());
            distances.add(1 - (c.getValue() * weightCoeff)); // Similarity for a given distance on map
//                distances.add(c.getValue()); // Distance on map
            neighbourhood.add(entityURIs);
        }
        SimilarityMatrix similarityMatrix = new SimilarityMatrix(annotation, neighbourhood);
        BestMatchAverage labels_BMA = new BestMatchAverage(annotation, neighbourhood, distances, ObjectiveFunctionMargin, map, similarityMatrix);
        finalScore = labels_BMA.labelize();
        return labels_BMA.getAnnotations();
    }

    /**
     *
     * @param trueAnnot
     * @return
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public double ObjectiveFunctionScore(LinkedHashSet<URI> trueAnnot) throws SLIB_Ex_Critic, SLIB_Exception {
        ArrayList<LinkedHashSet<URI>> neighbourhood = new ArrayList();
        ArrayList<Double> distances = new ArrayList();
        for (Entry<String, Double> c : neighbours.entrySet()) {
            LinkedHashSet<URI> entityURIs = new LinkedHashSet();
            entityURIs.addAll(Index.getInstance().getEntityById(c.getKey()).getConcepts());
            distances.add(1 - (c.getValue() * weightCoeff)); // Similarity for a given distance on map
            neighbourhood.add(entityURIs);
        }
        SimilarityMatrix similarityMatrix = new SimilarityMatrix(trueAnnot, neighbourhood);
        BestMatchAverage labels_BMA = new BestMatchAverage(trueAnnot, neighbourhood, distances, ObjectiveFunctionMargin, map, similarityMatrix);
        return labels_BMA.computeScore();
    }
}