package com.mitac.xml.parser;


import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.mitac.xml.model.Book;

public class SaxBookParser implements BookParser {
    
    @Override
    public List<Book> parse(InputStream is) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();  //
        SAXParser parser = factory.newSAXParser();                  //
        MyHandler handler = new MyHandler();                        //
        parser.parse(is, handler);                                  //
        return handler.getBooks();
    }

    @Override
    public String serialize(List<Book> books) throws Exception {
        SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();//
        TransformerHandler handler = factory.newTransformerHandler();           //
        Transformer transformer = handler.getTransformer();                     //
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");            // 
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");                // 
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        handler.setResult(result);
        
        String uri = "";
        String localName = "";
        
        handler.startDocument();
        handler.startElement(uri, localName, "books", null);
        
        AttributesImpl attrs = new AttributesImpl();
        char[] ch = null;
        for (Book book : books) {
            attrs.clear();
            attrs.addAttribute(uri, localName, "id", "string", String.valueOf(book.getId()));
            handler.startElement(uri, localName, "book", attrs);
            
            handler.startElement(uri, localName, "name", null);
            ch = String.valueOf(book.getName()).toCharArray();
            handler.characters(ch, 0, ch.length);
            handler.endElement(uri, localName, "name");
            
            handler.startElement(uri, localName, "price", null);
            ch = String.valueOf(book.getPrice()).toCharArray();
            handler.characters(ch, 0, ch.length);
            handler.endElement(uri, localName, "price");
            
            handler.endElement(uri, localName, "book");
        }
        handler.endElement(uri, localName, "books");
        handler.endDocument();
        
        return writer.toString();
    }
    
    private class MyHandler extends DefaultHandler {

        private List<Book> books;
        private Book book;
        private StringBuilder builder;
        
        public List<Book> getBooks() {
            return books;
        }
        
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            books = new ArrayList<Book>();
            builder = new StringBuilder();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (localName.equals("book")) {
                book = new Book();
            }
            builder.setLength(0);
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            builder.append(ch, start, length);
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equals("id")) {
                book.setId(Integer.parseInt(builder.toString()));
            } else if (localName.equals("name")) {
                book.setName(builder.toString());
            } else if (localName.equals("price")) {
                book.setPrice(Float.parseFloat(builder.toString()));
            } else if (localName.equals("book")) {
                books.add(book);
            }
        }
    }
}
