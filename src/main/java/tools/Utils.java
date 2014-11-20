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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.URI;
import slib.graph.model.impl.repo.URIFactoryMemory;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class Utils {

    /**
     *
     * @param set
     * @param base
     * @return
     */
    public static Set<URI> convertToURI(Set<String> set, String base) {
        URIFactoryMemory factory = URIFactoryMemory.getSingleton();
        HashSet<URI> uris = new HashSet();
        for (String s : set) {
            URI uri = factory.getURI(base + s);
            uris.add(uri);
        }
        return uris;
    }

    /**
     *
     * @param set
     * @param base
     * @return
     */
    public static Set<String> convertToStrings(Set<URI> set, String base) {
        HashSet<String> strings = new HashSet();
        for (URI s : set) {
            String id = s.toString().replace(base, "");
            strings.add(id);
        }
        return strings;
    }

    /**
     *
     * @param path
     * @param data
     */
    public static void appendToFile(String path, String data) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(data);
            bufferWritter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param path
     * @param data
     */
    public static void writeToFile(String path, String data) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        appendToFile(path, data);
    }

    /**
     *
     * @param path
     * @return
     */
    public static String readFile(String path) {
        BufferedReader br = null;
        String out = "";
        try {

            String sCurrentLine;

            br = new BufferedReader(new FileReader(path));

            while ((sCurrentLine = br.readLine()) != null) {
                out += sCurrentLine;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return out;
    }

//    public static LinkedHashSet<String> queryObirs(Obirs obirs, Set<URI> annotation, int numberOfResults) throws ParseException {
//        LinkedHashSet<String> ids = new LinkedHashSet();
//        int number = annotation.size();
//
//        // Create curl query
//        String curl = "{\"numberOfResults\": " + numberOfResults + ",\"concepts\": [";
//        int done = 0;
//        for (URI a : annotation) {
//            if (!a.getLocalName().equals("D006801")) {
//                if (done != 0) {
//                    curl += ",";
//                }
//                curl += "{\"id\": \"" + a.getLocalName() + "\", \"weight\": 1}";
//                done++;
//            }
//        }
//        curl += "]}";
//
//        // Launch query and store results in indexJson
//        String indexJson = obirs.query(curl);
//
//        // Parsing
//        Object obj = JSONValue.parse(indexJson);
//        JSONArray json = (JSONArray) obj;
//        for (int j = 0; j < json.size(); j++) {
//            JSONObject docO = (JSONObject) json.get(j);
//            String id = (String) docO.get("docId");
//            if (!id.startsWith("D_")) {
//                ids.add("D_" + id);
//            } else {
//                ids.add(id);
//            }
//        }
//        return ids;
//    }
    /**
     *
     * @param progressPercentage
     */
    public static void updateProgress(double progressPercentage) {
        final int width = 50; // progress bar width in chars
        System.out.print("\r[");
        int i = 0;
        for (; i <= (int) (progressPercentage * width); i++) {
            System.out.print(".");
        }
        for (; i < width; i++) {
            System.out.print(" ");
        }
        System.out.print("] (" + (Math.round(progressPercentage * 10000.0) / 100.0) + "%)");
    }
}
