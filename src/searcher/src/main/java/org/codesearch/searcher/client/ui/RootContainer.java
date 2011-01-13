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
package org.codesearch.searcher.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The container of all views. Allows to define UI that shows up everywhere.
 * @author Samuel Kogler
 * @author Stiboller Stephan
 */
public class RootContainer extends Composite implements AcceptsOneWidget {

    private static RootContainerUiBinder uiBinder = GWT.create(RootContainerUiBinder.class);

    interface RootContainerUiBinder extends UiBinder<Widget, RootContainer> {
    }
    @UiField
    LayoutPanel contentPanel;
    @UiField
    LayoutPanel errorPanel;

    public RootContainer() {
        initWidget(uiBinder.createAndBindUi(this));
        errorPanel.setVisible(true);
        errorPanel.add(createErrorPanel("lololol"));

    }

    /** {@inheritDoc} */
    @Override
    public void setWidget(IsWidget w) {
        contentPanel.clear();
        Widget widget = Widget.asWidgetOrNull(w);
        if (widget != null) {
            contentPanel.add(w);
        }
    }

    /**
     * Creates an error panel display for the given errorMessage
     * @param String errorMessage to be displayed
     * @return VerticalPanel the error display
     */
    public HorizontalPanel createErrorPanel(String errorMessage) {
        //Image icon = new Image("images/error_msg_close.png");
        Label label = new Label();
        label.setText(errorMessage);
        label.setVisible(true);
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.getElement().setId("errorMessageContainer");
        horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        horizontalPanel.add(label);
        horizontalPanel.setSpacing(3);
        horizontalPanel.setVisible(true);
        return horizontalPanel;
    }
}
