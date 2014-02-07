package com.spirent.its.bgpmonitor;

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
