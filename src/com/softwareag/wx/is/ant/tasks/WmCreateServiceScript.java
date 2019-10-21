package com.softwareag.wx.is.ant.tasks;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.softwareag.wx.is.ant.tasks.WmServiceTask;
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSRecord;
import com.wm.lang.ns.NSService;

public class WmCreateServiceScript extends WmServiceTask
{
  private String serviceName;
  private String propertyName;
  private String targetName;
  private boolean skipOptionalInputs;

  public void execute() throws BuildException
  {
    prepare();
    try
    {
      NSService serviceNode = (NSService) getContext().getQuery().getNode(NSName.create(serviceName));
      NSRecord record = serviceNode.getSignature().getInput();
      String script = getRecordAsScript(record);
      script = script.replaceAll("<\\?xml version.+?>", "");
      getProject().setProperty(propertyName, script);
      System.out.println(script);
    }
    catch (Exception e)
    {
      throw new BuildException(e);
    }
  }

  private String getRecordAsScript(NSRecord record)
  {
    try
    {
      String[] serviceParts = serviceName.split(":");
      StringWriter writer = new StringWriter();
      ContentHandler serializationHandler = createSerializationHandler(writer);
      serializationHandler.startDocument();
      boolean targetIncluded = false;
      if (targetName != null && targetName.length() > 0)
      {
        targetIncluded = true;
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(null, null, "name", "CDATA", targetName);
        serializationHandler.startElement(null, null, "target", attributes);
      }
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute(null, null, "folder", "CDATA", serviceParts[0]);
      attributes.addAttribute(null, null, "service", "CDATA", serviceParts[1]);
      serializationHandler.startElement(null, null, "wm:service", attributes);
      serializeRecord(record, serializationHandler, writer);
      serializationHandler.endElement(null, null, "wm:service");
      if (targetIncluded)
      {
        serializationHandler.endElement(null, null, "target");
      }
      serializationHandler.endDocument();
      return writer.toString();
    }
    catch (Exception e)
    {
      throw new BuildException(e);
    }
  }

  private void serializeRecord(NSRecord record, ContentHandler serializationHandler, Writer writer) throws SAXException, IOException
  {
    if (record != null)
    {
      serializationHandler.startElement(null, null, "wm:idata", null);
      NSField[] fields = record.getFields();
      for (int i = 0; i < fields.length; i++)
      {
        if (skipOptionalInputs && fields[i].isOptional())
        {
          continue;
        }
        
        String name = fields[i].getName();
        int dimensions = fields[i].getDimensions();
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(null, null, "name", "CDATA", name);
        if (!(fields[i] instanceof NSRecord))
        {
          attributes.addAttribute(null, null, "value", "CDATA", "${" + name + "}");
        }

        if (dimensions != 0)
        {
          attributes.addAttribute(null, null, "array", "CDATA", "true");
        }

        serializationHandler.startElement(null, null, "wm:variable", attributes);
        if (fields[i] instanceof NSRecord)
        {
          serializeRecord((NSRecord) fields[i], serializationHandler, writer);
        }
        serializationHandler.endElement(null, null, "wm:variable");
      }
      serializationHandler.endElement(null, null, "wm:idata");
    }
  }

  private ContentHandler createSerializationHandler(Writer writer) throws IOException
  {
    Properties props = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
    props.put(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
    props.put("indent", "yes");
    Serializer serializer = SerializerFactory.getSerializer(props);
    serializer.setWriter(writer);
    return serializer.asContentHandler();
  }

  protected void prepare() throws BuildException
  {
    checkRequiredStringAttribute(serverAttributeName, getServer());
    checkRequiredNumericalAttribute(portAttributeName, getPort());
    checkRequiredStringAttribute(userAttributeName, getUser());
    checkRequiredStringAttribute(passwordAttributeName, getPassword());
    checkRequiredAttribute("serviceName", serviceName);
    checkRequiredAttribute("propertyName", propertyName);
  }

  public void setServiceName(String serviceName)
  {
    this.serviceName = serviceName;
  }

  public void setPropertyName(String propertyName)
  {
    this.propertyName = propertyName;
  }
  
  public void setTargetName(String targetName)
  {
    this.targetName = targetName;
  }

  public void setSkipOptionalInputs(boolean skipOptionalInputs)
  {
    this.skipOptionalInputs = skipOptionalInputs;
  }

  public static void main(String[] args)
  {
    WmCreateServiceScript s = new WmCreateServiceScript();
    s.setTargetName("create-trigger");
    s.setServiceName("pub.trigger:createTrigger");
    s.setPropertyName("test.prop");
    s.setSkipOptionalInputs(true);
    Project p = new Project();
    s.setProject(p);
    s.execute();
  }
}
