/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spirent.its.bgpmonitor;

/**
 *
 * @author hbennett
 */
public class SnmpResult {
    private String oid;
    private String value;
    
    public SnmpResult() {
        this.oid = "";
        this.value = "";
    }
    
    public SnmpResult( String oid, String value ) {
        this.oid = oid;
        this.value = value;
    }
    
    public void setOid(String oid) {
        this.oid = oid;
    }
    
    public String getOid() {
        return this.oid;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public String toString() {
        return String.format( "OID:%s - %s", this.oid, this.value);   
    }
}
