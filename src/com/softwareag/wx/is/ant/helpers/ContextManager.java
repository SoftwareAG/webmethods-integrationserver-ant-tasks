package com.softwareag.wx.is.ant.helpers;

import java.util.HashMap;

import org.apache.tools.ant.Project;

import com.wm.app.b2b.client.Context;
import com.wm.app.b2b.client.ServiceException;

public class ContextManager
{
  private static HashMap<String, Context> contexts = new HashMap<String, Context>();
  static
  {
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          for (Context context : contexts.values())
          {
            if (context != null && context.isConnected())
            {
              context.disconnect();
            }
          }
        }
        catch (Throwable t)
        {

        }
      }
    });
  }

  public static Context getContext(Project project, String server, String user, String password) throws ServiceException
  {
    String key = server + "__" + user + "__" + password;
    Context context = contexts.get(key);
    if (context == null)
    {
      context = new Context();
      contexts.put(key, context);
    }
    if (!context.isConnected())
    {
      project.log("Connecting to Integration Server at " + server, Project.MSG_DEBUG);
      context.connect(server, user, password);
    }
    else
    {
      project.log("Already connected to Integration Server at " + server, Project.MSG_DEBUG);
    }
    return context;
  }

  public static String toString(Object o)
  {
    return o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
  }
}