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

package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class Config {

    /**
     *
     */
    public static String resources;
    /**
     *
     */
    public static String output;
    /**
     *
     */
    public static String schema;
    /**
     *
     */
    public static String converter;
    /**
     *
     */
    public static boolean MAP = false;
    /**
     *
     */
    public static double SIMTHRESHOLD = -Double.MAX_VALUE;
    /**
     *
     */
    public static double SCALE = -Double.MAX_VALUE;
    /**
     *
     */
    public static double OBJECTIVE_FUNCTION_MARGIN = -Double.MAX_VALUE;
    /**
     *
     */
    public static int MINNEIGHBOURS = Integer.MIN_VALUE;
    /**
     *
     */
    public static int SPREAD = Integer.MIN_VALUE;
    /**
     *
     */
    public static int FILTER = 1;

    /**
     *
     * @param c
     * @param softCheck
     */
    public static void loadConfig(String c, boolean softCheck) {
        if (c.contains("simfilter")) {
            FILTER = 2;
        } else if (c.contains("nofilter") || !c.contains("filter")) {
            FILTER = 0;
        }
        if (c.contains("map")) {
            MAP = true;
        }

        if (!softCheck) {
            checkConf();
        }
    }

    /**
     *
     */
    public static void checkConf() {
        boolean ok = true;
        String error = "";
        if (SPREAD < MINNEIGHBOURS) {
            ok = false;
            error += "The SPREAD value must be equal or higher than the MINNEIGHBOURS value.\n";
        }
        if (MINNEIGHBOURS == Integer.MIN_VALUE || OBJECTIVE_FUNCTION_MARGIN == -Double.MAX_VALUE) {
            ok = false;
            error += "You must initialize the number of neighbors and the margin of the objective function (set the variables MINNEIGHBOURS and OBJECTIVE_FUNCTION_MARGIN)\n";
        }
        if (FILTER == 2 && SIMTHRESHOLD == -Double.MAX_VALUE) {
            ok = false;
            error += "You must initialize the value of similarity threshold when using expansion or filter (set the variable SIMTHRESHOLD)\n";
        }
        if (!ok) {
            System.err.println(error);
            System.exit(0);
        }
    }

    /**
     *
     */
    public static void exportParameters() {
        try {
            Properties props = new Properties();
            props.setProperty("resources", resources);
            props.setProperty("output", "" + output);
            props.setProperty("schema", "" + schema);
            props.setProperty("converter", "" + converter);
            props.setProperty("MAP", "" + MAP);
            props.setProperty("SIMTHRESHOLD", "" + SIMTHRESHOLD);
            props.setProperty("SCALE", "" + SCALE);
            props.setProperty("OBJECTIVE_FUNCTION_MARGIN", "" + OBJECTIVE_FUNCTION_MARGIN);
            props.setProperty("MINNEIGHBOURS", "" + MINNEIGHBOURS);
            props.setProperty("SPREAD", "" + SPREAD);
            props.setProperty("FILTER", "" + FILTER);
            File f = new File("usi.properties");
            OutputStream out = new FileOutputStream(f);
            props.store(out, "This is an optional header comment string");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param path
     */
    public static void loadParams(String path) {
        Properties props = new Properties();
        InputStream is = null;
        try {
            File f = new File("usi.properties");
            is = new FileInputStream(f);
        } catch (Exception e) {
            is = null;
        }
        try {
            props.load(is);
        } catch (Exception e) {
        }

        resources = props.getProperty("resources");
        output = props.getProperty("output");
        schema = props.getProperty("schema");
        converter = props.getProperty("converter");
        MAP = Boolean.valueOf(props.getProperty("MAP"));
        SIMTHRESHOLD = new Double(props.getProperty("SIMTHRESHOLD"));
        SCALE = new Double(props.getProperty("SCALE"));
        OBJECTIVE_FUNCTION_MARGIN = new Double(props.getProperty("OBJECTIVE_FUNCTION_MARGIN"));
        MINNEIGHBOURS = new Integer(props.getProperty("MINNEIGHBOURS"));
        SPREAD = new Integer(props.getProperty("SPREAD"));
        FILTER = new Integer(props.getProperty("FILTER"));
    }
}
