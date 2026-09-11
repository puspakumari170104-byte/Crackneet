package com.crackneet.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity {
    private final ArrayList<Question> all = new ArrayList<>();
    private ArrayList<Question> current = new ArrayList<>();
    private int index = 0, score = 0, answered = 0;
    private TextView title, meta, question, progress, result, explanation;
    private RadioGroup options;
    private Button action;
    private String exam = "";
    private String subjectFilter = "All";
    private String typeFilter = "All";
    private String difficultyFilter = "All";
    private String chapterFilter = "All";

    static class Question {
        String exam, subject, chapter, type, text, solution;
        String[] options; int answer;
        Question(JSONObject o) throws Exception {
            exam=o.getString("exam"); subject=o.getString("subject"); chapter=o.getString("chapter");
            type=o.getString("type"); text=o.getString("q"); solution=o.optString("solution","");
            JSONArray a=o.getJSONArray("options"); options=new String[a.length()];
            for(int i=0;i<a.length();i++) options[i]=a.getString(i);
            answer=o.getInt("answer");
        }
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        title=findViewById(R.id.title); meta=findViewById(R.id.meta); question=findViewById(R.id.question);
        progress=findViewById(R.id.progress); result=findViewById(R.id.result);
        explanation=findViewById(R.id.explanation); options=findViewById(R.id.options); action=findViewById(R.id.action);
        loadQuestions();
        showHome();
    }

    private void loadQuestions() {
        try {
            InputStream in=getAssets().open("question_bank.json");
            byte[] bytes=new byte[in.available()]; in.read(bytes); in.close();
            JSONArray arr=new JSONObject(new String(bytes, StandardCharsets.UTF_8)).getJSONArray("questions");
            for(int i=0;i<arr.length();i++) all.add(new Question(arr.getJSONObject(i)));
        } catch(Exception e) { Toast.makeText(this,"Question bank load error",Toast.LENGTH_LONG).show(); }
    }

    private void showHome() {
        title.setText("CrackNEET");
        meta.setText("NEET + JEE Main • PYQ-pattern practice");
        question.setText("Choose an exam to start practice");
        progress.setText(all.size()+" questions available");
        options.removeAllViews(); result.setText(""); explanation.setText("");
        action.setText("NEET Practice");
        action.setOnClickListener(v -> chooseFilters("NEET"));
        Button jee=findViewById(R.id.secondary);
        jee.setVisibility(View.VISIBLE); jee.setText("JEE Main Practice");
        jee.setOnClickListener(v -> chooseFilters("JEE Main"));
    }

    private void chooseFilters(String e) {
        exam=e;
        final String[] subjects = {"All","Physics","Chemistry","Biology","Mathematics"};
        new AlertDialog.Builder(this).setTitle("Choose subject")
            .setSingleChoiceItems(subjects,0,(d,w)->{
                subjectFilter=subjects[w]; d.dismiss();
                ArrayList<String> chapters=new ArrayList<>(); chapters.add("All");
                for(Question q:all) if(q.exam.equals(e) && (subjectFilter.equals("All") || q.subject.equals(subjectFilter)) && !chapters.contains(q.chapter)) chapters.add(q.chapter);
                new AlertDialog.Builder(this).setTitle("Choose chapter")
                    .setSingleChoiceItems(chapters.toArray(new String[0]),0,(d2,x)->{
                        chapterFilter=chapters.get(x); d2.dismiss();
                        final String[] types={"All","MCQ","Statement","Assertion-Reason","Numerical"};
                        new AlertDialog.Builder(this).setTitle("Question type")
                            .setSingleChoiceItems(types,0,(d3,y)->{
                                typeFilter=types[y]; d3.dismiss();
                                final String[] levels={"All","Easy","Moderate","Hard"};
                                new AlertDialog.Builder(this).setTitle("Difficulty")
                                    .setSingleChoiceItems(levels,0,(d4,z)->{ difficultyFilter=levels[z]; d4.dismiss(); start(exam); }).show();
                            }).show();
                    }).show();
            }).show();
    }

    private void start(String e) {
        exam=e; current=new ArrayList<>();
        for(Question q:all) if(q.exam.equals(e)) current.add(q);
        Collections.shuffle(current); index=0; score=0; answered=0;
        findViewById(R.id.secondary).setVisibility(View.GONE);
        action.setText("Next Question");
        action.setOnClickListener(v -> next());
        render();
    }

    private void render() {
        if(index>=current.size()) { finishQuiz(); return; }
        Question q=current.get(index);
        meta.setText(q.exam+" • "+q.subject+" • "+q.chapter+" • "+q.type);
        question.setText(q.text); progress.setText("Question "+(index+1)+" / "+current.size());
        result.setText(""); explanation.setText(""); options.removeAllViews();
        for(int i=0;i<q.options.length;i++){ RadioButton r=new RadioButton(this); r.setText(q.options[i]); r.setTextSize(16); r.setPadding(4,12,4,12); options.addView(r); }
        action.setText(index==current.size()-1?"Finish":"Next");
    }

    private void next() {
        if(options.getCheckedRadioButtonId()==-1){ Toast.makeText(this,"Please select an answer",Toast.LENGTH_SHORT).show(); return; }
        int chosen=options.indexOfChild(findViewById(options.getCheckedRadioButtonId()));
        Question q=current.get(index);
        answered++; if(chosen==q.answer){score++; result.setText("✓ Correct");} else result.setText("✗ Incorrect • Correct answer: "+q.options[q.answer]);
        explanation.setText(q.solution);
        index++; render();
    }

    private void finishQuiz() {
        question.setText("Test completed");
        meta.setText(exam+" Practice");
        progress.setText("Score: "+score+" / "+answered+" correct • "+current.size()+" questions");
        options.removeAllViews(); result.setText("Great job! Review the explanations and practice again.");
        explanation.setText("");
        action.setText("Back to Home"); action.setOnClickListener(v -> showHome());
    }
}