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

package io.github.tanphat1095.validator.message;

import org.junit.jupiter.api.Test;
import io.github.tanphat1095.validator.ValidationError;
import io.github.tanphat1095.validator.common.rule.CommonRules;
import io.github.tanphat1095.validator.common.rule.RuleRequired;
import io.github.tanphat1095.validator.context.DefaultValidationContext;
import io.github.tanphat1095.validator.context.ValidationContext;

import java.text.MessageFormat;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Le Tan Phat
 * @since 1.0.0
 */
class MessageProviderTest {

    @Test
    void defaultMessageProviderShouldWork() {
        RuleRequired<String> rule = CommonRules.required();
        ValidationContext<String, Object> context = new DefaultValidationContext();
        
        ValidationError error = rule.validate(null, context, "username");
        
        assertThat(error).isNotNull();
        assertThat(error.message()).isEqualTo("Field username is required.");
    }

    @Test
    void customMessageViaFluentApiShouldHaveHighestPriority() {
        RuleRequired<String> rule = CommonRules.<String>required().withMessage("Custom hardcoded message for username");

        ValidationContext<String, Object> context = new DefaultValidationContext();
        context.setMessageProvider(new DefaultMessageProvider());

        ValidationError error = rule.validate(null, context, "username");

        assertThat(error).isNotNull();
        assertThat(error.message()).isEqualTo("Custom hardcoded message for username");
    }

    @Test
    void defaultMessageProviderShouldReturnDefaultMessageWhenNoCustomMessageSet() {
        RuleRequired<String> rule = CommonRules.required();
        ValidationContext<String, Object> context = new DefaultValidationContext();
        context.setMessageProvider(new DefaultMessageProvider());

        ValidationError error = rule.validate(null, context, "email");

        assertThat(error).isNotNull();
        assertThat(error.message()).isEqualTo("Field email is required.");
    }

    @Test
    void customMessageProviderShouldWork() {
        RuleRequired<String> rule = CommonRules.<String>required().withErrorCode("ERR_REQUIRED");
        ValidationContext<String, Object> context = new DefaultValidationContext();
        context.setMessageProvider((template, args) -> MessageFormat.format("Test {0}", args));
        ValidationError error = rule.validate(null, context, "username");
        assertThat(error).isNotNull();
        assertThat(error.message()).isEqualTo("Test username");
        assertThat(error.errorCode()).isEqualTo("ERR_REQUIRED");
    }

    @Test
    void defaultMessageProviderShouldHandleNullTemplate() {
        DefaultMessageProvider provider = new DefaultMessageProvider();
        assertThat(provider.getMessage(null, "some-arg")).isNull();
    }

    @Test
    void defaultMessageProviderShouldMatchMessageFormatOnRepeatedCalls() {
        DefaultMessageProvider provider = new DefaultMessageProvider();
        Object[][] cases = {
                {"Field {0} should not more than {1} characters", new Object[]{"name", 1000}},
                {"It''s {0} at {1,number,#.##}", new Object[]{"price", 3.14159}},
                {"No args here", new Object[]{}},
                {"Missing {0} and {1}", new Object[]{"one"}},
                {"Null args {0}", null},
        };
        for (int i = 0; i < 3; i++) {
            for (Object[] c : cases) {
                String template = (String) c[0];
                Object[] args = (Object[]) c[1];
                assertThat(provider.getMessage(template, args)).isEqualTo(MessageFormat.format(template, args));
            }
        }
    }

    @Test
    void defaultMessageProviderShouldFollowDefaultLocaleChanges() {
        Locale original = Locale.getDefault(Locale.Category.FORMAT);
        DefaultMessageProvider provider = new DefaultMessageProvider();
        String template = "Value {0}";
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.US);
            assertThat(provider.getMessage(template, 1234.5)).isEqualTo("Value 1,234.5");
            Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY);
            assertThat(provider.getMessage(template, 1234.5)).isEqualTo("Value 1.234,5");
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, original);
        }
    }

    @Test
    void defaultMessageProviderShouldStillRejectInvalidTemplate() {
        DefaultMessageProvider provider = new DefaultMessageProvider();
        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> provider.getMessage("Broken {0", "x"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void defaultMessageProviderShouldBeThreadSafe() {
        DefaultMessageProvider provider = new DefaultMessageProvider();
        String template = "Field {0} has {1,number,integer} items";
        java.util.stream.IntStream.range(0, 10_000).parallel().forEach(i ->
                assertThat(provider.getMessage(template, "f" + i, i))
                        .isEqualTo(MessageFormat.format(template, "f" + i, i)));
    }
}
