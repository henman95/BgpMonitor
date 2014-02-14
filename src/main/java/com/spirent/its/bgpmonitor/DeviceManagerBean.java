/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spirent.its.bgpmonitor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.faces.bean.RequestScoped;
import javax.inject.Named;

@Startup
@Singleton
public class DeviceManagerBean {
    public enum States { NOTREADY, READY }
    
    private DeviceManager manager;
    private States        state;
    
    
    @PostConstruct
    public void init() {
        state = States.NOTREADY;
     
        System.out.println( "Device Manager Starting");
        manager = new DeviceManager();
        try {
            manager.loadConfigFromFile( "" );
            //manager.sendCommandJoined( "refresh" );
            state = States.READY;
            
            System.out.println( "Device Manager Started");
            
            for( Device device: manager.getDeviceList() )
                System.out.println( device.getName() );

        } catch (IOException ex) {
            Logger.getLogger(DeviceManagerBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public DeviceManager getManager() {
        return manager;
    }
    
    public DeviceManager getManager1() {
        System.out.println( manager.toString() );
        return manager;
    }
    
    public States getState() {
        return this.state;
    }
    
    public void setState( States state ) { 
        this.state = state;
    }
    
}
