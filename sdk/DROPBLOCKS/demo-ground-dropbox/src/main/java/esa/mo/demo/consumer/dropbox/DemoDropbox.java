/* ----------------------------------------------------------------------------
 * Copyright (C) 2015      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : ESA NanoSat MO Framework
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.demo.consumer.dropbox;

import esa.mo.helpertools.connections.ConnectionConsumer;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import esa.mo.nanosatmoframework.ground.adapter.GroundMOAdapter;
import esa.mo.nanosatmoframework.ground.listeners.SimpleDataReceivedListener;

/**
 * Ground consumer: Demo Dropbox
 *
 */
public class DemoDropbox {

    private final GroundMOAdapter groundMOadapter;

    public DemoDropbox() {

        ConnectionConsumer connection = new ConnectionConsumer();

        try {
            connection.loadURIs();
        } catch (MalformedURLException ex) {
            Logger.getLogger(DemoDropbox.class.getName()).log(Level.SEVERE, null, ex);
        }

        groundMOadapter = new GroundMOAdapter(connection);
        groundMOadapter.addDataReceivedListener(new DataReceivedAdapter());
        
        // Sync the dropbox folder with the one in space
        // https://github.com/dropbox/dropbox-sdk-java
        // 
        
    }

    /**
     * Main command line entry point.
     *
     * @param args the command line arguments
     * @throws java.lang.Exception If there is an error
     */
    public static void main(final String args[]) throws Exception {
        DemoDropbox demo = new DemoDropbox();
    }

    class DataReceivedAdapter extends SimpleDataReceivedListener {
        @Override
        public void onDataReceived(String parameterName, Serializable data) {
        }
    }

}