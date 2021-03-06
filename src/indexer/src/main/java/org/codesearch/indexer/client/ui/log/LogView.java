/**
 * Copyright 2010 David Froehlich   <david.froehlich@businesssoftware.at>,
 *                Samuel Kogler     <samuel.kogler@gmail.com>,
 *                Stephan Stiboller <stistc06@htlkaindorf.at>
 *
 * This file is part of Codesearch.
 *
 * Codesearch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Codesearch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Codesearch.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.codesearch.indexer.client.ui.log;

import java.util.List;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Displays general information like current status and errors.
 * @author Samuel Kogler
 */
public interface LogView extends IsWidget {

    /**
     * Resets the state of the view.
     */
    void cleanup();

    /**
     * Sets the presenter for this view.
     * @param presenter The presenter
     */
    void setPresenter(LogView.Presenter presenter);

    /**
     * Connects event handlers for hotkeys.
     */
    void connectEventHandlers();

    /**
     * Disconnects event handlers for hotkeys.
     */
    void disconnectEventHandlers();

    /**
     * Sets the displayed log.
     * @param log
     */
    void setLog(List<String> log);

    Presenter getPresenter();

    interface Presenter {
        void goTo(Place place);
        void refresh();
    }
}
