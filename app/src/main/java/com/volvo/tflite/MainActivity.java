package com.volvo.tflite;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ca.rmen.porterstemmer.PorterStemmer;

public class MainActivity extends AppCompatActivity {

    protected Interpreter tflite;
    public static String nlu_intent = null, message = null;
    public static String MODEL_PATH = "nlu.tflite";
    TextView textView;
    Button button;
    EditText editText;


    String[] words = {"a", "afternoon", "are", "assist", "befor", "broken", "bunk", "can", "check", "day", "diesel", "distanc", "drink", "eat", "empti", "even", "famish", "far", "farther", "fill", "food", "for", "fuel", "ga", "go", "good", "hello", "help", "hey", "hi", "how", "hungri", "indic", "is", "left", "light", "long", "lot", "me", "morn", "much", "near", "on", "petrol", "place", "pump", "refil", "remain", "restaur", "right", "side", "so", "starv", "station", "tank", "thank", "the", "thirsti", "to", "ton", "travel", "turn", "we", "what", "you"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.text);
        editText = (EditText)findViewById(R.id.edit_text);
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nlu_classify();
            }
        });

        try {
            tflite = new Interpreter(loadModelFile(MainActivity.this));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void nlu_classify() {

        String str = editText.getText().toString();

        if(!str.isEmpty()) {
            float[][] inputArray = string_converter(str);
            float[][] ProbArray = new float[1][7];

            tflite.run(inputArray, ProbArray);

            int index = highestpredictionindex(ProbArray);

            switch (index) {
                case 0 : nlu_intent = "Distance";break;
                case 1 : nlu_intent = "FuelStation";break;
                case 2 : nlu_intent = "Greetings";break;
                case 3 : nlu_intent = "LeftIndicator";break;
                case 4 : nlu_intent = "Restaurant";break;
                case 5 : nlu_intent = "LeftIndicator";break;
                case 6 : nlu_intent = "Thanking";break;
            }

            message = message + "\nIntent =" + nlu_intent;
            textView.setText(message);

            Log.e("MainActivity", nlu_intent);
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

    public float[][] string_converter(String str) {

        ArrayList<String> tokens = new ArrayList<>();

        StringTokenizer defaultTokenizer = new StringTokenizer(str);

        while (defaultTokenizer.hasMoreTokens())
        {
            PorterStemmer porterStemmer = new PorterStemmer();
            String stem = porterStemmer.stemWord(defaultTokenizer.nextToken().toLowerCase());
            tokens.add(stem);
        }

        int len = words.length;
        float[][] bag = new float[1][len];
        for(int i = 0; i<words.length; i++) {
            for(int j=0;j<tokens.size(); j++) {
                if(words[i].equals(tokens.get(j))){
                    bag[0][i] = 1;
                }
            }
        }

        return bag;
    }

    public int highestpredictionindex(float[][] input) {

        ArrayList<Float> predictons = new ArrayList<>();

        for (int i=0; i<7; i++){
            predictons.add(input[0][i]);
        }
        int maxAt = 0;

        for (int i = 0; i < input[0].length; i++) {
            maxAt = input[0][i] > input[0][maxAt] ? i : maxAt;
        }

        message = "Accuracy = " + input[0][maxAt];

        return maxAt;
    }

}
