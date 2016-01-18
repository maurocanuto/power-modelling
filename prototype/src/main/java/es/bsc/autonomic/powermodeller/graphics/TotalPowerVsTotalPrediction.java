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
import java.util.List;

import javax.swing.JPanel;

import es.bsc.autonomic.powermodeller.DataSet;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

public class TotalPowerVsTotalPrediction extends ApplicationFrame{

    private static final long serialVersionUID = 1L;
    private static XYDataset data;
    private static String NAME = "Measured Power VS Predicted Power";

    public TotalPowerVsTotalPrediction(DataSet ds) {
        super(NAME);
        data = dataSetToJFreeChartXYDataSet(ds);
        JPanel jpanel = createPanel();
        jpanel.setPreferredSize(new Dimension(500, 270));
        //jpanel.setPreferredSize(jpanel.getMaximumSize());
        getContentPane().add(jpanel);
    }

    public static JPanel createPanel() {
        //Axis configuration
        NumberAxis sampleAxis = new NumberAxis("Sample");
        sampleAxis.setAutoRangeIncludesZero(false);
        NumberAxis powerAxis = new NumberAxis("Power (Watts)");
        powerAxis.setAutoRangeIncludesZero(false);

        XYSplineRenderer xysplinerenderer = new XYSplineRenderer();

        XYPlot xyplot = new XYPlot(data, sampleAxis, powerAxis, xysplinerenderer);
        for(int i = 0; i < data.getSeriesCount(); i++) {
            xyplot.getRenderer().setSeriesShape(i, new Rectangle());
        }

        //Panel layout configuration
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.setAxisOffset(new RectangleInsets(4D, 4D, 4D, 4D));

        JFreeChart jfreechart = new JFreeChart(NAME, JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);

        ChartUtilities.applyCurrentTheme(jfreechart);
        ChartPanel chartpanel = new ChartPanel(jfreechart);

        return chartpanel;
    }

    private static XYDataset dataSetToJFreeChartXYDataSet(DataSet ds) {

        XYSeriesCollection xyseriescollection = new XYSeriesCollection();

        for(String metric : ds.getHeader()) {
            List<Double> column = ds.getCol(metric);
            XYSeries xyseries = new XYSeries(metric);
            for(int i = 0; i < column.size(); i++) {
                xyseries.add(i, column.get(i));
            }
            xyseriescollection.addSeries(xyseries);
        }

        return xyseriescollection;
    }

    public void display() {
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }
}

