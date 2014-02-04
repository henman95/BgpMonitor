/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spirent.its.bgpmonitor;

import java.io.IOException;

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
            this.deviceType = snmp.getAsString( ".1.3.6.1.2.1.1.1.0" );
            deviceStatus = "ok";
        } catch( IOException e ) {
            deviceStatus = "SnmpError";
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
