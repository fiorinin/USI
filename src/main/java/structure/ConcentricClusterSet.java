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
import java.util.Collections;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class ConcentricClusterSet {

    private ArrayList<PointDist> points = new ArrayList();
    private static ConcentricClusterSet instance = null;

    /**
     *
     * @return
     */
    public static ConcentricClusterSet getInstance() {
        if (instance == null) {
            instance = new ConcentricClusterSet();
        }
        return instance;
    }

    /**
     *
     * @return
     */
    public ArrayList<PointDist> getPoints() {
        return points;
    }

    /**
     *
     * @param id
     * @return
     */
    public PointDist getMDSDistanceById(String id) {
        PointDist point = null;
        for (PointDist p : points) {
            if (p.getP().getLabel().equals(id)) {
                point = p;
            }
        }
        return point;
    }

    /**
     *
     * @param id
     */
    public void removePoint(String id) {
        for (PointDist p : points) {
            if (p.getP().getLabel().equals(id)) {
                points.remove(p);
                break;
            }
        }
    }

    private ConcentricClusterSet() {
    }

    /**
     *
     * @param reference
     * @param pts
     */
    public void process(Point reference, ArrayList<Point> pts) {
        points.clear();
        for (Point p : pts) {
            points.add(new PointDist(p, reference));
        }
        Collections.sort(points);
    }

    /**
     *
     * @param points
     */
    public void setPoints(ArrayList<PointDist> points) {
        this.points = points;
    }

    public String toString() {
        String description = "";
        for (PointDist p : points) {
            description += "Point " + p.getP().getLabel() + " [" + p.getP().getX() + "," + p.getP().getY() + "] located at " + p.getDist() + " from centroid.\n";
        }
        return description;
    }
}