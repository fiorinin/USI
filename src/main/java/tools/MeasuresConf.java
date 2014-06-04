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

import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> / <contact@creatox.com>
 */
public class MeasuresConf {

    /**
     *
     */
    public static ICconf icConf;

    /**
     *
     * @return
     * @throws SLIB_Ex_Critic
     */
    public static SMconf getMeasure() throws SLIB_Ex_Critic {
        icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
        //ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SANCHEZ_2011);
        //ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_RESNIK_1995);

        // Lin indirect
        SMconf similarityMeasureConf = new SMconf("", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);
        //SMconf similarityMeasureConf = new SMconf("", SMConstants.FLAG_ICI_RESNIK_1995, icConf);

        // Schlicker indirect
        //SMconf similarityMeasureConf = new SMconf("", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_SCHLICKER_2006, icConf);
        //ICconf prob = new IC_Conf_Topo(SMConstants.FLAG_ICI_PROB_OCCURENCE_PROPAGATED);
        //similarityMeasureConf.addParam("ic_prob", prob);

        // Term Overlap direct
        //SMconf similarityMeasureConf = new SMconf("", SMConstants.FLAG_SIM_GROUPWISE_DAG_TO, icConf);

        return similarityMeasureConf;
    }

    /**
     *
     * @return
     * @throws SLIB_Ex_Critic
     */
    public static SMconf getAgreg() throws SLIB_Ex_Critic {
        icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
        // BMA
        SMconf similarityMeasureConf = new SMconf("", SMConstants.FLAG_SIM_GROUPWISE_BMA, icConf);
        return similarityMeasureConf;
    }
}