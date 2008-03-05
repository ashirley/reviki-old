package net.hillsdon.svnwiki.vc;

import java.util.Date;

import junit.framework.TestCase;

public class TestChangeInfo extends TestCase {

  public void testGetDescriptionStripsURL() {
    ChangeInfo changeInfo = new ChangeInfo("MyPage", "MyPage", "mth", new Date(), 1, "Twiddled frobs.\nhttp://www.example.com/wiki/MyPage\n", StoreKind.PAGE);
    assertEquals("Twiddled frobs.", changeInfo.getDescription());
  }
 
  public void testMinorEditPrefixIgnored() {
    ChangeInfo changeInfo = new ChangeInfo("MyPage", "MyPage", "mth", new Date(), 1, "[minor edit]\nTwiddled frobs.\nhttp://www.example.com/wiki/MyPage\n", StoreKind.PAGE);
    assertEquals("Twiddled frobs.", changeInfo.getDescription());
  }
  
  public void testAutoGeneratedMessagesNotReturnedInDescription() {
    ChangeInfo changeInfo = new ChangeInfo("MyPage", "MyPage", "mth", new Date(), 1, "[svnwiki commit]\nhttp://www.example.com/wiki/MyPage\n", StoreKind.PAGE);
    assertEquals("None", changeInfo.getDescription());
  }
  
}

