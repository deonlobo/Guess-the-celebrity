package com.deonlobo.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebUrls=new ArrayList<String>();
    ArrayList<String> celebNames=new ArrayList<String>();
    int chosenCeleb = 0;
    int locationOfCorrectAnswer;
    String[] answers = new String[4];

    ImageView imageView ;
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    public void celebChosen (View view) {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();

        }else{
            Toast.makeText(this, "Wrong! It was "+celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }
        createNewQuestion();

    }

    public class ImageDownloader extends AsyncTask<String ,Void ,Bitmap>{


        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String ,Void ,String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection =null;
            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null){
                    str.append(line);
                    str.append("\n");
                }
                in.close();
                return (result = str.toString());

            }
            catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        DownloadTask task = new DownloadTask();
        String result =null;
        try {

            result = task.execute("https://www.imdb.com/list/ls052283250/").get();
            String[] splitresult1 = result.split("<div class=\"desc lister-total-num-results\">");

            String [] splitresult2 = splitresult1[1].split("<div class=\"row text-center lister-working");

            Pattern p = Pattern.compile("src=\"(.*?)\"");
            Matcher m = p.matcher(splitresult2[0]);

            while(m.find()){
                celebUrls.add(m.group(1));
            }
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitresult2[0]);

            while(m.find()){
                celebNames.add(m.group(1));
            }

            createNewQuestion();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    public void createNewQuestion() {

        try {

            Random random = new Random();
            chosenCeleb = random.nextInt(celebUrls.size());

            ImageDownloader imgTask = new ImageDownloader();
            Bitmap celebImage;

            celebImage = imgTask.execute(celebUrls.get(chosenCeleb)).get();

            imageView.setImageBitmap(celebImage);
            locationOfCorrectAnswer = random.nextInt(4);

            int incorrectAnswerLocation;

            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = random.nextInt(celebUrls.size());

                    while (incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = random.nextInt(celebUrls.size());
                    }
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

