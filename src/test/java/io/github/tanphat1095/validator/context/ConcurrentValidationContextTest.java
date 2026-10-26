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

import io.github.tanphat1095.validator.DataValidator;
import io.github.tanphat1095.validator.ValidationError;
import io.github.tanphat1095.validator.message.DefaultMessageProvider;
import io.github.tanphat1095.validator.rule.LongRuleValidator;
import io.github.tanphat1095.validator.type.LongValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Le Tan Phat
 * @since 1.0.2
 */
class ConcurrentValidationContextTest {

    @Test
    void shouldStoreAndClearMessageProvider() {
        ConcurrentValidationContext context = new ConcurrentValidationContext();
        DefaultMessageProvider provider = new DefaultMessageProvider();

        context.setMessageProvider(provider);
        assertThat(context.getMessageProvider()).isSameAs(provider);

        context.setMessageProvider(null);
        assertThat(context.getMessageProvider()).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDetectDuplicatesCorrectlyInParallelStream() {
        int records = 100_000;
        int distinctIds = 1_000;
        ConcurrentValidationContext context = new ConcurrentValidationContext();
        context.put("seen_ids", ConcurrentHashMap.newKeySet());

        LongRuleValidator uniqueIdRule = (value, ctx, fieldName) -> {
            Set<Long> seen = (Set<Long>) ctx.get("seen_ids");
            return seen.add(value) ? null : ValidationError.fail(fieldName, "Duplicate ID: " + value);
        };
        DataValidator<Map<String, Object>> validator = new DataValidator<>(
                List.of(new LongValidator<>("id", List.of(uniqueIdRule), row -> (Long) row.get("id"))),
                context);

        long duplicates = validator.validate(IntStream.range(0, records).parallel()
                        .mapToObj(i -> (Map<String, Object>) new ConcurrentHashMap<String, Object>(Map.of("id", (long) (i % distinctIds))))
                        .parallel())
                .filter(row -> !((List<ValidationError>) row.get("VALIDATION_RESULT")).isEmpty())
                .count();

        assertThat(duplicates).isEqualTo(records - distinctIds);
    }
}
