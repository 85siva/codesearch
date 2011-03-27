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
package org.codesearch.searcher.server.rpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.codesearch.commons.configuration.ConfigurationReader;
import org.codesearch.commons.configuration.xml.XmlConfigurationReader;
import org.codesearch.commons.configuration.xml.dto.RepositoryDto;
import org.codesearch.commons.database.DBAccess;
import org.codesearch.commons.database.DatabaseAccessException;
import org.codesearch.commons.database.DatabaseEntryNotFoundException;
import org.codesearch.commons.plugins.PluginLoader;
import org.codesearch.commons.plugins.PluginLoaderException;
import org.codesearch.commons.plugins.codeanalyzing.ast.AstNode;
import org.codesearch.commons.plugins.codeanalyzing.ast.ExternalUsage;
import org.codesearch.commons.plugins.codeanalyzing.ast.Usage;
import org.codesearch.commons.plugins.highlighting.HighlightingPlugin;
import org.codesearch.commons.plugins.highlighting.HighlightingPluginException;
import org.codesearch.commons.plugins.vcs.VersionControlPlugin;
import org.codesearch.commons.plugins.vcs.VersionControlPluginException;
import org.codesearch.commons.utils.mime.MimeTypeUtil;
import org.codesearch.searcher.client.rpc.SearcherService;
import org.codesearch.searcher.server.DocumentSearcher;
import org.codesearch.searcher.server.InvalidIndexException;
import org.codesearch.searcher.shared.FileDto;
import org.codesearch.searcher.shared.SearcherServiceException;
import org.codesearch.searcher.shared.OutlineNode;
import org.codesearch.searcher.shared.SearchResultDto;
import org.codesearch.searcher.shared.SidebarNode;

/**
 * Service used for search operations.
 * @author Samuel Kogler
 */
@Singleton
public class SearcherServiceImpl extends RemoteServiceServlet implements SearcherService {

    private static final long serialVersionUID = 1389141189614933738L;
    /** The logger. */
    private static final Logger LOG = Logger.getLogger(SearcherServiceImpl.class);
    /** The document searcher used to search the index. */
    private DocumentSearcher documentSearcher;
    private ConfigurationReader configurationReader;
    private PluginLoader pluginLoader;
    private DBAccess dba;
    private List<String> repositories;
    private List<String> repositoryGroups;

