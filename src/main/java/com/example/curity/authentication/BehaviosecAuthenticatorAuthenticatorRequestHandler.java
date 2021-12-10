/*
 *  Copyright 2021 Curity AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.curity.authentication;

import com.behaviosec.cloud.BehavioCloudApi;
import com.behaviosec.cloud.BehavioCloudProperties;
import com.behaviosec.cloud.ScoreResult;
import com.behaviosec.cloud.SessionDataResponse;
import com.behaviosec.cloud.exceptions.BehavioCloudException;
import com.example.curity.config.BehaviosecAuthenticatorAuthenticatorPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.Nullable;
import se.curity.identityserver.sdk.attribute.Attribute;
import se.curity.identityserver.sdk.attribute.AttributeName;
import se.curity.identityserver.sdk.attribute.AttributeValue;
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.errors.ErrorCode;
import se.curity.identityserver.sdk.http.HttpStatus;
import se.curity.identityserver.sdk.service.CredentialManager;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.UserPreferenceManager;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;
import se.curity.identityserver.sdk.web.alerts.ErrorMessage;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static se.curity.identityserver.sdk.web.Response.ResponseModelScope.NOT_FAILURE;
import static se.curity.identityserver.sdk.web.ResponseModel.templateResponseModel;

public final class BehaviosecAuthenticatorAuthenticatorRequestHandler implements AuthenticatorRequestHandler<RequestModel>
{
    private static final Logger _logger = LoggerFactory.getLogger(BehaviosecAuthenticatorAuthenticatorRequestHandler.class);

    private final BehaviosecAuthenticatorAuthenticatorPluginConfig _config;
    private final ExceptionFactory _exceptionFactory;
    private final UserPreferenceManager _userPreferenceManager;
    private final CredentialManager _credentialManager;
    public final String SESSION_ID_PREFIX;

    /**
     * Tiny container for template variable keys.
     */
    private static class ViewDataKeys
    {
        static final String USERNAME = "_username";
    }

    public BehaviosecAuthenticatorAuthenticatorRequestHandler(BehaviosecAuthenticatorAuthenticatorPluginConfig config,
                                                              ExceptionFactory exceptionFactory)
    {
        _config = config;
        _exceptionFactory = exceptionFactory;
        _userPreferenceManager = config.getUserPreferenceManager();
        _credentialManager = config.getCredentialManager();
        SESSION_ID_PREFIX = config.getSessionIdPrefix();
    }

    @Override
    public Optional<AuthenticationResult> get(RequestModel requestModel, Response response)
    {
        // authentication is never performed using GET, so return an empty optional to let the server know
        // authentication has not been performed.
        return Optional.empty();
    }

    @Override
    public Optional<AuthenticationResult> post(RequestModel requestModel, Response response)
    {
        RequestModel.Post model = requestModel.getPostRequestModel();

        Optional<AuthenticationResult> result = Optional.empty();

        @Nullable
        AuthenticationAttributes attributes = _credentialManager.verifyPassword(
                model.getUserName(),
                model.getPassword(),
                CredentialManager.NO_CONTEXT);

        SessionDataResponse sessionData = getBehavioCloudSession(model.getJourneyId(), model.getUserName());

        if (attributes != null)
        {
            Attribute scoreResult = Attribute.of(AttributeName.of("scoreResult"), AttributeValue.of(sessionData.getScoreResult()));
            Attribute risk = Attribute.of(AttributeName.of("risk"), AttributeValue.of( sessionData.getRisk().doubleValue()));
            Attribute isTrained = Attribute.of(AttributeName.of("isTrained"), AttributeValue.of( sessionData.isTrained()));

            result = Optional.of(new AuthenticationResult(attributes
                    .withContextAttribute(scoreResult)
                    .withContextAttribute(risk)
                    .withContextAttribute(isTrained)
            ));

            _userPreferenceManager.saveUsername(model.getUserName());
        }
        else
        {
            response.addErrorMessage(ErrorMessage.withMessage("validation.error.incorrect.credentials"));

            // report a bad request so that the server will come back to the login template
            response.setHttpStatus(HttpStatus.BAD_REQUEST);
        }

        return result;
    }

    @Override
    public RequestModel preProcess(Request request, Response response)
    {
        // set the template and model for responses on the NOT_FAILURE scope
        response.setResponseModel(templateResponseModel(
                singletonMap(ViewDataKeys.USERNAME, _userPreferenceManager.getUsername()),
                "authenticate/get"), NOT_FAILURE);

        // on request validation failure, we should use the same template as for NOT_FAILURE
        response.setResponseModel(templateResponseModel(emptyMap(),
                "authenticate/get"), HttpStatus.BAD_REQUEST);

        //Make the api-key available in the frontend
        response.putViewData("API_KEY", _config.getApiKey(), Response.ResponseModelScope.ANY);

        return new RequestModel(request);
    }

    private SessionDataResponse getBehavioCloudSession(String journeyId, String userName)
    {
        BehavioCloudProperties behavioCloudProperties = BehavioCloudProperties
                .builder()
                .apiKey(_config.getApiKey())
                .apiSecret(_config.getApiSecret())
                .build();

        BehavioCloudApi behavioCloudApi = new BehavioCloudApi(behavioCloudProperties);

        SessionDataResponse sessionDataResponse = null;

        try
        {
            sessionDataResponse = behavioCloudApi.registerLogin(
                    journeyId,
                    userName,
                    createSession().getSessionId());
        }
        catch(BehavioCloudException e)
        {
            _logger.debug("Unable to register login with BehavioSec Cloud {}", e.getMessage());
            throw _exceptionFactory.internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }

        return sessionDataResponse;
    }

    //This is example of session id generator for demo purposes only.
    //Not recommended for production.
    private SessionData createSession() {
        return new SessionData(SESSION_ID_PREFIX + UUID.randomUUID());
    }
}
