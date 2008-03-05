package net.hillsdon.svnwiki.wiki;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.web.RequestBasedWikiUrls;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class TestFeedWriter extends TestCase {

  public void test() throws Exception {
    StringWriter out = new StringWriter();
    List<ChangeInfo> changes = Arrays.asList(new ChangeInfo("SomeWikiPage", "mth", new Date(0), 123, "Change description"));
    FeedWriter.writeAtom(new RequestBasedWikiUrls("http://www.example.com/svnwiki"), new PrintWriter(out), changes);

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    Document dom = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(out.toString())));
    Element selfLink = (Element) dom.getElementsByTagName("link").item(0);
    assertTrue(selfLink.getAttributeNS(null, "href").endsWith("/atom.xml"));
    
    NodeList entries = dom.getElementsByTagNameNS(FeedWriter.ATOM_NS, "entry");
    assertEquals(1, entries.getLength());
    // TODO, actually assert something useful.
  }
  
}
