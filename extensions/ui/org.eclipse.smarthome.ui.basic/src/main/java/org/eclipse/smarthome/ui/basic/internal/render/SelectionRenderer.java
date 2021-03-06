/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.ui.basic.internal.render;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.util.UnitUtils;
import org.eclipse.smarthome.model.sitemap.Mapping;
import org.eclipse.smarthome.model.sitemap.Selection;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.basic.render.RenderException;
import org.eclipse.smarthome.ui.basic.render.WidgetRenderer;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Selection widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - BasicUI changes
 *
 */
@Component(service = WidgetRenderer.class)
public class SelectionRenderer extends AbstractWidgetRenderer {

    private final Logger logger = LoggerFactory.getLogger(SelectionRenderer.class);

    @Override
    @Activate
    protected void activate(BundleContext bundleContext) {
        super.activate(bundleContext);
    }

    @Override
    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        super.deactivate(bundleContext);
    }

    @Override
    public boolean canRender(Widget w) {
        return w instanceof Selection;
    }

    /**
     * Get command-label map for a Selection widget
     *
     * @return String representing JSON object
     */
    private String getMappingsJSON(Selection w) {
        JsonObject resultObject = new JsonObject();
        for (Mapping mapping : w.getMappings()) {
            resultObject.addProperty(mapping.getCmd(), mapping.getLabel());
        }
        String result = resultObject.toString();
        result = StringEscapeUtils.escapeHtml(result);
        return result;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        String snippet = getSnippet("selection");

        snippet = preprocessSnippet(snippet, w);
        snippet = StringUtils.replace(snippet, "%value_map%", getMappingsJSON((Selection) w));
        snippet = StringUtils.replace(snippet, "%label_header%", getLabel(w));

        State state = itemUIRegistry.getState(w);
        Selection selection = (Selection) w;
        String mappingLabel = null;

        Item item = null;
        try {
            item = itemUIRegistry.getItem(w.getItem());
        } catch (ItemNotFoundException e) {
            logger.debug("Failed to retrieve item during widget rendering: {}", e.getMessage());
        }

        StringBuilder rowSB = new StringBuilder();
        for (Mapping mapping : selection.getMappings()) {
            String rowSnippet = getSnippet("selection_row");

            String command = mapping.getCmd() != null ? mapping.getCmd() : "";
            String label = mapping.getLabel();

            if (item instanceof NumberItem && ((NumberItem) item).getDimension() != null) {
                String unit = getUnitForWidget(w);
                command = StringUtils.replace(command, UnitUtils.UNIT_PLACEHOLDER, unit);
                label = StringUtils.replace(label, UnitUtils.UNIT_PLACEHOLDER, unit);

                // Special treatment for °C since uom library uses a single character: ℃
                // This will ensure the current state matches the cmd and the buttonClass is set accordingly.
                command = StringUtils.replace(command, "°C", "℃");
            }

            rowSnippet = StringUtils.replace(rowSnippet, "%item%", w.getItem() != null ? w.getItem() : "");
            rowSnippet = StringUtils.replace(rowSnippet, "%cmd%", escapeHtml(command));
            rowSnippet = StringUtils.replace(rowSnippet, "%label%", label != null ? escapeHtml(label) : "");

            State compareMappingState = state;
            if (state instanceof QuantityType) { // convert the item state to the command value for proper
                                                 // comparison and "checked" attribute calculation
                compareMappingState = convertStateToLabelUnit((QuantityType<?>) state, command);
            }

            if (compareMappingState.toString().equals(command)) {
                mappingLabel = label;
                rowSnippet = StringUtils.replace(rowSnippet, "%checked%", "checked=\"true\"");
            } else {
                rowSnippet = StringUtils.replace(rowSnippet, "%checked%", "");
            }
            rowSB.append(rowSnippet);
        }
        snippet = StringUtils.replace(snippet, "%rows%", rowSB.toString());
        snippet = StringUtils.replace(snippet, "%value_header%", mappingLabel != null ? mappingLabel : "");

        // Process the color tags
        snippet = processColor(w, snippet);

        sb.append(snippet);
        return null;
    }

    @Override
    @Reference
    protected void setItemUIRegistry(ItemUIRegistry ItemUIRegistry) {
        super.setItemUIRegistry(ItemUIRegistry);
    }

    @Override
    protected void unsetItemUIRegistry(ItemUIRegistry ItemUIRegistry) {
        super.unsetItemUIRegistry(ItemUIRegistry);
    }

}
