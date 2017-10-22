package test.demo1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GenomeRx extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genome_data);


        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);


    }
}
