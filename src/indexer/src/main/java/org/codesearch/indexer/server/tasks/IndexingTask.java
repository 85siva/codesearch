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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.codesearch.commons.configuration.dto.RepositoryDto;
import org.codesearch.commons.configuration.properties.IndexStatusManager;
import org.codesearch.commons.constants.IndexConstants;
import org.codesearch.commons.database.DBAccess;
import org.codesearch.commons.database.DatabaseAccessException;
import org.codesearch.commons.plugins.PluginLoader;
import org.codesearch.commons.plugins.PluginLoaderException;
import org.codesearch.commons.plugins.codeanalyzing.CodeAnalyzerPlugin;
import org.codesearch.commons.plugins.codeanalyzing.CodeAnalyzerPluginException;
import org.codesearch.commons.plugins.codeanalyzing.ast.AstNode;
import org.codesearch.commons.plugins.codeanalyzing.ast.Usage;
import org.codesearch.commons.plugins.lucenefields.LuceneFieldPlugin;
import org.codesearch.commons.plugins.lucenefields.LuceneFieldPluginLoader;
import org.codesearch.commons.plugins.lucenefields.LuceneFieldValueException;
import org.codesearch.commons.plugins.vcs.FileDto;
import org.codesearch.commons.plugins.vcs.FileIdentifier;
import org.codesearch.commons.plugins.vcs.VcsFileNotFoundException;
import org.codesearch.commons.plugins.vcs.VersionControlPlugin;
import org.codesearch.commons.plugins.vcs.VersionControlPluginException;
import org.codesearch.commons.utils.mime.MimeTypeUtil;
import org.codesearch.indexer.server.exceptions.NotifySearcherException;
import org.codesearch.indexer.server.exceptions.TaskExecutionException;
import org.codesearch.indexer.server.manager.IndexingJob;

import com.google.inject.Inject;

/**
 * This task performs basic indexing of one repository.
 * 
 * @author Stephan Stiboller
 * @author David Froehlich
 */
public class IndexingTask implements Task {

    /**
     * The Logger.
     */
    private static final Logger LOG = Logger.getLogger(IndexingTask.class);
    /**
     * The affected repositories.
     */
    private List<RepositoryDto> repositories;
    /**
     * The currently active IndexWriter
     */
    private IndexWriter indexWriter;
    /**
     * The index directory, contains all index files
     */
    private Directory indexDirectory;
    /**
     * The Version control Plugin
     */
    private VersionControlPlugin versionControlPlugin;
    /**
     * used to read the repository revision status
     */
    private IndexStatusManager indexStatusManager;
    /**
     * the plugins that will be used to create the fields for each document
     */
    private List<LuceneFieldPlugin> luceneFieldPlugins = new LinkedList<LuceneFieldPlugin>();
    /**
     * The database access object
     */
    private DBAccess dba;
    /**
     * The plugin loader.
     */
    private PluginLoader pluginLoader;
    /**
     * The URI that is called to update the searcher application.
     */
    private URI searcherLocation;
    /**
     * The parent {@link IndexingJob}.
     */
    private IndexingJob job;
    /**
     * the CodeAnalyzerPlugins used, one per mimetype
     */
    private Map<String, CodeAnalyzerPlugin> caPlugins = new HashMap<String, CodeAnalyzerPlugin>();
    /**
     * The wrapper analyzer constructed by the plugin loader
     */
    private PerFieldAnalyzerWrapper caseInsensitiveAnalyzer;

    @Inject
    public IndexingTask(DBAccess dba, PluginLoader pluginLoader, URI searcherLocation, LuceneFieldPluginLoader luceneFieldPluginLoader,
            IndexStatusManager indexStatusManager, List<RepositoryDto> repositories, Directory indexDirectory, IndexingJob job)
            throws IOException, TaskExecutionException {
        if (job == null) {
            throw new TaskExecutionException("Parent job must be set in constructor, was null");
        }
        luceneFieldPlugins = luceneFieldPluginLoader.getAllLuceneFieldPlugins();
        caseInsensitiveAnalyzer = luceneFieldPluginLoader.getPerFieldAnalyzerWrapper(false);
        this.indexStatusManager = indexStatusManager;
        this.repositories = repositories;
        this.searcherLocation = searcherLocation;
        this.dba = dba;
        this.pluginLoader = pluginLoader;
        this.indexDirectory = indexDirectory;
        this.job = job;
        IndexWriterConfig config = new IndexWriterConfig(IndexConstants.LUCENE_VERSION, caseInsensitiveAnalyzer);

        indexWriter = new IndexWriter(indexDirectory, config);
        LOG.debug("IndexWriter initialization successful");
    }

