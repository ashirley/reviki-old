package net.hillsdon.svnwiki.wiki;

import static java.util.Arrays.asList;
import junit.framework.TestCase;

public class TestRenderedPage extends TestCase {

  private RenderedPageFactory _factory;

  @Override
  protected void setUp() throws Exception {
    _factory = new RenderedPageFactory(MarkupRenderer.AS_IS);
  }
  
  private RenderedPage create(String content) throws Exception {
    return _factory.create("FrontPage", content);
  }

  public void testGetPage() throws Exception {
    RenderedPage rendered = create("");
    rendered.getPage().equals("FrontPage");
  }
  
  public void testGetOutgoingLinksExistingPage() throws Exception {
    RenderedPage existingPage = create("<a href='pages/Foo'>Foo</a> to <a class='existing-page other-class' href='pages/Bar'>Bar description</a>");
    assertEquals(asList("Bar"), existingPage.findOutgoingWikiLinks());
  }

  public void testGetOutgoingLinksNewPage() throws Exception {
    RenderedPage newPage = create("<a href='pages/Foo'>Foo</a> to <a class='other-class new-page' href='pages/Bar'>Bar description</a>");
    assertEquals(asList("Bar"), newPage.findOutgoingWikiLinks());
  }
  
}
