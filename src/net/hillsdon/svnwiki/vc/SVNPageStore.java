package net.hillsdon.svnwiki.vc;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hillsdon.svnwiki.vc.SVNHelper.SVNAction;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNTimeUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * Stores pages in an SVN repository.
 * 
 * @author mth
 */
public class SVNPageStore implements PageStore {

  /**
   * The assumed encoding of files from the repository.
   */
  private static final String UTF8 = "UTF8";

  private final SVNHelper _helper;

  /**
   * Note the repository URL can be deep, it need not refer to the root of the
   * repository itself. We put pages in the root of what we're given.
   */
  public SVNPageStore(final SVNRepository repository) {
    _helper = new SVNHelper(repository);
  }

  public List<ChangeInfo> recentChanges(final int limit) throws PageStoreException {
    return _helper.execute(new SVNAction<List<ChangeInfo>>() {
      public List<ChangeInfo> perform(final SVNRepository repository) throws SVNException {
        return _helper.log(PathTranslator.ATTACHMENT_TO_PAGE, "", limit, false, 0, -1);
      }
    });
  }

  public List<ChangeInfo> history(final PageReference ref) throws PageStoreException {
    return _helper.execute(new SVNAction<List<ChangeInfo>>() {
      public List<ChangeInfo> perform(final SVNRepository repository) throws SVNException {
        return _helper.log(PathTranslator.ATTACHMENT_TO_PAGE, ref.getPath(), -1, true, 0, -1);
      }
    });
  }


