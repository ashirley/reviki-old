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
package net.hillsdon.reviki.wiki.renderer.creole;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.wiki.renderer.context.PageRenderContext;
import net.hillsdon.reviki.wiki.renderer.result.LeafResultNode;

public class HtmlEscapeResultNode extends LeafResultNode {

  private final String _text;

  public HtmlEscapeResultNode(final String text, PageRenderContext context) {
    super(context);
    _text = text;
  }
  
  public String toXHTML() {
    return Escape.html(_text);
  }

}
