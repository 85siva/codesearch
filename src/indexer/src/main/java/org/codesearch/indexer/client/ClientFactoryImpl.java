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

package org.codesearch.indexer.client;


import org.codesearch.indexer.client.ui.dashboard.DashboardView;
import org.codesearch.indexer.client.ui.dashboard.DashboardViewImpl;
import org.codesearch.indexer.client.ui.log.LogView;
import org.codesearch.indexer.client.ui.log.LogViewImpl;
import org.codesearch.indexer.client.ui.manualIndexing.ManualIndexingView;
import org.codesearch.indexer.client.ui.manualIndexing.ManualIndexingViewImpl;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

/**
 * Implementation of the factory.
 * @author Samuel Kogler
 */
public class ClientFactoryImpl extends ClientFactory {

    private final EventBus eventBus = new SimpleEventBus();
    private final PlaceController placeController = new PlaceController(eventBus);
    private final DashboardView dashboardView = new DashboardViewImpl();
    private final LogView logView = new LogViewImpl();
    private final ManualIndexingView manualIndexingView = new ManualIndexingViewImpl();

    /** {@inheritDoc} */
    @Override
    public ManualIndexingView getManualIndexingView() {
        return manualIndexingView;
    }

    /** {@inheritDoc} */
    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    /** {@inheritDoc} */
    @Override
    public PlaceController getPlaceController() {
        return placeController;
    }

    @Override
    public DashboardView getDashboardView() {
        return dashboardView;
    }

	@Override
	public LogView getLogView() {
		return logView;
	}
}
