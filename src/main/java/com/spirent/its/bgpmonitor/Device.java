/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spirent.its.bgpmonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hbennett
 */
public class Device {
    private String name;
    private String address;
    private String community;
    
    private String deviceType;
    private String deviceStatus;
    
    public Device() {
        name = "";
        address = "";
        community = "";
        
        deviceType = "unknown";
        deviceStatus = "uninitialized";
    }
    
    // Get Initial state and type of system
    public void initialize() {
        SnmpDevice snmp = new SnmpDevice( this.address, this.community );
        
        try {
            String oid = snmp.getNext(".1.3.6.1.4.1").getOid();
            
            // Default
            deviceType   = "unknown" + oid;
            deviceStatus = "unsupported";
            
            // Cisco 
            if( oid.startsWith("1.3.6.1.4.1.9") ) {
                deviceType   = "cisco";
                deviceStatus = "ok";
            }
            
            // Palo Alto
               if( oid.startsWith("1.3.6.1.4.1.25461") ) {
                deviceType   = "paloalto";
                deviceStatus = "ok";
            }         
        } catch( IOException ex ) {
            deviceType   = "unknown";
            deviceStatus = "error";
        }
    }
    
    public void refresh() {
        ArrayList<String> keyList = new ArrayList<>();
        SnmpDevice snmp = new SnmpDevice( this.address, this.community );
        
        try {
            keyList = snmp.getTableIndex( "1.3.6.1.2.1.15.3.1.1" );
            
            String localAS = snmp.get( "1.3.6.1.2.1.15.2.0" ).getValue();
            
            for( String key: keyList) {
                String localAddress  = snmp.get( "1.3.6.1.2.1.15.3.1.5." + key ).getValue();
                String remoteAddress = snmp.get( "1.3.6.1.2.1.15.3.1.7." + key ).getValue();
                String state         = snmp.get( "1.3.6.1.2.1.15.3.1.2." + key ).getValue();
                String remoteAS      = snmp.get( "1.3.6.1.2.1.15.3.1.9." + key ).getValue();
                
                BgpEntry entry = new BgpEntry(localAddress, remoteAddress, state, localAS, remoteAS );
                
                System.out.println( entry );
            }
            
        } catch( IOException ex ) {
            Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    // Getters and Setters
    public void setName( String name ) { 
        this.name = name;
    }
    
    public String getName( ) {
        return name;
    }
    
    public void setAddress( String address ) {
        this.address = address;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setCommunity( String community ) {
        this.community = community;
    }
    
    public String getCommunity() {
        return community;
    }
    
    public String getType() {
        return this.deviceType;
    }
   
    public String getStatus() {
        return this.deviceStatus;
    }
    
    @Override
    public String toString() {
        String newline = "\n";
        StringBuilder out = new StringBuilder();
        
        out.append( String.format("%-20s %-15s %s", this.name,this.address,this.community));
        out.append( String.format("  %-10s %-15s", this.deviceType, this.deviceStatus ));
        out.append(newline);
           
        
        return out.toString();
    }

}
