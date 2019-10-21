package com.softwareag.wx.is.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;


public class WmInstallPackageFromArchive extends WmServiceTask
{
  protected String archive;
  protected boolean activateOnInstall = true;
  protected boolean ignoreError = true;

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

  public String getArchive()
  {
    return archive;
  }

  public void setArchive(String archive)
  {
    this.archive = archive;
  }

  /**
   * @return Returns the activateOnInstall.
   */
  public boolean isActivateOnInstall()
  {
    return activateOnInstall;
  }

  /**
   * @param activateOnInstall The activateOnInstall to set.
   */
  public void setActivateOnInstall(boolean activateOnInstall)
  {
    this.activateOnInstall = activateOnInstall;
  }

  /** 
   * @see com.softwareag.wx.is.ant.tasks.is.WmServiceTask#getFolder()
   */
  public String getFolder()
  {
    return "wm.server.packages";
  }

  /**
   * @see com.softwareag.wx.is.ant.tasks.is.WmServiceTask#getService()
   */
  public String getService()
  {
    return "packageInstall";
  }

  protected void prepare() throws BuildException
  {
    super.prepare();
    checkRequiredStringAttribute("archive", getArchive());
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
    IDataUtil.put(cursor, "file", archive);
    IDataUtil.put(cursor, "packageFile", archive);
    IDataUtil.put(cursor, "activateOnInstall", String.valueOf(activateOnInstall));
    cursor.destroy();
    super.execute();
    cursor = getOutput().getCursor();
    if (!isIgnoreError() && IDataUtil.getInt(cursor, "installcode", -1) < 0)
    {
      String message = IDataUtil.getString(cursor, "message");
      cursor.destroy();
      throw new BuildException("Package install failed : " + message);
    }
    cursor.destroy();
  }
}