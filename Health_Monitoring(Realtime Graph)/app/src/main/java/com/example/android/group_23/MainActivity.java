package com.example.android.group_23;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends Activity {

    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series;
    private int xcoor = 0;
    private boolean flag1 = true, runFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final GraphView graph = (GraphView) findViewById(R.id.plotgraph);
        final GraphView temp = (GraphView) findViewById(R.id.plotgraph1);
        series = new LineGraphSeries<DataPoint>();
        series.setDrawDataPoints(true);
        series.setDrawBackground(true);
        graph.addSeries(series);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(10);
        viewport.setScrollable(true);
        viewport.scrollToEnd();
        temp.setVisibility(View.INVISIBLE);
        final Button run = findViewById(R.id.button3);
        final Button stop = findViewById(R.id.button4);
        /**
         * Handling RUN button onClick event
         */
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graph.setVisibility(View.VISIBLE);
                temp.setVisibility(View.INVISIBLE);
                flag1=true;
                /**
                 * Used runFlag to stop multiple thread from spawning
                 */
                if(!runFlag) {
                    /**
                     * Spawning a new thread to keep updating the GraphView
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                /**
                                 * Runs the specified action on the UI thread.
                                 * Here the current thread is not the UI thread, the action is posted to the event queue of the UI thread.
                                 */
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        /**
                                         * The graph is paused and a temporary graph is overlapped when the STOP button onClick event occurs.
                                         */
                                        stop.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                flag1 = false;
                                                graph.setVisibility(View.INVISIBLE);
                                                temp.setVisibility(View.VISIBLE);
                                            }
                                        });
                                        if (flag1) {
                                            addEntry();
                                        }
                                    }
                                });

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                    runFlag = true;
                }
            }
        });
    }
    private void addEntry() {
        series.appendData(new DataPoint(xcoor++, RANDOM.nextDouble() * 10d), false, 10);
    }

}