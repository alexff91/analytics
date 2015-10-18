package com.nz.simplecrud.service;

import com.nz.simplecrud.entity.File;
import com.nz.simplecrud.entity.Role;
import javax.ejb.Stateless;

/**
 * @author Emre Simtay <emre@simtay.com>
 */

@Stateless
public class FileService extends DataAccessService<File> {

  public FileService() {
    super(File.class);
  }
}
