package com.softwareag.wx.is.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class WmWaitForServer extends WmServiceTask
{
  private int intervalSec = 30;
  private int numPings = 10;
  private String timeoutProperty;
  private boolean failWhenNotReachable = true;

  public String getFolder()
  {
    return "wm.server";
  }

  public String getService()
  {
    return "ping";
  }

  public void execute() throws BuildException
  {
    int pingsDone = 0;

    log("Starting to check Integration Server ...", Project.MSG_DEBUG);
    while (pingsDone < numPings)
    {
      try
      {
        log("Attempt " + (pingsDone+1) + " of " + numPings, Project.MSG_DEBUG);
        super.execute();
        log("Connected to the server successfully", Project.MSG_DEBUG);
        return;
      }
      catch (Exception e)
      {
        log(e, Project.MSG_DEBUG);
      }
      pingsDone++;
      if (pingsDone >= numPings)
      {
        String errorMessage = "Exhausted all attemmpts. Server is not available";
        if (failWhenNotReachable)
        {
          throw new BuildException(errorMessage);
        }
        log(errorMessage, Project.MSG_DEBUG);
        break;
      }
        
      try
      {
        log("Pausing for " + intervalSec + " seconds", Project.MSG_DEBUG);
        Thread.sleep(intervalSec * 1000);
      }
      catch (InterruptedException e)
      {
      }
    }
    if (timeoutProperty != null || timeoutProperty.length() > 0)
    {
      getProject().setProperty(timeoutProperty, "true");
    }    
  }

  public int getIntervalSec()
  {
    return intervalSec;
  }

  public void setIntervalSec(int intervalSec)
  {
    this.intervalSec = intervalSec;
  }

  public int getNumPings()
  {
    return numPings;
  }

  public void setNumPings(int noPings)
  {
    this.numPings = noPings;
  }

  public String getTimeoutProperty()
  {
    return timeoutProperty;
  }

  public void setTimeoutProperty(String timeoutProperty)
  {
    this.timeoutProperty = timeoutProperty;
  }
  
  public boolean isFailWhenNotReachable()
  {
    return failWhenNotReachable;
  }

  public void setFailWhenNotReachable(boolean failWhenNotReachable)
  {
    this.failWhenNotReachable = failWhenNotReachable;
  }
  
}
