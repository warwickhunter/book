/*
 * Copyright 2008-2011 Grant Ingersoll, Thomas Morton and Drew Farris
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * -------------------
 * To purchase or learn more about Taming Text, by Grant Ingersoll, Thomas Morton and Drew Farris, visit
 * http://www.manning.com/ingersoll
 */

package com.tamingtext.tika;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tamingtext.TamingTextTestJ4;


/**
 * Demonstrate basic Tika usage
 *
 **/
public class TikaTest extends TamingTextTestJ4 {
  
  private static final int MAX_FILE_SIZE = 100 * 1024 * 1024;
    
  @Test
  public void testTika() throws Exception {
    boolean useMyHandler = true;
      
    //<start id="tika"/>
    InputStream input = new FileInputStream(new File("src/test/resources/avalon.pdf")); // pdfBox-sample.pdf"));//<co id="tika.is"/>
    ContentHandler textHandler = (useMyHandler) ? new MyHandler() : new BodyContentHandler(MAX_FILE_SIZE);//<co id="tika.handler"/>
    Metadata metadata = new Metadata();//<co id="tika.metadata"/>
    Parser parser = new AutoDetectParser();//<co id="tika.parser"/>
    ParseContext context = new ParseContext();
    parser.parse(input, textHandler, metadata, context);//<co id="tika.parse"/>
    for (String name : metadata.names()) {
        System.out.printf("%s=%s %n", name, metadata.get(name));
    }
    //System.out.println("Body: " + textHandler.toString());//<co id="tika.body"/>
    /*
<calloutlist>
    <callout arearefs="tika.is"><para>Create the <classname>InputStream</classname> to read in the content</para></callout>
    <callout arearefs="tika.handler"><para>The <classname>BodyContentHandler</classname> is a Tika-provided <classname>ContentHandler</classname> that extracts just the "body" of the InputStream</para></callout>
  <callout arearefs="tika.metadata"><para>The <classname>Metadata</classname> object will hold metadata like author, title, etc. about the content in a map.</para></callout>
  <callout arearefs="tika.parser"><para>The <classname>AutoDetectParser</classname> will figure out the MIME type of the document automatically when parse is called.  Since we know our input is a PDF file, we could have used the <classname>PDFParser</classname> instead.</para></callout>
  <callout arearefs="tika.parse"><para>Execute the parse</para></callout>
  <callout arearefs="tika.title"><para>Get the title from the <classname>Metadata</classname> instance</para></callout>
  <callout arearefs="tika.body"><para>Print out the body from the <classname>ContentHandler</classname></para></callout>

</calloutlist>
*/
    //<end id="tika"/>
  }

  private static class MyHandler extends DefaultHandler {

    private static final int MAX_ELEMENTS = 100;
    private int m_elementCounter = 0;
    
    @Override
    public void startElement(String namespace, String localName, String qName, Attributes atts) throws SAXException {
        super.startElement(namespace, localName, qName, atts);
        if (m_elementCounter > MAX_ELEMENTS) {
            return;
        }
        System.out.printf("<%s", qName);
        for (int i = 0; i < atts.getLength(); ++i) {
            System.out.printf(" %s=\"%s\"", atts.getQName(i), atts.getValue(i));
        }
        System.out.println(">");
        ++m_elementCounter;
    }

    @Override
    public void characters(char[] ch, int offset, int length) throws SAXException {
        super.characters(ch, offset, length);
        if (m_elementCounter > MAX_ELEMENTS) {
            return;
        }
        System.out.printf("%s", new String(ch, offset, length));
    }

    @Override
    public void endElement(String namespace, String localName, String qName) throws SAXException {
        if (m_elementCounter > MAX_ELEMENTS) {
            return;
        }
        System.out.printf("</%s>%n", qName);
    }

  }
  
  @Test
  public void testHtml() throws Exception {
    String html = "<html><head><title>The Big Brown Shoe</title></head><body><p>The best pizza place " +
            "in the US is <a href=\"http://antoniospizzas.com/\">Antonio's Pizza</a>.</p>" +
            "<p>It is located in Amherst, MA.</p></body></html>";
    //<start id="tika-html"/>
    InputStream input = new ByteArrayInputStream(html.getBytes(Charset.forName("UTF-8")));
    ContentHandler text = new BodyContentHandler();//<co id="html.text.co"/>
    LinkContentHandler links = new LinkContentHandler();//<co id="html.link.co"/>
    ContentHandler handler = new TeeContentHandler(links, text);//<co id="html.merge"/>
    Metadata metadata = new Metadata();//<co id="html.store"/>
    Parser parser = new AutoDetectParser(); // HtmlParser();//<co id="html.parser"/>
    ParseContext context = new ParseContext();
    parser.parse(input, handler, metadata, context);//<co id="html.parse"/>
    System.out.println("Title: " + metadata.get(Metadata.TITLE));
    System.out.println("Body: " + text.toString());
    System.out.println("Links: " + links.getLinks());
    /*
    <calloutlist>
        <callout arearefs="html.text.co"><para>Construct a ContentHandler that will just extract between the body tags.</para></callout>
        <callout arearefs="html.link.co"><para>Construct ContentHandler that knows about HTML links</para></callout>
        <callout arearefs="html.merge"><para>Wrap up our ContentHandlers into one</para></callout>
        <callout arearefs="html.store"><para>Metadata is a simple storage mechanism where the extracted metadata gets stored</para></callout>
        <callout arearefs="html.parser"><para>We know the input is HTML, so construct a Parser to parse it</para></callout>
        <callout arearefs="html.parse"><para>Do the parse</para></callout>
    </calloutlist>
    */
    //<end id="tika-html"/>
  }

}