    /**
     * executes the task, updates the index fields of the set repository
     * 
     * @throws TaskExecutionException
     */
    @Override
    public void execute() throws TaskExecutionException {
        // whether or not previous database operations were executed successfully
        // once a DB-operation fails no additional operations are executed in this task to prevent log flooding
        boolean databaseConnectionValid = true;
        if (repositories != null) {
            StringBuilder repos = new StringBuilder();
            for (RepositoryDto repositoryDto : repositories) {
                repos.append(repositoryDto.getName()).append(" ");
            }
            LOG.info("Starting indexing of repositories: " + repos.toString().trim());
            try {
                int i = 0;
                for (RepositoryDto repository : repositories) {
                    job.setCurrentRepository(i);
                    job.getJobDataMap().put(IndexingJob.FIELD_STEP, "Getting newest revision number");
                    try {
                        LOG.info("Indexing repository: " + repository.getName()
                                + (repository.isCodeNavigationEnabled() ? " using" : " without") + " code analyzing");
                        long start = System.currentTimeMillis();
                        // Read the index status file
                        String lastIndexedRevision = indexStatusManager.getStatus(repository.getName());
                        LOG.info("Last indexed revision: " + lastIndexedRevision);
                        // Get the version control plugins
                        versionControlPlugin = pluginLoader.getPlugin(VersionControlPlugin.class, repository.getVersionControlSystem());
                        if (versionControlPlugin == null) {
                            LOG.error("Could not load VersionControlPlugin, skipping repository " + repository.getName());
                        }
                        // get the changed files
                        versionControlPlugin.setRepository(repository);
                        LOG.info("Pulling new changes of the repository");
                        job.getJobDataMap().put(IndexingJob.FIELD_STEP, "Pulling repository changes");
                        versionControlPlugin.pullChanges();
                        String repositoryRevision = versionControlPlugin.getRepositoryRevision();
                        LOG.info("Newest revision      : " + repositoryRevision);
                        job.getJobDataMap().put(IndexingJob.FIELD_STEP, "Getting changed files");
                        Set<FileIdentifier> changedFiles = versionControlPlugin.getChangedFilesSinceRevision(lastIndexedRevision,
                                repository.getBlacklistEntries(), repository.getWhitelistEntries());
                        if (!versionControlPlugin.supportsBlacklistingChanges()) {
                            filterChangedFiles(changedFiles, repository.getWhitelistEntries(), repository.getBlacklistEntries());
                        }
                        LOG.info(changedFiles.size() + " files have changed since the last indexing");
                        job.getJobDataMap().put(IndexingJob.FIELD_STEP, "Deleting changed files");
                        // clear the index of the old verions of the files
                        deleteFilesFromIndex(changedFiles);
                        if (repository.isCodeNavigationEnabled()) {
                            try {
                                String lastAnalysisRevision = dba.getLastAnalyzedRevisionOfRepository(repository.getName());
                                if (!lastAnalysisRevision.equals(lastIndexedRevision)) {
                                    throw new TaskExecutionException(
                                            "The code information in the database is not at the same revision as the regular indexed information\n"
                                                    + "The index of the repository must be cleared first");
                                }
                            } catch (DatabaseAccessException ex) {
                                LOG.error("Code analyzing failed, no code analyzing data will be available: \n" + ex);
                                databaseConnectionValid = false;
                            }
                        }

                        // check whether the changed files should be indexed
                        job.getJobDataMap().put(IndexingJob.FIELD_STEP, "Applying black- and whitelist");
                        removeNotToBeIndexedFiles(changedFiles);
                        job.getJobDataMap().put(IndexingJob.FIELD_STEP, "Indexing changed files");
                        job.getJobDataMap().put(IndexingJob.FIELD_CURRENT_STEPS, changedFiles.size());
                        int finishedFiles = 0;
                        for (FileIdentifier currentIdentifier : changedFiles) {
                            job.getJobDataMap().put(IndexingJob.FIELD_FINISHED_STEPS, finishedFiles);
                            if (!currentIdentifier.isDeleted()) {
                                try {
                                    FileDto currentDto = versionControlPlugin.getFile(currentIdentifier,
                                            VersionControlPlugin.UNDEFINED_VERSION);
                                    addFileToIndex(currentDto);

                                    if (repository.isCodeNavigationEnabled() && databaseConnectionValid) {
                                        executeCodeAnalysisForFile(currentDto);
                                    }
                                } catch (VcsFileNotFoundException ex) {
                                    LOG.error("File not found: " + currentIdentifier + " , skipping file.");
                                } catch (CodeAnalyzerPluginException ex) {
                                    LOG.error("Code analyzing failed, skipping file\n" + ex);
                                    // in case either of those exceptions occurs try to keep indexing the remaining files
                                } catch (LuceneFieldValueException ex) {
                                    LOG.error(ex);
                                } catch (DatabaseAccessException ex) {
                                    LOG.error("Code analyzing failed: Database error:" + ex);
                                    databaseConnectionValid = false;
                                } catch (VersionControlPluginException ex) {
                                    LOG.error("Could not retrieve file: " + currentIdentifier.getFilePath());
                                    LOG.debug("VersionControlPlugin threw exception: \n" + ex);
                                }
                            }
                            finishedFiles++;
                        }
                        long duration = System.currentTimeMillis() - start;
                        LOG.info("Indexing of repository " + repository.getName() + " took " + duration / 1000 + " seconds");
                        indexStatusManager.setStatus(repository.getName(), repositoryRevision);
                        if (repository.isCodeNavigationEnabled()) {
                            try {
                                dba.setLastAnalyzedRevisionOfRepository(repository.getName(), repositoryRevision);
                            } catch (DatabaseAccessException ex) {
                                databaseConnectionValid = false;
                            }
                        }
                    } catch (VersionControlPluginException ex) {
                        LOG.error("Fatal error in VersionControlPlugin, skipping repository " + repository.getName(), ex);
                    }
                    i++;
                }
                indexWriter.commit();
                try {
                    // notify the searcher about the update of the indexer
                    notifySearcher();
                } catch (NotifySearcherException ex) {
                    LOG.warn("Notification of searcher failed, changes in the index will not be recognized without a restart: " + ex);
                }
                LOG.info("Finished indexing");
            } catch (FileNotFoundException ex) {
                LOG.error("Could not write the index status file: " + ex);
            } catch (IOException ex) {
                LOG.error("IOException occured at indexing: " + ex);
            } finally {
                cleanup();
            }
        } else {
            LOG.warn("No repositories specified, skipping indexing.");
        }
    }

