package com.exadel.frs.exception;

import com.exadel.frs.commonservice.exception.BasicException;

import static com.exadel.frs.commonservice.handler.CrudExceptionCode.INCORRECT_STATISTICS_ROLE;
import static java.lang.String.format;

public class IncorrectStatisticsTypeException extends BasicException {

    public static final String STATISTICS_TYPE_NOT_EXISTS_MESSAGE = "Statistics type %s does not exists";

    public IncorrectStatisticsTypeException(final String statisticsType) {
        super(INCORRECT_STATISTICS_ROLE, format(STATISTICS_TYPE_NOT_EXISTS_MESSAGE, statisticsType));
    }
}