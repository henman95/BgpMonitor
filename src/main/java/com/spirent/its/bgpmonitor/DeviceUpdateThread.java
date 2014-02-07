package com.spirent.its.bgpmonitor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceUpdateThread extends Thread{
    private static int count = 0;
    
    private final Device device;
    private final String action;
    
    public DeviceUpdateThread( Device device, String action ) {
        this.device = device;
        this.action = action;
    } 
    
    public void run() {
        incrementCount();
        runAction();
        decrementCount();
    }
    
    private void runAction() {
        try {
            Thread.sleep( 100 );
        } catch (InterruptedException ex) {
            Logger.getLogger(DeviceUpdateThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //System.out.println( String.format("Started %-20s %d", device.getName(), DeviceUpdateThread.getCount()));
        if( "initialize".equals( action ) )
            device.initialize();
        
        if( "refresh".equals( action ))
            if( "cisco".equals( device.getType() ))
                device.refresh();
        
        //System.out.println( String.format("Stopped %-20s %d", device.getName(), DeviceUpdateThread.getCount()));
    }
    
    private synchronized static void incrementCount() {
        DeviceUpdateThread.count++;
    }
    
    private synchronized static void decrementCount() {
        DeviceUpdateThread.count--;
    }
    
    public synchronized static int getCount() {
        return DeviceUpdateThread.count;
    }     
}
