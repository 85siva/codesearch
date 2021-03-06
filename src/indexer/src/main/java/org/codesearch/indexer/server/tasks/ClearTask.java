/**
 * Copyright 2010 David Froehlich <david.froehlich@businesssoftware.at>, Samuel
 * Kogler <samuel.kogler@gmail.com>, Stephan Stiboller <stistc06@htlkaindorf.at>
 *
 * This file is part of Codesearch.
 *
 * Codesearch is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Codesearch is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Codesearch. If not, see <http://www.gnu.org/licenses/>.
 */
package org.codesearch.indexer.server.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.StaleReaderException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.codesearch.commons.configuration.dto.RepositoryDto;
import org.codesearch.commons.configuration.properties.IndexStatusManager;
import org.codesearch.commons.constants.IndexConstants;
import org.codesearch.commons.database.DBAccess;
import org.codesearch.commons.database.DatabaseAccessException;
import org.codesearch.commons.plugins.vcs.VersionControlPlugin;
import org.codesearch.indexer.server.exceptions.TaskExecutionException;
import org.codesearch.indexer.server.manager.IndexingJob;

import com.google.inject.Inject;

/**
 * Clears the index of the specified repositories. If none are specified,
 * deletes the entire index.
 *
 * @author David Froehlich
 * @author Samuel Kogler
 */
public class ClearTask implements Task {

    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(ClearTask.class);
    /**
     * the location of the index
     */
    private File indexLocation;
    /**
     * the repository to clear
     */
    private List<RepositoryDto> repositories;
    /**
     * The database access object
     */
    private DBAccess dba;
    /**
     * The parent {@link IndexingJob}.
     */
    private IndexingJob job;
    /**
     * The properties manager used to reset the indexed revision.
     */
    private IndexStatusManager indexStatusManager;

    @Inject
    public ClearTask(DBAccess dba, IndexStatusManager indexStatusManager, List<RepositoryDto> repositories, File indexLocation, IndexingJob job) {
        this.dba = dba;
        this.indexStatusManager = indexStatusManager;
        this.repositories = repositories;
        this.indexLocation = indexLocation;
        this.job = job;
    }

    /**
     * executes the task deletes all index files and resets the
     * lastIndexingRevision of all repositories
     *
     * @throws TaskExecutionException
     */
    @Override
    public void execute() throws TaskExecutionException {
        if (repositories == null || repositories.isEmpty()) { // Clear the whole index
            LOG.info("Clearing the whole index");
            boolean deleteSuccess = true;
            if (indexLocation.listFiles() != null) {
                for (File f : indexLocation.listFiles()) {
                    if (!f.delete()) {
                        LOG.error("Could not delete file: " + f.getName());
                        deleteSuccess = false;
                    }
                }
            }
            if (deleteSuccess) {
                LOG.debug("Successfully cleared index");
            } else {
                LOG.error("Could not delete all index files");
            }
            try {
                dba.purgeDatabaseEntries();
                LOG.debug("Cleared code analysis index");
            } catch (DatabaseAccessException ex) {
                LOG.warn("Could not clear code analysis index: \n" + ex);
            }
        } else { // Clear specific repository from the index
            StringBuilder repos = new StringBuilder();
            for (RepositoryDto repositoryDto : repositories) {
                repos.append(repositoryDto.getName()).append(" ");
            }
            LOG.info("Clearing index for repositories: " + repos.toString().trim());
            if (job != null) {
                job.getJobDataMap().put(IndexingJob.FIELD_CURRENT_STEPS, repositories.size());
            }
            IndexSearcher searcher = null;
            try {
                FSDirectory fsd = FSDirectory.open(indexLocation);
                searcher = new IndexSearcher(fsd, false);

                int index = 0;
                for (RepositoryDto repositoryDto : repositories) {
                    if (job != null) {
                        // set the status
                        job.setCurrentRepository(index);
                        job.getJobDataMap().put(IndexingJob.FIELD_FINISHED_STEPS, index);
                    }
                    Term term = new Term(IndexConstants.INDEX_FIELD_REPOSITORY, repositoryDto.getName().toLowerCase());

                    LOG.debug("Deleting documents where field '" + term.field() + "' is '" + term.text() + "'");
                    int deleteCount = 0;
                    try {
                        deleteCount = searcher.getIndexReader().deleteDocuments(term);
                    } catch (IOException ex) {
                        LOG.error("Error encountered while clearing index: " + ex);
                    }

                    indexStatusManager.setStatus(repositoryDto.getName(), VersionControlPlugin.UNDEFINED_VERSION);

                    LOG.debug("Cleared " + deleteCount + " documents for repository " + repositoryDto.getName());
                    index++;
                }
            } catch (CorruptIndexException ex) {
                LOG.error("Could not clear index because it is corrupt: \n" + ex);
            } catch (LockObtainFailedException ex) {
                LOG.error("Could not clear index because it is locked.");
            } catch (StaleReaderException ex) {
                LOG.error("The index was modified by another program/instance while clearing.");
            } catch (IOException ex) {
                // if there is no index nothing has to be cleared
            } finally {
                if (searcher != null) {
                    try {
                        searcher.close();
                    } catch (IOException ex) {
                    }
                }
            }

            for (RepositoryDto repositoryDto : repositories) {
                try {
                    dba.deleteRepository(repositoryDto.getName());
                    LOG.debug("Cleared code analysis index for repository " + repositoryDto.getName());
                } catch (DatabaseAccessException ex) {
                    LOG.warn("Could not clear code analysis index for repository: " + repositoryDto.getName() + ", ignore if code analysis is not enabled \n" + ex);
                }
            }
        }

        LOG.info("Finished clearing index");
    }
}
