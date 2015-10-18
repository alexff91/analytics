package com.nz.simplecrud.service;

import com.nz.simplecrud.entity.Role;
import com.nz.simplecrud.entity.UserTable;
import javax.ejb.Stateless;

/**
 * @author Emre Simtay <emre@simtay.com>
 */

@Stateless
public class RoleService extends DataAccessService<Role> {

  public RoleService() {
    super(Role.class);
  }
}
