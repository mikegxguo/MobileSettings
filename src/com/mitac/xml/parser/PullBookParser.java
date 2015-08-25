package com.mitac.xml.parser;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.mitac.xml.model.Book;

public class PullBookParser implements BookParser {
    
    @Override
    public List<Book> parse(InputStream is) throws Exception {
        List<Book> books = null;
        Book book = null;
        
//      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//      XmlPullParser parser = factory.newPullParser();
        
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "UTF-8");

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_DOCUMENT:
                books = new ArrayList<Book>();
                break;
            case XmlPullParser.START_TAG:
                if (parser.getName().equals("book")) {
                    book = new Book();
                } else if (parser.getName().equals("id")) {
                    eventType = parser.next();
                    book.setId(Integer.parseInt(parser.getText()));
                } else if (parser.getName().equals("name")) {
                    eventType = parser.next();
                    book.setName(parser.getText());
                } else if (parser.getName().equals("price")) {
                    eventType = parser.next();
                    book.setPrice(Float.parseFloat(parser.getText()));
                }
                break;
            case XmlPullParser.END_TAG:
                if (parser.getName().equals("book")) {
                    books.add(book);
                    book = null;    
                }
                break;
            }
            eventType = parser.next();
        }
        return books;
    }
    
    @Override
    public String serialize(List<Book> books) throws Exception {
//      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//      XmlSerializer serializer = factory.newSerializer();
        
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", true);
        serializer.startTag("", "books");
        for (Book book : books) {
            serializer.startTag("", "book");
            serializer.attribute("", "id", book.getId() + "");
            
            serializer.startTag("", "name");
            serializer.text(book.getName());
            serializer.endTag("", "name");
            
            serializer.startTag("", "price");
            serializer.text(book.getPrice() + "");
            serializer.endTag("", "price");
            
            serializer.endTag("", "book");
        }
        serializer.endTag("", "books");
        serializer.endDocument();
        
        return writer.toString();
    }
}
