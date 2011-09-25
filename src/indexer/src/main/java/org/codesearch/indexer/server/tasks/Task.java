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
package org.codesearch.indexer.server.tasks;

import java.io.File;
import java.util.List;

import org.codesearch.commons.configuration.dto.RepositoryDto;
import org.codesearch.indexer.server.exceptions.TaskExecutionException;
import org.codesearch.indexer.server.manager.IndexingJob;

/**
 * Base class for all IndexingTasks
 * @author Stephan Stiboller
 * @author David Froehlich
 */
public interface Task {

    /**
     * executes the task
     * @throws TaskExecutionException if the execution of the task failed
     */
    void execute() throws TaskExecutionException;

    /**
     * Sets the repositories this task should handle.
     * @param repositories The list of repositories
     */
    void setRepositories(List<RepositoryDto> repositories);

    /**
     * Sets the location of the index.
     * @param indexLocation The index location
     */
    void setIndexLocation(File indexLocation);
    
    
    /**
     * The parent job instance of this task.
     * Is used to set the current status.
     * @param job The job instance.
     */
    void setJob(IndexingJob job);
}
