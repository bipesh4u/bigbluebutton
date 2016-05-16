/**
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 * <p>
 * Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * <p>
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along
 * with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
 */

package org.bigbluebutton.presentation.imp;

@SuppressWarnings("serial")
public class CountingPageException extends Exception {

    private final int maxNumberOfPages;
    private final ExceptionType exceptionType;
    private final int pageCount;

    public enum ExceptionType {PAGE_COUNT_EXCEPTION, PAGE_EXCEEDED_EXCEPTION}

    ;

    public CountingPageException(ExceptionType type, int pageCount, int maxNumberOfPages) {
        super("Exception while trying to determine number of pages.");
        this.pageCount = pageCount;
        this.maxNumberOfPages = maxNumberOfPages;
        exceptionType = type;
    }

    public int getMaxNumberOfPages() {
        return maxNumberOfPages;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    public int getPageCount() {
        return pageCount;
    }
}
