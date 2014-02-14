/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spirent.its.bgpmonitor;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        return deviceManagerBean.getManager().getDeviceList();
    }

    public ArrayList<BgpPeer> getPeers(Device device) {
        return deviceManagerBean.getManager().getBgpPeers();
    }
    
    public DeviceManager getManager() {
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
