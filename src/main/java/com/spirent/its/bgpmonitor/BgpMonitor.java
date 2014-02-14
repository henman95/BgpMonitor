package com.spirent.its.bgpmonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BgpMonitor {
    DeviceManager manager;
    
    public BgpMonitor() {
        manager = new DeviceManager();
    }
    
    public void run() {
        ManagerUpdateThread thread = new ManagerUpdateThread( manager );
        
        try {
            manager.loadConfigFromFile( "/home/hbennett/Projects/bgpstatus/devicelist.conf" );
            //manager.loadConfigFromFile( "./devicelist.conf" );
            manager.sendCommand( "initialize" );
        } catch (IOException ex) {
            Logger.getLogger(BgpMonitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RuntimeException ex) {
            Logger.getLogger(BgpMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( manager.hasConfigKey( "refreshRate" ) ) {
            String newRate = manager.getConfig( "refreshRate" );
            thread.setInterval( Integer.parseInt(newRate));
        }
       
        System.out.println( "Initial Refresh Starting" );
        //manager.sendCommandJoined( "refresh" );
        
        System.out.println( "Periodic Refresh Starting" );
        thread.start();
        
        for( int i=0;i<20;i++ ) {
            OutputBgpList();
            
            BgpMonitor.sleep( 5000 );
        }
        
        thread.signalExit();
        
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(BgpMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void OutputBgpList() {
        String newline = "\n";

        System.out.println( String.format( "Last Run : %s" , manager.getCommandTime( "refresh" ).toString() ));
        System.out.println( String.format( "Last Run : %d" , manager.elapseCommandTime( "refresh" ) ));
    }
    
    public static void sleep( int ms ) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(BgpMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BgpMonitor app = new BgpMonitor();
        
        app.run();
    }
    
}
