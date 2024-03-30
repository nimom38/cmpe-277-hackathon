package com.cmpe277.macroeconomicfoodsecurity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
//        responseTextView = view.findViewById(R.id.chatResponse);

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
            String apiUrl = "https://api.chatpdf.com/v1/chats/message";
            String requestBody = "{\"sourceId\": \"cha_XYOVt6WC3Jc7HaHG0lZdB\",\"messages\": [{\"role\": \"user\",\"content\": \""+text+"\"}]}";

            ApiCallTask apiCallTask = new ApiCallTask();
            apiCallTask.execute(apiUrl, requestBody);
            // Implement the logic to send text only
        }
        // Reset or update UI as necessary
    }


    public class ApiCallTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String urlString = params[0];
            String requestBody = params[1];
            String response = "";

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("x-api-key", "sec_0mrMVQyrpLTpsVn4vSHJ0NMyZr4agM2R");
                urlConnection.setDoOutput(true);

                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(requestBody.getBytes());
                outputStream.flush();
                outputStream.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }
                    in.close();
                    response = stringBuilder.toString();
//                    chatResponse.setText(response);

                    JSONObject jsonObject = new JSONObject(response);
                    setText(chatResponse, jsonObject.getString("content"));
//                    responseTextView.setText(response);

                } else {
                    response = "Error: " + responseCode;
                }
                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                response = "Error: " + e.getMessage();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            return response;
        }

        protected void onPostExecute(String result) {
            // Handle the API response here
            // result contains the response from the API
        }
    }
    private void setText(final TextView text,final String value){
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                chatResponse.setText(value);
                // Stuff that updates the UI

            }
        });
    }
}
