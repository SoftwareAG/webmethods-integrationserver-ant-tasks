package com.softwareag.wx.is.ant.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.wm.app.b2b.client.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;

public class WmAssignAcls extends WmServiceTask
{
  private static final String executeAclType = "execute";
  private static final String listAclType = "list";
  private static final String readAclType = "read";
  private static final String writeAclType = "write";

  private boolean ignoreError = false;
  private String configAlias;
  
  public static String aclServicesPropertyPrefix = "webMethods.integrationServer.acls.svc";
  public static String listAclServicesPropertyPrefix = "webMethods.integrationServer.listacls.svc";
  public static String readAclServicesPropertyPrefix = "webMethods.integrationServer.readacls.svc";
  public static String writeAclServicesPropertyPrefix = "webMethods.integrationServer.writeacls.svc";
  public static String aclAllowGroupsPropertyPrefix = "webMethods.integrationServer.acls.groups";
  public static String aclsDenyGroupsPropertyPrefix = "webMethods.integrationServer.acls.denygroups";
  public static String groupUsersPropertyPrefix = "webMethods.integrationServer.groups.users";

  private Properties configProps = new Properties();
  
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

  /**
   * @return the configAlias
   */
  public String getConfigAlias()
  {
    return configAlias;
  }

  /**
   * @param configAlias
   *          the configAlias to set
   */
  public void setConfigAlias(String configAlias)
  {
    this.configAlias = configAlias;
  }

  /**
   * @see com.softwareag.wx.is.ant.tasks.is.WmServiceTask#prepare()
   */
  protected void prepare() throws BuildException
  {
    checkRequiredStringAttribute(serverAttributeName, getServer());
    checkRequiredNumericalAttribute(portAttributeName, getPort());
    checkRequiredStringAttribute(userAttributeName, getUser());
    checkRequiredStringAttribute(passwordAttributeName, getPassword());
    checkRequiredStringAttribute("configAlias", configAlias);
    try
    {
      String configFileName = configAlias + ".properties";
      File configFile = new File(configFileName);
      if (!configFile.exists() || !configFile.canRead())
      {
        throw new BuildException("File " + configFileName + " does not exist or is not readable");
      }
      configProps.load(new FileInputStream(configFile));
    }
    catch (Exception e)
    {
      throw new BuildException(e);
    }
  }

  private String[] parseList(String list)
  {
    if (list == null || list.trim().length() == 0)
    {
      return null;
    }
    return list.split("\\s*,\\s*");
  }

  private void addGroup(String group) throws ServiceException
  {
    if (group != null)
    {
      IData input = IDataFactory.create();
      IDataCursor cursor = input.getCursor();
      IDataUtil.put(cursor, "groups", group);
      cursor.destroy();
      log("Adding group " + group, Project.MSG_DEBUG);
      invoke("wm.server.access", "addGroups", input);
    }
  }

  private IData invoke(String folder, String service, IData input, boolean ignoreError)
  {
    try
    {
      IData out = super.invoke(folder, service, input);
      IDataCursor cursor = out.getCursor();
      String message = IDataUtil.getString(cursor, "message");
      if (message != null)
      {
        log(message, Project.MSG_DEBUG);
      }
      cursor.destroy();
      return out;
    }
    catch (Exception e)
    {
      if (!ignoreError)
      {
        throw new BuildException(e);
      }
      else
      {
        log("Error " + e.getMessage() + " ignored as attribute ignoreError was set to true", Project.MSG_WARN);
      }
    }
    return null;
  }

  protected IData invoke(String folder, String service, IData input)
  {
    return invoke(folder, service, input, ignoreError);
  }

  private void addUser(String user, String group) throws ServiceException
  {
    IData input = IDataFactory.create();
    IDataCursor cursor = input.getCursor();
    IDataUtil.put(cursor, "users", user);
    IDataUtil.put(cursor, "pass", "manage");
    cursor.destroy();
    log("Adding user " + user + createStringFromArray(" to groups ", new String[] { group }), Project.MSG_DEBUG);
    invoke("wm.server.access", "addUsers", input);
    updateUsers(user, group);
  }

