package com.nz.simplecrud.entity;

import com.nz.simplecrud.entity.UserTable;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2017-01-31T00:34:20")
@StaticMetamodel(File.class)
public class File_ extends BaseEntity_ {

    public static volatile SingularAttribute<File, String> filename;
    public static volatile ListAttribute<File, UserTable> userTables;

}