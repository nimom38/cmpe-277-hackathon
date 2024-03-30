package com.cmpe277.macroeconomicfoodsecurity;

import androidx.appcompat.app.AppCompatActivity;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.GraphView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.cmpe277.macroeconomicfoodsecurity.sqldb.DBController;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;
import java.util.HashMap;

public class TradeResult extends AppCompatActivity implements AdapterView.OnItemSelectedListener,View.OnClickListener {

    String[] countries = {"India", "China", "USA"};
    GraphView graph;
    DBController controller;
    ListAdapter adapter;
    ArrayList<HashMap<String, String>> myList;

    EditText startYearEd;
    EditText endYearEd;
    Button apply;
    Button annotation;
    Button annotationShow;

    String country="";
    String fromYear="";
    String toYear="";
    String reserves="";
    String gni="";
    String totalDebt="";
    String gni_curr="";

    private static final String SHARED_PREFS_KEY_USER = "user_type";

    private static final String SHARED_PREFS_KEY = "notes-trade";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_result);
        country = getIntent().getExtras().getString("country");
        System.out.println(country);
        fromYear = getIntent().getExtras().getString("fromYear");
        System.out.println(fromYear);
        toYear = getIntent().getExtras().getString("toYear");
        System.out.println(toYear);
        reserves = getIntent().getExtras().getString("reserves");
        System.out.println(reserves);
        gni = getIntent().getExtras().getString("gni");
        System.out.println(gni);
        totalDebt = getIntent().getExtras().getString("totalDebt");
        System.out.println(totalDebt);
        gni_curr = getIntent().getExtras().getString("gni_curr");
        System.out.println(gni_curr);

        apply = (Button) findViewById(R.id.apply);
        apply.setOnClickListener(this);

        annotation = (Button) findViewById(R.id.annotation);
        annotationShow = (Button) findViewById(R.id.annotationShow);
        annotation.setOnClickListener(this);
        annotationShow.setOnClickListener(this);

        if(!getUserType(this).equals("researcher")){
            annotation.setVisibility(View.GONE);
//            annotationShow.setVisibility(View.GONE);
        }

        Spinner spinnerLanguages = findViewById(R.id.spinner_country);
        ArrayAdapter adapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                countries);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerLanguages.setAdapter(adapter);
        spinnerLanguages.setOnItemSelectedListener(this);

        startYearEd =  (EditText) findViewById(R.id.Start_year);
        endYearEd =  (EditText) findViewById(R.id.End_year);


        graph = (GraphView) findViewById(R.id.graph);
        graph.setVisibility(View.VISIBLE);
        graph.onDataChanged(true,true);

        controller = new DBController(this);

        myList = controller.getAllProducts(country, fromYear, toYear, reserves, gni, totalDebt, gni_curr);
        loadGraph(myList);

    }

    private void loadGraph(ArrayList<HashMap<String, String>> myList) {
        int i = 0;
        if (myList.size() != 0) {
            DataPoint init = new DataPoint(0, 1);
            DataPoint[] dataPoints_reserve = new DataPoint[myList.size()];
            DataPoint[] dataPoints_in = new DataPoint[myList.size()];
            DataPoint[] dataPoints_out = new DataPoint[myList.size()];
            DataPoint[] dataPoints_net = new DataPoint[myList.size()];
//            dataPoints_reserve[0] = init;
            try {
                Double max_in =Double.MIN_VALUE;
                Double max_out =Double.MIN_VALUE;
                Double max_net =Double.MIN_VALUE;

                while (i < myList.size()) {


                    if(reserves.equalsIgnoreCase("Yes")){
                        Double x = Double.valueOf(myList.get(i).get("a"));
                        Double y = Double.valueOf(myList.get(i).get("j"));
                        DataPoint dp = new DataPoint(x, y);
                        dataPoints_reserve[i] = dp;
                    }
                     if(gni.equalsIgnoreCase("Yes")){

                        Double x = Double.valueOf(myList.get(i).get("a"));
                        Double y = Double.valueOf(myList.get(i).get("k"));


                        DataPoint dp = new DataPoint(x, y*100000000);
                        dataPoints_in[i] = dp;

                        if(y>max_in){
                            max_in = y;
                        }
                    }
                    System.out.println("Max value of y "+max_in);
                    if(totalDebt.equalsIgnoreCase("Yes")){
                        Double x = Double.valueOf(myList.get(i).get("a"));
                        Double y = Double.valueOf(myList.get(i).get("l"));
                        DataPoint dp = new DataPoint(x, y);
                        dataPoints_out[i] = dp;

                        if(y>max_out){
                            max_out = y;
                        }
                    }
                    if(gni_curr.equalsIgnoreCase("Yes")){
                        Double x = Double.valueOf(myList.get(i).get("a"));
                        Double y = Double.valueOf(myList.get(i).get("m"));
                        DataPoint dp = new DataPoint(x, y);
                        dataPoints_net[i] = dp;

                        if(y>max_net){
                            max_net = y;
                        }
                    }

                    i++;
                }
                if(dataPoints_reserve[0]!=null){
                    LineGraphSeries<DataPoint> series_gdp = new LineGraphSeries<>(dataPoints_reserve);
                    graph.addSeries(series_gdp);
                }

                if(dataPoints_in[0]!=null){
                    LineGraphSeries<DataPoint> series_in = new LineGraphSeries<>(dataPoints_in);
                    graph.addSeries(series_in);
                    series_in.setColor(Color.RED);
                }

                if(dataPoints_out[0]!=null){
                    LineGraphSeries<DataPoint> series_out = new LineGraphSeries<>(dataPoints_out);
                    graph.addSeries(series_out);
                    series_out.setColor(Color.GREEN);
                }
                if(dataPoints_net[0]!=null){
                    LineGraphSeries<DataPoint> series_net = new LineGraphSeries<>(dataPoints_net);
                    graph.addSeries(series_net);
                    series_net.setColor(Color.YELLOW);
                }






            } catch (IllegalArgumentException e) {
                Toast.makeText(TradeResult.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int position, long id) {
        switch (position) {
            case 1:
                Log.d("Macro","China");
                country = "China";
                break;
            case 0:
                Log.d("Macro","India");
                country = "India";
                break;
            case 2:
                Log.d("Macro","USA");
                country = "USA";
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {

//        String fromYear = startYearEd.getText().toString();
//        String toYear = endYearEd.getText().toString();
//        ArrayList<HashMap<String, String>> myListApply;
//        myListApply = controller.getAllProducts(country, fromYear, toYear, reserves, gni, totalDebt, gni_curr);
//        loadGraph(myListApply);

        switch (view.getId()){
            case R.id.apply:
                String fromYear = startYearEd.getText().toString();
                String toYear = endYearEd.getText().toString();
                ArrayList<HashMap<String, String>> myListApply;
                myListApply = controller.getAllProducts(country, fromYear, toYear, reserves, gni, totalDebt, gni_curr);
                loadGraph(myListApply);
                break;
            case R.id.annotation:
                showNoteDialog();
//                String note = noteEditText.getText().toString().trim();
//                saveNote(note);
                break;
            case R.id.annotationShow:
                retrieveNotes();
                break;


        }
    }

    private void showNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_note, null);
        builder.setView(dialogView);

        final EditText noteEditText = dialogView.findViewById(R.id.noteEditText);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String note = noteEditText.getText().toString().trim();
            if (!note.isEmpty()) {
                saveNote(note);
            } else {
                Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveNote(String note) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Generate a unique key for each note by using the current timestamp
        String key = String.valueOf(System.currentTimeMillis());
        editor.putString(key, note);
        editor.apply();
        Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void retrieveNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        StringBuilder notes = new StringBuilder();

        for (String key : sharedPreferences.getAll().keySet()) {
            String note = sharedPreferences.getString(key, "");
            notes.append(note).append("\n");
        }

        if (notes.length() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Saved Notes");
            builder.setMessage(notes.toString());
            builder.setPositiveButton("OK", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            Toast.makeText(this, "No notes found", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getUserType(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_KEY_USER, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SHARED_PREFS_KEY_USER, "researcher");
    }
}