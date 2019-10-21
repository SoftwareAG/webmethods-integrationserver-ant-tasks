package com.softwareag.wx.is.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;

public class WmSyncDoctypes extends WmServiceTask
{
  public String getFolder()
  {
    return "wm.server.ed";
  }

  public String getService()
  {
    return "submitMany";
  }

  public void execute() throws BuildException
  {
    IData pipeline = invoke("wm.broker.sync", "listOutOfSyncs", null);

    String[] nsNames = null;

    IDataCursor pipelineCursor = pipeline.getCursor();
    IData[] outOfSyncs = IDataUtil.getIDataArray(pipelineCursor, "outOfSyncs");
    if (outOfSyncs != null)
    {
      nsNames = new String[outOfSyncs.length];
      for (int i = 0; i < outOfSyncs.length; i++)
      {
        IDataCursor outOfSyncsCursor = outOfSyncs[i].getCursor();
        nsNames[i] = IDataUtil.getString(outOfSyncsCursor, "nsName");
        outOfSyncsCursor.destroy();
      }
    }
    pipelineCursor.destroy();

    IDataUtil.remove(pipelineCursor, "outOfSyncs");

    IDataCursor pipelineCursor1 = pipeline.getCursor();
    IDataUtil.put(pipelineCursor1, "nsNames", nsNames);
    IData input = getInput();
    if (input == null)
    {
      setInput(pipeline);
    }
    super.execute();
    IData output = getOutput();
    IDataCursor cursor = output.getCursor();
    IData[] errors = IDataUtil.getIDataArray(cursor, "errors");
    cursor.destroy();
    if (errors != null && errors.length > 0)
    {
      cursor = errors[0].getCursor();
      String errorMessage = IDataUtil.getString(cursor, "errorMessage");
      cursor.destroy();
      throw new BuildException(errorMessage);
    }
    if (nsNames == null || nsNames.length == 0)
    {
      log("No documents need to be synchronized", Project.MSG_DEBUG);
    }
    else
    {
      log("Successfully synchronized " + nsNames.length + " documents.", Project.MSG_DEBUG);
    }
  }
  
  public static void main(String[] args) throws Exception
  {
    WmSyncDoctypes task = new WmSyncDoctypes();
    task.setProject(new Project());
    task.execute();
  }
}
