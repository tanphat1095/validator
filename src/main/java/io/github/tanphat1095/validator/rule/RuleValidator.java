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

import io.github.tanphat1095.validator.context.ValidationContext;
import io.github.tanphat1095.validator.ValidationError;

/**
 * @author Le Tan Phat
 * @since 1.0.0
 */
@FunctionalInterface
public interface RuleValidator<T> {
    ValidationError validate(T value, ValidationContext<?,?> context, String targetName);

    /**
     * If true, the remaining rules of the same field are skipped once this rule fails.
     *
     * @return whether this rule is fail-fast
     * @since 1.0.2
     */
    default boolean failFast() {
        return false;
    }

    /**
     * Wraps any rule, including a lambda, so that it is fail-fast.
     *
     * @param <T> the value type
     * @param rule the rule to wrap
     * @return a fail-fast rule delegating to the given one
     * @since 1.0.2
     */
    static <T> RuleValidator<T> failFast(RuleValidator<T> rule) {
        return new RuleValidator<>() {
            @Override
            public ValidationError validate(T value, ValidationContext<?,?> context, String targetName) {
                return rule.validate(value, context, targetName);
            }

            @Override
            public boolean failFast() {
                return true;
            }
        };
    }
}
