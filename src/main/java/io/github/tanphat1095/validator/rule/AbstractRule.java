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

package io.github.tanphat1095.validator.rule;

import io.github.tanphat1095.validator.ValidationError;
import io.github.tanphat1095.validator.context.ValidationContext;
import io.github.tanphat1095.validator.message.DefaultMessageProvider;
import io.github.tanphat1095.validator.message.MessageProvider;

import java.util.Optional;

/**
 * @author Le Tan Phat
 * @since 1.0.0
 */
public abstract class AbstractRule<T, R extends AbstractRule<T, R>> implements RuleValidator<T> {
    protected String customMessage;
    protected String errorCode;
    protected boolean failFast;

    @SuppressWarnings("unchecked")
    public R withMessage(String message) {
        this.customMessage = message;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R withErrorCode(String code) {
        this.errorCode = code;
        return (R) this;
    }

    /**
     * Marks this rule as fail-fast: the remaining rules of the same field are skipped once it fails.
     *
     * @return this rule
     * @since 1.0.2
     */
    @SuppressWarnings("unchecked")
    public R withFailFast() {
        this.failFast = true;
        return (R) this;
    }

    @Override
    public boolean failFast() {
        return failFast;
    }

    protected ValidationError fail(ValidationContext<?,?> context, String fieldName, String defaultTemplate, Object... args) {
        MessageProvider messageProvider = new DefaultMessageProvider();
        if(context != null)
            messageProvider = Optional.ofNullable(context.getMessageProvider()).orElse(messageProvider);
        String template = (customMessage != null) ? customMessage : defaultTemplate;
        return ValidationError.fail(fieldName, messageProvider.getMessage(template, args), errorCode);
    }
}
