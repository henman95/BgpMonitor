/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spirent.its.bgpmonitor;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named(value = "dmBean")
@RequestScoped
public class DeviceManagerBackingBean implements Serializable {

    @EJB
    private DeviceManagerBean deviceManagerBean;
    
    public DeviceManager getManager() {
        System.out.println( "Getting Manager" );
        return deviceManagerBean.getManager();
    }

    public void clear() {
        deviceManagerBean.getManager().clearDevices();
    }

    public void reload() {
        try {
            deviceManagerBean.getManager().loadConfigFromFile(null);
        } catch (IOException ex) {
            Logger.getLogger(DeviceManagerBackingBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void init() {
        deviceManagerBean.getManager().sendCommand("initialize");
    }

    public void refresh() {
        deviceManagerBean.getManager().sendCommand("refresh");
    }

    public void dump() {
        System.out.println(deviceManagerBean.getManager().toString());
    }

}
