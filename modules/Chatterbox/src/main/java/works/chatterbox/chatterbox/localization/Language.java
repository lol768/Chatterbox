/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package works.chatterbox.chatterbox.localization;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

public class Language extends PropertyResourceBundle {

    public Language(final Reader reader) throws IOException {
        super(reader);
    }

    @Nullable
    public String getAString(@NotNull final String key) {
        Preconditions.checkNotNull(key, "key was null");
        try {
            return this.getString(key);
        } catch (final MissingResourceException ex) {
            return null;
        }
    }

    @Nullable
    public String getFormattedString(@NotNull final String key, @NotNull final Object... objects) {
        Preconditions.checkNotNull(key, "key was null");
        Preconditions.checkNotNull(objects, "objects was null");
        final String string = this.getAString(key);
        if (string == null) return null;
        return String.format(string, objects);
    }
}
