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

import io.github.tanphat1095.validator.type.DoubleValidator;
import io.github.tanphat1095.validator.type.ObjectValidator;
import io.github.tanphat1095.validator.type.IntValidator;
import io.github.tanphat1095.validator.type.LongValidator;
import io.github.tanphat1095.validator.type.Validator;
import org.junit.jupiter.api.Test;
import io.github.tanphat1095.validator.common.rule.CommonRules;
import io.github.tanphat1095.validator.handler.MapValidationResultHandler;
import io.github.tanphat1095.validator.rule.DoubleRuleValidator;
import io.github.tanphat1095.validator.rule.IntRuleValidator;
import io.github.tanphat1095.validator.rule.LongRuleValidator;
import io.github.tanphat1095.validator.rule.RuleValidator;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Le Tan Phat
 * @since 1.0.0
 */
class DataValidatorTest {

    @Test
    void shouldValidateCorrectData() {
        // Given
        TestObject validObj = new TestObject("John Doe", 25, "Senior Developer", 1000L, 99.9);
        List<TestObject> data = List.of(validObj);

        var maxLength10 = CommonRules.maxStringLength(10);
        IntRuleValidator ageMoreThan18 = (value, context, fieldName) -> 
            value >= 18 ? null : ValidationError.fail(fieldName, "Too young");
        LongRuleValidator positiveId = (value, context, fieldName) -> 
            value > 0 ? null : ValidationError.fail(fieldName, "Negative ID");
        DoubleRuleValidator positivePrice = (value, context, fieldName) -> 
            value > 0.0 ? null : ValidationError.fail(fieldName, "Negative Price");

        Validator<TestObject> nameValidator = new ObjectValidator<>("name", List.of(CommonRules.required(), maxLength10), TestObject::getName);
        Validator<TestObject> ageValidator = new IntValidator<>("age", List.of(ageMoreThan18), TestObject::getAge);
        Validator<TestObject> idValidator = new LongValidator<>("id", List.of(positiveId), TestObject::getId);
        Validator<TestObject> priceValidator = new DoubleValidator<>("price", List.of(positivePrice), TestObject::getPrice);

        DataValidator<TestObject> dataValidator = new DataValidator<>(List.of(nameValidator, ageValidator, idValidator, priceValidator), null);

        // When
        dataValidator.validate(data);

        assertThat(validObj.getValidationResult()).isNotNull().isEmpty();
    }

    @Test
    void shouldSupportLazyStreamValidation() {
        TestObject validObj = new TestObject("John Doe", 25, "Senior Developer", 1000L, 99.9);
        Stream<TestObject> dataStream = Stream.of(validObj);

        IntRuleValidator ageMoreThan18 = (value, context, fieldName) -> 
            value >= 18 ? null : ValidationError.fail(fieldName, "Too young");

        Validator<TestObject> ageValidator = new IntValidator<>("age", List.of(ageMoreThan18), TestObject::getAge);
        DataValidator<TestObject> dataValidator = new DataValidator<>(List.of(ageValidator), null);

        Stream<TestObject> validatedStream = dataValidator.validate(dataStream);
        
        assertThat(validObj.getValidationResult()).isNullOrEmpty();

        validatedStream.forEach(e -> {});

        assertThat(validObj.getValidationResult()).isNullOrEmpty();
    }

    @Test
    void shouldHandleCustomLambdaRules() {
        TestObject obj = new TestObject("SecretUser", 30, "No entry", 1L, 10.0);
        List<TestObject> data = List.of(obj);

        RuleValidator<String> mustContainSecret = (value, context, fieldName) ->
            (value != null && value.contains("Secret")) 
                ? null
                : ValidationError.fail(fieldName, "Must contain 'Secret'");

        IntRuleValidator mustBeEven = (value, context, fieldName) ->
            (value % 2 == 0) 
                ? null
                : ValidationError.fail(fieldName, "Must be even");

        Validator<TestObject> nameValidator = new ObjectValidator<>("name", List.of(mustContainSecret), TestObject::getName);
        Validator<TestObject> ageValidator = new IntValidator<>("age", List.of(mustBeEven), TestObject::getAge);

        DataValidator<TestObject> dataValidator = new DataValidator<>(List.of(nameValidator, ageValidator), null);

        dataValidator.validate(data);

        assertThat(obj.getValidationResult()).isNullOrEmpty();

        obj.setName("NormalUser");
        obj.setAge(31);
        dataValidator.validate(data);
        
        assertThat(obj.getValidationResult()).hasSize(2);
        assertThat(obj.getValidationResult().get(0).message()).contains("Must contain 'Secret'");
        assertThat(obj.getValidationResult().get(1).message()).contains("Must be even");
    }

