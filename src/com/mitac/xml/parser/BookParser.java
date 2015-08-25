package com.mitac.xml.parser;


import java.io.InputStream;
import java.util.List;

import com.mitac.xml.model.Book;

public interface BookParser {
    /**
     * parse
     * @param is
     * @return
     * @throws Exception
     */
    public List<Book> parse(InputStream is) throws Exception;
    
    /**
     * serialize
     * @param books
     * @return
     * @throws Exception
     */
    public String serialize(List<Book> books) throws Exception;
}