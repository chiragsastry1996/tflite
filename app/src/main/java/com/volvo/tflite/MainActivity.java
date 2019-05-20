package com.volvo.tflite;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    protected Interpreter tflite;
    public static String MODEL_PATH = "nlu.tflite";
//    int[][] inputArray = new int[1][65];
    float[][] ProbArray = new float[1][7];
    ArrayList<Float> predictons = new ArrayList<>();
    ArrayList<String> tokens = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        float[][] inputArray = {{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
//        0,0,1,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
//
//        String str = "I am sample string and will be tokenized on space";
//
//        StringTokenizer defaultTokenizer = new StringTokenizer(str);
//
//        while (defaultTokenizer.hasMoreTokens())
//        {
//            tokens.add(defaultTokenizer.nextToken().toLowerCase());
//        }
//        Log.e("MainActivity", tokens.toString());
//        try {
//            Log.e("MainActivity", "Started Running");
//            tflite = new Interpreter(loadModelFile(MainActivity.this));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        tflite.run(inputArray, ProbArray);
//        for (int i=0; i<7; i++){
//            System.out.println("MainActivity" + ProbArray[0][i]);
//            predictons.add(ProbArray[0][i]);
//        }
//        int maxAt = 0;
//
//        for (int i = 0; i < ProbArray[0].length; i++) {
//            maxAt = ProbArray[0][i] > ProbArray[0][maxAt] ? i : maxAt;
//        }
//
//        System.out.println("The highest maximum for the December is: " + maxAt);


        try{
            // for tilda expansion
            //if (filepath.startsWith("~" + File.separator)) {
            //filepath = System.getProperty("user.home") + filepath.substring(1);
            //}

            //ProcessBuilder builder = new ProcessBuilder("python", "-c", "import sys; import nltk; print \"whatever\"");
            ProcessBuilder builder = new ProcessBuilder("python", "main.py", "four scores and seven years ago");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            InputStream stdout = p.getInputStream();
            BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));

            String line;
            while ((line = reader.readLine ()) != null) {
                System.out.println("Stdout: " + line);
            }
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}
