package net.hillsdon.svnwiki.wiki.renderer;

import java.io.IOException;
import java.io.StringWriter;

import net.hillsdon.svnwiki.configuration.Configuration;
import net.hillsdon.svnwiki.configuration.InterWikiLinker;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.SimplePageStore;
import net.hillsdon.svnwiki.wiki.InternalLinker;

import org.codehaus.jackson.JsonParseException;

public class TestRenderingExtensions extends JsonDrivenRenderingTest {

  public class FakeConfiguration implements Configuration {
    public InterWikiLinker getInterWikiLinker() throws PageStoreException {
      InterWikiLinker linker = new InterWikiLinker();
      linker.addWiki("foo", "http://www.example.com/foo/Wiki?%s");
      return linker;
    }
  }

  public TestRenderingExtensions() throws JsonParseException, IOException {
    super(TestRenderingExtensions.class.getResource("rendering-extensions.json"));
  }

  @Override
  protected String render(final String input) throws IOException, PageStoreException {
    CreoleMarkupRenderer renderer = new CreoleMarkupRenderer(new FakeConfiguration(), new InternalLinker(new SimplePageStore()));
    final StringWriter out = new StringWriter();
    renderer.render(new PageReference(""), input, out);
    return out.toString();
  }
  
}