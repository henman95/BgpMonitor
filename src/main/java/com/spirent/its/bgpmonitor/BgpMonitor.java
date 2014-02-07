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

        for( int i=0;i<5;i++ ) {
            OutputBgpList();
            
            BgpMonitor.sleep( 1000 );
        }
    }

    private void OutputBgpList() {
        String newline = "\n";

        for( Device device: manager.getDeviceList() ) {
            for( BgpPeer peer: device.getBgpPeers() ) {
                StringBuilder out = new StringBuilder();
                
                out.append( String.format("%-20s %-15s", device.getName(), device.getAddress() ) );
                out.append( String.format(" %-15s %-15s", peer.getLocalAddress(), peer.getRemoteAddress() ));
                out.append( String.format(" %3s", peer.getState() ));
                out.append( String.format(" %5s %5s", peer.getLocalAS(), peer.getRemoteAS() ));
                
                System.out.println( out );
            }
        }
        
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
