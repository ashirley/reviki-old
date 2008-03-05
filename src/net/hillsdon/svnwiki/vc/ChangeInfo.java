package net.hillsdon.svnwiki.vc;

import java.util.Date;

/**
 * Describes a change.
 * 
 * @author mth
 */
public class ChangeInfo {

  private final String _page;
  private final String _user;
  private final Date _date;
  private final long _revision;
  private final String _commitMessage;
  
  public ChangeInfo(final String page, final String user, final Date date, final long revision, final String commitMessage) {
    _page = page;
    _user = user;
    _date = date;
    _revision = revision;
    _commitMessage = commitMessage.trim();
  }

  public String getPage() {
    return _page;
  }

  public String getUser() {
    return _user;
  }

  public Date getDate() {
    return _date;
  }
  
  public long getRevision() {
    return _revision;
  }

  public String getDescription() {
    String description = stripFinalURL();
    if (description.startsWith("[") && description.endsWith("]")) {
      description = "None";
    }
    return description;
  }

  private String stripFinalURL() {
    int nl = _commitMessage.lastIndexOf("\n");
    if (nl != -1) {
      String lastLine = _commitMessage.substring(nl + 1).trim();
      if (lastLine.startsWith("http://") || lastLine.startsWith("https://")) {
        return _commitMessage.substring(0, nl).trim();
      }
    }
    return _commitMessage;
  }
  
  public String getCommitMessage() {
    return _commitMessage;
  }
  
}
