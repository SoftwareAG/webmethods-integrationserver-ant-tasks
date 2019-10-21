package com.softwareag.wx.is.ant.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.tools.ant.BuildException;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;


public class WmReleaseArchiveToServer extends WmServiceTask
{
  protected File archive;
  protected String remoteArchive;
  protected String remoteFolder = "pkgin";
  protected boolean ignoreError = true;
  private static String validFolders[] = { "home", "log", "conf", "pkgin", "pkgout", "pkgsalvage" };

  public File getArchive()
  {
    return archive;
  }

  public void setArchive(File archive)
  {
    this.archive = archive;
  }

  /**
   * @return Returns the ignoreError.
   */
  public boolean isIgnoreError()
  {
    return ignoreError;
  }

  /**
   * @param ignoreError The ignoreError to set.
   */
  public void setIgnoreError(boolean ignoreError)
  {
    this.ignoreError = ignoreError;
  }

  /**
   * @return Returns the remoteFolder.
   */
  public String getRemoteFolder()
  {
    return remoteFolder;
  }

  /**
   * @param remoteFolder The remoteFolder to set.
   */
  public void setRemoteFolder(String remoteFolder)
  {
    this.remoteFolder = remoteFolder;
  }

  /**
   * @return Returns the remoteArchive.
   */
  public String getRemoteArchive()
  {
    return remoteArchive;
  }

  /**
   * @param remoteArchive The remoteArchive to set.
   */
  public void setRemoteArchive(String remoteArchive)
  {
    this.remoteArchive = remoteArchive;
  }

  /** 
   * @see com.softwareag.wx.is.ant.tasks.is.WmServiceTask#getFolder()
   */
  public String getFolder()
  {
    return "wm.server.util";
  }

  /**
   * @see com.softwareag.wx.is.ant.tasks.is.WmServiceTask#getService()
   */
  public String getService()
  {
    return "putFile";
  }

  /**
   * Tries to read the file fully and returns a byte array 
   */
  private void readFully(FileInputStream fis, byte[] buffer) throws IOException
  {
    int offset = 0;
    int numRead = 0;

    while ((numRead = fis.read(buffer, offset, buffer.length)) >= 0)
    {
      offset += numRead;
      if (offset >= buffer.length)
      {
        break;
      }
    }
  }

  protected void prepare() throws BuildException
  {
    super.prepare();

    if (getArchive() == null)
    {
      throw new BuildException("Attribute 'archive' is required.");
    }
    else if (!getArchive().exists() || !getArchive().isFile())
    {
      throw new BuildException("Archive " + getArchive().getAbsolutePath() + " does not exist or is not a file.");
    }

    int index = -1;
    for (int i = 0; i < validFolders.length; i++)
    {
      if (!remoteFolder.equalsIgnoreCase(validFolders[i]))
      {
        continue;
      }
      index = i;
      break;
    }

    if (index < 0)
    {
      throw new BuildException("Invalid remoteFolder name. Valid options are home, log, conf, pkgin, pkgout, pkgsalvage");
    }

  }

  public void execute() throws BuildException
  {
    IData input = getInput();
    if (input == null)
    {
      input = IDataFactory.create();
      setInput(input);
    }

    IDataCursor cursor = input.getCursor();

    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream(archive);
      byte[] buffer = new byte[(int) archive.length()];
      readFully(fis, buffer);
      IDataUtil.put(cursor, "$filedata", buffer);
    }
    catch (IOException ioe)
    {
      throw new BuildException(ioe);
    }
    finally
    {
      if (fis != null)
      {
        try
        {
          fis.close();
        }
        catch (IOException ioe)
        {
          //Can't do much if close fails
        }
      }

    }

    IDataUtil.put(cursor, "outdir", getRemoteFolder());
    IDataUtil.put(cursor, "outfile", getRemoteArchive() == null ? archive.getName() : getRemoteArchive());

    cursor.destroy();
    super.execute();
    cursor = getOutput().getCursor();
    if (!isIgnoreError() && !"OK".equalsIgnoreCase(IDataUtil.getString(cursor, "$result")))
    {
      cursor.destroy();
      throw new BuildException("Package install to remote server failed.");
    }
    cursor.destroy();
  }
}