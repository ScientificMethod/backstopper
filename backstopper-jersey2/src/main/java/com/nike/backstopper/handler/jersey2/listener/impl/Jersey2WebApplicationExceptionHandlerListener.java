package com.nike.backstopper.handler.jersey2.listener.impl;

import com.nike.backstopper.apierror.ApiError;
import com.nike.backstopper.apierror.SortedApiErrorSet;
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors;
import com.nike.backstopper.handler.ApiExceptionHandlerUtils;
import com.nike.backstopper.handler.listener.ApiExceptionHandlerListener;
import com.nike.backstopper.handler.listener.ApiExceptionHandlerListenerResult;
import com.nike.internal.util.Pair;

import org.glassfish.jersey.server.ParamException;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.nike.backstopper.apierror.SortedApiErrorSet.singletonSortedSetOf;

/**
 * Handles any known errors thrown by the Jersey framework.
 *
 * @author dsand7
 * @author Michael Irwin
 */
@Singleton
@SuppressWarnings("WeakerAccess")
public class Jersey2WebApplicationExceptionHandlerListener implements ApiExceptionHandlerListener {

    protected final ProjectApiErrors projectApiErrors;
    protected final ApiExceptionHandlerUtils utils;

    /**
     * @param projectApiErrors The {@link ProjectApiErrors} that should be used by this instance when finding {@link
     *                         ApiError}s. Cannot be null.
     * @param utils The {@link ApiExceptionHandlerUtils} that should be used by this instance.
     */
    @Inject
    public Jersey2WebApplicationExceptionHandlerListener(ProjectApiErrors projectApiErrors,
                                                         ApiExceptionHandlerUtils utils) {
        if (projectApiErrors == null)
            throw new IllegalArgumentException("ProjectApiErrors cannot be null");

        if (utils == null)
            throw new IllegalArgumentException("ApiExceptionHandlerUtils cannot be null");

        this.projectApiErrors = projectApiErrors;
        this.utils = utils;
    }

    @Override
    public ApiExceptionHandlerListenerResult shouldHandleException(Throwable ex) {

        ApiExceptionHandlerListenerResult result;
        SortedApiErrorSet handledErrors = null;
        List<Pair<String, String>> extraDetailsForLogging = new ArrayList<>();

        if (ex instanceof ParamException.UriParamException) {
            utils.addBaseExceptionMessageToExtraDetailsForLogging(ex, extraDetailsForLogging);
            // Returning a 404 is intentional here.
            //      The Jersey contract for URIParamException states it should map to a 404.
            handledErrors = singletonSortedSetOf(projectApiErrors.getNotFoundApiError());
        }
        else if (ex instanceof ParamException) {
            utils.addBaseExceptionMessageToExtraDetailsForLogging(ex, extraDetailsForLogging);
            handledErrors = singletonSortedSetOf(projectApiErrors.getMalformedRequestApiError());
        }

        // Return an indication that we will handle this exception if handledErrors got set
        if (handledErrors != null) {
            result = ApiExceptionHandlerListenerResult.handleResponse(handledErrors, extraDetailsForLogging);
        }
        else {
            result = ApiExceptionHandlerListenerResult.ignoreResponse();
        }

        return result;
    }
}
