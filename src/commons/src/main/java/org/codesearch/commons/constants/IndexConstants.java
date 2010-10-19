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

package org.codesearch.commons.constants;

import org.apache.lucene.util.Version;

/**
 * @author Stephan Stiboller
 * @author David Froehlich
 */
public final class IndexConstants {
      /** Lucene version used */
    public static final Version LUCENE_VERSION = Version.LUCENE_30;
    public static final String INDEX_FIELD_REPOSITORY="repository";
    public static final String INDEX_FIELD_FILEPATH="filepath";
    public static final String INDEX_FIELD_CONTENT="content";
    public static final String INDEX_FIELD_TITLE="title";
    public static final String INDEX_FIELD_REVISION="revision";
    public static final String INDEX_FILED_REPOSITORY_GROUP = "repository-groups";
    public static final String INDEX_FIELD_FILEPATH_LC="filepath_lc";
    public static final String INDEX_FIELD_CONTENT_LC="content_lc";
    public static final String INDEX_FIELD_TITLE_LC="title_lc";
    public static final String INDEX_FIELD_FILE_TYPE = "file_type";
    
    private IndexConstants(){
        
    }
}
