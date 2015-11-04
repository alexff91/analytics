package com.nz.simplecrud.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.6.0.v20150309-rNA", date="2015-09-06T18:18:21")
@StaticMetamodel(Address.class)
public class Address_ extends BaseEntity_ {

    public static volatile SingularAttribute<Address, String> country;
    public static volatile SingularAttribute<Address, String> city;
    public static volatile SingularAttribute<Address, String> street;
    public static volatile SingularAttribute<Address, String> suburb;

}