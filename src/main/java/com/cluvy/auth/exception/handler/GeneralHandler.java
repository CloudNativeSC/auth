package com.cluvy.auth.exception.handler;

import com.cluvy.auth.exception.GeneralException;
import com.cluvy.auth.response.BaseErrorCode;

public class GeneralHandler extends GeneralException {
    public GeneralHandler(BaseErrorCode code) {
        super(code);
    }
}
