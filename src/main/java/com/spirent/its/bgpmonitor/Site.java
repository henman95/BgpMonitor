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
public class Site {
    private String name;
    private String designator;
    private String asnumber;
    
    public Site() {
        name = "";
        designator = "";
        asnumber = "0";
    }
    
    public Site( String name, String designator, String asnumber ) {
        this.name = name;
        this.designator = designator;
        this.asnumber = asnumber;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setDesignator( String designator ) {
        this.designator = designator;
    }
    
    public String getDesignator() {
        return this.designator;
    }
    
    public void setAsnumber( String asnumber ) {
        this.asnumber = asnumber;
    }
    
    public String getAsnumber() {
        return this.asnumber;
    }
  
    @Override
    public String toString() {
        String newline = "\n";
        StringBuilder out = new StringBuilder();
        
        out.append( String.format("%-13s %-4s %-5s", this.name,this.designator,this.asnumber)).append(newline);
        
        return out.toString();
    }
}
