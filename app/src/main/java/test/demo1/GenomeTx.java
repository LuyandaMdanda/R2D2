package test.demo1;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;

public class GenomeTx extends AppCompatActivity {
  public static final char[] ALPHABET = new char[]{
      'a', 'b', 'c', 'd', 'e',
      'f', 'g', 'h', 'i', 'j',
      'k', 'l', 'm', 'n', 'o',
      'p', 'q', 'r', 's', 't',
      'u', 'v', 'w', 'x', 'y',
      'z', '\n', '0', '1', '2',
      '3', '4', '5', '6', '7',
      '8', '9', ',', '.', ' '
  };

  public static final int AUDIO_SAMPLE_RATE = 44100;
  public static final int CHIRP_LEN = 100;
  public static final int SAMPLE_TIME = 30;
  public static final int MS_PER_SEC = 1000;
  public static final int NUM_SAMPLES = MS_PER_SEC / SAMPLE_TIME;
  public static final int BUFFER_SIZE = 7800;

  private AudioDispatcher dispatcher;
  private StringBuilder builder;

  /* Member Variables. */
  private AudioTrack mAudioTrack;
  private boolean mChirping;
  private Thread mAudioThread;

  public String message;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_genome_tx);

    this.mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, MainActivity.WRITE_AUDIO_RATE_SAMPLE_HZ, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 7168, AudioTrack.MODE_STREAM);
    this.mAudioThread = null;
    //this.mAudioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(MainActivity.WRITE_AUDIO_RATE_SAMPLE_HZ, 7168, 0); /** TODO: Abstract constants. */

    dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(AUDIO_SAMPLE_RATE, BUFFER_SIZE, 0);
    builder = new StringBuilder();
  }

  /**
   * Called when the user taps the Send button
   */
  public void sendMessage(View view) {
    //Intent intent = new Intent(this, DisplayMessageActivity.class);
    EditText editText = (EditText) findViewById(R.id.editText);
    message = "adbefcdbbabccd";
    GenomeTx.this.chirp((byte) 100);
    //intent.putExtra(EXTRA_MESSAGE, message);
    //startActivity(intent);
  }

  private final void chirp(final byte freq) {
    Log.e("gen_tx", "Chirp started: " + freq);
    // Declare an AsyncTask which we'll use for generating audio.
    final AsyncTask lAsyncTask = new AsyncTask<Void, Void, Void>() {
      /** Initialize the play. */
      @Override
      protected final void onPreExecute() {
        // Assert that we're chirping.
        GenomeTx.this.setChirping(true);
        // Play the AudioTrack.
        GenomeTx.this.getAudioTrack().play();
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

        final byte[] lChirp = GenomeTx.this.onGenerateChirp(message, CHIRP_LEN);
        // Write the ChirpFactory to the Audio buffer.
        GenomeTx.this.getAudioTrack().write(lChirp, 0, lChirp.length);
        // Satisfy the parent.
        return null;
      }

      /** Cyclic. */
      @Override
      protected final void onPostExecute(Void pIsUnused) {
        // Stop the AudioTrack.
        GenomeTx.this.getAudioTrack().stop();
        // Assert that we're no longer chirping.
        GenomeTx.this.setChirping(false);
      }
    };
    // Execute the AsyncTask on the pre-prepared ThreadPool.
    lAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
  }

  /**
   * Generates a tone for the audio stream.
   */
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
      for(int j = 0; j < ALPHABET.length; j++) {
        if (ALPHABET[j] == lData) {
          final double lFrequency = 500 * Math.pow(1.05, j) + 250;
          Log.d("gen_tx", pData.charAt(i) + " " + (500 * Math.pow(1.05, j) + 250));
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

  /**
   * Handle resumption of the Activity.
   */
  @Override
  protected final void onResume() {
    // Implement the Parent.
    super.onResume();
    // Allocate the AudioThread.
    this.setAudioThread(new Thread(this.getAudioDispatcher()));
    // Start the AudioThread.
    this.getAudioThread().start();
  }

  @Override
  protected final void onPause() {
    // Implement the Parent.
    super.onPause();
    // Stop the AudioDispatcher; implicitly stops the owning Thread.
    this.getAudioDispatcher().stop();
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  /* Getters. */
  private final AudioTrack getAudioTrack() {
    return this.mAudioTrack;
  }

  private final void setChirping(final boolean pIsChirping) {
    this.mChirping = pIsChirping;
  }

  private final boolean isChirping() {
    return this.mChirping;
  }

  private AudioDispatcher getAudioDispatcher() {
    return this.dispatcher;
  }

  private final void setAudioThread(final Thread pThread) {
    this.mAudioThread = pThread;
  }

  private final Thread getAudioThread() {
    return this.mAudioThread;
  }

}
