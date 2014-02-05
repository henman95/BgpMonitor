/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spirent.its.bgpmonitor;

import java.io.IOException;
import java.util.ArrayList;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SnmpDevice {
    private final String address;
    private final String community;
    
    private Snmp snmp = null;
    
    public SnmpDevice( String address, String community ) {
        this.address = String.format( "udp:%s/161" , address);
        this.community = community;
        
        try {
             TransportMapping transport = new DefaultUdpTransportMapping();
             snmp = new Snmp( transport );
             transport.listen();
         } catch( IOException ex ) {
             Logger.getLogger(SnmpDevice.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
    public String getAsString(String oid) throws IOException {
        SnmpResult result = this.get( oid );
        
        return result.getValue();
    }
    
    public SnmpResult get( String oid ) throws IOException {
        ResponseEvent event;
        String returnOid,returnValue;
                
        event       = get( new OID[]{new OID(oid)});
        returnOid   = event.getResponse().get(0).getOid().toString();
        returnValue = event.getResponse().get(0).getVariable().toString();
        
        return new SnmpResult( returnOid, returnValue );
    }
    
    public ResponseEvent get(OID oids[]) throws IOException {
        PDU pdu = new PDU();
        for( OID oid: oids ) {
            pdu.add( new VariableBinding(oid));
        }   
        
        pdu.setType( PDU.GET );
        
        ResponseEvent event = snmp.send( pdu, getTarget(), null);

        if( event != null ) {
            return event;
        }

        throw new RuntimeException( "GET timed out" );
    }
    
    public SnmpResult getNext( String oid ) throws IOException {
        ResponseEvent event;
        String returnOid, returnValue;
        
        event       = getnext(new OID[]{new OID(oid)});
        returnOid   = event.getResponse().get(0).getOid().toString();
        returnValue = event.getResponse().get(0).getVariable().toString();
        
        return new SnmpResult(returnOid, returnValue);
    }
    
    public ResponseEvent getnext(OID oids[]) throws IOException {
        PDU pdu = new PDU();
        
        for(OID oid: oids)
            pdu.add( new VariableBinding(oid));

        
        pdu.setType( PDU.GETNEXT );
        
        ResponseEvent event = snmp.send(pdu, getTarget(), null);
        
        if( event != null)
            return event;
        
        throw new RuntimeException( "GETNEXT timed out" );
        
    }
    
    public ArrayList<String> getTableIndex(String oid) throws IOException {
        ResponseEvent event;
        ArrayList<String> values = new ArrayList<>();
        
        SnmpResult result = getNext( oid );
        
        
        while( result.getOid().startsWith( oid ) ) {
            values.add( result.getOid().substring( oid.length()+1) );
            result = getNext( result.getOid() );
        }
 
        return values;
    }
    
    private Target getTarget() {
        CommunityTarget target = new CommunityTarget();
        
        target.setCommunity(new OctetString(this.community));
        target.setAddress(GenericAddress.parse(this.address));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        
        return target;
    }
    
    public static OID getOid( String oid ) {
        return new OID( oid );
    }
}
