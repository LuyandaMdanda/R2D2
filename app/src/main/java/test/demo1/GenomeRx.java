package test.demo1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;

public class GenomeRx extends AppCompatActivity implements ChirpListener {

    private static final String[] CONDITIONS =
            {
                    "Liver Cancer",
                    "Lung Cancer",
                    "Breast Cancer",
                    "Colorectal Cancer",
                    "Gastric Cancer",
                    "Pancreatic Cancer",
                    "Prostate Cancer",
                    "Diabetes"
            };
    private static final String[] LIKELIHOOD =
            {
                    "Lower Risk",
                    "Slightly Lower Risk",
                    "Intermediate",
                    "Slightly Higher Risk",
                    "High Risk"
            };


    private volatile int conditionIndex;
    private LinearLayout layout;
    private AudioDispatcher dispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genome_data);

        layout = (LinearLayout)findViewById(R.id.layout);

        conditionIndex = 0;

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(MainActivity.AUDIO_SAMPLE_RATE, MainActivity.BUFFER_SIZE, 0);
        dispatcher.addAudioProcessor(new OurProcessor(MainActivity.NUM_SAMPLES, this, MainActivity.AUDIO_SAMPLE_RATE,
                dispatcher));
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        new Thread(dispatcher).start();
    }

    @Override
    public void addChar(char c) {
        Log.d("addChar", "got: " + c);
        int severity = 0;
        switch (c) {
            case 'a':
                severity = 0;
                break;
            case 'b':
                severity = 1;
                break;
            case 'c':
                severity = 2;
                break;
            case 'd':
                severity = 3;
                break;
            case 'e':
                severity = 4;
                break;
            default:
                return;
        }

            try{
                final int finalSeverity = severity;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (conditionIndex < CONDITIONS.length) {
                            TextView tv = new TextView(GenomeRx.this);
                            tv.setText(CONDITIONS[conditionIndex] + ": " + LIKELIHOOD[finalSeverity]);
                            layout.addView(tv);
                            conditionIndex++;
                        }
                    }
                });
            } catch (Exception e) {}

    }
}
