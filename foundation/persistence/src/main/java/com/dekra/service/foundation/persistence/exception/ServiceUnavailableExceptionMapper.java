package com.dekra.service.foundation.persistence.exception;



import com.dekra.service.foundation.domaincore.exception.ServiceUnavailableException;
import com.mongodb.MongoException;
import org.springframework.dao.DataAccessException;

public final class ServiceUnavailableExceptionMapper {

    private ServiceUnavailableExceptionMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static Throwable mapDatabaseUnavailable(Throwable e) {
        if (isDatabaseException(e)) {
            return new ServiceUnavailableException("error.database.unavailable", e);
        }
        return e;
    }

    private static boolean isDatabaseException(Throwable e) {
        return e instanceof MongoException
                || e instanceof DataAccessException;
    }
}