    /**
     * Filters the list of changed files by the given white and blacklist
     * 
     * @param changedFiles
     * @param whitelistEntries
     * @param blacklistEntries
     */
    private void filterChangedFiles(Set<FileIdentifier> changedFiles, List<String> whitelistEntries, List<String> blacklistEntries) {
        List<Pattern> compiledBlacklist = new LinkedList<Pattern>();
        List<Pattern> compiledWhitelist = new LinkedList<Pattern>();
        for (String s : blacklistEntries) {
            compiledBlacklist.add(Pattern.compile(s));
        }
        for (String s : whitelistEntries) {
            compiledWhitelist.add(Pattern.compile(s));
        }

        outer: for (Iterator<FileIdentifier> iterator = changedFiles.iterator(); iterator.hasNext();) {
            FileIdentifier fileIdentifier = iterator.next();
            if (!compiledWhitelist.isEmpty()) {
                for (Pattern pattern : compiledWhitelist) {
                    if (!pattern.matcher(fileIdentifier.getFilePath()).find()) {
                        iterator.remove();
                        continue outer;
                    }
                }
            }

            if (!compiledBlacklist.isEmpty()) {
                for (Pattern pattern : compiledBlacklist) {
                    if (pattern.matcher(fileIdentifier.getFilePath()).find()) {
                        iterator.remove();
                        continue outer;
                    }
                }
            }
        }
    }

