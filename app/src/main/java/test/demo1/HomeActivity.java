package test.demo1;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

  private int[] personal_severities;
  private String severityString;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    personal_severities = new int[] {3, 2, 1, 4, 0, 2, 3, 2};
    severityString = "";
    for (int i : personal_severities) {
      // Add all conditions into a string to play
      severityString += i;
    }
  }

  public void launchMain(View view) {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }

  public void launchGenome(View view) {
    Intent intent = new Intent(this, GenomeData.class);
    startActivity(intent);
  }

  private final byte[] onGenerateChirp(String pData, final int pPeriod) {

    pData = pData + "\n";
    // Calculate the Number of Samples per chirp.
    final int lNumberOfSamples = (int) (MainActivity.WRITE_AUDIO_RATE_SAMPLE_HZ * (pPeriod / 1000.0f));
    // Declare the SampleArray.
    double[] lSampleArray = new double[pData.length() * lNumberOfSamples];
    // Declare the Generation.
    final byte[] lGeneration = new byte[lSampleArray.length * 2];
    // Declare the Offset.
    int lOffset = 0;

    // Iterate the Transmission.
    for (int i = 0; i < pData.length(); i++) {
      // Fetch the Data.
      final Character lData = Character.valueOf(pData.charAt(i));
      // Fetch the Frequency.
      for(int j = 0; j < MainActivity.ALPHABET.length; j++) {
        if (MainActivity.ALPHABET[j] == lData) {
          final double lFrequency = 500 * Math.pow(1.05, j) + 250;
          // Iterate the NumberOfSamples. (Per chirp data.)
          for (int k = 0; k < lNumberOfSamples; k++) {
            // Update the SampleArray.
            lSampleArray[lOffset] = Math.sin(2 * Math.PI * k / (MainActivity.WRITE_AUDIO_RATE_SAMPLE_HZ / lFrequency));
            // Increase the Offset.
            lOffset++;
          }
        }
      }
    }
    // Reset the Offset.
    lOffset = 0;
    // Iterate between each sample.
    for (int i = 0; i < pData.length(); i++) {
      // Fetch the Start and End Indexes of the Sample.
      final int lIo = i * lNumberOfSamples;
      final int lIa = lIo + lNumberOfSamples;
      // Declare the RampWidth. We'll change it between iterations for more tuneful sound.)
      final int lRw = (int) (lNumberOfSamples * 0.3);
      // Iterate the Ramp.
      for (int j = 0; j < lRw; j++) {
        // Calculate the progression of the Ramp.
        final double lP = j / (double) lRw;
        // Scale the corresponding samples.
        lSampleArray[lIo + j + 0] *= lP;
        lSampleArray[lIa - j - 1] *= lP;
      }
    }

    // Declare the filtering constant.
    final double lAlpha = 0.3;
    double lPrevious = 0;

    // Iterate the SampleArray.
    for (int i = 0; i < lSampleArray.length; i++) {
      // Fetch the Value.
      final double lValue = lSampleArray[i];
      // Filter the Value.
      final double lFiltered = (lAlpha < 1.0) ? ((lValue - lPrevious) * lAlpha) : lValue;
      // Assume normalized, so scale to the maximum amplitude.
      final short lPCM = (short) ((lFiltered * 32767));
      // Supply the Generation with 16-bit PCM. (The first byte is the low-order byte.)
      lGeneration[lOffset++] = (byte) (lPCM & 0x00FF);
      lGeneration[lOffset++] = (byte) ((lPCM & 0xFF00) >>> 8);
      // Overwrite the Previous with the Filtered value.
      lPrevious = lFiltered;
    }
    // Return the Generation.
    return lGeneration;
  }

  private final void chirp(final byte freq) {
    Log.e(TAG, "Chirp started: " + freq);
    // Declare an AsyncTask which we'll use for generating audio.
    final AsyncTask lAsyncTask = new AsyncTask<Void, Void, Void>() {
      /** Initialize the play. */
      @Override
      protected final void onPreExecute() {
        // Assert that we're chirping.
        MainActivity.this.setChirping(true);
        // Play the AudioTrack.
        MainActivity.this.getAudioTrack().play();
      }

      /** Threaded audio generation. */
      @Override
      protected Void doInBackground(final Void[] pIsUnused) {
        // Re-buffer the new tone.
        //final byte[] lChirp = new byte[100000];
                /*
                for(int i = 0; i < lChirp.length; i++) {
                  lChirp[i] = freq;
                }
                */

        final byte[] lChirp = MainActivity.this.onGenerateChirp(message, CHIRP_LEN);
        // Write the ChirpFactory to the Audio buffer.
        MainActivity.this.getAudioTrack().write(lChirp, 0, lChirp.length);
        // Satisfy the parent.
        return null;
      }

      /** Cyclic. */
      @Override
      protected final void onPostExecute(Void pIsUnused) {
        // Stop the AudioTrack.
        MainActivity.this.getAudioTrack().stop();
        // Assert that we're no longer chirping.
        MainActivity.this.setChirping(false);
      }
    };
    // Execute the AsyncTask on the pre-prepared ThreadPool.
    lAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
  }

}
