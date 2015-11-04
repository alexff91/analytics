package com.nz.simplecrud.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-09-06T18:18:21")
@StaticMetamodel(UserTable.class)
public class User_ extends BaseEntity_ {

    public static volatile SingularAttribute<UserTable, String> firstname;
    public static volatile SingularAttribute<UserTable, String> password;
    public static volatile SingularAttribute<UserTable, Address> address;
    public static volatile ListAttribute<UserTable, Role> roles;
    public static volatile SingularAttribute<UserTable, String> email;
    public static volatile SingularAttribute<UserTable, String> username;
    public static volatile SingularAttribute<UserTable, String> lastname;

}