  private void updateUsers(String user, String group)
  {
    IData out = invoke("wm.server.access", "userList", IDataFactory.create(), false);
    IDataCursor cursor = out.getCursor();
    IData[] users = IDataUtil.getIDataArray(cursor, "users");
    cursor.destroy();
    if (users == null || users.length == 0)
    {
      throw new BuildException("Could not retrieve current user list from server");
    }

    ArrayList userList = new ArrayList();
    for (int i = 0; i < users.length; i++)
    {
      cursor = users[i].getCursor();
      String name = IDataUtil.getString(cursor, "name");
      if (user.equals(name))
      {
        String[] existingGroups = IDataUtil.getStringArray(cursor, "membership");
        mergeArrayWithList(userList, existingGroups);
      }
      cursor.destroy();
    }
    mergeArrayWithList(userList, new String[] { group });
    String[] mergedUserArray = (String[]) userList.toArray(new String[0]);

    if (mergedUserArray != null)
    {
      IData input = IDataFactory.create();
      cursor = input.getCursor();
      IDataUtil.put(cursor, "username", user);
      IDataUtil.put(cursor, "password", "**"); // Password that begins with "*" is not changed.
      IDataUtil.put(cursor, "groupList", mergedUserArray);
      cursor.destroy();
      invoke("wm.server.access", "userChange", input);
    }
  }

  private String createStringFromArray(String prefix, String[] items)
  {
    if (items == null || items.length == 0)
    {
      return "";
    }

    StringBuffer buffer = new StringBuffer(prefix);
    for (int i = 0; i < items.length; i++)
    {
      buffer.append(items[i]);
      if (i < items.length - 1)
      {
        buffer.append(",");
      }
    }
    return buffer.toString();
  }

  private void mergeArrayWithList(ArrayList list, String[] array)
  {
    if (array != null)
    {
      for (int i = 0; i < array.length; i++)
      {
        if (!list.contains(array[i]))
        {
          list.add(array[i]);
        }
      }
    }
  }

  private void updateAcls(String acl, String[] allowGroups, String[] denyGroups)
  {
    IData out = invoke("wm.server.access", "aclList", IDataFactory.create(), false);
    IDataCursor cursor = out.getCursor();
    IData[] aclGroups = IDataUtil.getIDataArray(cursor, "aclgroups");
    cursor.destroy();
    if (aclGroups == null || aclGroups.length == 0)
    {
      throw new BuildException("Could not retrieve current ACL list from server");
    }

    ArrayList allowList = new ArrayList();
    ArrayList denyList = new ArrayList();
    for (int i = 0; i < aclGroups.length; i++)
    {
      cursor = aclGroups[i].getCursor();
      String name = IDataUtil.getString(cursor, "name");
      if (acl.equals(name))
      {
        String[] existingAllowGroups = IDataUtil.getStringArray(cursor, "allow");
        String[] existingDenyGroups = IDataUtil.getStringArray(cursor, "deny");
        mergeArrayWithList(allowList, existingAllowGroups);
        mergeArrayWithList(denyList, existingDenyGroups);
      }
      cursor.destroy();
    }
    mergeArrayWithList(allowList, allowGroups);
    mergeArrayWithList(denyList, denyGroups);
    String[] mergedAllowArray = (String[]) allowList.toArray(new String[0]);
    String[] mergedDenyArray = (String[]) denyList.toArray(new String[0]);

    IData input = IDataFactory.create();
    cursor = input.getCursor();
    IDataUtil.put(cursor, "aclname", acl);
    if (mergedAllowArray != null)
    {
      IDataUtil.put(cursor, "allowList", mergedAllowArray);
    }
    if (mergedDenyArray != null)
    {
      IDataUtil.put(cursor, "denyList", mergedDenyArray);
    }
    invoke("wm.server.access", "aclChange", input);
  }

  private void addAcl(String acl, String[] allowGroups, String[] denyGroups) throws ServiceException
  {
    if (allowGroups != null)
    {
      for (int i = 0; i < allowGroups.length; i++)
      {
        addGroup(allowGroups[i]);
      }
    }

    if (denyGroups != null)
    {
      for (int i = 0; i < denyGroups.length; i++)
      {
        addGroup(denyGroups[i]);
      }
    }

    IData input = IDataFactory.create();
    IDataCursor cursor = input.getCursor();
    IDataUtil.put(cursor, "aclname", acl);
    if (allowGroups != null)
    {
      IDataUtil.put(cursor, "allowList", allowGroups);
    }
    if (denyGroups != null)
    {
      IDataUtil.put(cursor, "denyList", denyGroups);
    }
    cursor.destroy();
    log("Adding ACL " + acl + createStringFromArray(" with allow groups ", allowGroups) + createStringFromArray(" and deny groups ", denyGroups), Project.MSG_DEBUG);
    invoke("wm.server.access", "aclAdd", input);
    updateAcls(acl, allowGroups, denyGroups);
  }

