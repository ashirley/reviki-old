/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.web.pages.impl;

import static net.hillsdon.fij.core.Functional.iter;
import static net.hillsdon.fij.core.Functional.map;
import static net.hillsdon.fij.core.Functional.set;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.ATTR_BACKLINKS;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.ATTR_BACKLINKS_LIMITED;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.ATTR_MARKED_UP_DIFF;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.ATTR_PAGE_INFO;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.PARAM_DIFF_REVISION;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.vc.impl.PageRevisionReference;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.MockHttpServletRequest;
import net.hillsdon.reviki.web.pages.DiffGenerator;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.feeds.FeedWriter;
import net.hillsdon.reviki.wiki.graph.WikiGraph;
import net.hillsdon.reviki.wiki.renderer.result.LiteralResultNode;

import org.easymock.EasyMock;

/**
 * Tests.
 *
 * @author mth
 */
public class TestDefaultPageImplGet extends TestCase {

  private static final PageReference THE_PAGE = new PageReferenceImpl("ThePage");

  private CachingPageStore _store;
  private MarkupRenderer _renderer;
  private WikiGraph _graph;
  private MockHttpServletRequest _request;
  private HttpServletResponse _response;
  private DefaultPageImpl _page;
  private DiffGenerator _diffGenerator;
  private WikiUrls _wikiUrls;

  private FeedWriter _feedWriter;

  @Override
  protected void setUp() throws Exception {
    _request = new MockHttpServletRequest();
    _response = null;
    _store = createMock(CachingPageStore.class);
    _renderer = createMock(MarkupRenderer.class);
    _graph = createMock(WikiGraph.class);
    _diffGenerator = createMock(DiffGenerator.class);
    _wikiUrls = createMock(WikiUrls.class);
    _feedWriter = createMock(FeedWriter.class);
    _page = new DefaultPageImpl(null, _store, _renderer, _graph, _diffGenerator, _wikiUrls, _feedWriter);
  }

  /**
   * The usual case of viewing an existing page.
   */
  public void testNoRevisionNoDiffViewsHead() throws Exception {
    // We should get all the links.
    expectGetIncomingLinks(getCountIncomingLinks(MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY));
    PageInfo expectedPageInfo = expectGetContent();
    expectRenderContent();
    replay();
    JspView view = (JspView) _page.get(THE_PAGE, ConsumedPath.EMPTY, _request, _response);
    assertEquals("ViewPage", view.getName());
    // Check data provided to view.
    assertNotNull(_request.getAttribute(DefaultPageImpl.ATTR_RENDERED_CONTENTS));
    assertEquals(MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY, ((Collection<?>) _request.getAttribute(ATTR_BACKLINKS)).size());
    assertNull(_request.getAttribute(ATTR_BACKLINKS_LIMITED));
    assertSame(expectedPageInfo, _request.getAttribute(ATTR_PAGE_INFO));
    verify();
  }

  public void testLimitsBacklinksIfMoreThanBackLinkLimitAndAddsAttributeToRequestIndicatingTheLimitWasReached() throws Exception {
    expectGetIncomingLinks(getCountIncomingLinks(MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY + 1));
    expectGetContent();
    expectRenderContent();
    replay();
    _page.get(THE_PAGE, ConsumedPath.EMPTY, _request, _response);
    assertEquals(MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY, ((Collection<?>) _request.getAttribute(ATTR_BACKLINKS)).size());
    assertNotNull(_request.getAttribute(ATTR_BACKLINKS_LIMITED));
    verify();
  }

