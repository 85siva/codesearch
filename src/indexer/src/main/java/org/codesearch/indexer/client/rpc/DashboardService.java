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
/**
 *
 */
package org.codesearch.indexer.client.rpc;

import org.codesearch.indexer.shared.DashboardData;
import org.codesearch.indexer.shared.DashboardServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author Samuel Kogler
 * The service used the retrieve the data for the dashboard.
 */
@RemoteServiceRelativePath("../dashboard.rpc")
public interface DashboardService extends RemoteService {

	/**
	 * Retrieves the data for the dashboard.
	 * @return The data for the dashboard.
	 */
	DashboardData getData() throws DashboardServiceException;

}