package net.hillsdon.reviki.webtests;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import net.hillsdon.fij.text.Escape;

/**
 * Test for the Search Macro. For the search form at the top of the pages and the backlinks at the bottom see {@link TestSearch}
 */
public class TestSearchMacro extends WebTestSupport {

  public void testSearchByPath() throws Exception {
    final String targetPageName = uniqueWikiPageName("SearchTarget");
    editWikiPage(targetPageName, "This page should be found by a search macro", "", true);

    final String sourcePageName = uniqueWikiPageName("SearchSource");
    editWikiPage(sourcePageName, "<<search:(search=\"path:" + targetPageName + "\")>>", "", true);

    HtmlPage page = getWikiPage(sourcePageName);
    assertAnchorPresentByHrefContains(page, Escape.urlEncodeUTF8(targetPageName));

    editWikiPage(sourcePageName, "<<search:(search=\"path:SearchTarget*\")>>", "", false);

    page = getWikiPage(sourcePageName);
    assertAnchorPresentByHrefContains(page, Escape.urlEncodeUTF8(targetPageName));

    //TODO put the asterisk at the beginning or beginning and end.
  }

  public void testSearchByOutgoingLinks() throws Exception {
    final String referred = uniqueWikiPageName("SearchTargetReferred");
    editWikiPage(referred, "This page should be found by a search macro", "", true);
    final String refers = uniqueWikiPageName("SearchTargetRefers");
    editWikiPage(refers, referred, "", true);

    final String sourcePageName = uniqueWikiPageName("SearchSource");
    editWikiPage(sourcePageName, "<<search:(search=\"outgoing-links:" + referred + "\")>>", "", true);

    HtmlPage page = getWikiPage(sourcePageName);
    assertAnchorPresentByHrefContains(page, Escape.urlEncodeUTF8(refers));


    final String refers2 = uniqueWikiPageName("SearchTargetRefersTwo");
    editWikiPage(refers2, referred, "", true);

    editWikiPage(sourcePageName, "<<search:(search=\"outgoing-links:" + referred + " AND NOT path:" + refers + "\")>>", "", false);

    page = getWikiPage(sourcePageName);
    assertAnchorAbsentByHrefContains(page, Escape.urlEncodeUTF8(refers));
    assertAnchorPresentByHrefContains(page, Escape.urlEncodeUTF8(refers2));
  }
}
