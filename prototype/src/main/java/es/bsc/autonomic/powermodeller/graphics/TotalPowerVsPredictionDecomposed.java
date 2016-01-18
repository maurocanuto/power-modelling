/*
    Copyright 2015 Barcelona Supercomputing Center
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package es.bsc.autonomic.powermodeller.graphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;

import es.bsc.autonomic.powermodeller.DataSet;
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class TotalPowerVsPredictionDecomposed extends ApplicationFrame {

    private static final long serialVersionUID = 1L;
    private static HashMap<String, DefaultCategoryDataset> jFreeChartDataSets;
    private static String NAME = "Measured Power VS Predicted Power Decomposition";

//    private static XYDataset pActualDS;
//    private static CategoryDataset pPredictedDS;

    public TotalPowerVsPredictionDecomposed(DataSet dataset) {
        super(NAME);
        jFreeChartDataSets = dataSetToJFreeChartDefaultCategoryDataset(dataset);

//        pActualDS = createActualPowerDataset(dataset);
//        pPredictedDS = createPredictedPowerDataset(dataset);

        JPanel jpanel = createPanel();
        jpanel.setPreferredSize(new Dimension(500, 270));
        //jpanel.setPreferredSize(jpanel.getMaximumSize());
        setContentPane(jpanel);
    }

    private static HashMap<String, DefaultCategoryDataset> dataSetToJFreeChartDefaultCategoryDataset(DataSet dataset) {
        HashMap<String, DefaultCategoryDataset> ret = new HashMap<String, DefaultCategoryDataset>();

        List<String> columns = new ArrayList(dataset.getHeader());
        //List<String> neededColumns = new ArrayList<String>();
        columns.remove(dataset.getIndependent());

        //Processing independent variable first.
        DefaultCategoryDataset pactualDS = new DefaultCategoryDataset();
        List<Double> independent = dataset.getCol(CoreConfiguration.PACTUAL_LABEL);
        for(int i = 0; i < independent.size(); i++) {
            pactualDS.addValue(independent.get(i), CoreConfiguration.PACTUAL_LABEL, Integer.toString(i));
        }
        ret.put(CoreConfiguration.PACTUAL_LABEL, pactualDS);

        //Adding rest of variables
        DefaultCategoryDataset metricDS = new DefaultCategoryDataset();
//        for(String metric : columns) {
//
//            List<Double> values = dataset.getCol(metric);
//            for(int i = 0; i < values.size(); i++) {
//                metricDS.addValue(values.get(i), metric, Integer.toString(i));
//            }
//        }
        addColumnToDefaultCategoryDataSet(metricDS, CoreConfiguration.PIDLE_LABEL, dataset.getCol(CoreConfiguration.PIDLE_LABEL));
        addColumnToDefaultCategoryDataSet(metricDS, CoreConfiguration.PCPU_LABEL, dataset.getCol(CoreConfiguration.PCPU_LABEL));
        addColumnToDefaultCategoryDataSet(metricDS, CoreConfiguration.PMEM_LABEL, dataset.getCol(CoreConfiguration.PMEM_LABEL));
        addColumnToDefaultCategoryDataSet(metricDS, CoreConfiguration.PDISK_LABEL, dataset.getCol(CoreConfiguration.PDISK_LABEL));
        addColumnToDefaultCategoryDataSet(metricDS, CoreConfiguration.PNET_LABEL, dataset.getCol(CoreConfiguration.PNET_LABEL));

        ret.put(CoreConfiguration.PPREDICTED_LABEL, metricDS);

        return ret;
    }

    private static void addColumnToDefaultCategoryDataSet(DefaultCategoryDataset ds, String name, List<Double> col) {
        for(int i = 0; i < col.size(); i++) {
            ds.addValue(col.get(i), name, Integer.toString(i));
        }
    }

    private static JFreeChart createChart() {

        //
        DefaultCategoryDataset Pactual = jFreeChartDataSets.get(CoreConfiguration.PACTUAL_LABEL);
        DefaultCategoryDataset Ppredicted = jFreeChartDataSets.get(CoreConfiguration.PPREDICTED_LABEL);

        StandardCategoryItemLabelGenerator standardcategoryitemlabelgenerator = new StandardCategoryItemLabelGenerator();

        LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
        /*renderer1.setBaseItemLabelGenerator(standardcategoryitemlabelgenerator);
        renderer1.setBaseItemLabelsVisible(true);*/
        StackedAreaRenderer renderer2 = new StackedAreaRenderer();

        //plot
        CategoryPlot plot = new CategoryPlot();
        plot.setDataset(Ppredicted);
        plot.setRenderer(renderer2);
        plot.setDataset(1, Pactual);
        plot.setRenderer(1, renderer1);
        CategoryAxis axis = new CategoryAxis("Sample");
        axis.setTickLabelsVisible(false);
        axis.setCategoryMargin(0.0);
        plot.setDomainAxis(axis);

        //more rangeAxis
        plot.setRangeAxis(new NumberAxis("Power (Watts)"));
        plot.mapDatasetToRangeAxis(2, 1);
        //
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(true);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        //chart
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(NAME);
        return chart;
    }

    private static JPanel createPanel() {
        JFreeChart jfreechart = createChart();
        ChartPanel chartpanel = new ChartPanel(jfreechart);
        chartpanel.addChartMouseListener(new ChartMouseListener() {

            public void chartMouseClicked(ChartMouseEvent chartmouseevent) {
                System.out.println(chartmouseevent.getEntity());
            }

            public void chartMouseMoved(ChartMouseEvent chartmouseevent) {
            }

        });
        return chartpanel;
    }

    public void display() {
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

//    private static XYDataset createActualPowerDataset(DataSet ds) {
//
//        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
//
//        List<Double> column = ds.getCol(CoreConfiguration.PACTUAL_LABEL);
//        XYSeries xyseries = new XYSeries(CoreConfiguration.PACTUAL_LABEL);
//        for(int i = 0; i < column.size(); i++) {
//            xyseries.add(i, column.get(i));
//        }
//        xyseriescollection.addSeries(xyseries);
//
//        return xyseriescollection;
//    }
//
//    private static CategoryDataset createPredictedPowerDataset(DataSet ds) {
//
//        String rowKeys[] = new String[ds.getSize()];
//        for(int i = 0; i < rowKeys.length; i++) {
//            rowKeys[i] = Integer.toString(i);
//        }
//
//        String columnKeys[] = new String[]{CoreConfiguration.PIDLE_LABEL, CoreConfiguration.PCPU_LABEL, CoreConfiguration.PMEM_LABEL, CoreConfiguration.PDISK_LABEL, CoreConfiguration.PNET_LABEL};
//
//        double[][] data = new double[columnKeys.length][ds.getSize()];
//        for(int i = 0; i < columnKeys.length; i++) {
//            for(int j = 0; j < ds.getSize(); j++) {
//                data[i][j] = ds.getCol(columnKeys[i]).get(j);
//            }
//        }
//
//        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(columnKeys, rowKeys, data);
//        return dataset;
//    }
//
//    private static JFreeChart createChart() {
//
//        //
//        DefaultCategoryDataset Pactual = jFreeChartDataSets.get(CoreConfiguration.PACTUAL_LABEL);
//        DefaultCategoryDataset Ppredicted = jFreeChartDataSets.get(CoreConfiguration.PPREDICTED_LABEL);
//
//        StandardCategoryItemLabelGenerator standardcategoryitemlabelgenerator = new StandardCategoryItemLabelGenerator();
//
//        LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
//        renderer1.setBaseItemLabelGenerator(standardcategoryitemlabelgenerator);
//        renderer1.setBaseItemLabelsVisible(true);
//        StackedAreaRenderer renderer2 = new StackedAreaRenderer();
//
//        //plot
//        CategoryPlot plot = new CategoryPlot();
//        plot.setDataset(Ppredicted);
//        plot.setRenderer(renderer2);
//        plot.setDataset(1, Pactual);
//        plot.setRenderer(1, renderer1);
//        ValueAxis rangeAxis2 = new NumberAxis("Value 2");
//        CategoryAxis axis = new CategoryAxis("Sample");
//        axis.setCategoryMargin(0.0);
//        plot.setRangeAxis(rangeAxis2);
//        //plot.setDomainAxis(axis);
//
//        //more rangeAxis
//        plot.setRangeAxis(new NumberAxis("Power (Watts)"));
//        plot.mapDatasetToRangeAxis(2, 1);
//        //
//        plot.setOrientation(PlotOrientation.VERTICAL);
//        plot.setRangeGridlinesVisible(true);
//        plot.setDomainGridlinesVisible(true);
//        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
////        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
//
//        //chart
//        JFreeChart chart = new JFreeChart(plot);
//        chart.setTitle(NAME);
//        return chart;
//    }


}
