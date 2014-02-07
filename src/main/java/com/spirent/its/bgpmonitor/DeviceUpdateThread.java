package com.spirent.its.bgpmonitor;

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
        if( "initialize".equals( action ) )
            device.initialize();
        
        if( "refresh".equals( action ))
            if( "cisco".equals( device.getType() ))
                device.refresh();
    }
    
    private synchronized void incrementCount() {
        DeviceUpdateThread.count++;
    }
    
    private synchronized void decrementCount() {
        DeviceUpdateThread.count--;
    }
    
    public static synchronized int getCount() {
        return DeviceUpdateThread.count;
    }     
}
