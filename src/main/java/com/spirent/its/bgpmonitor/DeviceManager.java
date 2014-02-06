/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spirent.its.bgpmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceManager {
    private final HashMap<String,String> configList;
    private final HashMap<String,Device> deviceList;
    private final HashMap<String,Site>   siteList;
    
    Pattern reComment;
    Pattern reConfig;
    Pattern reDevice;
    Pattern reSite;
    
    public DeviceManager() {
        // Internal Databases
        configList = new HashMap<>();
        deviceList = new HashMap<>();
        siteList   = new HashMap<>();

        // Configuration file matching
        reComment = Pattern.compile( "^#.*$" );
        reConfig  = Pattern.compile( "^config\\s+(?<key>\\S+)\\s+(?<value>\\S+).*" );
        reDevice  = Pattern.compile( "^device\\s+(?<name>\\S+)\\s+(?<address>\\S+)\\s+(?<community>\\S+)\\s*$" );
        reSite    = Pattern.compile( "^site\\s+(?<name>\\S+)\\s+(?<designator>\\S+)\\s+(?<asnumber>\\S+)\\s*$" );
        
        // Default Parameters
        configList.put( "defaultCommunity", "public" );
        configList.put( "defaultRestKey", "" );   
    }
    
    public void loadConfigFromFile( String filename ) throws IOException {
        Path    file;
        String  line;
        Matcher matcher;

        try {
            URL resourceUrl = getClass().getResource( "/META-INF/text/devicelist.conf" );
            file = Paths.get(resourceUrl.toURI());
            
        } catch (URISyntaxException ex) {
            Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
            file = Paths.get(filename);
        }
        
        BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset() );
              
        while( (line = reader.readLine()) != null ) {
            matcher = reComment.matcher(line);
            if( matcher.matches() ) {
            } 
            
            matcher = reConfig.matcher(line);
            if( matcher.matches() )
                setConfig( matcher.group("key"), matcher.group("value") );
            
            matcher = reDevice.matcher(line);
            if( matcher.matches() ) {
                String name      = matcher.group("name");
                String address   = matcher.group("address");
                String community = matcher.group("community");
            
                addDevice( DeviceFactory.createDevice( name,address,community, this));
            }
            
            matcher = reSite.matcher( line );
            if( matcher.matches() ) {
                String name       = matcher.group("name");
                String designator = matcher.group("designator");
                String asnumber   = matcher.group("asnumber");
                
                addSite( new Site( name,designator,asnumber) );
            }
        }
        
    }
    
    public void initializeDevices() {
        for( Device device: getDeviceList() ){
            device.initialize();
        }    
    }
    
    public void refreshDevices() {
        for( Device device: getDeviceList() ) 
            device.refresh();
    }
    
    // Configuration Methods
    public void setConfig( String key, String value ) {
        configList.put(key, value);
    }
    
    public String getConfig( String key ) {
        if( configList.containsKey(key) ) {
            return configList.get(key);
        }
        
        return null;
    }
    
    public void delConfig( String key ) {
        if( hasConfigKey(key) ) {
            configList.remove(key);
        }
    }
    
    public boolean hasConfigKey( String key ) {
        return configList.containsKey(key);
    }
    
    // Device Methods
    public void addDevice( Device device ) {
        if( !hasDevice( device.getName()) )
            deviceList.put( device.getName(), device);
    }
    
    public Device getDevice( String name ) {
        if( hasDevice(name) )
            return deviceList.get(name);
        
        return null;
    }
    
    public ArrayList<Device> getDeviceList() {
        ArrayList<Device> result = new ArrayList<>();
        
        for( String key: deviceList.keySet() )
            result.add( getDevice(key ) );
            
        return result;
    }
       
    public void delDevice( String name ) {
        if( hasDevice(name) ) {
            deviceList.remove( name );
        }
            
    }
    
    public boolean hasDevice( String name ) {
        return deviceList.containsKey(name);
    }
    
    
    // Site Methods
    public void addSite( Site site ) {
        if( !hasSite( site.getName() ) )
            siteList.put( site.getAsnumber(), site );
    }
    
    public Site getSite( String key ) {
        if( hasSite( key ) )
            return siteList.get( key );
    
        return null;
    }
    
    public ArrayList<Site> getSiteList() {
        ArrayList<Site> result = new ArrayList<>();
        
        for( String key: siteList.keySet() )
            result.add( getSite( key ));
        
        return result;
    }
    
    public void delSite( String key ) {
        if( hasSite( key ) )
            siteList.remove( key );
    } 
    
    public boolean hasSite( String key ) {
        return siteList.containsKey( key ) ;
    }
    
    @Override
    public String toString() {
        String newline = "\n";
        StringBuilder out = new StringBuilder();
        
        out.append( "Configuration Items" ).append(newline);
        out.append( "-----------------    ----------------------------------------------------" ).append(newline);
        for( String key: configList.keySet() ) {
            out.append(String.format("%-20s %s", key,getConfig(key) ) ).append(newline);
        }

        out.append( newline );
        out.append( "Devices" ).append(newline);
        out.append( "-----------------------------------").append(newline);
        for( Device device: getDeviceList() )
            out.append( device );

        out.append( newline );
        out.append( "Sites" ).append(newline);
        out.append( "-----------------------------------").append(newline);
        for( Site site: getSiteList() )
            out.append( site );
        
        
        out.append( "Peer List" ).append(newline);
        out.append( "-----------------------------------").append(newline);
        for( Device device: getDeviceList() ) {
            for( BgpPeer peer: device.getBgpPeers() ) {
                out.append( String.format("%-20s %-15s", device.getName(), device.getAddress() ) );
                out.append( String.format(" %-15s %-15s", peer.getLocalAddress(), peer.getRemoteAddress() ));
                out.append( String.format(" %3s", peer.getState() ));
                out.append( String.format(" %5s %5s", peer.getLocalAS(), peer.getRemoteAS() ));
                out.append( newline );
            }
        }
        
        
        
        
        return out.toString();
    }
}
