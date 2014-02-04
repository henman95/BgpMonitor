package com.spirent.its.bgpmonitor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hbennett
 */
public class DeviceFactory {
    public static Device createDevice( String name, String address, String community ) {
        Device device = new Device();
        
        device.setName(name);
        device.setAddress(address);
        device.setCommunity(community);
        
        return device;
    }
}
