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
package com.example.curity.descriptor;

import com.example.curity.authentication.BehaviosecAuthenticatorAuthenticatorRequestHandler;
import com.example.curity.config.BehaviosecAuthenticatorAuthenticatorPluginConfig;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.plugin.descriptor.AuthenticatorPluginDescriptor;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableMap;

public final class BehaviosecAuthenticatorAuthenticatorPluginDescriptor implements AuthenticatorPluginDescriptor<BehaviosecAuthenticatorAuthenticatorPluginConfig>
{
    @Override
    public String getPluginImplementationType()
    {
        return "behaviosec";
    }

    @Override
    public Class<? extends BehaviosecAuthenticatorAuthenticatorPluginConfig> getConfigurationType()
    {
        return BehaviosecAuthenticatorAuthenticatorPluginConfig.class;
    }

    @Override
    public Map<String, Class<? extends AuthenticatorRequestHandler<?>>> getAuthenticationRequestHandlerTypes()
    {
        return unmodifiableMap(singletonMap("index", BehaviosecAuthenticatorAuthenticatorRequestHandler.class));
    }
}