    @Test
    void shouldCaptureSpecializedValidationErrors() {
        TestObject invalidObj = new TestObject("Joe", 15, "Description", -1L, -5.5);
        List<TestObject> data = List.of(invalidObj);

        var minLength5 = CommonRules.minStringLength(5);
        IntRuleValidator ageMoreThan18 = (value, context, fieldName) -> 
            value >= 18 ? null : ValidationError.fail(fieldName, "Too young");
        LongRuleValidator positiveId = (value, context, fieldName) -> 
            value > 0 ? null : ValidationError.fail(fieldName, "Negative ID");
        DoubleRuleValidator positivePrice = (value, context, fieldName) -> 
            value > 0.0 ? null : ValidationError.fail(fieldName, "Negative Price");

        Validator<TestObject> nameValidator = new ObjectValidator<>("name", List.of(minLength5), TestObject::getName);
        Validator<TestObject> ageValidator = new IntValidator<>("age", List.of(ageMoreThan18), TestObject::getAge);
        Validator<TestObject> idValidator = new LongValidator<>("id", List.of(positiveId), TestObject::getId);
        Validator<TestObject> priceValidator = new DoubleValidator<>("price", List.of(positivePrice), TestObject::getPrice);

        DataValidator<TestObject> dataValidator = new DataValidator<>(List.of(nameValidator, ageValidator, idValidator, priceValidator), null);

        dataValidator.validate(data);

        List<ValidationError> results = invalidObj.getValidationResult();
        assertThat(results).hasSize(4);
        
        assertThat(results.stream().filter(r -> r.targetName().equals("name")).findFirst().get().valid()).isFalse();
        assertThat(results.stream().filter(r -> r.targetName().equals("age")).findFirst().get().valid()).isFalse();
        assertThat(results.stream().filter(r -> r.targetName().equals("id")).findFirst().get().valid()).isFalse();
        assertThat(results.stream().filter(r -> r.targetName().equals("price")).findFirst().get().valid()).isFalse();
    }

    @Test
    void shouldPassIfNoRulesConfigured() {
        TestObject obj = new TestObject("John Doe", 25, "Senior Developer", 1000L, 99.9);
        List<TestObject> data = List.of(obj);

        // String field with empty rules
        Validator<TestObject> stringValidator = new ObjectValidator<>("name", List.of(), TestObject::getName);
        // Primitive int with null rules
        Validator<TestObject> intValidator = new IntValidator<>("age", null, TestObject::getAge);
        // Primitive long with empty rules
        Validator<TestObject> longValidator = new LongValidator<>("id", List.of(), TestObject::getId);
        // Primitive double with null rules
        Validator<TestObject> doubleValidator = new DoubleValidator<>("price", null, TestObject::getPrice);
        
        DataValidator<TestObject> dataValidator = new DataValidator<>(
            List.of(stringValidator, intValidator, longValidator, doubleValidator), 
            null
        );

        dataValidator.validate(data);

        assertThat(obj.getValidationResult()).isNullOrEmpty();
    }

    @Test
    void shouldPassIfOptionalFieldIsNull() {
        // Given: Name is null, but no 'required' rule is set
        TestObject obj = new TestObject(null, 25, "Senior Developer", 1000L, 99.9);
        List<TestObject> data = List.of(obj);

        var maxLength10 = CommonRules.maxStringLength(10);
        Validator<TestObject> nameValidator = new ObjectValidator<>("name", List.of(maxLength10), TestObject::getName);
        DataValidator<TestObject> dataValidator = new DataValidator<>(List.of(nameValidator), null);

        // When
        dataValidator.validate(data);

        // Then
        assertThat(obj.getValidationResult()).isNullOrEmpty();
    }

