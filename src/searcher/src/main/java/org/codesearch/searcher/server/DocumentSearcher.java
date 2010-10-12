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
package org.codesearch.searcher.server;

import org.codesearch.searcher.shared.InvalidIndexLocationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.codesearch.commons.configuration.xml.ConfigReaderConstants;
import org.codesearch.commons.configuration.xml.XmlConfigurationReader;
import org.codesearch.commons.constants.IndexConstants;
import org.codesearch.searcher.shared.SearchResultDto;

/**
 * Provides methods to search the index.
 * @author David Froehlich
 */
public class DocumentSearcher {

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(DocumentSearcher.class);
    /** The parser used for parsing search terms to lucene queries */
    private QueryParser queryParser;
    /** The searcher used for searching the lucene index */
    private IndexSearcher indexSearcher;
    /** Whether the searcher has been initialized. **/
    private boolean searcherInitialized = false;
    /** The location of the index. **/
    private String indexLocation;
    
    private XmlConfigurationReader configReader = new XmlConfigurationReader();

    
    /**
     * Creates a new DocumentSearcher instance
     * @throws ConfigurationException if no value for the key specified in the constant INDEX_LOCATION_KEY could be found in the in the configuration via the XmlConfigurationReader
     * @throws IOException if the index could not be opened
     */
    public DocumentSearcher(XmlConfigurationReader configReader) throws ConfigurationException {
        // Retrieve index location from the configuration
        indexLocation = configReader.getSingleLinePropertyValue(ConfigReaderConstants.INDEX_LOCATION);
        LOG.debug("Index location set to: " + indexLocation);
        //TODO replace with appropriate Analyzer
        queryParser = new QueryParser(Version.LUCENE_30, "", new WhitespaceAnalyzer());
        try {
            initSearcher();
        } catch (InvalidIndexLocationException ex) {
            LOG.warn(ex);
            LOG.warn("Will try to re-initialize searcher at the first search operation");
        }
        LOG.debug("DocumentSearcher created");
    }

    /**
     * Performs a search against the lucene index and returns each result as an ResultItem
     * @param searchString the String that will be parsed to a query
     * @return the results as ResultItems
     * @throws ParseException if the searchString could not be parsed to a query
     * @throws IOException if the Index could not be read
     */
    public List<SearchResultDto> search(String searchString, boolean caseSensitive, List<String> repositoryNames, List<String> repositoryGroupNames) throws ParseException, IOException, InvalidIndexLocationException{
        if (!searcherInitialized) {
            initSearcher();
        }
        List<SearchResultDto> results = new LinkedList<SearchResultDto>();
        Query query;
        try {
            query = queryParser.parse(this.parseQuery(searchString, caseSensitive, repositoryNames, repositoryGroupNames));
        } catch (ConfigurationException ex) {
            //TODO add handling for exception
            throw new NotImplementedException();
        }
        LOG.info("Searching index with query: " + query.toString());
        //Retrieve all search results from search
        TopDocs topDocs = indexSearcher.search(query, 10000);
        LOG.info("Found " + topDocs.scoreDocs.length + " results");
        Document doc;
        //Add each search result in form of a ResultItem to the results-list
        for (ScoreDoc sd : topDocs.scoreDocs) {
            doc = indexSearcher.doc(sd.doc);
            SearchResultDto searchResult = new SearchResultDto();
            searchResult.setRepository(doc.get(IndexConstants.INDEX_FIELD_REPOSITORY));
            searchResult.setFilePath(doc.get(IndexConstants.INDEX_FIELD_FILEPATH));
            searchResult.setRevision(doc.get(IndexConstants.INDEX_FIELD_REVISION));
            searchResult.setRelevance(sd.score);
            results.add(searchResult);
        }
        return results;
    }

    /**
     * parses the search term to a lucene conform query keeping in mind the several options
     * @param term the search term
     * @return the lucene conform query
     */
    public String parseQuery(String term, boolean caseSensitive, List<String> repositoryNames, List<String> repositoryGroupNames) throws ConfigurationException { //TODO rename, same name as lucene and make private after finished testing
        String query = "";

        if (term.contains(":")) {
            throw new NotImplementedException();
        }

        if (caseSensitive) {
            query = IndexConstants.INDEX_FIELD_CONTENT+":\"" + term + "\"";
        } else {
            query = IndexConstants.INDEX_FIELD_CONTENT_LC+":\"" + term + "\"";
        }
        query = appendRepositoriesToQuery(query, repositoryNames, repositoryGroupNames);
        return query;
    }

    private String appendRepositoriesToQuery(String query, List<String> repositoryNames, List<String> repositoryGroupNames) throws ConfigurationException{
        if(repositoryGroupNames.isEmpty() && repositoryNames.isEmpty()){
            return query;
        }
        
        query += " AND (";
        for(String repoGroup : repositoryGroupNames){
            for(String repo : configReader.getRepositoriesForGroup(repoGroup)){
                repositoryNames.add(repo);
            }
        }
        for(String repo : repositoryNames){
            query += IndexConstants.INDEX_FIELD_REPOSITORY + ":" + repo + " OR ";
        }
               
        query = query.substring(0, query.length()-4)+")";
        return query;
    }

    private void initSearcher() throws InvalidIndexLocationException {
        try {
            indexSearcher = new IndexSearcher(FSDirectory.open(new File(indexLocation)), true);
            searcherInitialized = true;
        } catch (IOException exc) {
            searcherInitialized = false;
            throw new InvalidIndexLocationException("No valid index found at: " + indexLocation + "\n" + exc);
        }
    }
}
