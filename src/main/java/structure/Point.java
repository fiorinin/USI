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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class Point {

    private double x;
    private double y;
    private String label;

    /**
     *
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     *
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     *
     * @return
     */
    public double getX() {
        return x;
    }

    /**
     *
     * @param x
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     *
     * @return
     */
    public double getY() {
        return y;
    }

    /**
     *
     * @param y
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     *
     * @param x
     * @param y
     * @param l
     */
    public Point(double x, double y, String l) {
        label = l;
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @param p
     * @return
     */
    public double distance(Point p) {
        double sqrt = ((p.x - x) * (p.x - x)) + ((p.y - y) * (p.y - y));
        return Math.sqrt(sqrt);
    }

    public String toString() {
        return label + " at " + x + "," + y;
    }

    /**
     *
     * @param path
     * @return
     */
    public static ArrayList<Point> readFromFile(String path) {
        ArrayList<Point> pts = new ArrayList();
        try {
            BufferedReader b = new BufferedReader(new FileReader(path));
            String c;
            while ((c = b.readLine()) != null) {
                Point p = new Point(Double.parseDouble(c.split(",")[0]), Double.parseDouble(c.split(",")[1]), c.split(",")[2]);
                pts.add(p);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Point.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Point.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pts;
    }
}
