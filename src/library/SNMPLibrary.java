/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.naming.ldap.ManageReferralControl.OID;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 *
 * @author fredm
 */
public class SNMPLibrary {
    public static int ping (String adresse)
 /* -1: erreur ; 0: pas de réponse ; 1: réponse reçue */
    {
    System.out.println("Adresse testée = " + adresse);
    Process p = null;
    String commande = "ping -n 4 -w 1000 " + adresse;
    System.out.println("Commande testée = " + commande);
    BufferedReader bfIn = null;
    try
    {
        p=Runtime.getRuntime().exec(commande);
    if (p == null)
    {
        System.out.println("** Erreur d'exécution de la commande **"); return -1;
    }

    bfIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String strLine;
    boolean pasDeReponse = false;
    while ((strLine = bfIn.readLine()) != null)
    {
        System.out.println(strLine); // pour trace
        if (Trouve100(strLine))
        {
            System.out.println("La machine " + adresse + " ne répond pas"); return 0;
        }
    }
    bfIn.close();
    System.out.println("La machine " + adresse + " a répondu");
    return 1;
    }
    catch(IOException e)
    { System.out.println("Exception IO = " + e.getMessage()); }
    catch(Exception e)
    { System.out.println("Exception = " + e.getMessage()); }
    return -1;
    }
    static boolean Trouve100 (String s)
 /* true : on a trouvé 100% de paquets rejetés
 * false : certains paquets n'ont pas eté refusés
 */
    {
    boolean trouve = false;
    StringTokenizer scan = new StringTokenizer (s, " ");
    int cpt = 0;
    while (scan.hasMoreTokens())
    {
        String essai = scan.nextToken();
        int pp = essai.indexOf("%");
        if (pp != -1)
        {
        int p100 = essai.indexOf("100");
        trouve = (p100 != -1);
        }
        if (trouve) return true;
    }
    return false;
    } 
public static Boolean setSynchro(String oid, String info, String adresse)
{
    TransportMapping transport=null;
    try
    {
    transport = new DefaultUdpTransportMapping();
    transport.listen();
    }
    catch (IOException ex)
    { System.out.println(ex.getMessage());}

    CommunityTarget target = new CommunityTarget();
    target.setVersion(SnmpConstants.version1);
    target.setCommunity(new OctetString("2326fmah"));
    Address targetAddress;
    adresse += "/161";
    adresse = adresse.trim();
    targetAddress = new UdpAddress(adresse);
    //targetAddress = GenericAddress.parse("udp:192.168.1.3/161");
    target.setAddress(targetAddress);
    target.setRetries(2);
    target.setTimeout(1500);

    PDU pdu = new PDU();
    pdu.setType(PDU.SET);
    pdu.add(new VariableBinding(new OID(oid),
    new OctetString(info)));

    Snmp snmp = new Snmp(transport);
    ResponseEvent paquetReponse = null;
    try
    {
        paquetReponse = snmp.set(pdu, target);
        System.out.println("Requete SNMP envoyée à l'agent");
    }
    catch (IOException ex) 
    {
        System.out.println(ex.getMessage());
    }
    return true;
}
public static String getSynchro(String oid, String adresse)
{
    String reponse = "";
    TransportMapping transport=null;
     try
     {
        transport = new DefaultUdpTransportMapping();
        transport.listen();
     }
     catch (IOException ex)
     { System.out.println(ex.getMessage()); }
     
     CommunityTarget target = new CommunityTarget();
     target.setVersion(SnmpConstants.version1);
     target.setCommunity(new OctetString("2326fmah"));
     adresse += "/161";
     adresse = adresse.trim();
     //System.out.println(adresse.trim());
     Address targetAddress = new UdpAddress(adresse);
     //Address targetAddress = GenericAddress.parse("udp:127.0.0.1/161");
     target.setAddress(targetAddress);
     target.setRetries(2);
     target.setTimeout(1500);

     PDU pdu = new PDU();
     pdu.add(new VariableBinding(new OID(oid)));
     pdu.setType(PDU.GET);
     Snmp snmp = new Snmp(transport);
     ResponseEvent paquetReponse = null;
     try
     {
        paquetReponse = snmp.get(pdu, target);
        //System.out.println("Requete SNMP envoyée à l'agent");
     }
     catch (IOException e)
     { System.out.println(e.getMessage()); }
     if (paquetReponse !=null)
     {
         try
         {
             PDU pduReponse = paquetReponse.getResponse();
            //System.out.println("Status de la réponse = " + pduReponse.getErrorStatus());
            //System.out.println("Status de la réponse = " + pduReponse.getErrorStatusText());
            Vector vecReponse = (Vector) pduReponse.getVariableBindings();
            for (int i=0; i<vecReponse.size(); i++)
            {
            System.out.println("Elément n°"+i+ " : "+vecReponse.elementAt(i));
            reponse += vecReponse.elementAt(i);
            }
         }
         catch(NullPointerException e)
         {
             System.out.println("null detecte");
         }
        
     }
     
     return reponse;
}
public static void getNextASynchro(String adresse)
{    
    try
    {
        TransportMapping transport = new DefaultUdpTransportMapping();
        transport.listen();
        Snmp snmp = new Snmp(transport);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("2326fmah"));
        //Address targetAddress = GenericAddress.parse("udp:127.0.0.1/161");
        adresse += "/161";
        adresse = adresse.trim();
     //System.out.println(adresse.trim());
        Address targetAddress = new UdpAddress(adresse);
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version1);
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,1})));
        pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,2})));
        pdu.setType(PDU.GETNEXT);
        SnmpListener listener = new SnmpListener(snmp);
        snmp.send(pdu, target, null, listener);
        synchronized(snmp)
        {
        snmp.wait();
        }
     }
    catch (IOException ex)
    {
        System.out.println(ex.getMessage());
    }
    catch (InterruptedException ex)
    {
        System.out.println(ex.getMessage());
    }
}





}
