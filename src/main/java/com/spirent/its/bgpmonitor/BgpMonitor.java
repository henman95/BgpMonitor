package com.spirent.its.bgpmonitor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BgpMonitor {
    DeviceManager manager;
    
    public BgpMonitor() {
        manager = new DeviceManager();
    }
    
    public void run() {
        try {
            manager.loadConfigFromFile( "/home/hbennett/Projects/bgpstatus/devicelist.conf" );
            //manager.loadConfigFromFile( "./devicelist.conf" );
            manager.sendCommand( "initialize" );
        } catch (IOException ex) {
            Logger.getLogger(BgpMonitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RuntimeException ex) {
            Logger.getLogger(BgpMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        manager.sendCommand( "refresh" );
        //manager.setConfig("defaultCommunity", "peekaboo" );
        
        System.out.println( manager );
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BgpMonitor app = new BgpMonitor();
        
        app.run();
    }
    
}
