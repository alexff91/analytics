package com.nz.simplecrud.service;

import com.nz.simplecrud.entity.UserTable;

import javax.ejb.Stateless;

/**
 * @author Emre Simtay <emre@simtay.com>
 */

@Stateless
public class UserService extends DataAccessService<UserTable> {

  public UserService() {
    super(UserTable.class);
  }
}
