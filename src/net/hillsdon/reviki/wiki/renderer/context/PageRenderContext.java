package net.hillsdon.reviki.wiki.renderer.context;

import java.util.LinkedHashMap;

/**
 * The context in which we render a whole page. This can be  used to store state between RenderNodes but only for the duration of a single page render.
 */
public class PageRenderContext {
  LinkedHashMap<String, Object> _attributes = new LinkedHashMap<String, Object>();

  public void setAttribute(final String name, final Object value) {
    _attributes.put(name, value);
  }

  public Object getAttribute(final String name) {
    return _attributes.get(name);
  }
}
