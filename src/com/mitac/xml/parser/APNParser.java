package com.mitac.xml.parser;

import java.io.InputStream;
import java.util.List;

import com.mitac.xml.model.APN;

public interface APNParser {
    /**
     * parse
     * @param is
     * @return
     * @throws Exception
     */
    public List<APN> parse(InputStream is) throws Exception;
    
    /**
     * serialize
     * @param apns
     * @return
     * @throws Exception
     */
    public String serialize(List<APN> apns) throws Exception;
}
