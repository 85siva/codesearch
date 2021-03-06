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

import org.codesearch.indexer.client.ui.RootContainer;
import org.codesearch.indexer.client.ui.dashboard.DashboardPlace;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;


/**
 * The entry point for the searcher.
 * @author Samuel Kogler
 */
public class IndexerEntryPoint implements EntryPoint {

    private RootContainer rootContainer;
    private Place defaultPlace = new DashboardPlace();

    /** {@inheritDoc} */
    @Override
    public void onModuleLoad() {
        ClientFactory clientFactory = ClientFactory.getDefaultFactory();
        EventBus eventBus = clientFactory.getEventBus();
        rootContainer = new RootContainer(eventBus);

        PlaceController placeController = clientFactory.getPlaceController();

        ActivityMapper activityMapper = new IndexerActivityMapper(clientFactory);
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(rootContainer);

        IndexerPlaceHistoryMapper historyMapper = GWT.create(IndexerPlaceHistoryMapper.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, defaultPlace);

        RootLayoutPanel.get().add(rootContainer);
        historyHandler.handleCurrentHistory();
    }
}
