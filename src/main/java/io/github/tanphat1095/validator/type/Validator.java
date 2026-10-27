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

import io.github.tanphat1095.validator.ValidationError;
import io.github.tanphat1095.validator.context.ValidationContext;

import java.util.List;

/**
 * To avoid the performance costs of autoboxing, I implemented specialized interfaces for primitive types.
 *
 * @author Le Tan Phat
 * @since 1.0.0
 */
public interface Validator<E> {
    List<ValidationError> validate(E data, ValidationContext<?,?> context);

    /**
     * If true, the remaining validators of the same record are skipped once this one returns errors.
     *
     * @since 1.0.2
     */
    default boolean failFast() {
        return false;
    }

    /**
     * Wraps any validator so that it is fail-fast.
     *
     * @since 1.0.2
     */
    static <E> Validator<E> failFast(Validator<E> validator) {
        return new Validator<>() {
            @Override
            public List<ValidationError> validate(E data, ValidationContext<?,?> context) {
                return validator.validate(data, context);
            }

            @Override
            public boolean failFast() {
                return true;
            }
        };
    }
}
