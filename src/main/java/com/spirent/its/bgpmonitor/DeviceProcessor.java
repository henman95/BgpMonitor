/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spirent.its.bgpmonitor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
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

public class DeviceProcessor {

    private final DeviceManager manager;

    public DeviceProcessor(DeviceManager manager) {
        this.manager = manager;
    }

    public void sendCommand(String command) {
        sendCommand(command, true);
    }

    public void sendCommand(String command, boolean concurrent) {
        System.out.println("Command: " + command);

        if ("initialize".equals(command)) {
            processAllDevices("initialize");
        }

        if ("refresh".equals(command)) {
            processAllDevices("refresh");
        }
    }

    public void processAllDevices(String command) {
        // Default to run concurrently
        processAllDevices(command, true);
    }

    public void processAllDevices(String command, boolean concurrent) {
        for (Device device : manager.getDeviceList()) {
            DeviceProcessorThread thread = new DeviceProcessorThread(this, manager, device, command);

            System.out.println("Thread Start: " + device.getName());

            thread.start();

            if (!concurrent) {
                try {
                    thread.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            while (DeviceProcessorThread.getCount() > 0) {
                try {
                    Thread.sleep(50);
                    //System.out.println( "ThreadCount " + lastThread.getCount() );
                } catch (InterruptedException ex) {
                    Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        manager.setCommandTime(command, new Date());
    }

    // Initialize Command
    public void initializeDevice(Device device) {
        SnmpDevice snmp = new SnmpDevice(device.getAddress(), device.getCommunity());

        System.out.println("Init Device:" + device.getName());

        try {
            String oid = snmp.getNext(".1.3.6.1.4.1").getOid();

            // Default
            device.setType("unknown" + oid);
            device.setStatus("unsupported");

            // Cisco 
            if (oid.startsWith("1.3.6.1.4.1.9")) {
                device.setType("cisco");
                device.setStatus("ok");
            }

            // Palo Alto
            if (oid.startsWith("1.3.6.1.4.1.25461")) {
                device.setType("paloalto");
                device.setStatus("ok");
            }
        } catch (IOException ex) {
            device.setType("unknown");
            device.setStatus("error");
        }
    }

    // Refresh Command
    public void refreshDevice(Device device) {
        System.out.println(device.getName() + ":" + device.getType());

        if ("cisco".equals(device.getType())) {
            refreshWithSnmp(device);
        }

        if ("paloalto".equals(device.getType())) {
            refreshWithRest(device);
        }
    }

    private void refreshWithSnmp(Device device) {
        ArrayList<String> keyList = new ArrayList<>();
        SnmpDevice snmp = new SnmpDevice(device.getAddress(), device.getCommunity());

        try {
            keyList = snmp.getTableIndex("1.3.6.1.2.1.15.3.1.1");

            String localAS = snmp.get("1.3.6.1.2.1.15.2.0").getValue();
            //bgpPeerList.clear();

            for (String key : keyList) {
                String localAddress = snmp.get("1.3.6.1.2.1.15.3.1.5." + key).getValue();
                String remoteAddress = snmp.get("1.3.6.1.2.1.15.3.1.7." + key).getValue();
                String state = snmp.get("1.3.6.1.2.1.15.3.1.2." + key).getValue();
                String remoteAS = snmp.get("1.3.6.1.2.1.15.3.1.9." + key).getValue();

                manager.addBgpPeer(new BgpPeer(device, localAddress, remoteAddress, device.getStateTable(state), localAS, remoteAS));
            }

        } catch (IOException ex) {
            Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void refreshWithRest(Device device) {
        Client client;
        WebTarget target;
        Response response;
        DocumentBuilderFactory dbFactory;
        DocumentBuilder dBuilder;
        Document doc;
        NodeList nodes;
        String localAS;

        try {
            client = ClientBuilder.newClient();
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            localAS = "0";

            // Get LocalAS
            target = client.target("http://{host}/api")
                    .resolveTemplate("host", device.getAddress())
                    .queryParam("key", manager.getConfig("defaultRestKey"))
                    .queryParam("type", "op")
                    .queryParam("cmd", "<show><routing><protocol><bgp><summary></summary></bgp></protocol></routing></show>");
            response = target.request().get();

            doc = dBuilder.parse(new InputSource(new StringReader(response.readEntity(String.class))));
            nodes = doc.getElementsByTagName("entry");

            if (nodes.getLength() == 1 && nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
                localAS = getXmlChildText((Element) nodes.item(0), "local-as");
            }

            // Get Peer Table
            //client = ClientBuilder.newClient();
            target = client.target("http://{host}/api")
                    .resolveTemplate("host", device.getAddress())
                    .queryParam("key", manager.getConfig("defaultRestKey"))
                    .queryParam("type", "op")
                    .queryParam("cmd", "<show><routing><protocol><bgp><peer></peer></bgp></protocol></routing></show>");
            response = target.request().get();

            doc = dBuilder.parse(new InputSource(new StringReader(response.readEntity(String.class))));
            nodes = doc.getElementsByTagName("result");

            if (nodes.getLength() == 1 && nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
                ArrayList<Element> entries = getXmlChildren((Element) nodes.item(0), "entry");

                for (Element entry : entries) {
                    String localAddress = getXmlChildText(entry, "local-address");
                    String remoteAddress = getXmlChildText(entry, "peer-address");
                    String state = getXmlChildText(entry, "status");
                    String remoteAS = getXmlChildText(entry, "remote-as");

                    localAddress = stripPortFromAddress(localAddress);
                    remoteAddress = stripPortFromAddress(remoteAddress);

                    manager.addBgpPeer(new BgpPeer(device, localAddress, remoteAddress, device.getStateTable(state), localAS, remoteAS));
                }
            }

            client.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            device.setStatus("errREST");
        }

    }

    private static String stripPortFromAddress(String address) {
        String result = address;

        if (address.contains(":")) {
            result = address.substring(0, address.indexOf(":"));
        }

        return result;
    }

    private static ArrayList<Element> getXmlChildren(Element element, String name) {
        ArrayList<Element> results = new ArrayList<>();
        NodeList children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName())) {
                results.add((Element) node);
            }
        }

        return results;
    }

    private static String getXmlChildText(Element element, String name) {
        String value = null;
        ArrayList<Element> results = getXmlChildren(element, name);

        if (results.size() > 0) {
            value = results.get(0).getTextContent();
        }

        return value;
    }
}