    @Inject
    public SearcherServiceImpl(DocumentSearcher documentSearcher, ConfigurationReader configurationReader, PluginLoader pluginLoader, DBAccess dba) {
        this.documentSearcher = documentSearcher;
        this.configurationReader = configurationReader;
        this.pluginLoader = pluginLoader;
        this.dba = dba;
        repositoryGroups = configurationReader.getRepositoryGroups();
        repositories = new LinkedList<String>();
        for (RepositoryDto dto : configurationReader.getRepositories()) {
            if (dto != null) {
                repositories.add(dto.getName());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<SearchResultDto> doSearch(String query, boolean caseSensitive, List<String> selectedRepositories, List<String> selectedRepositoryGroups) throws SearcherServiceException {
        List<SearchResultDto> resultItems = new LinkedList<SearchResultDto>();
        try {
            resultItems = documentSearcher.search(query, caseSensitive, selectedRepositories, selectedRepositoryGroups);
        } catch (ParseException ex) {
            throw new SearcherServiceException("Invalid search query: \n" + ex);
        } catch (IOException ex) {
            throw new SearcherServiceException("Exception searching the index: \n" + ex);
        } catch (InvalidIndexException ex) {
            throw new SearcherServiceException("Invalid index: \n" + ex);
        }
        return resultItems;
    }

    /** {@inheritDoc} */
    @Override
    public FileDto getFile(String repository, String filePath) throws SearcherServiceException {
        LOG.debug("Retrieving file content for file: " + filePath + " @ " + repository);
        FileDto file = new FileDto();
        try {
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            LOG.debug("file name: " + fileName);

            String guessedMimeType = MimeTypeUtil.guessMimeTypeViaFileEnding(fileName);

            LOG.debug("detected mime is: " + guessedMimeType);
            file.setBinary(MimeTypeUtil.isBinaryType(guessedMimeType));

            RepositoryDto repositoryDto = configurationReader.getRepositoryByName(repository);
            VersionControlPlugin vcPlugin = pluginLoader.getPlugin(VersionControlPlugin.class, repositoryDto.getVersionControlSystem());

            vcPlugin.setRepository(repositoryDto);
            org.codesearch.commons.plugins.vcs.FileDto vcFile = vcPlugin.getFileForFilePath(filePath);

            // GET OUTLINE IF EXISTING
            try {
                AstNode fileNode = dba.getBinaryIndexForFile(filePath, repository);
                if (fileNode != null) {
                    List<OutlineNode> outline = new LinkedList<OutlineNode>();
                    for (AstNode a : fileNode.getChildNodes()) {
                        if (a == null) {
                            continue;
                        }
                        outline.add(convertAstNodeToOutlineNode(a));
                    }
                    file.setOutline(outline);
                }

            } catch (DatabaseEntryNotFoundException ex) {
                //in case the file has no binary index it is simply displayed without an outline
            } catch (DatabaseAccessException ex) {
                LOG.error("Could not access database: \n" + ex);
            }
            if (MimeTypeUtil.UNKNOWN.equals(guessedMimeType)) {
                MagicMatch magicMatch;
                try {
                    magicMatch = Magic.getMagicMatch(vcFile.getContent());
                    LOG.debug("MAGIC DETECT: " + magicMatch.getMimeType());
                    if (magicMatch.getMimeType().startsWith("text/")) {
                        file.setBinary(false);
                    }
                } catch (MagicParseException ex) {
                    LOG.debug("NO MAGIC MATCH"); //TODO add a better log entry
                } catch (MagicMatchNotFoundException ex) {
                    LOG.debug("NO MAGIC MATCH");
                } catch (MagicException ex) {
                    LOG.debug("NO MAGIC MATCH");
                }
            }

            try {
                HighlightingPlugin hlPlugin = pluginLoader.getPlugin(HighlightingPlugin.class, guessedMimeType);
                String highlightingEscapeStartToken = hlPlugin.getEscapeStartToken();
                String highlightingEscapeEndToken = hlPlugin.getEscapeEndToken();
                byte[] parsedFileContent = addUsageLinksToFileContent(vcFile.getContent(), filePath, repository, highlightingEscapeStartToken, highlightingEscapeEndToken);
                file.setFileContent(hlPlugin.parseToHtml(parsedFileContent, guessedMimeType));
            } catch (PluginLoaderException ex) {
                // No plugin found, just escape to HTML
                file.setFileContent(StringEscapeUtils.escapeHtml(new String(vcFile.getContent())));
            }
        } catch (HighlightingPluginException ex) {
            LOG.error(ex);
        } catch (VersionControlPluginException ex) {
            throw new SearcherServiceException("Could not get file: \n" + ex);
        } catch (PluginLoaderException ex) {
            throw new SearcherServiceException("Problem loading plugin: \n" + ex);
        }
        LOG.debug("Finished retrieving file content for file: " + filePath + " @ " + repository);
        return file;
    }

    @Override
    public SearchResultDto resolveUsage(int usageId, String repository, String filePath) throws SearcherServiceException {
        LOG.debug("Looking up usage: " + usageId + " in file: " + filePath + "@" + repository);
        try {
            ExternalUsage usage = dba.getUsageForIdInFile(usageId, filePath, repository);
            usage.resolveLink(filePath, repository);
            LOG.debug(usage.getTargetClassName());
            LOG.debug(usage.getTargetFilePath());
            if (usage.getTargetFilePath() != null) {
                SearchResultDto searchResultDto = new SearchResultDto();
                searchResultDto.setFilePath(usage.getTargetFilePath());
                //FIXME maybe add possibility to jump between repositories
                searchResultDto.setRepository(repository);
                return searchResultDto;
            }
        } catch (DatabaseEntryNotFoundException ex) {
            //TODO redirect to current file here, probably return null
        } catch (DatabaseAccessException ex) {
            LOG.error(ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getAvailableRepositoryGroups() {
        return repositoryGroups;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getAvailableRepositories() {
        return repositories;
    }

    public void setDocumentSearcher(DocumentSearcher documentSearcher) {
        this.documentSearcher = documentSearcher;
    }

    public void setXmlConfigurationReader(XmlConfigurationReader xmlConfigurationReader) {
        this.configurationReader = xmlConfigurationReader;
    }

    private OutlineNode convertAstNodeToOutlineNode(AstNode astNode) {
        if (astNode.showInOutline()) {
            OutlineNode outlineNode = new OutlineNode();
            outlineNode.setName(astNode.getOutlineName());
            outlineNode.setStartLine(astNode.getStartLine());
            outlineNode.setCssClasses(astNode.getModifiers());
            List<SidebarNode> childs = new LinkedList<SidebarNode>();
            for (AstNode a : astNode.getChildNodes()) {
                if (a == null) {
                    continue;
                }
                childs.add(convertAstNodeToOutlineNode(a));
            }
            outlineNode.setChilds(childs);
            return outlineNode;
        } else {
            return null;
        }
    }

    private byte[] addUsageLinksToFileContent(byte[] fileContentBytes, String filePath, String repository, String hlEscapeStartToken, String hlEscapeEndToken) {
        try {
            List<Usage> usages;
            try {
                usages = dba.getUsagesForFile(filePath, repository);
            } catch (DatabaseEntryNotFoundException ex) {
                //in case the file does not have a binary index just display it without links
                usages = null;
            }
            String resultString = "";
            if (usages == null) {
                //in case there is no entry for the filePath it is a file that has not been analyzed
                return fileContentBytes;
            }
            String[] contentLines = new String(fileContentBytes).split("\n");
            int usageIndex = 0;
            outer:
            for (int lineNumber = 1; lineNumber <= contentLines.length; lineNumber++) {
                String currentLine = contentLines[lineNumber - 1];
                while (usageIndex < usages.size()) {
                    Usage currentUsage = usages.get(usageIndex);
                    if (currentUsage.getStartLine() == lineNumber) {
                        int startColumn = currentUsage.getStartColumn();
                        String preamble = currentLine.substring(0, startColumn - 1); //-1
                        String javaScriptEvent = "";
                        if (currentUsage instanceof ExternalUsage) {
                            javaScriptEvent = "goToUsage(" + usageIndex + ");";
                        } else {
                            javaScriptEvent = "goToLine(" + currentUsage.getReferenceLine() + ");";
                        }
                        String anchorBegin = hlEscapeStartToken + "<a class='testLink' onclick='" + javaScriptEvent + "'>" + hlEscapeEndToken;
                        String anchorEnd = hlEscapeStartToken + "</a>" + hlEscapeEndToken;
                        String remainingLine = currentLine.substring(startColumn - 1 + currentUsage.getReplacedString().length());
                        currentLine = preamble + anchorBegin + currentUsage.getReplacedString() + anchorEnd + remainingLine;
                        usageIndex++;
                    } else {
                        resultString += currentLine + "\n";
                        continue outer;
                    }
                }
                resultString += currentLine + "\n";
            }
            resultString = resultString.substring(0, resultString.length() - 1); //Truncates the last \n char
            return resultString.getBytes();
        } catch (DatabaseAccessException ex) {
            LOG.error(ex);
        }
        return fileContentBytes;
    }
}
