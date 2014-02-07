/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spirent.its.bgpmonitor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hbennett
 */
public class ManagerUpdateThread extends Thread {
    private int interval;
    private boolean exit;
    private final DeviceManager manager;

    
    public ManagerUpdateThread( DeviceManager manager) {
        this.manager  = manager;
        this.interval = 60;
        this.exit     = false;
    } 
    
    public ManagerUpdateThread( DeviceManager manager, int interval ) {
        this.manager  = manager;
        this.interval = interval;
        this.exit     = false;
    }
    
    public void run() {
        while( !exit ) {
            
            System.out.println( "checking:" + manager.elapseCommandTime( "refresh" ) + ":" + this.interval  );
            if( manager.elapseCommandTime( "refresh" ) > this.interval ) {
                System.out.println( "Refreshing" );
                manager.sendCommand( "refresh" );
            }
            
            try {
                Thread.sleep( 1000 );
            } catch (InterruptedException ex) {
                Logger.getLogger(ManagerUpdateThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        System.out.println( "Exitiing" );
    }
    
    public synchronized void signalExit() {
        System.out.println( "Quiting" );
        this.exit = true;
    }
        
    public synchronized void setInterval( int interval ) {
        this.interval = interval;
    }
    
    public synchronized int getInterval() {
        return interval;
    }
  
}
