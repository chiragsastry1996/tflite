package com.volvo.tflite;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
import java.util.Locale;
import java.util.StringTokenizer;

import ca.rmen.porterstemmer.PorterStemmer;


public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    protected Interpreter tflite;
    public static String nlu_intent = null, message = "";
    public static String MODEL_PATH = "nlu.tflite";
    TextView textView;
    Button button;
    EditText editText;


    String[] words = {"a", "afternoon", "are", "assist", "befor", "broken", "bunk", "can", "check", "day", "diesel", "distanc", "drink",
            "eat", "empti", "even", "famish", "far", "farther", "fill", "food", "for", "fuel", "ga", "go", "good", "hello", "help", "hey",
            "hi", "how", "hungri", "indic", "is", "left", "light", "long", "lot", "me", "morn", "much", "near", "on", "petrol", "place",
            "pump", "refil", "remain", "restaur", "right", "side", "so", "starv", "station", "tank", "thank", "the", "thirsti", "to", "ton",
            "travel", "turn", "we", "what", "you"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.text);
        editText = (EditText)findViewById(R.id.edit_text);
        editText.setVisibility(View.GONE);
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = "";
                startVoiceInput();
            }
        });

        try {
            tflite = new Interpreter(loadModelFile(MainActivity.this));
        } catch (IOException e) {
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

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    nlu_classify(result.get(0));
//                    mVoiceInputTv.setText(result.get(0));
                }
                break;
            }

        }
    }

    public void nlu_classify(String str) {

//        String str = editText.getText().toString();

        message = message + str + "\n";

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

        int maxAt = 0;

        for (int i = 0; i < input[0].length; i++) {
            maxAt = input[0][i] > input[0][maxAt] ? i : maxAt;
        }

        message = message + "Accuracy = " + input[0][maxAt];

        return maxAt;
    }

}
