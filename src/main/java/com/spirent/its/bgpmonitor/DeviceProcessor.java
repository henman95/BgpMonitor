/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spirent.its.bgpmonitor;

import java.io.IOException;

public class DeviceProcessor {

    private final DeviceManager manager;

    public DeviceProcessor(DeviceManager manager) {
        this.manager = manager;
    }

    public void sendCommand(String command) {
        if( "initializeDevices".equals( command ) ) {
            initializeDevices();
        }
    }
    
    public void processAllDevices( String Command )  {
        setCommandTime( command, new Date() );
        
        for( Device device: manager.getDeviceList() ) {
        }
    }

    public void initializeDevices() {
        
    }
    
    public void initializeDevice( Device device ) {
        
    }
    

   
}
