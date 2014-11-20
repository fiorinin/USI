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

import java.util.Set;
import managers.EngineOverlay;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import slib.indexer.IndexHash;
import slib.indexer.mesh.Indexer_MESH_XML;
import slib.sglib.algo.graph.utils.GAction;
import slib.sglib.algo.graph.utils.GActionType;
import slib.sglib.algo.graph.utils.GraphActionExecutor;
import slib.sglib.algo.graph.validator.dag.ValidatorDAG;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class USI_IO {
    
    public static EngineOverlay loadGraph(String ontologyFilePath, String descriptorPath, GFormat gFormat, String baseURI) throws SLIB_Ex_Critic, SLIB_Exception {
        URIFactoryMemory factory = URIFactoryMemory.getSingleton();
        IndexHash ih = new IndexHash();
        String b;
        G ontologyGraph;
        
        if(gFormat.equals(GFormat.MESH_XML)) {
            b = "http://usi";
            URI uri = factory.createURI(b);
            ontologyGraph = new GraphMemory(uri);

            GDataConf data = new GDataConf(gFormat, ontologyFilePath);
            data.addParameter("prefix", b+"/");
            GraphLoaderGeneric.populate(data, ontologyGraph);
            removeMeshCycles(ontologyGraph);
        } else if(gFormat.equals(GFormat.NTRIPLES)) {
            
        } else {
            b = baseURI;
            URI uri = factory.createURI(b);
            ontologyGraph = new GraphMemory(uri);
            GDataConf data = new GDataConf(gFormat, ontologyFilePath);
            GraphLoaderGeneric.populate(data, ontologyGraph);
            GraphActionExecutor.applyAction(factory, new GAction(GActionType.REROOTING), ontologyGraph);
            ih = Indexer_JSON.buildIndex(factory, descriptorPath, b);
        } 
        
        return new EngineOverlay(ontologyGraph, ih);
    }

    /*
     * We remove the cycles of the graph in order to obtain 
     * a rooted directed acyclic graph (DAG) and therefore be able to 
     * use most of semantic similarity measures.
     * see http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh
     */
    private static void removeMeshCycles(G meshGraph) throws SLIB_Ex_Critic {
        URIFactoryMemory factory = URIFactoryMemory.getSingleton();

        // We remove the edges creating cycles
        URI ethicsURI = factory.createURI(meshGraph.getURI().stringValue() + "/D004989");
        URI moralsURI = factory.createURI(meshGraph.getURI().stringValue() + "/D009014");

        // We retrieve the direct subsumers of the concept (D009014)
        Set<E> moralsEdges = meshGraph.getE(RDFS.SUBCLASSOF, moralsURI, Direction.OUT);
        for (E e : moralsEdges) {

            System.out.println("\t" + e);
            if (e.getTarget().equals(ethicsURI)) {
                System.out.println("\t*** Removing edge " + e);
                meshGraph.removeE(e);
            }
        }

        ValidatorDAG validatorDAG = new ValidatorDAG();
        boolean isDAG = validatorDAG.containsTaxonomicDag(meshGraph);

        System.out.println("MeSH Graph is a DAG: " + isDAG);

        // We remove the edges creating cycles
        // see http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh

        URI hydroxybutyratesURI = factory.createURI(meshGraph.getURI().stringValue() + "/D006885");
        URI hydroxybutyricAcidURI = factory.createURI(meshGraph.getURI().stringValue() + "/D020155");

        // We retrieve the direct subsumers of the concept (D009014)
        Set<E> hydroxybutyricAcidEdges = meshGraph.getE(RDFS.SUBCLASSOF, hydroxybutyricAcidURI, Direction.OUT);
        for (E e : hydroxybutyricAcidEdges) {
            System.out.println("\t" + e);
            if (e.getTarget().equals(hydroxybutyratesURI)) {
                System.out.println("\t*** Removing edge " + e);
                meshGraph.removeE(e);
            }
        }
    }
}
