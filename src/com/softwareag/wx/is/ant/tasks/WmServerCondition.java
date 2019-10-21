package com.softwareag.wx.is.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;

public abstract class WmServerCondition extends WmServiceTask implements Condition
{
  public abstract boolean eval() throws BuildException;
}
