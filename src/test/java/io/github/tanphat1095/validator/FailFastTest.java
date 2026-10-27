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

package io.github.tanphat1095.validator;

import io.github.tanphat1095.validator.common.rule.CommonRules;
import io.github.tanphat1095.validator.handler.MapValidationResultHandler;
import io.github.tanphat1095.validator.rule.DoubleRuleValidator;
import io.github.tanphat1095.validator.rule.IntRuleValidator;
import io.github.tanphat1095.validator.rule.LongRuleValidator;
import io.github.tanphat1095.validator.rule.RuleValidator;
import io.github.tanphat1095.validator.type.DoubleValidator;
import io.github.tanphat1095.validator.type.IntValidator;
import io.github.tanphat1095.validator.type.LongValidator;
import io.github.tanphat1095.validator.type.ObjectValidator;
import io.github.tanphat1095.validator.type.Validator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Le Tan Phat
 * @since 1.0.2
 */
class FailFastTest {

    private static RuleValidator<String> counting(AtomicInteger calls) {
        return (value, ctx, field) -> {
            calls.incrementAndGet();
            return ValidationError.fail(field, "always fails");
        };
    }

    @SuppressWarnings("unchecked")
    private static List<ValidationError> errorsOf(Map<String, Object> row) {
        return (List<ValidationError>) row.get(MapValidationResultHandler.MAP_RESULT_ERROR);
    }

    @Test
    void defaultShouldStillCollectAllErrors() {
        AtomicInteger calls = new AtomicInteger();
        Validator<Map<String, Object>> name = new ObjectValidator<>("name",
                List.of(CommonRules.maxStringLength(3), counting(calls)),
                m -> (String) m.get("name"));

        Map<String, Object> row = new HashMap<>(Map.of("name", "abcdef"));
        new DataValidator<>(List.of(name), null).validate(row);

        assertThat(errorsOf(row)).hasSize(2);
        assertThat(calls).hasValue(1);
    }

    @Test
    void failFastRuleShouldSkipRemainingRulesOfSameField() {
        AtomicInteger calls = new AtomicInteger();
        Validator<Map<String, Object>> name = new ObjectValidator<>("name",
                List.of(CommonRules.maxStringLength(3).withFailFast(), counting(calls)),
                m -> (String) m.get("name"));
        Validator<Map<String, Object>> code = new ObjectValidator<>("code",
                List.of(CommonRules.required()),
                m -> (String) m.get("code"));

        Map<String, Object> row = new HashMap<>(Map.of("name", "abcdef"));
        new DataValidator<>(List.of(name, code), null).validate(row);

        assertThat(calls).hasValue(0);
        assertThat(errorsOf(row)).extracting(ValidationError::targetName).containsExactly("name", "code");
    }

    @Test
    void failFastRuleShouldNotStopWhenItPasses() {
        AtomicInteger calls = new AtomicInteger();
        Validator<Map<String, Object>> name = new ObjectValidator<>("name",
                List.of(CommonRules.maxStringLength(10).withFailFast(), counting(calls)),
                m -> (String) m.get("name"));

        Map<String, Object> row = new HashMap<>(Map.of("name", "abcdef"));
        new DataValidator<>(List.of(name), null).validate(row);

        assertThat(calls).hasValue(1);
        assertThat(errorsOf(row)).hasSize(1);
    }

    @Test
    void failFastFieldShouldSkipRemainingFieldsOfRecord() {
        AtomicInteger calls = new AtomicInteger();
        Validator<Map<String, Object>> name = Validator.failFast(new ObjectValidator<>("name",
                List.of(CommonRules.maxStringLength(3)),
                m -> (String) m.get("name")));
        Validator<Map<String, Object>> other = new ObjectValidator<>("other",
                List.of(counting(calls)),
                m -> (String) m.get("other"));

        Map<String, Object> row = new HashMap<>(Map.of("name", "abcdef"));
        new DataValidator<>(List.of(name, other), null).validate(row);

        assertThat(calls).hasValue(0);
        assertThat(errorsOf(row)).extracting(ValidationError::targetName).containsExactly("name");
    }

    @Test
    void lambdaRulesCanBeMarkedFailFast() {
        AtomicInteger calls = new AtomicInteger();

        ObjectValidator<String, String> text = new ObjectValidator<>("text", List.of(
                RuleValidator.failFast((v, ctx, f) -> ValidationError.fail(f, "bad")),
                counting(calls)),
                s -> s);
        IntValidator<int[]> i = new IntValidator<>("i", List.of(
                IntRuleValidator.failFast((v, ctx, f) -> v >= 0 ? null : ValidationError.fail(f, "negative")),
                (v, ctx, f) -> { calls.incrementAndGet(); return null; }),
                a -> a[0]);
        LongValidator<long[]> l = new LongValidator<>("l", List.of(
                LongRuleValidator.failFast((v, ctx, f) -> v >= 0 ? null : ValidationError.fail(f, "negative")),
                (v, ctx, f) -> { calls.incrementAndGet(); return null; }),
                a -> a[0]);
        DoubleValidator<double[]> d = new DoubleValidator<>("d", List.of(
                DoubleRuleValidator.failFast((v, ctx, f) -> v >= 0 ? null : ValidationError.fail(f, "negative")),
                (v, ctx, f) -> { calls.incrementAndGet(); return null; }),
                a -> a[0]);

        assertThat(text.validate("x", null)).hasSize(1);
        assertThat(i.validate(new int[]{-1}, null)).hasSize(1);
        assertThat(l.validate(new long[]{-1}, null)).hasSize(1);
        assertThat(d.validate(new double[]{-1}, null)).hasSize(1);
        assertThat(calls).hasValue(0);
    }
}
