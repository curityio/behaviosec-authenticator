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
package com.example.curity.config;

import se.curity.identityserver.sdk.config.Configuration;
import se.curity.identityserver.sdk.config.annotation.DefaultString;
import se.curity.identityserver.sdk.config.annotation.Description;
import se.curity.identityserver.sdk.service.CredentialManager;
import se.curity.identityserver.sdk.service.UserPreferenceManager;

@SuppressWarnings("InterfaceNeverImplemented")
public interface BehaviosecAuthenticatorAuthenticatorPluginConfig extends Configuration
{
    @DefaultString("curity-demo-sid-")
    @Description("A string to prefix the BehavioSec Cloud sessions")
    String getSessionIdPrefix();

    @Description("The BehavioSec Cloud API Key")
    String getApiKey();

    @Description("The BehavioSec Cloud API Secret")
    String getSecret();

    CredentialManager getCredentialManager();

    UserPreferenceManager getUserPreferenceManager();
}
