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
package org.codesearch.commons.plugins.bazaar;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import org.codesearch.commons.plugins.vcs.FileDto;
import org.codesearch.commons.plugins.vcs.VersionControlPlugin;
import org.codesearch.commons.plugins.vcs.VersionControlPluginException;
import org.codesearch.commons.utils.CommonsUtils;
import org.commons.codesearch.utils.bazaar.BazaarUtils;
import org.springframework.stereotype.Component;
import org.vcs.bazaar.client.IBazaarLogMessage;
import org.vcs.bazaar.client.IBazaarStatus;
import org.vcs.bazaar.client.core.BazaarClientException;
import org.vcs.bazaar.client.core.BranchLocation;

/**
 * A plugin used to access files stored in Subversion repositories.
 * @author Stephan Stiboller
 */
@Component
public class BazaarPlugin implements VersionControlPlugin {

    private BazaarUtils bzr_util;
    private BranchLocation bl;

    public BazaarPlugin() {
        bzr_util = BazaarUtils.getInstance();
    }

    @Override
    public void setRepository(URI url, String username, String password) throws VersionControlPluginException {
        try {
            bl = bzr_util.createBranchLocation(url.toString(), username, password);
        } catch (URISyntaxException ex) {
            throw new VersionControlPluginException(ex.toString());
        }
    }

    @Override
    public byte[] getFileContentForFilePath(String filePath) throws VersionControlPluginException {
        try {
            return bzr_util.retrieveFileContent(new URI(filePath), getRepositoryRevision());
        } catch (URISyntaxException ex) {
            throw new VersionControlPluginException(ex.toString());
        } catch (BazaarClientException ex) {
            throw new VersionControlPluginException(ex.toString());
        } catch (IOException ex) {
            throw new VersionControlPluginException(ex.toString());
        }
    }

    @Override
    public Set<FileDto> getChangedFilesSinceRevision(String revision) throws VersionControlPluginException {
        Set<FileDto> files = null;
        try {
            List<IBazaarLogMessage> iblm = bzr_util.getChangesSinceRevison(bl.getURI().toString(), revision);
            for (IBazaarLogMessage log : iblm) {
                for (IBazaarStatus bs : log.getAffectedFiles()) {
                    boolean exists = false;
                    for (FileDto file : files) {
                        if (file.getFilePath().equals(bs.getAbsolutePath())) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        byte[] content = CommonsUtils.convertFileToByteArray(bs.getFile());
                        FileDto fd = new FileDto(bs.getAbsolutePath(), content, false); //TODO: ADD MIME TYPE...
                        files.add(fd);
                    }
                }
            }
        } catch (BazaarClientException ex) {
            throw new VersionControlPluginException(ex.toString());
        } catch (URISyntaxException ex) {
            throw new VersionControlPluginException(ex.toString());
        } catch (Exception ex) {
            throw new VersionControlPluginException(ex.toString());
        }
        return files;
    }

    @Override
    public String getRepositoryRevision() throws VersionControlPluginException {
        try {
            return bzr_util.getLatestRevisionNumber(bl);
        } catch (BazaarClientException ex) {
            throw new VersionControlPluginException(ex.toString());
        }
    }

    @Override
    public String getPurposes() {
        return "BRZ";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}