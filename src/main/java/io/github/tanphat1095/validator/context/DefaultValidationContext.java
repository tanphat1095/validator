/*
 * Copyright 2026 Le Tan Phat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.tanphat1095.validator.context;

import io.github.tanphat1095.validator.message.MessageProvider;

import java.util.HashMap;

/**
 * Default {@link ValidationContext} backed by a {@link HashMap}. It is not thread-safe:
 * when rules write to the context while validating a parallel stream,
 * use {@link ConcurrentValidationContext} instead.
 *
 * @author Le Tan Phat
 * @since 1.0.0
 */
public class DefaultValidationContext extends HashMap<String, Object> implements ValidationContext<String,Object>{

    private static final String MESSAGE_PROVIDER = "MESSAGE_PROVIDER";

    @Override
    public void setMessageProvider(MessageProvider provider) {
        put(MESSAGE_PROVIDER, provider);
    }

    @Override
    public MessageProvider getMessageProvider() {
        return (MessageProvider) get(MESSAGE_PROVIDER);
    }
}
