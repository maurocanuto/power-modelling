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
import es.bsc.autonomic.powermodeller.configuration.CoreConfiguration;
import org.jfree.chart.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.*;

public class TotalPowerAndPredictionDifference extends ApplicationFrame
{
    private static XYDataset data;
    private static String NAME = "Measured Power VS Predicted Power Difference";

    public TotalPowerAndPredictionDifference(DataSet ds)
    {
        super(NAME);
        data = dataSetToJFreeChartXYDataSet(ds);
        JPanel jpanel = createPanel();
        jpanel.setPreferredSize(new Dimension(500, 270));
        //jpanel.setPreferredSize(jpanel.getMaximumSize());
        getContentPane().add(jpanel);
    }

    /*private static XYDataset dataSetToJFreeChartXYDataSet(DataSet ds) {

        XYSeriesCollection xyseriescollection = new XYSeriesCollection();

        List<Double> column = ds.getCol(CoreConfiguration.PACTUAL_LABEL);
        XYSeries xyseries = new XYSeries(CoreConfiguration.PACTUAL_LABEL);
        for(int i = 0; i < column.size(); i++) {
            xyseries.add(i, column.get(i));
        }
        xyseriescollection.addSeries(xyseries);

        column = ds.getCol(CoreConfiguration.PPREDICTED_LABEL);
        xyseries = new XYSeries(CoreConfiguration.PPREDICTED_LABEL);
        for(int i = 0; i < column.size(); i++) {
            xyseries.add(i, column.get(i));
        }
        xyseriescollection.addSeries(xyseries);

        return xyseriescollection;
    }*/

    private static XYDataset dataSetToJFreeChartXYDataSet(DataSet ds)
    {
        TimeSeries pactual = new TimeSeries(CoreConfiguration.PACTUAL_LABEL);
        TimeSeries ppredicted = new TimeSeries(CoreConfiguration.PPREDICTED_LABEL);
        double d = 0.0D;
        double d1 = 0.0D;
        Day day = new Day();
        List<Double> pactualCol = ds.getCol(CoreConfiguration.PACTUAL_LABEL);
        List<Double> ppredictedCol = ds.getCol(CoreConfiguration.PPREDICTED_LABEL);
        for (int i = 0; i < ds.getSize(); i++)
        {
            pactual.add(day, pactualCol.get(i));
            ppredicted.add(day, ppredictedCol.get(i));
            day = (Day)day.next();
        }

        TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
        timeseriescollection.addSeries(pactual);
        timeseriescollection.addSeries(ppredicted);
        return timeseriescollection;
    }

    private static JFreeChart createChart()
    {
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(NAME, "Power (Watts)", "Power (Watts)", data, true, true, false);
        jfreechart.setBackgroundPaint(Color.white);
        XYPlot xyplot = (XYPlot)jfreechart.getPlot();
        XYDifferenceRenderer xydifferencerenderer = new XYDifferenceRenderer(Color.green, Color.yellow, false);
        xydifferencerenderer.setRoundXCoordinates(true);
        xyplot.setDomainCrosshairLockedOnData(true);
        xyplot.setRangeCrosshairLockedOnData(true);
        xyplot.setDomainCrosshairVisible(true);
        xyplot.setRangeCrosshairVisible(true);
        xyplot.setRenderer(xydifferencerenderer);
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
        DateAxis dateaxis = new DateAxis("Samples");
        dateaxis.setTickLabelsVisible(false);
        dateaxis.setLowerMargin(0.0D);
        dateaxis.setUpperMargin(0.0D);
        xyplot.setDomainAxis(dateaxis);
        xyplot.setForegroundAlpha(0.5F);
        return jfreechart;
    }

    public static JPanel createPanel()
    {
        JFreeChart jfreechart = createChart();
        return new ChartPanel(jfreechart);
    }

    public void display()
    {
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }
}
