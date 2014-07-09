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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.URI;
import slib.indexer.IndexElementBasic;
import slib.indexer.IndexHash;
import slib.sglib.model.impl.repo.URIFactoryMemory;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class Indexer_JSON {

    public static IndexHash buildIndex(URIFactoryMemory factory, String descriptorPath, String base) {
        try {
            IndexHash index = new IndexHash();
            byte[] encoded = Files.readAllBytes(Paths.get(descriptorPath));
            String indexJson = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
            
            String[] keyvals = indexJson.replace("{", "}").split(",");
            for(String keyval : keyvals) {
                String[] split = keyval.trim().replace("\"", "").split(":");
                String key = split[0];
                String val = split[1];
                
                String uriConceptAsString = base + key;
                URI uriConcept = factory.createURI(uriConceptAsString);

                IndexElementBasic i = new IndexElementBasic(uriConcept, val.trim());
                index.addValue(uriConcept, i);
            }
            
            return index;
        } catch (IOException ex) {
            Logger.getLogger(Indexer_JSON.class.getName()).log(Level.SEVERE, null, ex);
            return new IndexHash();
        }
    }
    
}
