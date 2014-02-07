/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spirent.its.bgpmonitor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Device {
    private String name;
    private String address;
    private String community;
    
    private DeviceManager deviceManager;
    
    private String deviceType;
    private String deviceStatus;
    
    private ArrayList<BgpPeer> bgpPeerList;
    
    private HashMap<String,String> stateTable;
    
    public Device() {
        name          = "";
        address       = "";
        community     = "";
        deviceType    = "unknown";
        deviceStatus  = "uninitialized";
        deviceManager = null;
        bgpPeerList   = new ArrayList<>();
        stateTable    = new HashMap<>();
        
        stateTable.put( "Idle"       , "idle" );
        stateTable.put( "Connect"    , "connect" );
        stateTable.put( "Active"     , "active" );
        stateTable.put( "OpenSent"   , "opensent" );
        stateTable.put( "OpenConfirm", "openconfirm" );
        stateTable.put( "Established", "established" );
        stateTable.put( "1", "idle" );
        stateTable.put( "2", "connect" );
        stateTable.put( "3", "active" );
        stateTable.put( "4", "opensent" );
        stateTable.put( "5", "openconfirm" );
        stateTable.put( "6", "established" );
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
        if( "cisco".equals(deviceType) ) 
            refreshWithSnmp();
        
        if( "paloalto".equals(deviceType) )
            refreshWithRest();
    }
        
    private void refreshWithSnmp() {
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
                
                bgpPeerList.add(new BgpPeer(localAddress, remoteAddress, stateTable.get(state), localAS, remoteAS ));
            }
            
        } catch( IOException ex ) {
            Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void refreshWithRest() {
        Client                  client;
        WebTarget               target;
        Response                response;
        DocumentBuilderFactory  dbFactory;
        DocumentBuilder         dBuilder;
        Document                doc;
        NodeList                nodes;
        String                  localAS;

        try {
            client    = ClientBuilder.newClient();
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder  = dbFactory.newDocumentBuilder();
            localAS   = "0";

            // Get LocalAS
            target = client.target( "http://{host}/api" )
                .resolveTemplate( "host", this.address )
                .queryParam( "key"  , deviceManager.getConfig("defaultRestKey"))
                .queryParam( "type" , "op" )
                .queryParam( "cmd"  , "<show><routing><protocol><bgp><summary></summary></bgp></protocol></routing></show>" );
            response = target.   request().get();
            
            doc   = dBuilder.parse( new InputSource( new StringReader( response.readEntity( String.class))));
            nodes = doc.getElementsByTagName( "entry" );
            
            if( nodes.getLength() == 1 && nodes.item(0).getNodeType()== Node.ELEMENT_NODE)
                localAS = getXmlChildText((Element)nodes.item(0), "local-as");

            // Get Peer Table
            //client = ClientBuilder.newClient();
            target = client.target( "http://{host}/api" )
                .resolveTemplate( "host", this.address )
                .queryParam( "key"  , deviceManager.getConfig("defaultRestKey"))
                .queryParam( "type" , "op" )
                .queryParam( "cmd"  , "<show><routing><protocol><bgp><peer></peer></bgp></protocol></routing></show>" );
            response = target.request().get();
        
            doc   = dBuilder.parse( new InputSource( new StringReader( response.readEntity( String.class))));
            nodes = doc.getElementsByTagName( "result" );
            
            if( nodes.getLength() == 1 && nodes.item(0).getNodeType() == Node.ELEMENT_NODE ) {
                ArrayList<Element> entries = getXmlChildren( (Element)nodes.item(0), "entry" );
                
                for( Element entry: entries ){
                    String localAddress  = getXmlChildText(entry,"local-address");
                    String remoteAddress = getXmlChildText(entry,"peer-address");
                    String state         = getXmlChildText(entry,"status" );
                    String remoteAS      = getXmlChildText(entry,"remote-as");
                    
                    localAddress = stripPortFromAddress( localAddress );
                    remoteAddress = stripPortFromAddress( remoteAddress );
                    
                    bgpPeerList.add(new BgpPeer( localAddress, remoteAddress, stateTable.get(state), localAS, remoteAS ));
                }
            }
            
            client.close();
        } catch( Exception ex) {
            System.out.println( ex.toString() );
            deviceStatus = "errREST";
        }
        
    }
    
    private static String stripPortFromAddress( String address ) {
        String result = address;
        
        if( address.contains(":") ) 
            result = address.substring(0,address.indexOf(":"));
        
        return result;
    }
     
    private static ArrayList<Element> getXmlChildren( Element element, String name ) {
        ArrayList<Element> results = new ArrayList<>();
        NodeList children =element.getChildNodes();
        
        for( int i=0;i<children.getLength();i++ ) {
            Node node = children.item(i);
            if( node.getNodeType() == Node.ELEMENT_NODE &&  name.equals( node.getNodeName() )) {
                results.add( (Element) node );
            }
        }
        
        return results;
    }
    
    private static String getXmlChildText( Element element, String name ) { 
        String value = null;
        ArrayList<Element> results = getXmlChildren( element, name );
      
        if( results.size() > 0) 
            value = results.get(0).getTextContent();
        
        return value;
    }
    
    // Getters and Setters
    public void setManager( DeviceManager deviceManager ) {
        this.deviceManager = deviceManager;
    }
    
    public DeviceManager getManager() {
        return deviceManager;
    }
    
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
    
    public ArrayList<BgpPeer> getBgpPeers() {
        return bgpPeerList;
    }
    
    @Override
    public String toString() {
        String newline = "\n";
        StringBuilder out = new StringBuilder();
        
        out.append( String.format("%-20s %-15s %s", this.name,this.address,this.community));
        out.append( String.format("  %-10s %-15s %d", this.deviceType, this.deviceStatus, this.bgpPeerList.size() ));
        out.append(newline);
       
        return out.toString();
    }

}
