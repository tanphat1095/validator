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

package io.github.tanphat1095.validator.handler;

import org.junit.jupiter.api.Test;
import io.github.tanphat1095.validator.Validatable;
import io.github.tanphat1095.validator.ValidationError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidationResultHandlers and their Factory.
 *
 * @author Le Tan Phat
 * @since 1.0.0
 */
class ValidationResultHandlerTest {

    @Test
    void defaultHandlerShouldSupportValidatable() {
        DefaultValidationResultHandler handler = new DefaultValidationResultHandler();
        assertThat(handler.support(MockValidatable.class)).isTrue();
        assertThat(handler.support(HashMap.class)).isFalse();
    }

    @Test
    void defaultHandlerShouldSetResultsOnValidatable() {
        DefaultValidationResultHandler handler = new DefaultValidationResultHandler();
        MockValidatable obj = new MockValidatable();
        List<ValidationError> errors = List.of(ValidationError.fail("field", "error"));

        handler.handle(obj, errors);

        assertThat(obj.getValidationResult()).isEqualTo(errors);
    }

    @Test
    void mapHandlerShouldSupportMap() {
        MapValidationResultHandler handler = new MapValidationResultHandler();
        assertThat(handler.support(HashMap.class)).isTrue();
        assertThat(handler.support(String.class)).isFalse();
    }

    @Test
    void mapHandlerShouldSetResultsInMap() {
        MapValidationResultHandler handler = new MapValidationResultHandler();
        Map<String, Object> map = new HashMap<>();
        List<ValidationError> errors = List.of(ValidationError.fail("field", "error"));

        handler.handle(map, errors);

        assertThat(map.get(MapValidationResultHandler.MAP_RESULT_ERROR)).isEqualTo(errors);
    }

    @Test
    void factoryShouldProvideCorrectHandlers() {
        ValidationResultHandlerFactory factory = new ValidationResultHandlerFactory();

        Optional<ValidationResultHandler> mapHandler = factory.getHandler(HashMap.class);
        assertThat(mapHandler).isPresent().get().isInstanceOf(MapValidationResultHandler.class);

        Optional<ValidationResultHandler> defaultHandler = factory.getHandler(MockValidatable.class);
        assertThat(defaultHandler).isPresent().get().isInstanceOf(DefaultValidationResultHandler.class);

        Optional<ValidationResultHandler> unknownHandler = factory.getHandler(String.class);
        assertThat(unknownHandler).isEmpty();
    }

    @Test
    void factoryShouldRespectCustomHandlersPriority() {
        ValidationResultHandler customHandler = new ValidationResultHandler() {
            @Override
            public <T> void handle(T t, List<ValidationError> errors) {}
            @Override
            public boolean support(Class<?> clazz) {
                return clazz == HashMap.class;
            }
        };

        // When custom handler is added, it should be found first for HashMap
        ValidationResultHandlerFactory factory = new ValidationResultHandlerFactory(List.of(customHandler));
        
        Optional<ValidationResultHandler> handler = factory.getHandler(HashMap.class);
        assertThat(handler).isPresent().contains(customHandler);
    }

    @Test
    void factoryShouldReturnSameResultOnRepeatedLookups() {
        ValidationResultHandlerFactory factory = new ValidationResultHandlerFactory();
        for (int i = 0; i < 3; i++) {
            assertThat(factory.getHandler(HashMap.class)).isPresent().get().isInstanceOf(MapValidationResultHandler.class);
            assertThat(factory.getHandler(MockValidatable.class)).isPresent().get().isInstanceOf(DefaultValidationResultHandler.class);
            assertThat(factory.getHandler(String.class)).isEmpty();
            assertThat(factory.getHandler(null)).isEmpty();
        }
    }

    static class MockValidatable implements Validatable {
        private List<ValidationError> results;

        @Override
        public void setValidationResult(List<ValidationError> results) {
            this.results = results;
        }

        @Override
        public List<ValidationError> getValidationResult() {
            return results;
        }
    }
}
