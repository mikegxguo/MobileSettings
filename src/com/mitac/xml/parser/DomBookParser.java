package com.mitac.xml.parser;


import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mitac.xml.model.Book;

public class DomBookParser implements BookParser {

    @Override
    public List<Book> parse(InputStream is) throws Exception {
        List<Book> books = new ArrayList<Book>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        Element rootElement = doc.getDocumentElement();
        NodeList items = rootElement.getElementsByTagName("book");
        for (int i = 0; i < items.getLength(); i++) {
            Book book = new Book();
            Node item = items.item(i);
            NodeList properties = item.getChildNodes();
            for (int j = 0; j < properties.getLength(); j++) {
                Node property = properties.item(j);
                String nodeName = property.getNodeName();
                if (nodeName.equals("id")) {
                    book.setId(Integer.parseInt(property.getFirstChild().getNodeValue()));
                } else if (nodeName.equals("name")) {
                    book.setName(property.getFirstChild().getNodeValue());
                } else if (nodeName.equals("price")) {
                    book.setPrice(Float.parseFloat(property.getFirstChild().getNodeValue()));
                }
            }
            books.add(book);
        }
        return books;
    }

    @Override
    public String serialize(List<Book> books) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        
        Element rootElement = doc.createElement("books");

        for (Book book : books) {
            Element bookElement = doc.createElement("book");
            bookElement.setAttribute("id", book.getId() + "");
            
            Element nameElement = doc.createElement("name");
            nameElement.setTextContent(book.getName());
            bookElement.appendChild(nameElement);
            
            Element priceElement = doc.createElement("price");
            priceElement.setTextContent(book.getPrice() + "");
            bookElement.appendChild(priceElement);
            
            rootElement.appendChild(bookElement);
        }
        
        doc.appendChild(rootElement);
        
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        
        StringWriter writer = new StringWriter();
        
        Source source = new DOMSource(doc);
        Result result = new StreamResult(writer);
        transformer.transform(source, result);
        
        return writer.toString();
    }

}