package com.spirent.its.bgpmonitor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceProcessorThread extends Thread{
    private static int count = 0;
    
    private final DeviceProcessor processor;
    private final DeviceManager   manager;
    private final Device          device;
    private final String          command;
    
    public DeviceProcessorThread( DeviceProcessor processor, DeviceManager manager, Device device, String command ) {
        this.processor = processor;
        this.manager   = manager;
        this.device    = device;
        this.command   = command;
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
        
        if( "initialize".equals( command ) ) {
            processor.initializeDevice( this.device );
        }
            

    }
    
    private synchronized static void incrementCount() {
        DeviceProcessorThread.count++;
    }
    
    private synchronized static void decrementCount() {
        DeviceProcessorThread.count--;
    }
    
    public synchronized static int getCount() {
        return DeviceProcessorThread.count;
    }     
}