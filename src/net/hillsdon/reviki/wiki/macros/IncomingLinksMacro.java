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
package net.hillsdon.reviki.wiki.macros;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.wiki.graph.WikiGraph;

public class IncomingLinksMacro extends AbstractListOfPagesMacro {

  private final WikiGraph _wikiGraph;

  public IncomingLinksMacro(final WikiGraph wikiGraph) {
    _wikiGraph = wikiGraph;
  }
  
  public String getName() {
    return "outgoingLinks";
  }

  @Override
  protected Collection<String> getAllowedArgs() {
    return Collections.emptyList();
  }

  @Override
  protected Collection<SearchMatch> getPages(final String remainder) throws IOException, PageStoreException {
    return _wikiGraph.outgoingLinks(remainder);
  }

}
