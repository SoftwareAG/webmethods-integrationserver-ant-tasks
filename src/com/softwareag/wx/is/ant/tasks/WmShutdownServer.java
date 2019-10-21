package com.softwareag.wx.is.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;

public class WmShutdownServer extends WmServiceTask
{
  private boolean restart = true;
  private boolean immediate = true;
  private int timeout;

  /**
   * @see com.softwareag.wx.is.ant.tasks.is.WmServiceTask#getFolder()
   */
  public String getFolder()
  {
    return "wm.server.admin";
  }

  /**
   * @see com.softwareag.wx.is.ant.tasks.is.WmServiceTask#getService()
   */
  public String getService()
  {
    return "shutdown";
  }

  /**
   * @return Returns the immediate.
   */
  public boolean isImmediate()
  {
    return immediate;
  }

  /**
   * @param immediate
   *          The immediate to set.
   */
  public void setImmediate(boolean immediate)
  {
    this.immediate = immediate;
  }

  /**
   * @return Returns the restart.
   */
  public boolean isRestart()
  {
    return restart;
  }

  /**
   * @param restart
   *          The restart to set.
   */
  public void setRestart(boolean restart)
  {
    this.restart = restart;
  }

  /**
   * @return Returns the timeout.
   */
  public int getTimeout()
  {
    return timeout;
  }

  /**
   * @param timeout
   *          The timeout to set.
   */
  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  protected void prepare() throws BuildException
  {
    super.prepare();
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
    IDataUtil.put(cursor, "bounce", isRestart() ? "yes" : "no");
    if (isImmediate())
    {
      IDataUtil.put(cursor, "option", "force");
    }
    else if (getTimeout() > 0)
    {
      IDataUtil.put(cursor, "timeout", String.valueOf(getTimeout()));
    }
    cursor.destroy();
    super.execute();
    cursor = getOutput().getCursor();
    String message = IDataUtil.getString(cursor, "message");
    if (message != null && message.length() > 0)
    {
      System.out.println(message);
    }
    cursor.destroy();
  }
}