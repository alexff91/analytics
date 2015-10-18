package com.nz.simplecrud.util;

import com.nz.simplecrud.entity.File;
import com.nz.simplecrud.entity.Role;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.HashMap;

/**
 * Object converting utility is to convert between String and the desired Object type. JSF cannot
 * convert the given String value to the Object type, to be able to bind the selected Object to the
 * list we will have to override getAsObject and getAsString functions. Usage: <f:converter
 * converterId="com.nz.util.ObjectConverter" /> TODO: Make it generic
 * @author Emre Simtay <emre@simtay.com>
 */
@FacesConverter("com.nz.util.FileConverter")
public class FileConverter implements Converter {

  private static HashMap<String, File> map = new HashMap<String, File>();

  @Override
  public Object getAsObject(FacesContext context, UIComponent component, String value) {
    File file = map.get(value);
    return file;
  }

  @Override
  public String getAsString(FacesContext context, UIComponent component, Object value) {
    File file = (File) value;
    map.put(file.getId().toString(), file);
    return file.getId().toString();
  }
}
