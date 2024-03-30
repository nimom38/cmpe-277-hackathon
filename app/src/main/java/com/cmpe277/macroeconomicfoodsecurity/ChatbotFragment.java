package com.cmpe277.macroeconomicfoodsecurity;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.widget.TextView;
import java.util.List;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;


class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messagesList;

    public ChatAdapter(List<Message> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messagesList.get(position);
        holder.messageText.setText(message.getText());
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    // Determine the view type based on whether the message is from the user or the bot
    @Override
    public int getItemViewType(int position) {
        Message message = messagesList.get(position);
        if (message.isUserMessage()) {
            // User message layout
            return R.layout.item_user_message;
        } else {
            // Bot message layout
            return R.layout.item_bot_message;
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
        }
    }
}


class Message {
    private String text;
    private boolean isUserMessage; // True if the message is sent by the user

    public Message(String text, boolean isUserMessage) {
        this.text = text;
        this.isUserMessage = isUserMessage;
    }

    public String getText() {
        return text;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }
}

public class ChatbotFragment extends Fragment {

    private EditText messageInput;
    private Button sendButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private ArrayList<Message> messagesList = new ArrayList<>();

    private LinearLayout inputLayout;

    private class ApiCallTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            String requestBody = params[1];
            HttpURLConnection urlConnection = null;
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                // Replace "x-api-key" with the appropriate header or remove if not needed
                urlConnection.setRequestProperty("x-api-key", "sec_0mrMVQyrpLTpsVn4vSHJ0NMyZr4agM2R");
                urlConnection.setDoOutput(true);

                try (OutputStream os = urlConnection.getOutputStream()) {
                    os.write(requestBody.getBytes());
                    os.flush();
                }

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                } else {
                    // Handle server error response
                    response.append("Error response code: ").append(urlConnection.getResponseCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = new StringBuilder("Error: " + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            // Process the JSON response and update UI accordingly
            try {
                JSONObject jsonResponse = new JSONObject(result);
                Log.d("messageadsdaddsa", String.valueOf(jsonResponse));
                // Assume the JSON response has a "message" field containing the reply
                String botMessage = jsonResponse.getString("content");
                // Update your chat interface here with the bot's message
                Message botReply = new Message(botMessage, false);
                messagesList.add(botReply);
                if (chatAdapter != null) {
                    getActivity().runOnUiThread(() -> {
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messagesList.size() - 1);
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing error
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Failed to parse response", Toast.LENGTH_SHORT).show());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        chatRecyclerView = view.findViewById(R.id.chat_recyclerview);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatAdapter = new ChatAdapter(messagesList);
        chatRecyclerView.setAdapter(chatAdapter);

        inputLayout = view.findViewById(R.id.bottom_layout); // Initialize with your actual layout ID
        adjustInputLayoutForKeyboard(view.getRootView());

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return view;
    }

    private void adjustInputLayoutForKeyboard(View rootView) {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getHeight();
            int keypadHeight = screenHeight - r.bottom;

            // Assuming the bottom navigation bar is 56dp high
            final float scale = getContext().getResources().getDisplayMetrics().density;
            int bottomNavBarHeight = (int) (30 * scale + 0.5f); // Convert 56dp to pixels

            if (keypadHeight > screenHeight * 0.15) { // Keyboard is open
                inputLayout.setPadding(0, 0, 0, keypadHeight - bottomNavBarHeight);
            } else { // Keyboard is closed
                inputLayout.setPadding(0, 0, 0, 0);
            }
        });
    }


    private void sendMessage() {
        final String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Preparing the JSON body for the API request
            // Adjust the requestBody structure as per your API requirements
            String apiUrl = "https://api.chatpdf.com/v1/chats/message"; // Replace with your actual chatbot API endpoint
            String requestBody = "{\"sourceId\": \"cha_XYOVt6WC3Jc7HaHG0lZdB\",\"messages\": [{\"role\": \"user\",\"content\": \""+messageText+"\"}]}"; // Example JSON request body

            // Update the chat interface immediately with the user's message
            Message userMessage = new Message(messageText, true);
            messagesList.add(userMessage);
            hideKeyboard();
            if (chatAdapter != null) {
                getActivity().runOnUiThread(() -> {
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(messagesList.size() - 1);
                });
            }
            messageInput.setText(""); // Clear the input field

            // Use ApiCallTask to send the message and handle the response
            new ApiCallTask() {
                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    // This is where you would handle the API response.
                    // The base implementation updates the chat with the response.
                    // See the updated ApiCallTask class for details on how it's done.
                }
            }.execute(apiUrl, requestBody);
        } else {
            Toast.makeText(getActivity(), "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }


    private void hideKeyboard() {
        if (getView() != null && getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    // You need to create a Message class and ChatAdapter as per your requirement.
}
