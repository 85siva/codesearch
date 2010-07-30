/**
 * Copyright 2010 Samuel Kogler <samuel.kogler@gmail.com>
 *
 * This file is part of Scrumbeard.
 *
 * Scrumbeard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Scrumbeard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Scrumbeard.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.scrumbeard.shared;

/**
 *
 * @author daasdingo
 */
class Task {
    private String text;
    private TeamMember assignedTo;

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the assignedTo
     */
    public TeamMember getAssignedTo() {
        return assignedTo;
    }

    /**
     * @param assignedTo the assignedTo to set
     */
    public void setAssignedTo(TeamMember assignedTo) {
        this.assignedTo = assignedTo;
    }
}
