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

package io.github.tanphat1095.validator.type;

import io.github.tanphat1095.validator.rule.IntRuleValidator;
import io.github.tanphat1095.validator.context.ValidationContext;
import io.github.tanphat1095.validator.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * @author Le Tan Phat
 * @since 1.0.0
 */
public record IntValidator<E>(String targetName, List<IntRuleValidator> rules, ToIntFunction<E> getter) implements Validator<E> {

    public List<ValidationError> validate(E data, ValidationContext<?, ?> context) {
        if (rules == null || rules.isEmpty())
            return Collections.emptyList();
        int value = getter.applyAsInt(data);

        List<ValidationError> results = null;
        for (IntRuleValidator rule : rules) {
            ValidationError res = rule.validate(value, context, targetName);
            if (res != null) {
                if (results == null) {
                    results = new ArrayList<>();
                }
                results.add(res);
                if (rule.failFast()) break;
            }
        }
        return results;
    }
}
