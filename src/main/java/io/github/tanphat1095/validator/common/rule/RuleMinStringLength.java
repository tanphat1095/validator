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

package io.github.tanphat1095.validator.common.rule;

import io.github.tanphat1095.validator.ValidationError;
import io.github.tanphat1095.validator.context.ValidationContext;
import io.github.tanphat1095.validator.rule.AbstractRule;

/**
 * @author Le Tan Phat
 * @since 1.0.0
 */
public class RuleMinStringLength extends AbstractRule<String, RuleMinStringLength> {

    private final int minLength;

    public RuleMinStringLength(int minLength) {
        this.minLength = minLength;
    }

    @Override
    public ValidationError validate(String value, ValidationContext<?, ?> context, String targetName) {
        if(value != null && value.length() < minLength)
            return fail(context ,targetName, "Field {0} should not less than {1} characters", targetName, minLength);
        return null;
    }
}
