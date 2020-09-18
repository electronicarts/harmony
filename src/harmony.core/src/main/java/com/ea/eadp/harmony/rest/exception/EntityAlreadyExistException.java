/* Copyright (C) 2020 Electronic Arts Inc.  All rights reserved. */
package com.ea.eadp.harmony.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * User: leilin
 * Date: 10/7/14
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Duplicate key")
public class EntityAlreadyExistException extends RuntimeException {
    public EntityAlreadyExistException(String entity, String key) {
        super(String.format("Already exists entity %s with key %s", entity, key));
    }
}
