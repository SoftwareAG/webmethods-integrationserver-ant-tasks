package com.softwareag.wx.is.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class WmServerAvailable extends WmServerCondition
{
  private int intervalSec = 10;
  private int numPings = 3;
  private boolean failWhenNotReachable = true;

  public String getFolder()
  {
    return "wm.server";
  }

  public String getService()
  {
    return "ping";
  }

  public boolean eval() throws BuildException
  {
    prepare();
    int pingsDone = 0;

    System.out.println("Starting to check Integration Server ...");
    log("Starting to check Integration Server ...", Project.MSG_DEBUG);
    while (true)
    {
      try
      {
        log("Attempt " + (pingsDone+1) + " of " + numPings, Project.MSG_DEBUG);
        System.out.println("Attempt " + (pingsDone+1) + " of " + numPings);
        super.execute();
        log("Connected to the server successfully", Project.MSG_DEBUG);
        return true;
      }
      catch (Throwable e)
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
    return false;
  }

  public int getIntervalSec()
  {
    return intervalSec;
  }

  public void setIntervalSec(int intervalSec)
  {
    this.intervalSec = intervalSec;
  }

  public boolean isFailWhenNotReachable()
  {
    return failWhenNotReachable;
  }

  public void setFailWhenNotReachable(boolean failWhenNotReachable)
  {
    this.failWhenNotReachable = failWhenNotReachable;
  }

  public int getNumPings()
  {
    return numPings;
  }

  public void setNumPings(int noPings)
  {
    this.numPings = noPings;
  }
}