  private void setAcl(String service, String acl, String type) throws ServiceException
  {
    IData input = IDataFactory.create();
    IDataCursor cursor = input.getCursor();
    IDataUtil.put(cursor, "target", service);
    if (type.equals(executeAclType))
    {
      IDataUtil.put(cursor, "acl", acl);
    }
    else if (type.equals(listAclType))
    {
      IDataUtil.put(cursor, "browseaclgroup", acl);
    }
    else if (type.equals(readAclType))
    {
      IDataUtil.put(cursor, "readaclgroup", acl);
    }
    else
    {
      IDataUtil.put(cursor, "writeaclgroup", acl);
    }
    cursor.destroy();
    log("Assigning ACL [" + type + "] " + acl + " to service " + service, Project.MSG_DEBUG);
    invoke("wm.server.access", "aclAssign", input);
  }

  private String[] getKeysProps(String propertyPrefix)
  {
    ArrayList keys = new ArrayList();
    Set keySet = configProps.keySet();
    Iterator iter = keySet.iterator();
    while (iter.hasNext())
    {
      String strKey = (String) iter.next();
      if (strKey.startsWith(propertyPrefix))
      {
        keys.add(strKey.substring(propertyPrefix.length() + 1));
      }
    }
    if (keys.size() > 0)
    {
      return (String[]) keys.toArray(new String[0]);
    }
    return null;
  }

  public void execute() throws BuildException
  {
    prepare();

    try
    {
      String[] userGroups = getKeysProps(groupUsersPropertyPrefix);
      if (userGroups == null)
      {
        log("No groups to create.", Project.MSG_DEBUG);
      }
      else
      {
        for (int i = 0; i < userGroups.length; i++)
        {
          addGroup(userGroups[i]);
          String[] users = parseList(configProps.getProperty(groupUsersPropertyPrefix + "." + userGroups[i]));
          if (users == null)
          {
            log("No users to add for group " + userGroups[i], Project.MSG_DEBUG);
          }
          else
          {
            for (int j = 0; j < users.length; j++)
            {
              addUser(users[j], userGroups[i]);
            }
          }
        }
      }

      createAndAssignAcls(executeAclType);
      createAndAssignAcls(listAclType);
      createAndAssignAcls(readAclType);
      createAndAssignAcls(writeAclType);
    }
    catch (Exception e)
    {
      throw new BuildException(e);
    }
  }

  private void createAndAssignAcls(String type) throws ServiceException
  {
    String propertyName = aclServicesPropertyPrefix;
    if (type.equals(listAclType))
    {
      propertyName = listAclServicesPropertyPrefix;
    }
    else if (type.equals(readAclType))
    {
      propertyName = readAclServicesPropertyPrefix;
    }
    else if (type.equals(writeAclType))
    {
      propertyName = writeAclServicesPropertyPrefix;
    }

    String[] acls = getKeysProps(propertyName);
    if (acls == null)
    {
      log("No acls [" + type + "] to create.", Project.MSG_DEBUG);
    }
    else
    {
      for (int i = 0; i < acls.length; i++)
      {
        String[] allowGroups = parseList(configProps.getProperty(aclAllowGroupsPropertyPrefix + "." + acls[i]));
        String[] denyGroups = parseList(configProps.getProperty(aclsDenyGroupsPropertyPrefix + "." + acls[i]));
        addAcl(acls[i], allowGroups, denyGroups);
        assignAcl(propertyName, acls[i], type);
      }
    }
  }

  private void assignAcl(String propertyName, String acl, String type) throws ServiceException
  {
    String[] services = parseList(configProps.getProperty(propertyName + "." + acl));
    if (services == null)
    {
      log("No services to assign to acl [" + type + "] " + acl, Project.MSG_DEBUG);
    }
    else
    {
      for (int i = 0; i < services.length; i++)
      {
        setAcl(services[i], acl, type);
      }
    }
  }

  public static void main(String[] args) throws Exception
  {
    WmAssignAcls task = new WmAssignAcls();
    task.setConfigAlias("is-connection-default.properties");
    task.setServer("localhost");
    task.setProject(new Project());
    task.execute();
  }
}