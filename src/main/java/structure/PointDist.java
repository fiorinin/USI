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

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class PointDist implements Comparable {

    private Point p;
    private double dist;

    /**
     *
     * @return
     */
    public Point getP() {
        return p;
    }

    /**
     *
     * @return
     */
    public double getDist() {
        return dist;
    }

    /**
     *
     * @param p
     * @param c
     */
    public PointDist(Point p, Point c) {
        this.p = p;
        dist = p.distance(c);
    }

    /**
     *
     * @param p
     * @param dist
     */
    public PointDist(Point p, double dist) {
        this.p = p;
        this.dist = dist;
    }

    @Override
    public int compareTo(Object o) {
        PointDist p = (PointDist) o;
        return this.dist > p.getDist() ? 1 : (this.dist < p.getDist() ? -1 : 0);
    }

    public String toString() {
        return "Point " + p + ", distance from centroid = " + dist;
    }
}
