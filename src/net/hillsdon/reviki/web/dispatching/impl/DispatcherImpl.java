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
package net.hillsdon.reviki.web.dispatching.impl;

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.RequestAttributes;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.dispatching.Dispatcher;
import net.hillsdon.reviki.web.handlers.JumpToWikiUrl;
import net.hillsdon.reviki.web.handlers.ListWikis;
import net.hillsdon.reviki.web.redirect.RedirectView;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.vcintegration.RequestCompletedHandler;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAwareManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DispatcherImpl implements Dispatcher {

  private static final Log LOG = LogFactory.getLog(DispatcherServlet.class);

  private final ListWikis _list;
  private final WikiChoiceImpl _choice;
  private final JumpToWikiUrl _jumpToWiki;
  private final ApplicationUrls _applicationUrls;
  private final RequestLifecycleAwareManager _requestLifecycleAwareManager;
  private final RequestCompletedHandler _requestCompletedHandler;

  public DispatcherImpl(final ListWikis list, final WikiChoiceImpl choice, final JumpToWikiUrl jumpToWiki, final ApplicationUrls applicationUrls, final RequestLifecycleAwareManager requestLifecycleAwareManager, final RequestCompletedHandler requestCompletedHandler) {
    _list = list;
    _choice = choice;
    _jumpToWiki = jumpToWiki;
    _applicationUrls = applicationUrls;
    _requestLifecycleAwareManager = requestLifecycleAwareManager;
    _requestCompletedHandler = requestCompletedHandler;
  }

  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");
    request.setAttribute(ApplicationUrls.KEY, _applicationUrls);
    ConsumedPath path = new ConsumedPath(request);
    try {
      _requestLifecycleAwareManager.requestStarted(request);
      View view = handle(path, request, response);
      if (view != null) {
        view.render(request, response);
      }
    }
    catch (NotFoundException ex) {
      handleException(request, response, ex, false, "Could not find the file you were looking for.", 404);
    }
    catch (Exception ex) {
      handleException(request, response, ex, true, null, 500);
    }
    finally {
      _requestCompletedHandler.requestComplete();
    }
  }

  // This should be moved out of here...
  private View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    final String initial = path.next();
    // An internal hack (see index.jsp) to allow us to handle "/".
    if ("root".equals(initial)) {
      return new RedirectView(_applicationUrls.list());
    }
    else if ("pages".equals(initial)) {
      return _choice.handle(path, request, response);
    }
    else if ("jump".equals(initial)) {
      return _jumpToWiki.handle(path, request, response);
    }
    else if ("list".equals(initial)) {
      return _list.handle(path, request, response);
    }
    throw new NotFoundException();
  }

  private void handleException(final HttpServletRequest request, final HttpServletResponse response, final Exception ex, final boolean logTrace, final String customMessage, final int statusCode) throws ServletException, IOException {
    response.setStatus(statusCode);
    String user = (String) request.getAttribute(RequestAttributes.USERNAME);
    if (user == null) {
      user = "[none]";
    }
    String queryString = request.getQueryString();
    String uri = request.getRequestURI() + (queryString == null ? "" : "?" + queryString);
    String message = format("%s for user '%s' accessing '%s'.", ex.getClass().getSimpleName(), user, uri);
    if (logTrace) {
      LOG.error(message, ex);
    }
    else {
      LOG.warn(message, null);
    }
    request.setAttribute("exception", ex);
    request.setAttribute("customMessage", customMessage);
    request.getRequestDispatcher("/WEB-INF/templates/Error.jsp").forward(request, response);
  }

}
