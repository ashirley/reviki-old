package net.hillsdon.svnwiki.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

/**
 * Wherein we go to mad lengths to store the SVN URL somewhere.
 * 
 * @author mth
 */
public class Configuration {

  private static final String DEFAULT_CONFIG_DIR_NAME = "svnwiki-data";
  private static final String CONFIG_FILE_NAME = "svnwiki.properties";
  // Properties file keys:
  private static final String KEY_SVN_URL = "svn-url";
  
  private SVNURL _url = null;

  public SVNURL getUrl() {
    return _url;
  }

  public void setUrl(final String url) throws IllegalArgumentException {
    try {
      _url = SVNURL.parseURIDecoded(url);
    }
    catch (SVNException e) {
      throw new IllegalArgumentException("Invalid SVN URL", e);
    }
  }

  public boolean isComplete() {
    return _url != null;
  }

  /**
   * @return A configuration location if we can, otherwise null.
   */
  private File getConfigurationLocation() {
    String location = null;
    try {
      location = System.getProperty("svnwiki.data");
    }
    catch (SecurityException ex) {
    }
    if (location == null) {
      try {
        location = System.getenv("SVNWIKI_DATA");
      }
      catch (SecurityException ex) {
      }
    }
    if (location == null) {
      try {
        String home = System.getProperty("user.home");
        location = home + File.separator + DEFAULT_CONFIG_DIR_NAME;
      }
      catch (SecurityException ex) {
      }
    }
    if (location == null) {
      return null;
    }
    File dir = new File(location);
    try {
      if (!dir.exists()) {
        if (!dir.mkdir()) {
          return null;
        }
      }
    }
    catch (SecurityException ex) {
      return null;
    }
    return dir;
  }
  
  public synchronized void load() {
    File location = getConfigurationLocation();
    if (location != null) {
      File file = new File(location, CONFIG_FILE_NAME);
      try {
        FileInputStream in = new FileInputStream(file);
        try {
          Properties properties = new Properties();
          properties.load(in);
          try {
            setUrl(properties.getProperty(KEY_SVN_URL));
          }
          catch (IllegalArgumentException ex) {
            // Oh well.
          }
        }
        finally {
          in.close();
        }
      }
      catch (IOException ex) {
        // We swallow errors for now. 
      }
    }
  }

  public synchronized void save() {
    File location = getConfigurationLocation();
    if (location != null && location.canWrite()) {
      File file = new File(location, CONFIG_FILE_NAME);
      Properties properties = new Properties();
      properties.setProperty(KEY_SVN_URL, getUrl().toDecodedString());
      try {
        FileOutputStream in = new FileOutputStream(file);
        try {
          properties.store(in, "svnwiki configuration details");
        }
        finally {
          in.close();
        }
      }
      catch (IOException ex) {
        // We swallow errors for now.
      }
    }
  }

}
