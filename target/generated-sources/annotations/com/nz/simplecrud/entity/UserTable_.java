package com.nz.simplecrud.entity;

import com.nz.simplecrud.entity.File;
import com.nz.simplecrud.entity.Role;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-10-18T20:12:29")
@StaticMetamodel(UserTable.class)
public class UserTable_ { 

    public static volatile SingularAttribute<UserTable, String> firstname;
    public static volatile SingularAttribute<UserTable, String> password;
    public static volatile ListAttribute<UserTable, Role> roles;
    public static volatile ListAttribute<UserTable, File> files;
    public static volatile SingularAttribute<UserTable, String> email;
    public static volatile SingularAttribute<UserTable, String> username;
    public static volatile SingularAttribute<UserTable, String> lastname;

}