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

import managers.EngineOverlay;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.openrdf.model.URI;
import slib.utils.ex.SLIB_Ex_Critic;
import tools.IDGetter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Lin_1998;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.metrics.ic.utils.IcUtils;
import slib.utils.ex.SLIB_Exception;
import tools.MeasuresConf;
import tools.USI_IO;
import tools.Utils;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class Index {

    private ArrayList<Entity> index = new ArrayList();
    private HashMap<URI, Integer> idf = new HashMap();
    private EngineOverlay engineManager;
    /**
     *
     */
    public static Index instance = null;

    /**
     *
     * @param en
     */
    public void addEntity(Entity en) {
        index.add(en);
        for (URI u : en.getConcepts()) {
            addToIDF(u);
        }
    }

    /**
     *
     * @param en
     */
    public void removeEntity(Entity en) {
        index.remove(en);
        for (URI u : en.getConcepts()) {
            removeFromIDF(u);
        }
    }

    private void addToIDF(URI u) {
        if (idf.containsKey(u)) {
            idf.put(u, idf.get(u) + 1);
        } else {
            idf.put(u, 1);
        }
    }

    private void removeFromIDF(URI u) {
        idf.put(u, idf.get(u) - 1);
    }

    /**
     *
     * @return
     */
    public ArrayList<Entity> getIndex() {
        return index;
    }

    /**
     *
     * @return
     */
    public EngineOverlay getEngineManager() {
        return engineManager;
    }

    /**
     *
     * @param id
     * @return
     */
    public Entity getEntity(int id) {
        return index.get(id);
    }

    /**
     *
     * @param id
     * @return
     */
    public Entity getEntityById(final String id) {
        return (Entity) CollectionUtils.find(index, new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                return ((Entity) arg0).getId().equals(id);
            }
        });
    }

    /**
     *
     * @return
     */
    public static Index getInstance() {
        if (instance == null) {
            instance = new Index();
        }
        return instance;
    }

    private Index() {
    }

    /**
     *
     * @param ontologyPath
     * @param indexPath
     * @param descriptorPath
     * @param gFormat
     * @param basename
     * @param IDGetterPath
     * @throws SLIB_Ex_Critic
     */
    public void init(String ontologyPath, String descriptorPath, String indexPath, GFormat gFormat, String basename) throws SLIB_Ex_Critic {
        if (engineManager == null) {
            try {
                engineManager = USI_IO.loadGraph(ontologyPath, descriptorPath, gFormat, basename);
                if (!indexPath.equals("")) {
                    byte[] encoded = Files.readAllBytes(Paths.get(indexPath));
                    String indexJson = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();

                    // Root
                    Object obj = JSONValue.parse(indexJson);
                    JSONObject json = (JSONObject) obj;

                    // Index object = array
                    Object indexObj = json.get("index");
                    JSONArray arrayOfDocuments = (JSONArray) indexObj;
                    // Get all element and index them
                    for (int j = 0; j < arrayOfDocuments.size(); j++) {
                        JSONObject docObj = (JSONObject) arrayOfDocuments.get(j);
                        String href = (String) docObj.get("href");
                        String title = (String) docObj.get("title");
                        String id;
                        try {
                            id = "D_" + (Long) docObj.get("id");
                        } catch(java.lang.ClassCastException e) {
                            id = (String) docObj.get("id");
                        }
                        JSONArray annotation = (JSONArray) docObj.get("conceptIds");
                        Set<URI> conceptSet = new HashSet();
                        for (int k = 0; k < annotation.size(); k++) {
                            String base = engineManager.getEngine().getGraph().getURI().stringValue();
                            if(!base.endsWith("/") && !base.endsWith("."))
                                base += "/";
                            URI toAdd = URIFactoryMemory.getSingleton().createURI(base + (String) annotation.get(k));
                            if(!toAdd.getLocalName().equals(getEngineManager().getFromURI(toAdd)))
                                conceptSet.add(toAdd);
                        }
                        Entity en = new Entity();
                        en.setId(id);
                        en.setTitle(title);
                        en.setHref(href);
                        en.setConcepts(conceptSet);
                        addEntity(en);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void init(EngineOverlay EO) {
        engineManager = EO;
    }

    /**
     *
     * @return
     */
    public HashMap<URI, Double> getIDF() {
        HashMap<URI, Double> real = new HashMap();
        for (Entry<URI, Integer> e : idf.entrySet()) {
            Double v = Math.log((double) index.size() / (double) e.getValue());
            real.put(e.getKey(), v);
        }
        return real;
    }

    /**
     *
     * @param id1
     * @param id2
     * @return
     * @throws SLIB_Ex_Critic
     */
    public double computePairwise(String id1, String id2) throws SLIB_Ex_Critic {
        return engineManager.calculateGroupwise(getEntityById(id1).getConcepts(), getEntityById(id2).getConcepts());
    }

    /**
     *
     * @param path
     * @param reverse
     * @return
     * @throws SLIB_Ex_Critic
     */
    public double[][] computeIndexSquareSimilarityMatrix(String path, boolean reverse) throws SLIB_Ex_Critic {
        return computeGroupwiseSimilarityMatrix(path, index, index, reverse);
    }

    /**
     *
     * @param path
     * @param C
     * @param R
     * @param reverse
     * @return
     * @throws SLIB_Ex_Critic
     */
    public double[][] computeGroupwiseSimilarityMatrix(String path, ArrayList<Entity> C, ArrayList<Entity> R, boolean reverse) throws SLIB_Ex_Critic {
        double[][] matrix;
        File file = new File(path);
        file.delete();
        boolean square = false;
        if (R == C) {
            square = true;
        }

        matrix = new double[R.size()][C.size()];
        String r = "";

        int i = 0;
        if (!path.equals("")) {
            for (Entity e1 : index) {
                r += "," + e1.getId();
            }
            Utils.appendToFile(path, r + "\n");
        }
        for (Entity e1 : R) {
            int count = 0;
            if (!path.equals("")) {
                r = e1.getId();
            }
            for (Entity e2 : C) {
                if (matrix[count][i] != 0.0) {
                    if (!path.equals("")) {
                        r += "," + matrix[count][i];
                    }
                    matrix[i][count] = matrix[count][i];
                } else {
                    double result;
                    // Distance = 1-sim
                    if (reverse) {
                        result = 1 - engineManager.calculateGroupwise(e1.getConcepts(), e2.getConcepts());
                    } else {
                        result = engineManager.calculateGroupwise(e1.getConcepts(), e2.getConcepts());
                    }
                    if (!path.equals("")) {
                        r += result;
                    }
                    matrix[i][count] = result;
                }
                count++;
            }
            if (!path.equals("")) {
                r += "\n";
                Utils.appendToFile(path, r);
            }
            i++;
        }
        return matrix;
    }

    /**
     *
     * @param C
     * @param R
     * @param reverse
     * @return
     * @throws SLIB_Ex_Critic
     */
    public double[][] computePairwiseSimilarityMatrix(Set<URI> C, Set<URI> R, boolean reverse) throws SLIB_Ex_Critic {
        double[][] matrix;
        matrix = new double[C.size()][R.size()];

        int i = 0;
        for (URI u1 : C) {
            int count = 0;
            for (URI u2 : R) {
                double result;
                // Distance = 1-sim
                if (reverse) {
                    result = 1 - engineManager.calculatePairwise(u1, u2);
                } else {
                    result = engineManager.calculatePairwise(u1, u2);
                }
                matrix[i][count] = result;
                count++;
            }
            i++;
        }
        return matrix;
    }

    /**
     *
     * @param C
     * @param R
     * @param fullMatrix
     * @return
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public double[][] computeLinSimilarityMatrixOptimized(Set<URI> C, Set<URI> R, MatrixDouble fullMatrix) throws SLIB_Ex_Critic, SLIB_Exception {
        double[][] matrix;
        matrix = new double[fullMatrix.getNbColumns()][fullMatrix.getNbRows()];
        URI[] C_array = C.toArray(new URI[0]);
        URI[] R_array = R.toArray(new URI[0]);
        SM_Engine engine = engineManager.getEngine();
        ICconf ICconf = MeasuresConf.getMeasure().getICconf();

        for (int row = 0; row < R_array.length; row++) {
            for (int column = 0; column < C_array.length; column++) {
                matrix[column][row] = -1;
            }
        }

        int[] rowIDToColID = new int[R.size()];
        for (int row = 0; row < R_array.length; row++) {
            rowIDToColID[row] = fullMatrix.getColIndex(R_array[row]);
        }
        int[] colIDToRowID = new int[C.size()];
        for (int column = 0; column < C_array.length; column++) {
            colIDToRowID[column] = fullMatrix.getRowIndex(C_array[column]);
        }

        Set<URI>[] C_ancestors = new Set[C_array.length];
        for (int column = 0; column < C_array.length; column++) {
            C_ancestors[column] = engine.getAncestorsInc(C_array[column]);
        }

        for (int row = 0; row < R_array.length; row++) {
            Set<URI> R_ancestors = engine.getAncestorsInc(R_array[row]);
            double ic_R = engine.getIC(ICconf, R_array[row]);
            int columnSim = rowIDToColID[row];
            for (int column = 0; column < C_array.length; column++) {
                int rowSim = colIDToRowID[column];
                if (rowSim != -1 && columnSim != -1 && matrix[columnSim][rowSim] != -1) {
                    matrix[column][row] = matrix[columnSim][rowSim];
                } else {
                    double ic_C = engine.getIC(ICconf, C_array[column]);
                    double ic_MICA = IcUtils.searchMax_IC_MICA(R_array[row], C_array[column], R_ancestors, C_ancestors[column], engine.getIC_results(ICconf));
                    matrix[column][row] = Sim_pairwise_DAG_node_Lin_1998.sim(ic_R, ic_C, ic_MICA);
                    if (rowSim != -1 && columnSim != -1) {
                        matrix[columnSim][rowSim] = matrix[column][row];
                    }
                }
            }
        }
        return matrix;
    }

    private ArrayList<Entity> getEntitiesById(ArrayList<String> arr) {
        ArrayList<Entity> temp = new ArrayList();
        for (String s : arr) {
            Entity e = getEntityById(s);
            if (e != null) {
                temp.add(e);
            } else {
                System.out.println("error");
            }
        }
        return temp;
    }

    /**
     *
     * @param path
     * @param arr
     * @param reverse
     * @return
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public double[][] computeSquareSimilarityMatrix(String path, ArrayList<String> arr) throws SLIB_Ex_Critic, SLIB_Exception {
        ArrayList<Entity> temp = getEntitiesById(arr);
//        return computeGroupwiseSimilarityMatrix(path, temp, temp, reverse);
        return computeOptimizedDissimilarityMatrix(temp);
    }

    /**
     *
     * @param entities
     * @return
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public double[][] computeOptimizedDissimilarityMatrix(ArrayList<Entity> entities) throws SLIB_Ex_Critic, SLIB_Exception {
        double[][] matrix = new double[entities.size()][entities.size()];

        HashSet<URI> uris = new HashSet();
        ArrayList<LinkedHashSet<URI>> conceptLists = new ArrayList();
        for (Entity e : entities) {
            conceptLists.add(new LinkedHashSet(e.getConcepts()));
            uris.addAll(e.getConcepts());
        }

        SimilarityMatrix similarityMatrix = new SimilarityMatrix(uris, conceptLists);
        for (int c_i = 0; c_i < entities.size(); c_i++) {
            Entity e1 = entities.get(c_i);
            similarityMatrix.setValidConcepts(e1.getConcepts());
            double[] rows = similarityMatrix.getBMA();
            for (int r_i = c_i; r_i < entities.size(); r_i++) {
                matrix[c_i][r_i] = matrix[r_i][c_i] = 1 - rows[r_i];
            }
        }
        return matrix;
    }

    /**
     *
     * @param path
     * @throws Exception
     */
    public void exportIndex(String path) throws Exception {
        File file = new File(path);
        file.delete();

        // Only in json
        Utils.appendToFile(path, "{ \"index\": [");
        for (int k = 0; k < index.size(); k++) {
            Utils.appendToFile(path, index.get(k).formatJson());
            if (k != index.size() - 1) {
                Utils.appendToFile(path, ",\n");
            }
        }
        // Only in json
        Utils.appendToFile(path, "]}");
    }

    public String toString() {
        String indexStr = "{ \"index\": [";
        for (int k = 0; k < index.size(); k++) {
            indexStr += index.get(k).formatJson();
            if (k != index.size() - 1) {
                indexStr += ",\n";
            }
        }
        indexStr += "]}";
        return indexStr;
    }

    /**
     *
     * @param l
     * @return
     */
    public boolean correctEntities(ArrayList<String> l) {
        ArrayList<Entity> ents = getEntitiesById(l);
        boolean b = true;
        for (Entity e : ents) {
            if (e.getConcepts().isEmpty()) {
                b = false;
            }
        }
        return b;
    }
}