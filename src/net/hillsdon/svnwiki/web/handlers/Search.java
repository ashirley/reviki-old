package net.hillsdon.svnwiki.web.handlers;

import static java.lang.String.format;
import static net.hillsdon.svnwiki.text.WikiWordUtils.isWikiWord;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.RequestBasedWikiUrls;
import net.hillsdon.svnwiki.web.common.RequestHandler;

public class Search implements RequestHandler {

  private static final String OPENSEARCH_DESCRIPTION =
    "<?xml version='1.0' encoding='UTF-8'?>\n"
  + "<OpenSearchDescription xmlns='http://a9.com/-/spec/opensearch/1.1/'>\n"
  + "<ShortName>Wiki Search</ShortName>\n"
  + "<Description>Wiki Search</Description>\n"
  + "<Url type='text/html' template='%s?query={searchTerms}'/>\n"
  + "</OpenSearchDescription>\n";
  
  static final String PARAM_QUERY = "query";
  
  private final PageStore _store;
  private final SearchEngine _searchEngine;

  public Search(final PageStore store, final SearchEngine searchEngine) {
    _store = store;
    _searchEngine = searchEngine;
  }

  public void handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    if ("opensearch.xml".equals(path.next())) {
      response.setContentType("application/opensearchdescription+xml");
      response.getWriter().write(format(OPENSEARCH_DESCRIPTION, Escape.html(new RequestBasedWikiUrls(request).search())));
      return;
    }
    
    String query = request.getParameter(PARAM_QUERY);
    if (query == null) {
      query = "";
    }
    if (request.getParameter("force") == null && _store.list().contains(new PageReference(query))) {
      response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/pages/" + request.getAttribute("wikiName") + "/" + query));
    }
    else {
      if (isWikiWord(query)) {
        request.setAttribute("suggestCreate", query);
      }
      request.setAttribute("results", _searchEngine.search(query));
      request.getRequestDispatcher("/WEB-INF/templates/SearchResults.jsp").include(request, response);
    }
  }

}