    @Test
    void shouldHandleNullData() {
        DataValidator<TestObject> dataValidator = new DataValidator<>(List.of(), null);
        
        // Null iterable
        assertThat(dataValidator.validate((List<TestObject>) null)).isEmpty();
        
        // Null stream
        assertThat(dataValidator.validate((Stream<TestObject>) null)).isEmpty();
        
        // Null single object
        assertThat(dataValidator.validate((TestObject) null)).isNull();
    }

    @Test
    void shouldHandleNullItemsInData() {
        TestObject validObj = new TestObject("John Doe", 25, "Senior Developer", 1000L, 99.9);
        List<TestObject> data = new java.util.ArrayList<>();
        data.add(validObj);
        data.add(null);
        
        Validator<TestObject> nameValidator = new ObjectValidator<>("name", List.of(CommonRules.required()), TestObject::getName);
        DataValidator<TestObject> dataValidator = new DataValidator<>(List.of(nameValidator), null);

        // When
        dataValidator.validate(data);

        // Then
        assertThat(data).hasSize(2);
        assertThat(data.get(0).getValidationResult()).isNullOrEmpty();
        assertThat(data.get(1)).isNull();
    }

    @Test
    void shouldHandleNullItemsInStream() {
        TestObject validObj = new TestObject("John Doe", 25, "Senior Developer", 1000L, 99.9);
        Stream<TestObject> dataStream = Stream.of(validObj, null);
        
        Validator<TestObject> nameValidator = new ObjectValidator<>("name", List.of(CommonRules.required()), TestObject::getName);
        DataValidator<TestObject> dataValidator = new DataValidator<>(List.of(nameValidator), null);

        // When
        List<TestObject> results = dataValidator.validate(dataStream).toList();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getValidationResult()).isNullOrEmpty();
        assertThat(results.get(1)).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldValidateHashMapData() {
        // Given
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("username", null);
        data.put("age", 15);

        Validator<java.util.Map<String, Object>> nameValidator = new ObjectValidator<>(
                "username", 
                List.of(CommonRules.required()), 
                m -> (String) m.get("username")
        );
        
        IntRuleValidator ageMoreThan18 = (value, context, fieldName) -> 
            value >= 18 ? null : ValidationError.fail(fieldName, "Too young");
            
        Validator<java.util.Map<String, Object>> ageValidator = new IntValidator<>(
                "age", 
                List.of(ageMoreThan18), 
                m -> (Integer) m.get("age")
        );

        DataValidator<java.util.Map<String, Object>> dataValidator = new DataValidator<>(
                List.of(nameValidator, ageValidator), 
                null
        );

        // When
        dataValidator.validate(data);

        // Then
        assertThat(data).containsKey(MapValidationResultHandler.MAP_RESULT_ERROR);
        List<ValidationError> errors = (List<ValidationError>) data.get(MapValidationResultHandler.MAP_RESULT_ERROR);
        assertThat(errors).hasSize(2);
        assertThat(errors.get(0).targetName()).isEqualTo("username");
        assertThat(errors.get(1).targetName()).isEqualTo("age");
        assertThat(errors.get(1).message()).isEqualTo("Too young");
    }

    @Test
    void shouldValidateObjectItself() {
        // Given
        TestObject obj = new TestObject("John", 10, "Kid", 1L, 10.0);
        List<TestObject> data = List.of(obj);

        // Rule: if age < 18, name must start with "Junior"
        RuleValidator<TestObject> namingConvention = (value, context, targetName) -> {
            if (value.getAge() < 18 && !value.getName().startsWith("Junior")) {
                 return ValidationError.fail(targetName, "Underage must have Junior prefix");
            }
            return null;
        };

        // Validator for the object itself
        Validator<TestObject> selfValidator = new ObjectValidator<>(
                "self", 
                List.of(namingConvention), 
                java.util.function.Function.identity()
        );

        DataValidator<TestObject> dataValidator = new DataValidator<>(List.of(selfValidator), null);

        // When
        dataValidator.validate(data);

        // Then
        assertThat(obj.getValidationResult()).hasSize(1);
        assertThat(obj.getValidationResult().get(0).message()).isEqualTo("Underage must have Junior prefix");
        
        // Fix valid case
        obj.setName("Junior John");
        
        // Re-validate
        dataValidator.validate(data);
        
        // Depending on handler implementation, this might be null or empty
        if (obj.getValidationResult() != null) {
            assertThat(obj.getValidationResult()).isEmpty();
        }
    }

