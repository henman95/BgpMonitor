/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spirent.its.bgpmonitor;

import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named(value = "deviceManager")
@RequestScoped
public class DeviceManagerBackingBean implements Serializable {
    @EJB
    private DeviceManagerBean deviceManagerBean;
    
    public ArrayList<Device> getDevices() {
        
        DeviceManager manager = deviceManagerBean.getManager();
        System.out.println( "Getting Devices" );
        
        return manager.getDeviceList();
    }
}
