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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Le Tan Phat
 * @since 1.0.0
 */
public class DefaultMessageProvider implements MessageProvider{

    // ponytail: no eviction, templates past the cap are parsed on every call; switch to an LRU if apps build many dynamic templates
    private static final int MAX_CACHED_TEMPLATES = 1024;
    private static final Map<TemplateKey, MessageFormat> FORMAT_CACHE = new ConcurrentHashMap<>();

    private record TemplateKey(String template, Locale locale) {}

    @Override
    public String getMessage(String template, Object... args) {
        if (template == null) return null;
        // Same locale as MessageFormat.format(String, Object...)
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        TemplateKey key = new TemplateKey(template, locale);
        MessageFormat format = FORMAT_CACHE.get(key);
        if (format == null) {
            format = new MessageFormat(template, locale);
            if (FORMAT_CACHE.size() < MAX_CACHED_TEMPLATES) {
                FORMAT_CACHE.putIfAbsent(key, format);
            }
        }
        // MessageFormat is not thread-safe, format on a private copy
        return ((MessageFormat) format.clone()).format(args);
    }
}
