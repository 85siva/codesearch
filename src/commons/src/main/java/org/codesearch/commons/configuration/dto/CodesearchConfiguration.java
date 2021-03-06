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
package org.codesearch.commons.configuration.dto;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple bean representing the configuration of codesearch.
 *
 * @author Samuel Kogler
 */
public class CodesearchConfiguration {

    private URI searcherLocation;
    private File cacheDirectory;
    private File indexLocation;
    private List<JobDto> jobs = new LinkedList<JobDto>();
    private List<RepositoryDto> repositories = new LinkedList<RepositoryDto>();
    private List<String> globalWhitelist = new LinkedList<String>();
    private List<String> globalBlacklist = new LinkedList<String>();
    private List<String> repositoryGroups = new LinkedList<String>();

    public URI getSearcherLocation() {
        return searcherLocation;
    }

    public void setSearcherLocation(URI searcherLocation) {
        this.searcherLocation = searcherLocation;
    }

    public File getCacheDirectory() {
        return cacheDirectory;
    }

    public void setCacheDirectory(File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    public File getIndexLocation() {
        return indexLocation;
    }

    public void setIndexLocation(File indexLocation) {
        this.indexLocation = indexLocation;
    }

    public List<JobDto> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobDto> jobs) {
        this.jobs = jobs;
    }

    public List<RepositoryDto> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RepositoryDto> repositories) {
        this.repositories = repositories;
    }

    public List<String> getGlobalWhitelist() {
        return globalWhitelist;
    }

    public void setGlobalWhitelist(List<String> globalWhitelist) {
        this.globalWhitelist = globalWhitelist;
    }

    public List<String> getGlobalBlacklist() {
        return globalBlacklist;
    }

    public void setGlobalBlacklist(List<String> globalBlacklist) {
        this.globalBlacklist = globalBlacklist;
    }

    public List<String> getRepositoryGroups() {
        return repositoryGroups;
    }

    public void setRepositoryGroups(List<String> repositoryGroups) {
        this.repositoryGroups = repositoryGroups;
    }
}
