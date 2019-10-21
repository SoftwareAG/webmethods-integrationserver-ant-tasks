package com.softwareag.wx.is.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;


public class WmDeletePackage extends WmServiceTask
{
  protected String packageName;
  protected boolean safeDelete = true;
  protected boolean ignoreError = true;

  /**
   * @return Returns the safeDelete.
   */
  public boolean isSafeDelete()
  {
    return safeDelete;
  }

  /**
   * @param safeDelete
   *          The safeDelete to set.
   */
  public void setSafeDelete(boolean safeDelete)
  {
    this.safeDelete = safeDelete;
  }

  /**
   * @return Returns the ignoreError.
   */
  public boolean isIgnoreError()
  {
    return ignoreError;
  }

  /**
   * @param ignoreError
   *          The ignoreError to set.
   */
  public void setIgnoreError(boolean ignoreError)
  {
    this.ignoreError = ignoreError;
  }

  public String getPackage()
  {
    return packageName;
  }

  public void setPackage(String pkg)
  {
    this.packageName = pkg;
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
    return "packageDelete";
  }

  /**
   * @see com.softwareag.wx.is.ant.tasks.is.WmServiceTask#prepare()
   */
  protected void prepare() throws BuildException
  {
    super.prepare();
    checkRequiredStringAttribute("package", getPackage());
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
    IDataUtil.put(cursor, "package", packageName);
    IDataUtil.put(cursor, "safeDelete", String.valueOf(safeDelete));
    cursor.destroy();
    super.execute();
    cursor = getOutput().getCursor();
    if (!isIgnoreError() && IDataUtil.getInt(cursor, "code", -1) < 0)
    {
      String message = IDataUtil.getString(cursor, "message");
      cursor.destroy();
      throw new BuildException("Package delete failed : " + message);
    }
    cursor.destroy();
  }
}