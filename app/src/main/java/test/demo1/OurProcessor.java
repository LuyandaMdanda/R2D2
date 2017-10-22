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
import be.tarsos.dsp.pitch.PitchProcessor;

/**
 * Created by gfm13 on 10/21/2017.
 */

public class OurProcessor implements AudioProcessor {

    private int samples;
    private PitchDetectionHandler handler;
    private float sampleRate;
    private TarsosDSPAudioFormat format;

    public OurProcessor(int samples, PitchDetectionHandler handler, float sampleRate, AudioDispatcher dispatcher) {
        this.samples = samples;
        this.handler = handler;
        this.sampleRate = sampleRate;
        format = dispatcher.getFormat();
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

            PitchProcessor processor = new PitchProcessor(
                    PitchProcessor.PitchEstimationAlgorithm.MPM,
                    sampleRate,
                    smallBufferLength,
                    handler);

            AudioEvent littleEvent = new AudioEvent(littleFormat);
            littleEvent.setFloatBuffer(pitchBuffer);
            processor.process(littleEvent);
        }
        return true;
    }

    @Override
    public void processingFinished() {

    }
}
