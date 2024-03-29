package com.cmpe277.macroeconomicfoodsecurity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ChatbotFragment extends Fragment {

    private EditText userInput;
    private Button sendButton, attachPdfButton;
    private TextView chatResponse;
    private Uri selectedPdfUri = null; // To hold the URI of the selected PDF

    public ChatbotFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        userInput = view.findViewById(R.id.userInput);
        sendButton = view.findViewById(R.id.sendButton);
        attachPdfButton = view.findViewById(R.id.attachPdfButton); // Button to attach PDF
        chatResponse = view.findViewById(R.id.chatResponse);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = userInput.getText().toString();
                sendTextAndOptionalPdf(message, selectedPdfUri);
                selectedPdfUri = null; // Reset after sending
            }
        });

        attachPdfButton.setOnClickListener(v -> openFilePicker());

        return view;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), 123);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPdfUri = data.getData(); // Store the selected PDF URI
        }
    }

    private void sendTextAndOptionalPdf(String text, Uri pdfUri) {
        if (pdfUri != null) {
            // Implement the logic to send text with PDF
            // This might involve uploading the PDF and then sending the message with the PDF URL
        } else {
            // Implement the logic to send text only
        }
        // Reset or update UI as necessary
    }
}
