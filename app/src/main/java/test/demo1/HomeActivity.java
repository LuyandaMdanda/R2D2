package test.demo1;

import android.content.Intent;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
  }

  public void launchMain(View view) {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }

  public void launchGenomeTx(View view) {
    Intent intent = new Intent(this, GenomeTx.class);
    startActivity(intent);
  }

  public void launchGenomeRx(View view) {
    Intent intent = new Intent(this, GenomeRx.class);
    startActivity(intent);
  }
}
