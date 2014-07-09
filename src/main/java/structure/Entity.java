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

import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.URI;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class Entity {

    private String id;
    private String href;
    private String title;
    private Set<URI> concepts = new HashSet();

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param label
     */
    public void setId(String label) {
        this.id = label;
    }

    /**
     *
     * @return
     */
    public String getHref() {
        return href;
    }

    /**
     *
     * @param href
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     */
    public Set<URI> getConcepts() {
        return concepts;
    }

    /**
     *
     * @param concepts
     */
    public void setConcepts(Set<URI> concepts) {
        this.concepts = concepts;
    }

    /**
     *
     * @param c
     */
    public void removeConcept(URI c) {
        concepts.remove(c);
    }

    /**
     *
     * @param c
     */
    public void addConcept(URI c) {
        concepts.add(c);
    }

    /**
     *
     * @return
     */
    public String formatJson() {
        String json = "{";
        json += "\"id\":\"" + id + "\",";
        json += "\"title\":\"" + title + "\",";
        json += "\"href\":\"" + href + "\",";
        json += "\"conceptIds\":[";
        for (URI c : concepts) {
            json += "\"" + c.getLocalName() + "\",";
        }
        if (concepts.size() > 0) {
            json = json.substring(0, json.length() - 1);
        }
        json += "]}";
        return json;
    }

    public String toString() {
        String entity = "Item " + id + " with title '" + title + "' and concepts: ";
        for (URI s : concepts) {
            entity += s.getLocalName() + " ("+Index.getInstance().getEngineManager().getFromURI(s)+") ";
        }
        return entity;
    }
}