  public void testProvideRevisionAndDiffViewsDiffBetweenTheRevisions() throws Exception {
    _request.setParameter(DefaultPageImpl.PARAM_REVISION, "6");
    _request.setParameter(PARAM_DIFF_REVISION, "4");
    expectGetIncomingLinks();
    String sixContent = "Content at revision six.";
    String fourContent = "Content at revision four.";
    expectGetContent(6, sixContent);
    expectGetContent(4, fourContent);
    String theDiff = "It's changed!";
    expect(_diffGenerator.getDiffMarkup(fourContent, sixContent)).andReturn(theDiff);
    // We don't render anything.
    replay();
    JspView view = (JspView) _page.get(THE_PAGE, ConsumedPath.EMPTY, _request, _response);
    assertEquals("ViewDiff", view.getName());
    assertEquals(theDiff, _request.getAttribute(ATTR_MARKED_UP_DIFF));
    verify();
  }

  public void testProvideDiffAcrossRenamedPages() throws Exception {
    final PageReference page1 = new PageReferenceImpl("Pa.1?@ge1");
    final PageReference page2 = new PageReferenceImpl("Pa--.1?@geaa2");
    _request.setParameter(DefaultPageImpl.PARAM_REVISION, page1.getPath() + ".6");
    _request.setParameter(PARAM_DIFF_REVISION, page2.getPath() + ".4");
    expectGetIncomingLinks();
    String sixContent = "Content at revision six.";
    String fourContent = "Content at revision four.";
    expectGetContent(page1, 6, sixContent);
    expectGetContent(page2, 4, fourContent);
    String theDiff = "It's changed!";
    expect(_diffGenerator.getDiffMarkup(fourContent, sixContent)).andReturn(theDiff);
    // We don't render anything.
    replay();
    JspView view = (JspView) _page.get(THE_PAGE, ConsumedPath.EMPTY, _request, _response);
    assertEquals("ViewDiff", view.getName());
    assertEquals(theDiff, _request.getAttribute(ATTR_MARKED_UP_DIFF));
    verify();
  }

  public void testGetPageRevisionReference() throws Exception {
    PageReference defaultPage = new PageReferenceImpl("default");
    assertEquals(new PageRevisionReference(defaultPage, -1), _page.getPageRevisionReference(defaultPage, null, "revision"));
    assertEquals(new PageRevisionReference(defaultPage, 1), _page.getPageRevisionReference(defaultPage, "1", "revision"));
    assertEquals(new PageRevisionReference(new PageReferenceImpl("foo"), 1), _page.getPageRevisionReference(defaultPage, "foo.1", "revision"));

    try {
      assertEquals(new PageRevisionReference(new PageReferenceImpl("foo"), 1), _page.getPageRevisionReference(defaultPage, "foo", "revision"));
      fail();
    }
    catch (InvalidInputException expected) {
    }
  }

  private String[] getCountIncomingLinks(final int count) {
    String[] incomingLinks = new String[count];
    for (int i = 0; i < incomingLinks.length; ++i) {
      incomingLinks[i] = "IncomingLink" + (i + 1);
    }
    return incomingLinks;
  }

  private void verify() {
    EasyMock.verify(_store, _renderer, _graph, _diffGenerator);
  }

  private void replay() {
    EasyMock.replay(_store, _renderer, _graph, _diffGenerator);
  }

  private void expectRenderContent() throws Exception  {
    expect(_renderer.render(eq(THE_PAGE), eq("Content"), isA(URLOutputFilter.class))).andReturn(new LiteralResultNode("Content", null)).once();
  }

  private void expectGetIncomingLinks(final String... returnedPages) throws Exception  {
    expect(_graph.incomingLinks(THE_PAGE.getPath())).andReturn(set(map(iter(returnedPages), SearchMatch.FROM_PAGE_NAME))).once();
  }

  private PageInfo expectGetContent() throws Exception  {
    return expectGetContent(-1, "Content");
  }

  private PageInfo expectGetContent(final int revision, final String content) throws Exception  {
    return expectGetContent(THE_PAGE, revision, content);
  }

  private PageInfo expectGetContent(final PageReference page, final int revision, final String content) throws Exception {
    PageInfo pageInfo = new PageInfoImpl(page.getPath(), content, revision, revision, "", new Date(), "", "", null);
    expect(_store.get(page, revision)).andReturn(pageInfo).once();
    return pageInfo;
  }

}
