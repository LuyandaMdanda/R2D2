package test.demo1;

/**
 * Created by luyandamdanda on 10/21/17.
 */

import android.util.Log;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

/**
 * Created by gfm13 on 10/21/2017.
 */

public class OurProcessor implements AudioProcessor, PitchDetectionHandler {

    PitchProcessor[] processor;

    private static final char[] ALPHABET = new char[]{
        'a', 'b', 'c', 'd', 'e',
        'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o',
        'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y',
        'z', '\n', '0', '1', '2',
        '3', '4', '5', '6', '7',
        '8', '9', ',', '.', ' '
    };

    private static double[] frequencies;

    private int samples;
    private ChirpListener activity;
    private float sampleRate;
    private TarsosDSPAudioFormat format;
    private int[] nextLetterBuffer;
    private int currentVoteIndex;

    public OurProcessor(int samples, ChirpListener activity, float sampleRate, AudioDispatcher dispatcher) {
        this.samples = samples;
        this.activity = activity;
        this.sampleRate = sampleRate;
        format = dispatcher.getFormat();
        nextLetterBuffer = new int[MainActivity.CHIRP_LEN / MainActivity.NUM_SAMPLES * 6];
        currentVoteIndex = 0;

        processor = new PitchProcessor[samples];

        for(int i = 0; i < samples; i++) {
            processor[i] = new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.MPM,
                sampleRate,
                7800 / samples,
                this);
        }

        frequencies = new double[ALPHABET.length];
        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] = 500 * Math.pow(1.05, i) + 250;
        }
    }

    @Override
    public boolean process(AudioEvent event) {
        float[] fullBuffer = event.getFloatBuffer();
        int smallBufferLength = fullBuffer.length / samples;

        TarsosDSPAudioFormat littleFormat = new TarsosDSPAudioFormat(
                format.getEncoding(),
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getChannels(),
                smallBufferLength,
                format.getFrameRate(),
                format.isBigEndian());

        float[] pitchBuffer = new float[smallBufferLength];
        for (int i = 0; i < samples; i++) {
            System.arraycopy(fullBuffer, i * smallBufferLength, pitchBuffer, 0, smallBufferLength);

            AudioEvent littleEvent = new AudioEvent(littleFormat);
            littleEvent.setFloatBuffer(pitchBuffer);
            processor[i].process(littleEvent);
        }
        return true;
    }

    @Override
    public void processingFinished() {

    }

    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
        if (currentVoteIndex == 0 && pitchDetectionResult.getPitch() == -1) {
            return;
        }

        currentVoteIndex = (currentVoteIndex + 1) % nextLetterBuffer.length;
        // If we just filled in the last element, then we should fill it in.

        if (pitchDetectionResult.getProbability() > 0.90 &&
            pitchDetectionResult.getPitch() > 650) {
            double pitch = pitchDetectionResult.getPitch();
            int closestIndex = 0;

            Log.d("pitch_validation", "Pitch:" + String.valueOf(pitchDetectionResult.getPitch()) + " Confidence:" + String.valueOf(pitchDetectionResult.getProbability()));

            for (int i = 0; i < frequencies.length; i++) {
                if (Math.abs(pitch - frequencies[i]) < Math.abs(pitch - frequencies[closestIndex])) {
                    closestIndex = i;
                }
            }

            nextLetterBuffer[currentVoteIndex] = closestIndex;

        } else {
            nextLetterBuffer[currentVoteIndex] = -1;
        }

        if (currentVoteIndex == nextLetterBuffer.length - 1) {
            int[] mode = new int[ALPHABET.length + 1];
            for (int i : nextLetterBuffer) {
                mode[i + 1]++;
            }

            int maxIndex = 0;

            for (int i = 0; i < mode.length; i++) {
                if (mode[i] >= mode[maxIndex])
                {
                    maxIndex = i;
                }
            }

            if (mode[maxIndex] > 0 && maxIndex > 0) {
                activity.addChar(ALPHABET[maxIndex - 1]);
            }
        }
    }
}
