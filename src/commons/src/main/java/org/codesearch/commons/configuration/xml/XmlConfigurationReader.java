/**
 * Copyright 2010 David Froehlich <david.froehlich@businesssoftware.at>, Samuel Kogler <samuel.kogler@gmail.com>, Stephan Stiboller
 * <stistc06@htlkaindorf.at>
 *
 * This file is part of Codesearch.
 *
 * Codesearch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Codesearch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Codesearch. If not, see <http://www.gnu.org/licenses/>.
 */
package org.codesearch.commons.configuration.xml;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codesearch.commons.configuration.ConfigurationReader;
import org.codesearch.commons.configuration.InvalidConfigurationException;
import org.codesearch.commons.configuration.dto.AuthenticationType;
import org.codesearch.commons.configuration.dto.BasicAuthentication;
import org.codesearch.commons.configuration.dto.CodesearchConfiguration;
import org.codesearch.commons.configuration.dto.DatabaseConfiguration;
import org.codesearch.commons.configuration.dto.IndexerUserDto;
import org.codesearch.commons.configuration.dto.JobDto;
import org.codesearch.commons.configuration.dto.NoAuthentication;
import org.codesearch.commons.configuration.dto.RepositoryDto;
import org.codesearch.commons.configuration.dto.SshAuthentication;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.codesearch.commons.configuration.properties.PropertiesManager;
import org.codesearch.commons.constants.IndexConstants;

/**
 * Xml implementation of the configuration reader. By default, the properties are loaded from a file in the classpath called
 * codesearch_config.xml.
 *
 * @author Stephan Stiboller
 * @author David Froehlich
 * @author Samuel Kogler
 */
public class XmlConfigurationReader implements ConfigurationReader {

    private static final Logger LOG = Logger.getLogger(XmlConfigurationReader.class);
    /** The XMLConfiguration object that is used to read the properties from the XML-file */
    private XMLConfiguration config;
    /** The path to the configuration file. */
    private String configPath = "codesearch_config.xml";

    private CodesearchConfiguration codesearchConfiguration;

