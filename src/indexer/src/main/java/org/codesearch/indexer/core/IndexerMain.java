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
package org.codesearch.indexer.core;

import javax.servlet.ServletContextEvent;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.codesearch.indexer.manager.IndexingManager;
import org.quartz.SchedulerException;

/**
 *
 * @author David Froehlich
 */
public class IndexerMain implements javax.servlet.ServletContextListener {
    protected static final Logger LOG = Logger.getLogger(IndexerMain.class);
    /** The IndexingManager used to control the execution of the IndexingJobs */
    private IndexingManager indexingManager;

    /**
     * Instantiates the IndexingManager and calls the startScheduler
     * @param sce dummy parameter needed by the parent class implementation
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            indexingManager = new IndexingManager();
            indexingManager.startScheduler();
        } catch (SchedulerException ex) {
            LOG.error("Problem with scheduler at context initialization: " + ex);
        } catch (ConfigurationException ex) {
            LOG.error("Problem with configuration at context initialization: " + ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
