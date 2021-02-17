import com.dropbox.core.v2.DbxClientV2;

import javax.sound.sampled.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Threads
{
    private static TargetDataLine line;


    public static void threadForRecordingAndUploading(DbxClientV2 client)
    {
        new Thread(() ->
        {
            try
            {
                File tempFile = File.createTempFile("wav", "wav");

                line = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, SoundRecorder.getAudioFormat()));
                line.open(SoundRecorder.getAudioFormat());
                line.start();

                AudioSystem.write(new AudioInputStream(line), AudioFileFormat.Type.WAVE, tempFile);
                uploadAudio(client, tempFile);
            } catch (Exception e) { System.exit(1); }
        }).start();
    }


    public static void threadToFinishRecording(long RECORD_TIME)
    {
        new Thread(() ->
        {
            sleep(RECORD_TIME);

            line.stop();
            line.close();
        }).start();
    }


    private static void uploadAudio(DbxClientV2 client, File tempFile)
    {
        try (InputStream in = new ByteArrayInputStream(fileToByteArray(tempFile)))
        {
            client.files().uploadBuilder(getPathToDropbox()).uploadAndFinish(in);
            tempFile.delete();
        } catch (IOException e ) { e.printStackTrace(); }
        catch (com.dropbox.core.DbxException e) { e.printStackTrace(); }
    }


    private static String getPathToDropbox()
    {
        return new String("/" + new SimpleDateFormat("dd MM yyyy H:mm:ss").format(new Date()) + ".wav");
    }


    private static byte[] fileToByteArray(File tempFile)
    {
        byte[] arrayOfBytes = new byte[(int) tempFile.length()];

        try(FileInputStream file = new FileInputStream(tempFile))
        {
            file.read(arrayOfBytes, 0, arrayOfBytes.length);
        } catch (IOException e) { e.printStackTrace(); }

        return arrayOfBytes;
    }


    private static void sleep(long time)
    {
        try
        {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