    /**
     * checks all FileIdentifiers whether the files should be indexed or not
     * deletes those files that should not be indexed from the set
     * 
     * @param fileIdentifiers
     */
    private void removeNotToBeIndexedFiles(Set<FileIdentifier> fileIdentifiers) {
        if (!fileIdentifiers.isEmpty()) {
            Iterator<FileIdentifier> iter = fileIdentifiers.iterator();
            FileIdentifier currentFile = iter.next();
            for (; iter.hasNext(); currentFile = iter.next()) {
                if ((currentFile.isDeleted()) || (!shouldFileBeIndexed(currentFile))) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Sends a request to the searcher web application, notifying it to re-load
     * the index.
     * 
     * @throws NotifySearcherException in case the connection to the searcher
     *             could not be established
     */
    private void notifySearcher() throws NotifySearcherException {
        try {
            URL url = new URL(searcherLocation.toURL(), "refresh");
            url.openStream();
        } catch (IOException ex) {
            throw new NotifySearcherException("Could not connect to searcher at the configured address: " + searcherLocation + "\n"
                    + ex.toString());
        }
    }

    /**
     * executes the code analysis for the given file
     * 
     * @throws CodeAnalyzerPluginException if the source code of one of the
     *             files could not be analyzed
     */
    private void executeCodeAnalysisForFile(FileDto fileDto) throws DatabaseAccessException, CodeAnalyzerPluginException {
        // delete outdated record from database
        dba.deleteFile(fileDto.getFilePath(), fileDto.getRepository().getName());
        String fileType = MimeTypeUtil.guessMimeTypeViaFileEnding(fileDto.getFilePath());
        if (!fileType.equals(MimeTypeUtil.UNKNOWN)) {
            CodeAnalyzerPlugin plugin;
            if (!caPlugins.containsKey(fileType)) {
                caPlugins.put(fileType, pluginLoader.getPlugin(CodeAnalyzerPlugin.class, fileType));
            }
            plugin = caPlugins.get(fileType);
            if(plugin == null) {
                return;
            }
            LOG.debug("Analyzing file: " + fileDto.getFilePath());
            try {
                plugin.analyzeFile(new String(fileDto.getContent()));
                AstNode ast = plugin.getAst();
                List<String> typeDeclarations = plugin.getTypeDeclarations();
                List<Usage> usages = plugin.getUsages();
                List<String> imports = plugin.getImports();
                // add the externalLinks to the FileDto, so they
                // can be parsed after the regular indexing is finished
                // write the AST information into the database
                dba.setAnalysisDataForFile(fileDto.getFilePath(), fileDto.getRepository().getName(), ast, usages, typeDeclarations, imports);
            } catch (Exception ex) {
                LOG.error("Code analyzer plugin threw exception: \n" + ex);
            }
        }
    }

    /**
     * Adds all fields of the specified file to the specified document.
     * 
     * @param doc The target document
     * @param file The source file
     */
    private void addLuceneFields(Document doc, FileDto file) throws LuceneFieldValueException {
        for (LuceneFieldPlugin currentPlugin : luceneFieldPlugins) {
            String fieldValue = currentPlugin.getFieldValue(file);
            String currentFieldName = currentPlugin.getFieldName();
            Store store = currentPlugin.isStored() ? Field.Store.YES : Field.Store.NO;
            Index index = currentPlugin.getRegularCaseAnalyzer() != null ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED;
            Field regularField = new Field(currentFieldName, fieldValue, store, index);

            doc.add(regularField);

            if (currentPlugin.getLowerCaseAnalyzer() != null) {
                Field lowerCaseField = new Field(currentFieldName + IndexConstants.LC_POSTFIX, fieldValue.toLowerCase(), store, index);
                doc.add(lowerCaseField);
            }
        }
    }

    /**
     * Performs cleanup tasks after execution or after an error is encountered.
     */
    private void cleanup() {
        if (indexWriter != null) {
            try {
                indexWriter.close();
            } catch (IOException ex) {
                LOG.error("Could not close the index writer:\n" + ex);
            } catch (OutOfMemoryError error) {
                LOG.error("Out of memory, trying to save index");
                Runtime.getRuntime().gc();
                try {
                    indexWriter.close();
                } catch (IOException ex) {
                    LOG.error("Could not close the index writer:\n" + ex);
                }
            }
        }
        try {
            if (indexDirectory != null) {
                indexDirectory.close();
            }
        } catch (IOException ex) {
            LOG.error("Could not close the index directory");
        }
    }

    /**
     * Adds the specified files to the index.
     */
    private void addFileToIndex(FileDto file) throws VersionControlPluginException, CorruptIndexException, IOException,
            LuceneFieldValueException {
        if (indexWriter == null) {
            LOG.error("Creation of indexDirectory failed due to missing initialization of IndexWriter!");
            throw new IllegalStateException("IndexWriter was not initialized: fatal error");
        }
        Document doc = new Document();
        // Add fields
        addLuceneFields(doc, file);
        // Add document to the index
        indexWriter.addDocument(doc);
        // Logging
        if (LOG.isDebugEnabled()) {
            String fileName;
            try {
                fileName = file.getFilePath().substring(file.getFilePath().lastIndexOf('/') + 1);
            } catch (StringIndexOutOfBoundsException ex) {
                // if the file is in the root directory of the repository
                fileName = file.getFilePath();
            }
            LOG.debug("Added file: " + fileName + " to index.");
        }
    }

    /**
     * Removes the specified files from the index.
     */
    private void deleteFilesFromIndex(Set<FileIdentifier> files) {
        try {
            for (FileIdentifier file : files) {
                // FIXME this is now done via plugins -> what if constant changes?!
                Term repositoryTerm = new Term(IndexConstants.INDEX_FIELD_REPOSITORY, file.getRepository().getName());
                Term filepathTerm = new Term(IndexConstants.INDEX_FIELD_FILEPATH, file.getFilePath());
                BooleanQuery query = new BooleanQuery();
                query.add(new BooleanClause(new TermQuery(repositoryTerm), Occur.MUST));
                query.add(new BooleanClause(new TermQuery(filepathTerm), Occur.MUST));

                indexWriter.deleteDocuments(query);
            }
        } catch (CorruptIndexException ex) {
            LOG.error("Could not delete files from index because it is corrupted.");
        } catch (IOException ex) {
            LOG.error("Could not delete files from index: " + ex);
        }
    }

    /**
     * Checks whether the current file is on the list of files that will not be
     * indexed
     */
    private boolean shouldFileBeIndexed(FileIdentifier file) {
        String path = file.getFilePath();
        RepositoryDto repository = file.getRepository();
        Pattern p;
        boolean matchesElementOnWhitelist = false;
        boolean shouldFileBeIndexed = true;
        // if no whitelist is specified all files pass the whitelist check
        if (repository.getWhitelistEntries().isEmpty()) {
            matchesElementOnWhitelist = true;
        } else {
            // else check if the filename matches one of the whitelist entries
            for (String currentWhitelistEntry : repository.getWhitelistEntries()) {
                p = Pattern.compile(currentWhitelistEntry);
                Matcher m = p.matcher(path);
                if (m.find()) {
                    matchesElementOnWhitelist = true;
                    break;
                }
            }
        }
        // check if the filename matches one of the blacklist entries, if yes
        // return false, so the file won't be indexed
        if (matchesElementOnWhitelist) {
            for (String currentBlacklistEntry : repository.getBlacklistEntries()) {
                p = Pattern.compile(currentBlacklistEntry);
                Matcher m = p.matcher(path);
                if (m.find()) {
                    shouldFileBeIndexed = false;
                    break;
                }
            }
        }
        return shouldFileBeIndexed && matchesElementOnWhitelist;
    }
}
