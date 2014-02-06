/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spirent.its.bgpmonitor;

public class BgpPeer {
    private String localAddress;
    private String remoteAddress;
    private String state;
    private String localAS;
    private String remoteAS;

    public BgpPeer() {
    }

    public BgpPeer(String localAddress, String remoteAddress, String state, String localAS, String remoteAS) {
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
        this.state = state;
        this.localAS = localAS;
        this.remoteAS = remoteAS;
    }
    
    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLocalAS() {
        return localAS;
    }

    public void setLocalAS(String localAS) {
        this.localAS = localAS;
    }

    public String getRemoteAS() {
        return remoteAS;
    }

    public void setRemoteAS(String remoteAS) {
        this.remoteAS = remoteAS;
    }

    public String toString() {
        return String.format( "%15s %15s %15s %5s %5s", this.localAddress, this.remoteAddress, this.state, this.localAS, this.remoteAS );
    }
    
        
}
