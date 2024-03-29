package com.cmpe277.macroeconomicfoodsecurity;

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
    private Button sendButton;
    private TextView chatResponse;

    public ChatbotFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        userInput = view.findViewById(R.id.userInput);
        sendButton = view.findViewById(R.id.sendButton);
        chatResponse = view.findViewById(R.id.chatResponse);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPromptToAPI(userInput.getText().toString());
            }
        });

        return view;
    }

    private void sendPromptToAPI(String prompt) {
        // Here, you would call the GPT API with the user's prompt
        // For demonstration, we're just echoing the prompt back
        chatResponse.setText(prompt);
    }
}
