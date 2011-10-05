/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.visualization.components;

import com.connectina.swing.fontchooser.JFontChooser;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.java.dev.colorchooser.ColorChooser;
import org.gephi.ui.components.JColorButton;
import org.gephi.ui.components.JDropDownButton;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.vizmodel.TextModel;
import org.gephi.visualization.api.vizmodel.VizConfig;
import org.gephi.visualization.api.vizmodel.VizModel;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class VizBarController {

    private VizToolbarGroup[] groups;
    private VizToolbar toolbar;
    private VizExtendedBar extendedBar;

    public VizBarController() {
        createDefaultGroups();
    }

    private void createDefaultGroups() {
        groups = new VizToolbarGroup[5];

        groups[0] = new GlobalGroupBar();
        groups[1] = new NodeGroupBar();
        groups[2] = new EdgeGroupBar();
        groups[3] = new LabelGroupBar();
        groups[4] = new BackgroundGroupBar();

        VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
        vizModel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("init")) {
                    VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                    toolbar.setEnable(!vizModel.isDefaultModel());
                    ((NodeGroupBar)groups[1]).setModelValues(vizModel);
                    ((EdgeGroupBar)groups[2]).setModelValues(vizModel);
                    ((LabelGroupBar)groups[3]).setModelValues(vizModel);
                }
            }
        });
    }

    public VizToolbar getToolbar() {
        VizModel model = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
        toolbar = new VizToolbar(groups);
        toolbar.setEnable(!model.isDefaultModel());
        return toolbar;
    }

    public VizExtendedBar getExtendedBar() {
        extendedBar = new VizExtendedBar(groups);
        return extendedBar;
    }

    private static class GlobalGroupBar implements VizToolbarGroup {

        public String getName() {
            return "Global";
        }

        public JComponent[] getToolbarComponents() {
            JComponent[] components = new JComponent[2];

            //Background color
            VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
            final JButton backgroundColorButton = new JColorButton(vizModel.getBackground().getColor());
            backgroundColorButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.background"));
            backgroundColorButton.addPropertyChangeListener(JColorButton.EVENT_COLOR, new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                    if (!vizModel.getBackground().getColor().equals(((JColorButton) backgroundColorButton).getColor())) {
                        vizModel.setBackground(vizModel.getBackground().deriveBackground(((JColorButton) backgroundColorButton).getColor()));
                    }
                }
            });
            vizModel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(VizConfig.BACKGROUND)) {
                        VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                        if (!vizModel.getBackground().getColor().equals(((JColorButton) backgroundColorButton).getColor())) {
                            ((JColorButton) backgroundColorButton).setColor(vizModel.getBackground().getColor());
                        }
                    }
                }
            });
            components[0] = backgroundColorButton;

            //Screenshots
            JPopupMenu screenshotPopup = new JPopupMenu();
            JMenuItem configureScreenshotItem = new JMenuItem(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.screenshot.configure"));
            configureScreenshotItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ScreenshotSettingsPanel panel = new ScreenshotSettingsPanel();
                    panel.setup();
                    ValidationPanel validationPanel = ScreenshotSettingsPanel.createValidationPanel(panel);
                    if (validationPanel.showOkCancelDialog(NbBundle.getMessage(VizBarController.class, "ScreenshotMaker.configure.title"))) {
                        panel.unsetup();
                        return;
                    }
                }
            });
            screenshotPopup.add(configureScreenshotItem);
            final JButton screenshotButton = new JDropDownButton(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/visualization/components/screenshot.png")), screenshotPopup);
            screenshotButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Global.screenshot"));
            screenshotButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Lookup.getDefault().lookup(RenderingController.class).makeScreenshot(); 
                }
            });
            components[1] = screenshotButton;

            return components;
        }

        public JComponent getExtendedComponent() {
            GlobalSettingsPanel panel = new GlobalSettingsPanel();
            panel.setup();
            return panel;
        }

        public boolean hasToolbar() {
            return true;
        }

        public boolean hasExtended() {
            return true;
        }
    }
    
    private static class BackgroundGroupBar implements VizToolbarGroup {

        @Override
        public String getName() {
            return "Background";
        }

        @Override
        public JComponent[] getToolbarComponents() {
            return null;
        }

        @Override
        public JComponent getExtendedComponent() {
            BackgroundSettingsPanel panel = new BackgroundSettingsPanel();
            panel.setup();
            return panel;
        }

        @Override
        public boolean hasToolbar() {
            return false;
        }

        @Override
        public boolean hasExtended() {
            return true;
        }
        
        
    }

    private static class NodeGroupBar implements VizToolbarGroup {

        JComponent[] components = new JComponent[2];

        public String getName() {
            return "Nodes";
        }

        public void setModelValues(VizModel vizModel) {
            ((JToggleButton) components[0]).setSelected(vizModel.getTextModel().isShowNodeLabels());
            ((JToggleButton) components[1]).setSelected(vizModel.isShowHulls());
        }

        public JComponent[] getToolbarComponents() {
            //Show labels buttons
            VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
            final JToggleButton showLabelsButton = new JToggleButton();
            showLabelsButton.setSelected(vizModel.getTextModel().isShowNodeLabels());
            showLabelsButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Nodes.showLabels"));
            showLabelsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/visualization/components/showNodeLabels.png")));
            showLabelsButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                    vizModel.getTextModel().setShowNodeLabels(showLabelsButton.isSelected());
                }
            });
            vizModel.getTextModel().addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    TextModel textModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel().getTextModel();
                    if (showLabelsButton.isSelected() != textModel.isShowNodeLabels()) {
                        showLabelsButton.setSelected(textModel.isShowNodeLabels());
                    }
                }
            });
            components[0] = showLabelsButton;

            //Show hulls
            final JToggleButton showHullsButton = new JToggleButton();
            showHullsButton.setSelected(vizModel.isShowHulls());
            showHullsButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Nodes.showHulls"));
            showHullsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/visualization/components/showHulls.png")));
            showHullsButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                    vizModel.setShowHulls(showHullsButton.isSelected());
                }
            });
            vizModel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("showHulls")) {
                        VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                        if (showHullsButton.isSelected() != vizModel.isShowHulls()) {
                            showHullsButton.setSelected(vizModel.isShowHulls());
                        }
                    }
                }
            });
            components[1] = showHullsButton;

            return components;
        }

        public JComponent getExtendedComponent() {
            NodeSettingsPanel panel = new NodeSettingsPanel();
            panel.setup();
            return panel;
        }

        public boolean hasToolbar() {
            return true;
        }

        public boolean hasExtended() {
            return true;
        }
    }

    private static class EdgeGroupBar implements VizToolbarGroup {

        JComponent[] components = new JComponent[4];

        public String getName() {
            return "Edges";
        }

        public void setModelValues(VizModel vizModel) {
            //((JToggleButton) components[0]).setSelected(vizModel.isShowEdges());
            //((JToggleButton) components[1]).setSelected(!vizModel.isEdgeHasUniColor());
            ((JToggleButton) components[2]).setSelected(vizModel.getTextModel().isShowEdgeLabels());
            ((JSlider) components[3]).setValue((int) ((vizModel.getEdgeScale() - 0.1f) * 10));
        }

        public JComponent[] getToolbarComponents() {
            //Show edges buttons
            VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
            final JToggleButton showEdgeButton = new JToggleButton();
            showEdgeButton.setSelected(vizModel.isShowEdges());
            showEdgeButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.showEdges"));
            showEdgeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/visualization/components/showEdges.png")));
            showEdgeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                    vizModel.setShowEdges(showEdgeButton.isSelected());
                }
            });
            vizModel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("showEdges")) {
                        VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                        if (showEdgeButton.isSelected() != vizModel.isShowEdges()) {
                            showEdgeButton.setSelected(vizModel.isShowEdges());
                        }
                    }
                }
            });
            components[0] = showEdgeButton;

            //Edge color mode
            final JToggleButton edgeHasNodeColorButton = new JToggleButton();
            edgeHasNodeColorButton.setSelected(!vizModel.isEdgeHasUniColor());
            edgeHasNodeColorButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.edgeNodeColor"));
            edgeHasNodeColorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/visualization/components/edgeNodeColor.png")));
            edgeHasNodeColorButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                    vizModel.setEdgeHasUniColor(!edgeHasNodeColorButton.isSelected());
                }
            });
            vizModel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("edgeHasUniColor")) {
                        VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                        if (edgeHasNodeColorButton.isSelected() != !vizModel.isEdgeHasUniColor()) {
                            edgeHasNodeColorButton.setSelected(!vizModel.isEdgeHasUniColor());
                        }
                    }
                }
            });
            components[1] = edgeHasNodeColorButton;


            //Show labels buttons
            final JToggleButton showLabelsButton = new JToggleButton();
            showLabelsButton.setSelected(vizModel.getTextModel().isShowEdgeLabels());
            showLabelsButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.showLabels"));
            showLabelsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/visualization/components/showEdgeLabels.png")));
            showLabelsButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                    vizModel.getTextModel().setShowEdgeLabels(showLabelsButton.isSelected());
                }
            });
            vizModel.getTextModel().addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    TextModel textModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel().getTextModel();
                    if (showLabelsButton.isSelected() != textModel.isShowEdgeLabels()) {
                        showLabelsButton.setSelected(textModel.isShowEdgeLabels());
                    }
                }
            });
            components[2] = showLabelsButton;

            //EdgeScale slider
            final JSlider edgeScaleSlider = new JSlider(0, 100, (int) ((vizModel.getEdgeScale() - 0.1f) * 10));
            edgeScaleSlider.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Edges.edgeScale"));
            edgeScaleSlider.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                    if (vizModel.getEdgeScale() != (edgeScaleSlider.getValue() / 10f + 0.1f)) {
                        vizModel.setEdgeScale(edgeScaleSlider.getValue() / 10f + 0.1f);
                    }
                }
            });
            edgeScaleSlider.setPreferredSize(new Dimension(100, 20));
            edgeScaleSlider.setMaximumSize(new Dimension(100, 20));
            vizModel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("edgeScale")) {
                        VizModel vizModel = Lookup.getDefault().lookup(VisualizationController.class).getVizModel();
                        if (vizModel.getEdgeScale() != (edgeScaleSlider.getValue() / 10f + 0.1f)) {
                            edgeScaleSlider.setValue((int) ((vizModel.getEdgeScale() - 0.1f) * 10));
                        }
                    }
                }
            });
            components[3] = edgeScaleSlider;
            return components;
        }

        public JComponent getExtendedComponent() {
            EdgeSettingsPanel panel = new EdgeSettingsPanel();
            panel.setup();
            return panel;
        }

        public boolean hasToolbar() {
            return true;
        }

        public boolean hasExtended() {
            return true;
        }
    }

    private static class LabelGroupBar implements VizToolbarGroup {

        JComponent[] components = new JComponent[6];

        public String getName() {
            return "Labels";
        }

        public void setModelValues(VizModel vizModel) {
            TextModel model = vizModel.getTextModel();
            //((JPopupButton) components[0]).setSelectedItem(model.getSizeMode());
            //((JPopupButton) components[1]).setSelectedItem(model.getColorMode());
            ((JButton) components[2]).setText(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
            ((JSlider) components[3]).setValue((int) (model.getNodeSizeFactor() * 100f));
        }

        public JComponent[] getToolbarComponents() {          
            TextModel model = Lookup.getDefault().lookup(VisualizationController.class).getVizModel().getTextModel();
            
            // TODO remove when label modes included
            components[0] = new JToggleButton();
            components[1] = new JToggleButton();
            
            //Mode
            /*
            final JPopupButton labelSizeModeButton = new JPopupButton();
            TextManager textManager = VizController.getInstance().getTextManager();
            for (final SizeMode sm : textManager.getSizeModes()) {
                labelSizeModeButton.addItem(sm, sm.getIcon());
            }
            labelSizeModeButton.setSelectedItem(model.getSizeMode());
            labelSizeModeButton.setChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    SizeMode sm = (SizeMode) e.getSource();
                    TextModel model = VizController.getInstance().getVizModel().getTextModel();
                    model.setSizeMode(sm);
                }
            });
            labelSizeModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/visualization/components/labelSizeMode.png")));
            labelSizeModeButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.sizeMode"));
            model.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    TextModel model = VizController.getInstance().getVizModel().getTextModel();
                    if (model.getSizeMode() != labelSizeModeButton.getSelectedItem()) {
                        labelSizeModeButton.setSelectedItem(model.getSizeMode());
                    }
                }
            });
            components[0] = labelSizeModeButton;

            //Color mode
            final JPopupButton labelColorModeButton = new JPopupButton();
            for (final ColorMode cm : textManager.getColorModes()) {
                labelColorModeButton.addItem(cm, cm.getIcon());
            }
            labelColorModeButton.setSelectedItem(textManager.getModel().getColorMode());
            labelColorModeButton.setChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    ColorMode cm = (ColorMode) e.getSource();
                    TextModel model = VizController.getInstance().getVizModel().getTextModel();
                    model.setColorMode(cm);
                }
            });
            labelColorModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/visualization/components/labelColorMode.png")));
            labelColorModeButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.colorMode"));
            model.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    TextModel model = VizController.getInstance().getVizModel().getTextModel();
                    if (model.getColorMode() != labelColorModeButton.getSelectedItem()) {
                        labelColorModeButton.setSelectedItem(model.getColorMode());
                    }
                }
            });
            components[1] = labelColorModeButton;
*/
            //Font
            final JButton fontButton = new JButton(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
            fontButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.font"));
            fontButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    TextModel model = Lookup.getDefault().lookup(VisualizationController.class).getVizModel().getTextModel();
                    Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getNodeFont());
                    if (font != null && font != model.getNodeFont()) {
                        model.setNodeFont(font);
                    }
                }
            });
            model.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    TextModel model = Lookup.getDefault().lookup(VisualizationController.class).getVizModel().getTextModel();
                    fontButton.setText(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
                }
            });
            components[2] = fontButton;

            //Font size
            final JSlider fontSizeSlider = new JSlider(0, 100, (int) (model.getNodeSizeFactor() * 100f));
            fontSizeSlider.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.fontScale"));
            fontSizeSlider.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    TextModel model = Lookup.getDefault().lookup(VisualizationController.class).getVizModel().getTextModel();
                    model.setNodeSizeFactor(fontSizeSlider.getValue() / 100f);
                }
            });
            fontSizeSlider.setPreferredSize(new Dimension(100, 20));
            fontSizeSlider.setMaximumSize(new Dimension(100, 20));
            model.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    TextModel model = Lookup.getDefault().lookup(VisualizationController.class).getVizModel().getTextModel();
                    if (((int) (model.getNodeSizeFactor() * 100f)) != fontSizeSlider.getValue()) {
                        fontSizeSlider.setValue((int) (model.getNodeSizeFactor() * 100f));
                    }
                }
            });
            components[3] = fontSizeSlider;

            //Color
            final ColorChooser colorChooser = new ColorChooser(model.getNodeColor());
            colorChooser.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.defaultColor"));
            colorChooser.setPreferredSize(new Dimension(16, 16));
            colorChooser.setMaximumSize(new Dimension(16, 16));
            colorChooser.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(ColorChooser.PROP_COLOR)) {
                        TextModel model = Lookup.getDefault().lookup(VisualizationController.class).getVizModel().getTextModel();
                        model.setNodeColor(colorChooser.getColor());
                    }
                }
            });
            model.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    TextModel model = Lookup.getDefault().lookup(VisualizationController.class).getVizModel().getTextModel();
                    if (!model.getNodeColor().equals(colorChooser.getColor())) {
                        colorChooser.setColor(model.getNodeColor());
                    }
                }
            });
            components[4] = colorChooser;

            //Attributes
            final JButton attributesButton = new JButton();
            attributesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/visualization/components/configureLabels.png")));
            attributesButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "VizToolbar.Labels.attributes"));
            attributesButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    TextModel model = Lookup.getDefault().lookup(VisualizationController.class).getVizModel().getTextModel();
                    LabelAttributesPanel panel = new LabelAttributesPanel();
                    panel.setup(model);
                    DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(VizBarController.class, "LabelAttributesPanel.title"), true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
                    if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                        panel.unsetup();
                        return;
                    }
                }
            });
            components[5] = attributesButton;

            return components;
        }

        public JComponent getExtendedComponent() {
            LabelSettingsPanel panel = new LabelSettingsPanel();
            panel.setup();
            return panel;
        }

        public boolean hasToolbar() {
            return true;
        }

        public boolean hasExtended() {
            return true;
        }
    }
}