    PropertiesManager propertiesManager;
    /**
     * creates a new instance of XmlConfigurationReader
     *
     * @param configPath the classpath of the config file
     * @throws InvalidConfigurationException if the configuration file was invalid
     */
    @Inject
    public XmlConfigurationReader(@Named("configpath") String configPath) throws InvalidConfigurationException {
        if (StringUtils.isNotEmpty(configPath)) {
            this.configPath = configPath;
        }
        
        LOG.debug("Reading config file: " + this.configPath);
        loadConfig();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized List<IndexerUserDto> getIndexerUsers() {
        return codesearchConfiguration.getIndexerUsers();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized List<JobDto> getJobs() {
        return codesearchConfiguration.getJobs();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized RepositoryDto getRepositoryByName(String name) {
        for (RepositoryDto repo : codesearchConfiguration.getRepositories()) {
            if (repo.getName().equals(name)) {
                return repo;
            }
        }
        return null;
    }

    /**
     * returns a list of all globally whitelisted filenames, so every filename has to match at least one of the whitelist names (only if the
     * whitelist is not empty)
     */
    public synchronized List<String> getGlobalWhitelistEntries() {
        return codesearchConfiguration.getGlobalWhitelist();
    }

    /**
     * returns a list of all file name patterns that are listed to be ignored on the entire system
     */
    private synchronized List<String> getGlobalBlacklistEntries() {
        return codesearchConfiguration.getGlobalBlacklist();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized List<RepositoryDto> getRepositories() {
        return codesearchConfiguration.getRepositories();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized List<String> getRepositoryGroups() {
        return codesearchConfiguration.getRepositoryGroups();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized List<String> getRepositoriesForGroup(String groupName) {
        List<String> repos = new LinkedList<String>();
        for (RepositoryDto repo : getRepositories()) {
            if (repo.getRepositoryGroups().contains(groupName)) {
                repos.add(repo.getName());
            }
        }
        return repos;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized DatabaseConfiguration getDatabaseConfiguration() {
        return codesearchConfiguration.getDatabaseConfiguration();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized File getCacheDirectory() {
        return codesearchConfiguration.getCacheDirectory();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized File getIndexLocation() {
        return codesearchConfiguration.getIndexLocation();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized URI getSearcherLocation() {
        return codesearchConfiguration.getSearcherLocation();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void refresh() {
        try {
            loadConfig();
        } catch (InvalidConfigurationException e) {
            LOG.fatal("Reload of configuration failed: \n" + e);
        }
    }

    // TODO perform config value validation here
    private void loadConfig() throws InvalidConfigurationException {
        codesearchConfiguration = new CodesearchConfiguration();
        try {
            config = new XMLConfiguration(this.configPath);
            if(config == null) {
                throw new InvalidConfigurationException("Config was null at: " + this.configPath);
            }
        } catch (ConfigurationException ex) {
            throw new InvalidConfigurationException("Configuration file could not be read:\n" + ex);
        }
        loadCacheDirectory();
        loadIndexLocation();
        loadSearcherLocation();
        loadGlobalBlacklist();
        loadGlobalWhitelist();
        loadRepositoryGroups();
        loadRepositories();
        loadJobs();
        loadIndexerUsers();
        loadDatabaseConfiguration();
    }

    private void loadCacheDirectory() throws InvalidConfigurationException {
        File cacheDirectory = null;
        try {
            cacheDirectory = new File(config.getString(XmlConfigurationReaderConstants.CACHE_DIR));
        } catch (NullPointerException ex) {
            throw new InvalidConfigurationException("Cache directory not specified");
        }
        if (!(cacheDirectory.isDirectory() && cacheDirectory.canWrite())) {
            throw new InvalidConfigurationException("Cache directory is invalid.");
        }
        codesearchConfiguration.setCacheDirectory(cacheDirectory);
    }

    private void loadIndexLocation() throws InvalidConfigurationException {
        String indexLocation = null;
        try {
            indexLocation = config.getString(XmlConfigurationReaderConstants.INDEX_DIR);
        } catch (NullPointerException ex) {
            throw new InvalidConfigurationException("Index location not specified");
        }
        File index = new File(indexLocation);
        if (!(index.isDirectory() && index.canWrite())) {
            throw new InvalidConfigurationException("Index location \""+indexLocation+"\" is invalid.");
        }
        codesearchConfiguration.setIndexLocation(index);
    }

    private void loadSearcherLocation() throws InvalidConfigurationException {
        try {
            URI searcherLocation = new URI(config.getString(XmlConfigurationReaderConstants.SEARCHER_LOCATION));
            codesearchConfiguration.setSearcherLocation(searcherLocation);
        } catch (URISyntaxException ex) {
            throw new InvalidConfigurationException("Searcher location is not a valid URI: " + ex);
        }
    }

    private void loadRepositoryGroups() throws InvalidConfigurationException {
        List<String> repositoryGroups = Arrays.asList(config.getString(XmlConfigurationReaderConstants.REPOSITORY_GROUP_LIST).split(" "));
        codesearchConfiguration.setRepositoryGroups(repositoryGroups);
    }

    private void loadRepositories() throws InvalidConfigurationException {
        propertiesManager = new PropertiesManager(getIndexLocation() + File.separator + IndexConstants.REVISIONS_PROPERTY_FILENAME);
        LinkedList<RepositoryDto> repositories = new LinkedList<RepositoryDto>();
        List<HierarchicalConfiguration> repositoryConfigs = config.configurationsAt(XmlConfigurationReaderConstants.REPOSITORY_LIST);
        for (HierarchicalConfiguration repositoryConfig : repositoryConfigs) {
            repositories.add(loadRepository(repositoryConfig));
        }
        codesearchConfiguration.setRepositories(repositories);
    }

    /**
     * retrieves all required data about the given repository from the configuration via the HierarchicalConfiguration and returns it as a
     * RepositoryDto
     */
    private RepositoryDto loadRepository(HierarchicalConfiguration hc) {
        RepositoryDto repo = new RepositoryDto();
        // retrieve the repository blacklisted filenames and add all global filenames
        String name = hc.getString(XmlConfigurationReaderConstants.REPOSITORY_NAME);
        List<String> blacklistEntries = hc.getList(XmlConfigurationReaderConstants.REPOSITORY_BLACKLIST);
        if (blacklistEntries == null) {
            blacklistEntries = new LinkedList<String>();
        }
        blacklistEntries.addAll(getGlobalBlacklistEntries());

        // retrieve the repository whitelisted filenames and add all global filenames
        List<String> whitelistFileNames = hc.getList(XmlConfigurationReaderConstants.REPOSITORY_WHITELIST_FILENAMES);
        if (whitelistFileNames == null) {
            whitelistFileNames = new LinkedList<String>();
        }
        whitelistFileNames.addAll(getGlobalWhitelistEntries());

        List<String> repositoryGroups = hc.getList(XmlConfigurationReaderConstants.REPOSITORY_GROUPS);
        if (repositoryGroups == null) {
            repositoryGroups = new LinkedList<String>();
        }

        // retrieve the used authentication system and fill it with the required data
        AuthenticationType usedAuthentication = null;
        String authenticationType = hc.getString(XmlConfigurationReaderConstants.REPOSITORY_AUTHENTICATION_DATA);

        if (authenticationType == null || authenticationType.trim().isEmpty() || authenticationType.equals("none")) {
            usedAuthentication = new NoAuthentication();
        } else if (authenticationType.equals("basic")) {
            String username = hc.getString(XmlConfigurationReaderConstants.REPOSITORY_AUTHENTICATION_DATA_USERNAME);
            String password = hc.getString(XmlConfigurationReaderConstants.REPOSITORY_AUTHENTICATION_DATA_PASSWORD);
            usedAuthentication = new BasicAuthentication(username, password);
        } else if (authenticationType.equals("ssh")) {
            String sshFilePath = hc.getString(XmlConfigurationReaderConstants.REPOSITORY_AUTHENTICATION_DATA_SSH_FILE_PATH);
            usedAuthentication = new SshAuthentication(sshFilePath);
        }

        String indexedRevision = propertiesManager.getPropertyFileValue(name);
        repo = new RepositoryDto(name, hc.getString(XmlConfigurationReaderConstants.REPOSITORY_URL), usedAuthentication, hc
                .getBoolean(XmlConfigurationReaderConstants.REPOSITORY_CODE_NAVIGATION_ENABLED), hc
                .getString(XmlConfigurationReaderConstants.REPOSITORY_VCS), blacklistEntries, whitelistFileNames, repositoryGroups, indexedRevision);
        LOG.info("reading repository from configuration: " + repo.toString());
        return repo;
    }

    private void loadJobs() throws InvalidConfigurationException {
        List<JobDto> jobs = new LinkedList<JobDto>();
        // read the configuration for the jobs from the config
        List<HierarchicalConfiguration> jobConfig = config.configurationsAt(XmlConfigurationReaderConstants.INDEX_JOB);
        for (HierarchicalConfiguration hc : jobConfig) {
            // reads job specific values and adds them to the JobDto
            // FIXME test this, here was a try catch nullpointerexception
            JobDto job = new JobDto();
            String cronExpression;
            try {
                cronExpression = hc.getString(XmlConfigurationReaderConstants.JOB_CRON_EXPRESSION);
            } catch (NoSuchElementException ex) {
                // in case no interval was specified the job is set to execute only once
                cronExpression = "";
            }

            job.setCronExpression(cronExpression);

            String repositoryString = hc.getString(XmlConfigurationReaderConstants.JOB_REPOSITORY);
            // The list of repositories this job is associated with, each task specified in the configuration is created for each of
            // these repositories
            List<RepositoryDto> repositoriesForJob = getRepositoriesByNames(repositoryString);
            job.setRepositories(repositoriesForJob);
            boolean clearIndex = hc.getBoolean(XmlConfigurationReaderConstants.JOB_CLEAR);
            job.setClearIndex(clearIndex);
            jobs.add(job);
        }
        codesearchConfiguration.setJobs(jobs);
    }

    private void loadIndexerUsers() {
        List<IndexerUserDto> indexerUsers = new LinkedList<IndexerUserDto>();
        List<HierarchicalConfiguration> userConfig = config.configurationsAt(XmlConfigurationReaderConstants.INDEXER_USERS);
        for (HierarchicalConfiguration hc : userConfig) {
            IndexerUserDto userDto = new IndexerUserDto();
            userDto.setUserName(hc.getString(XmlConfigurationReaderConstants.USERNAME));
            userDto.setPassword((hc.getString(XmlConfigurationReaderConstants.PASSWORD)));
            indexerUsers.add(userDto);
        }
        codesearchConfiguration.setIndexerUsers(indexerUsers);
    }

    private void loadDatabaseConfiguration() {
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
        List<HierarchicalConfiguration> dbConfig = config.configurationsAt(XmlConfigurationReaderConstants.DB_SECTION);
        for (HierarchicalConfiguration hc : dbConfig) {
            databaseConfiguration.setHostName(hc.getString(XmlConfigurationReaderConstants.DB_HOSTNAME));
            databaseConfiguration.setPort(hc.getInt(XmlConfigurationReaderConstants.DB_PORT_NUMBER));
            databaseConfiguration.setDriver(hc.getString(XmlConfigurationReaderConstants.DB_DRIVER));
            databaseConfiguration.setDatabase(hc.getString(XmlConfigurationReaderConstants.DB_DATABASE));
            databaseConfiguration.setUsername(hc.getString(XmlConfigurationReaderConstants.DB_USERNAME));
            databaseConfiguration.setPassword(hc.getString(XmlConfigurationReaderConstants.DB_PASSWORD));
            databaseConfiguration.setMaxConnections(hc.getInt(XmlConfigurationReaderConstants.DB_MAX_CONNECTIONS));
            databaseConfiguration.setProtocol(hc.getString(XmlConfigurationReaderConstants.DB_PROTOCOL));
        }
        codesearchConfiguration.setDatabaseConfiguration(databaseConfiguration);
    }

    private void loadGlobalWhitelist() {
        List globalWhitelist = config.getList(XmlConfigurationReaderConstants.GLOBAL_WHITELIST);
        if (globalWhitelist == null) {
            globalWhitelist = new LinkedList<String>();
        }
        codesearchConfiguration.setGlobalWhitelist(globalWhitelist);
    }

    private void loadGlobalBlacklist() {
        List globalBlacklist = config.getList(XmlConfigurationReaderConstants.GLOBAL_BLACKLIST);
        if (globalBlacklist == null) {
            globalBlacklist = new LinkedList<String>();
        }
        codesearchConfiguration.setGlobalBlacklist(globalBlacklist);
    }

    /**
     * Takes a string of repository names and returns a {@link List} of the corresponding {@link RepositoryDto} objects.
     *
     * @param repositoryNames The names of the repositories in a single string separated by spaces.
     * @return the {@link List} of {@link RepositoryDto}s.
     */
    private List<RepositoryDto> getRepositoriesByNames(String repositoryNames) {
        if (repositoryNames == null) {
            return getRepositories();
        }
        List<RepositoryDto> repos = new LinkedList<RepositoryDto>();
        for (String name : repositoryNames.split(" ")) {
            for (RepositoryDto repositoryDto : codesearchConfiguration.getRepositories()) {
                if (repositoryDto.getName().equals(name)) {
                    repos.add(repositoryDto);
                }
            }
        }
        return repos;
    }

}
