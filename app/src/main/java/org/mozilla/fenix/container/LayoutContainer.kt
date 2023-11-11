package org.mozilla.fenix.container

import android.view.View

/**
 * A base interface for all view holders supporting Android Extensions-style view access.
 */
public interface LayoutContainer {
    /** Returns the root holder view. */
    public val containerView: View?
}