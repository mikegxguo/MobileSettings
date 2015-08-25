package com.mitac.xml.model;

public class APN {
    private int id;
    private String name;
    private String numeric;
    
    private String mcc;
    private String mnc;
    
    private String apn;
    private String user;
    private String server;
    private String password;
    
    private String proxy;
    private String  port;
    
    private String mmsproxy;
    private String mmsport;
    private String mmsc;
    
    private int authtype;
    private String type;
    private String current;
    
    private String protocol;
    private String roaming_protocol;
    
    private String carrier_enabled;
    private String bearer;
    
    
    public APN() {
        id = 0;
        name = "";
        numeric = "";
        
        mcc = "";
        mnc = "";
        
        apn = "";
        user = "";
        server = "";
        password = "";
        
         proxy = "";
         port = "";
        
         mmsproxy = "";
        mmsport = "";
         mmsc = "";
        
        authtype = -1;
        type = "";
        current = "";
        
        protocol = "IP";
        roaming_protocol = "IP";
        
        carrier_enabled = "1";
        bearer = "0";
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumeric() {
        return numeric;
    }

    public void setNumeric(String numeric) {
        this.numeric = numeric;
    }
    
    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }
    
    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }
    
    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }
    
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
    
    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }
    
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
    
    public String getMmsProxy() {
        return mmsproxy;
    }

    public void setMmsProxy(String mmsproxy) {
        this.mmsproxy = mmsproxy;
    }
    
    public String getMmsPort() {
        return mmsport;
    }

    public void setMmsPort(String mmsport) {
        this.mmsport = mmsport;
    }

    public String getMmsc() {
        return mmsc;
    }

    public void setMmsc(String mmsc) {
        this.mmsc = mmsc;
    }
   
    public int getAuthType() {
        return authtype;
    }

    public void setAuthType(int  authtype) {
        this.authtype = authtype;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getRoamingProtocol() {
        return roaming_protocol;
    }

    public void setRoamingProtocol(String roaming_protocol) {
        this.roaming_protocol = roaming_protocol;
    }
    
    public String getCarrierEnabled() {
        return carrier_enabled;
    }

    public void setCarrierEnabled(String carrier_enabled) {
        this.carrier_enabled = carrier_enabled;
    }
    
    public String getBearer() {
        return bearer;
    }

    public void setBearer(String bearer) {
        this.bearer = bearer;
    }

    @Override
    public String toString() {
        return "id:" + id + ", apn:" + apn + ", authtype:" + authtype;
    }
}
