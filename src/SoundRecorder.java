import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import javax.sound.sampled.*;
import java.util.concurrent.TimeUnit;


public class SoundRecorder
{
    private static final long RECORD_TIME = 5;


    public static AudioFormat getAudioFormat()
    {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);

        return format;
    }


    public static void uploadToDropbox(String ACCESS_TOKEN)
    {
        try
        {
            checkingAccessToken(ACCESS_TOKEN);
            DbxClientV2 client = getAccessToYourAccountThroughAccessToken(ACCESS_TOKEN);

            uploadSoundEvery60seconds(client);
        } catch (com.dropbox.core.DbxException e) { System.err.println("The access token is invalid.");}
    }


    private static void uploadSoundEvery60seconds(DbxClientV2 client)
    {

        while(true)
        {
            new Thread(() ->
            {
                recordAudio(client);
            }).start();

            try
            {
                TimeUnit.SECONDS.sleep(RECORD_TIME + 1);
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }


    private static void recordAudio(DbxClientV2 client)
    {
        start(client);
        finish();
    }


    private static void start(DbxClientV2 client)
    {
        Threads.threadForRecordingAndUploading(client);
    }


    private static void finish()
    {
        Threads.threadToFinishRecording(RECORD_TIME);
    }



    private static void checkingAccessToken(String ACCESS_TOKEN) throws com.dropbox.core.DbxException
    {
        new DbxClientV2(DbxRequestConfig.newBuilder("").build(), ACCESS_TOKEN).users().getCurrentAccount();
    }


    private static DbxClientV2 getAccessToYourAccountThroughAccessToken(String ACCESS_TOKEN)
    {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();

        return new DbxClientV2(config, ACCESS_TOKEN);
    }
}