  public Collection<PageReference> list() throws PageStoreException {
    return _helper.execute(new SVNAction<Collection<PageReference>>() {
      public Collection<PageReference> perform(final SVNRepository repository) throws SVNException {
        // Should  we be returning the entries here?
        List<PageReference> names = new ArrayList<PageReference>();
        for (PageStoreEntry e : _helper.listFiles("")) {
          names.add(new PageReference(e.getName()));
        }
        return names;
      }
    });
  }


  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    return _helper.execute(new SVNAction<PageInfo>() {
      public PageInfo perform(final SVNRepository repository) throws SVNException, PageStoreException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HashMap<String, String> properties = new HashMap<String, String>();

        SVNNodeKind kind = repository.checkPath(ref.getPath(), revision);
        if (SVNNodeKind.FILE.equals(kind)) {
          repository.getFile(ref.getPath(), revision, properties, baos);
          long actualRevision = SVNProperty.longValue(properties.get(SVNProperty.REVISION));
          long lastChangedRevision = SVNProperty.longValue(properties.get(SVNProperty.COMMITTED_REVISION));
          Date lastChangedDate = SVNTimeUtil.parseDate(properties.get(SVNProperty.COMMITTED_DATE));
          String lastChangedAuthor = properties.get(SVNProperty.LAST_AUTHOR);
          SVNLock lock = repository.getLock(ref.getPath());
          String lockOwner = lock == null ? null : lock.getOwner();
          String lockToken = lock == null ? null : lock.getID();
          return new PageInfo(ref.getPath(), toUTF8(baos.toByteArray()), actualRevision, lastChangedRevision, lastChangedAuthor, lastChangedDate, lockOwner, lockToken);
        }
        else if (SVNNodeKind.NONE.equals(kind)) {
          // Distinguishing between 'uncommitted' and 'deleted' would be useful
          // for history.
          return new PageInfo(ref.getPath(), "", PageInfo.UNCOMMITTED, PageInfo.UNCOMMITTED, null, null, null, null);
        }
        else {
          throw new PageStoreException(format("Unexpected node kind '%s' at '%s'", kind, ref));
        }
      }
    });
  }

  public PageInfo tryToLock(final PageReference ref) throws PageStoreException {
    final PageInfo page = get(ref, -1);
    if (page.isNew()) {
      return page;
    }

    return _helper.execute(new SVNAction<PageInfo>() {
      public PageInfo perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          long revision = page.getRevision();
          Map<String, Long> pathsToRevisions = singletonMap(ref.getPath(), revision);
          repository.lock(pathsToRevisions, "Locked by svnwiki.", false, new SVNLockHandlerAdapter());
          return get(ref, revision);
        }
        catch (SVNException ex) {
          if (SVNErrorCode.FS_PATH_ALREADY_LOCKED.equals(ex.getErrorMessage().getErrorCode())) {
            // The caller will check getLockedBy().
            return get(ref, -1);
          }
          throw ex;
        }
      }
    });
  }

  public void unlock(final PageReference path, final String lockToken) throws PageStoreException {
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        repository.unlock(singletonMap(path.getPath(), lockToken), false, new SVNLockHandlerAdapter());
        return null;
      }
    });
  }

  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage)
      throws PageStoreException {
    return set(ref.getPath(), lockToken, baseRevision, new ByteArrayInputStream(fromUTF8(content)), commitMessage);
  }

  private long set(final String path, final String lockToken, final long baseRevision, final InputStream content, final String commitMessage) throws PageStoreException {
    return _helper.execute(new SVNAction<Long>() {
      public Long perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          Map<String, String> locks = lockToken == null ? Collections.<String, String> emptyMap() : Collections.<String, String> singletonMap(path, lockToken);
          ISVNEditor commitEditor = repository.getCommitEditor(commitMessage, locks, false, null);
          if (baseRevision == PageInfo.UNCOMMITTED) {
            _helper.createFile(commitEditor, path, content);
          }
          else {
            _helper.editFile(commitEditor, path, baseRevision, content);
          }
          return commitEditor.closeEdit().getNewRevision();
        }
        catch (SVNException ex) {
          if (SVNErrorCode.FS_CONFLICT.equals(ex.getErrorMessage().getErrorCode())) {
            // What to do!
            throw new InterveningCommitException(ex);
          }
          throw ex;
        }
      }
    });
  }

  public void attach(final PageReference ref, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    String dir = attachmentPath(ref);
    ensureDir(dir, commitMessage);
    set(dir + "/" + storeName, null, baseRevision, in, commitMessage);
  }

  public Collection<AttachmentHistory> attachments(final PageReference ref) throws PageStoreException {
    final String attachmentPath = attachmentPath(ref);
    List<ChangeInfo> changed = _helper.execute(new SVNAction<List<ChangeInfo>>() {
      public List<ChangeInfo> perform(final SVNRepository repository) throws SVNException, PageStoreException {
        if (repository.checkPath(attachmentPath, -1).equals(SVNNodeKind.DIR)) {
          return _helper.log(PathTranslator.RELATIVE, attachmentPath, -1, false, 0, -1);
        }
        return Collections.emptyList();
      }
    });
    Map<String, AttachmentHistory> results = new LinkedHashMap<String, AttachmentHistory>();
    for (ChangeInfo change : changed) {
      if (attachmentPath.equals(change.getPath())) {
        // We don't want changes to the directory, just the files.
        continue;
      }
      AttachmentHistory history = results.get(change.getName());
      if (history == null) {
        history = new AttachmentHistory();
        results.put(change.getName(), history);
      }
      history.getVersions().add(change);
    }
    return results.values();
  }
  
  private String attachmentPath(final PageReference ref) {
    return ref.getPath() + "-attachments";
  }

  private void ensureDir(final String dir, final String commitMessage) throws PageStoreException {
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        if (repository.checkPath(dir, -1) == SVNNodeKind.NONE) {
          ISVNEditor commitEditor = repository.getCommitEditor(commitMessage, null);
          try {
            _helper.createDir(commitEditor, dir);
          }
          finally {
            commitEditor.closeEdit();
          }
        }
        return null;
      }
    });
  }


  private static String toUTF8(final byte[] bytes) {
    try {
      return new String(bytes, UTF8);
    }
    catch (UnsupportedEncodingException e) {
      throw new AssertionError("Java supports UTF8.");
    }
  }

  private static byte[] fromUTF8(final String string) {
    try {
      return string.getBytes(UTF8);
    }
    catch (UnsupportedEncodingException e) {
      throw new AssertionError("Java supports UTF8.");
    }
  }

  public void attachment(final PageReference ref, final String attachment, final long revision, final ContentTypedSink sink) throws NotFoundException, PageStoreException {
    final String path = SVNPathUtil.append(attachmentPath(ref), attachment);
    final OutputStream out = new OutputStream() {
      boolean _first = true;
      public void write(final int b) throws IOException {
        if (_first) {
          sink.setContentType("application/octet-stream"); 
          sink.setFileName(attachment);
          _first = false;
        }
        sink.stream().write(b);
      }
    };
    
    _helper.execute(new SVNAction<Void>() {
      public Void perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          repository.getFile(path, revision, null, out);
        }
        catch (SVNException ex) {
          // FIXME: Presumably this code would be different for non-http repositories.
          if (SVNErrorCode.RA_DAV_REQUEST_FAILED.equals(ex.getErrorMessage().getErrorCode())) {
            throw new NotFoundException(ex);
          }
          throw ex;
        }
        return null;
      }
    });
  }

  public Collection<PageReference> getChangedBetween(final long start, final long end) throws PageStoreException {
    return _helper.execute(new SVNAction<Collection<PageReference>>() {
      public Collection<PageReference> perform(final SVNRepository repository) throws SVNException, PageStoreException {
        try {
          List<ChangeInfo> log = _helper.log(PathTranslator.ATTACHMENT_TO_PAGE, "", -1, false, start, end);
          Set<PageReference> pages = new LinkedHashSet<PageReference>(log.size());
          for (ChangeInfo info : log) {
            // Ick... skipping attachments etc.
            if (info.getPath().indexOf('/') == -1 && !info.getPath().endsWith("-attachments")) {
              pages.add(new PageReference(info.getPath()));
            }
          }
          return pages;
        }
        catch (SVNException ex) {
          if (SVNErrorCode.FS_NO_SUCH_REVISION.equals(ex.getErrorMessage().getErrorCode())) {
            return Collections.emptySet();
          }
          throw ex;
        }
      }
    });
  }

  public long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException {
    return _helper.execute(new SVNAction<Long>() {
      public Long perform(final SVNRepository repository) throws SVNException, PageStoreException {
        return repository.getLatestRevision();
      }
    });
  }

}
