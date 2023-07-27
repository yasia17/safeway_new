package com.example.newas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;


public class Data_info extends AppCompatActivity {

    private TextView textViewHighlighted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_info);

        Button cont = findViewById(R.id.Continue);
        textViewHighlighted = findViewById(R.id.dearuser);

        // Full text with highlighted words
        String fullText = "To ensure the utmost security and safeguard your interests, we kindly request the provision of necessary data including a photograph of yourself along with a valid identification document.";

        // Words to highlight
        String wordToHighlight1 = "photograph of yourself";
        String wordToHighlight2 = "valid identification document";

        SpannableString spannableString = new SpannableString(fullText);

        // Find the start and end index of the words to highlight
        int startIndex1 = fullText.indexOf(wordToHighlight1);
        int endIndex1 = startIndex1 + wordToHighlight1.length();

        int startIndex2 = fullText.indexOf(wordToHighlight2);
        int endIndex2 = startIndex2 + wordToHighlight2.length();

        // Apply background color for the first word
        spannableString.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.light_blue)), startIndex1, endIndex1, 0);

        // Apply background color for the second word
        spannableString.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.light_blue)), startIndex2, endIndex2, 0);

        // Set the SpannableString to the TextView
        textViewHighlighted.setText(spannableString);


        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Data_info.this, Face_photo.class);
                startActivity(i);
            }
        });
    }
}