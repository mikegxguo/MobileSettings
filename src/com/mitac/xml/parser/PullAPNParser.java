package com.mitac.xml.parser;


import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import android.util.Log;

import com.mitac.xml.model.APN;

public class PullAPNParser implements APNParser {
    private final String TAG = "PullAPNParser";
    
    @Override
    public List<APN> parse(InputStream is) throws Exception {
        List<APN> apns = null;
        APN apn = null;
        
//      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//      XmlPullParser parser = factory.newPullParser();
        
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "UTF-8");

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_DOCUMENT:
                apns = new ArrayList<APN>();
                break;
            case XmlPullParser.START_TAG:
                if (parser.getName().equals("apn_setting")) {
                    apn = new APN();
                } else if (parser.getName().equals("id")) {
                    eventType = parser.next();
                    apn.setId(Integer.parseInt(parser.getText()));
                } else if (parser.getName().equals("name")) {
                    eventType = parser.next();
                    apn.setName(parser.getText());
                } else if (parser.getName().equals("numeric")) {
                    eventType = parser.next();
                    apn.setNumeric(parser.getText());
                } else if (parser.getName().equals("mcc")) {
                    eventType = parser.next();
                    apn.setMcc(parser.getText());
                } else if (parser.getName().equals("mnc")) {
                    eventType = parser.next();
                    apn.setMnc(parser.getText());
                } else if (parser.getName().equals("apn")) {
                    eventType = parser.next();
                    apn.setApn(parser.getText());
                } else if (parser.getName().equals("user")) {
                    eventType = parser.next();
                    apn.setUser(parser.getText());
                } else if (parser.getName().equals("password")) {
                    eventType = parser.next();
                    apn.setPassword(parser.getText());
                } else if (parser.getName().equals("server")) {
                    eventType = parser.next();
                    apn.setServer(parser.getText());
                } else if (parser.getName().equals("proxy")) {
                    eventType = parser.next();
                    apn.setProxy(parser.getText());
                } else if (parser.getName().equals("port")) {
                    eventType = parser.next();
                    apn.setPort(parser.getText());
                } else if (parser.getName().equals("mmsc")) {
                    eventType = parser.next();
                    apn.setMmsc(parser.getText());
                } else if (parser.getName().equals("mmsproxy")) {
                    eventType = parser.next();
                    apn.setMmsProxy(parser.getText());
                } else if (parser.getName().equals("mmsport")) {
                    eventType = parser.next();
                    apn.setMmsPort(parser.getText());
                } else if (parser.getName().equals("authtype")) {
                    eventType = parser.next();
                    apn.setAuthType(Integer.parseInt(parser.getText()));
                } else if (parser.getName().equals("type")) {
                    eventType = parser.next();
                    apn.setType(parser.getText());
                } else if (parser.getName().equals("current")) {
                    eventType = parser.next();
                    apn.setCurrent(parser.getText());
                } else if (parser.getName().equals("protocol")) {
                    eventType = parser.next();
                    apn.setProtocol(parser.getText());
                } else if (parser.getName().equals("roaming_protocol")) {
                    eventType = parser.next();
                    apn.setRoamingProtocol(parser.getText());
                } else if (parser.getName().equals("carrier_enabled")) {
                    eventType = parser.next();
                    apn.setCarrierEnabled(parser.getText());
                } else if (parser.getName().equals("bearer")) {
                    eventType = parser.next();
                    apn.setBearer(parser.getText());
                }
                break;
            case XmlPullParser.END_TAG:
                if (parser.getName().equals("apn_setting")) {
                    apns.add(apn);
                    apn = null;    
                }
                break;
            }
            eventType = parser.next();
        }
        return apns;
    }
    
    @Override
    public String serialize(List<APN> apns) throws Exception {
//      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//      XmlSerializer serializer = factory.newSerializer();
        
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", true);
        
        serializer.startTag("", "apn_settings");
        String temp = null;
        Log.d(TAG, "start");
        for (APN apn : apns) {
            serializer.startTag("", "apn_setting");
            serializer.attribute("", "id", apn.getId() + "");
            
            serializer.startTag("", "name");
            serializer.text((temp=apn.getName())==null?"":temp);
            serializer.endTag("", "name");
            
            serializer.startTag("", "numeric");
            serializer.text((temp=apn.getNumeric())==null?"":temp);
            serializer.endTag("", "numeric");

            serializer.startTag("", "mcc");
            serializer.text((temp=apn.getMcc())==null?"":temp);
            serializer.endTag("", "mcc");
            
            serializer.startTag("", "mnc");
            serializer.text((temp=apn.getMnc())==null?"":temp);
            serializer.endTag("", "mnc");
            
            serializer.startTag("", "apn");
            serializer.text((temp=apn.getApn())==null?"":temp);
            serializer.endTag("", "apn");
            
            serializer.startTag("", "user");
            serializer.text((temp=apn.getUser())==null?"":temp);
            serializer.endTag("", "user");

            serializer.startTag("", "password");
            serializer.text((temp=apn.getPassword())==null?"":temp);
            serializer.endTag("", "password");
   
            serializer.startTag("", "server");
            serializer.text((temp=apn.getServer())==null?"":temp);
            serializer.endTag("", "server");
                     

            serializer.startTag("", "proxy");
            serializer.text((temp=apn.getProxy())==null?"":temp);
            serializer.endTag("", "proxy");
            
            serializer.startTag("", "port");
            serializer.text((temp=apn.getPort())==null?"":temp);
            serializer.endTag("", "port");

            serializer.startTag("", "mmsproxy");
            serializer.text((temp=apn.getMmsProxy())==null?"":temp);
            serializer.endTag("", "mmsproxy");
            
            serializer.startTag("", "mmsport");
            serializer.text((temp=apn.getMmsPort())==null?"":temp);
            serializer.endTag("", "mmsport");

            serializer.startTag("", "mmsc");
            serializer.text((temp=apn.getMmsc())==null?"":temp);
            serializer.endTag("", "mmsc");

            serializer.startTag("", "authtype");
            serializer.text(apn.getAuthType()+"");
            serializer.endTag("", "authtype");
            
            serializer.startTag("", "type");
            serializer.text((temp=apn.getType())==null?"":temp);
            serializer.endTag("", "type");

            serializer.startTag("", "current");
            serializer.text((temp=apn.getCurrent())==null?"":temp);
            serializer.endTag("", "current");

            serializer.startTag("", "protocol");
            serializer.text((temp=apn.getProtocol())==null?"":temp);
            serializer.endTag("", "protocol");


            serializer.startTag("", "roaming_protocol");
            serializer.text((temp=apn.getRoamingProtocol())==null?"":temp);
            serializer.endTag("", "roaming_protocol");

            serializer.startTag("", "carrier_enabled");
            serializer.text((temp=apn.getCarrierEnabled())==null?"":temp);
            serializer.endTag("", "carrier_enabled");

            serializer.startTag("", "bearer");
            serializer.text((temp=apn.getBearer())==null?"":temp);
            serializer.endTag("", "bearer");
            
            serializer.endTag("", "apn_setting");
        }
        Log.d(TAG, "stop");
        serializer.endTag("", "apn_settings");
        serializer.endDocument();
        
        return writer.toString();
    }
}
