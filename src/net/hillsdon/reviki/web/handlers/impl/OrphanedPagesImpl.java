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
package net.hillsdon.reviki.web.handlers.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.OrphanedPages;
import net.hillsdon.reviki.wiki.graph.WikiGraph;

public class OrphanedPagesImpl implements OrphanedPages {

  private final WikiGraph _graph;

  public OrphanedPagesImpl(final WikiGraph graph) {
    _graph = graph;
  }

  public String getName() {
    return "OrphanedPages";
  }

  public View handlePage(ConsumedPath path, HttpServletRequest request, HttpServletResponse response, PageReference page) throws Exception {
    List<String> alphabetical = new ArrayList<String>(_graph.isolatedPages());
    Collections.sort(alphabetical);
    request.setAttribute("pageList", alphabetical);
    return new JspView("OrphanedPages");
  }

}
