/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package library;

import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;

/**
 *
 * @author fredm
 */
public class SnmpListener implements ResponseListener{
    private Snmp snmpManager;
    public SnmpListener (Snmp s) { snmpManager = s; }
    public void onResponse(ResponseEvent event)
    {
        ((Snmp)event.getSource()).cancel(event.getRequest(), this);
        System.out.println("\n SNMP Asynchrone :\n Réponse reçue (PDU): "+event.getResponse());
        synchronized(snmpManager)
        {
        snmpManager.notify();
        }
    } 

}
