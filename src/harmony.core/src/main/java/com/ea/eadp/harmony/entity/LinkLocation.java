/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by juding on 11/12/14.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class LinkLocation {
}