    @Test
    void shouldValidateBothFieldsAndObjectItself() {
        // Given
        // Case: Name is null (Field error), Age is 16 and Price is 100 (Object logic error: Kids can't spend > 50)
        TestObject obj = new TestObject(null, 16, "Kid", 1L, 100.0);
        List<TestObject> data = List.of(obj);

        // 1. Field Validation Rules
        Validator<TestObject> nameValidator = new ObjectValidator<>(
                "name", 
                List.of(CommonRules.required()), 
                TestObject::getName
        );

        // 2. Object Logic Validation Rules
        RuleValidator<TestObject> spendingLimitForKids = (value, context, fieldName) -> {
            if (value.getAge() < 18 && value.getPrice() > 50.0) {
                 return ValidationError.fail("spending", "Kids under 18 cannot spend more than 50.0");
            }
            return null;
        };

        Validator<TestObject> selfValidator = new ObjectValidator<>(
                "self", 
                List.of(spendingLimitForKids), 
                java.util.function.Function.identity()
        );

        // Combine both
        DataValidator<TestObject> dataValidator = new DataValidator<>(
                List.of(nameValidator, selfValidator), 
                null
        );

        // When
        dataValidator.validate(data);

        // Then
        List<ValidationError> errors = obj.getValidationResult();
        assertThat(errors).hasSize(2);
        
        // Assert field error
        assertThat(errors.stream().anyMatch(e -> 
            e.targetName().equals("name") && e.valid()
        )).isFalse();
        
        // Assert object logic error
        assertThat(errors.stream().anyMatch(e -> 
            e.targetName().equals("spending") && e.message().equals("Kids under 18 cannot spend more than 50.0")
        )).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDetectDuplicateValuesInList() {
        // Given
        TestObject obj1 = new TestObject("John", 25, "Senior", 1001L, 99.9);
        TestObject obj2 = new TestObject("Jane", 22, "Junior", 1002L, 50.0);
        TestObject obj3 = new TestObject("Jack", 30, "Lead", 1001L, 120.0); // Duplicate ID with obj1

        List<TestObject> data = List.of(obj1, obj2, obj3);

        // Define a mutable context to hold state across the validation process
        io.github.tanphat1095.validator.context.DefaultValidationContext context 
            = new io.github.tanphat1095.validator.context.DefaultValidationContext();
        
        // Initialize the "seen" set
        // In a real scenario, this key should be a constant
        context.put("seen_ids", new java.util.HashSet<Long>());

        // Stateful Rule: Check uniqueness
        LongRuleValidator uniqueIdRule = (value, ctx, fieldName) -> {
            java.util.Set<Long> seenIds = (java.util.Set<Long>) ctx.get("seen_ids");
            if (seenIds.contains(value)) {
                return ValidationError.fail(fieldName, "Duplicate ID: " + value);
            }
            seenIds.add(value);
            return null;
        };

        Validator<TestObject> idValidator = new LongValidator<>(
                "id", 
                List.of(uniqueIdRule), 
                TestObject::getId
        );

        DataValidator<TestObject> dataValidator = new DataValidator<>(List.of(idValidator), context);

        // When
        dataValidator.validate(data);

        // Then
        assertThat(obj1.getValidationResult()).isNullOrEmpty();
        assertThat(obj2.getValidationResult()).isNullOrEmpty();
        
        // obj3 should fail because 1001 was seen in obj1
        assertThat(obj3.getValidationResult()).hasSize(1);
        assertThat(obj3.getValidationResult().get(0).message()).contains("Duplicate ID: 1001");
    }


    @lombok.Getter
    @lombok.Setter
    @lombok.AllArgsConstructor
    static class TestObject implements Validatable {
        private String name;
        private int age;
        private String desc;
        private long id;
        private double price;
        private List<ValidationError> validationResult;

        public TestObject(String name, int age, String desc, long id, double price) {
            this.name = name;
            this.age = age;
            this.desc = desc;
            this.id = id;
            this.price = price;
        }
    }
}
