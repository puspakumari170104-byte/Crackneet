package com.crackneet.app;

import android.app.AlertDialog;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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
    private final ArrayList<Integer> selectedAnswers = new ArrayList<>();
    private int attempted = 0;
    private long startTime;
    private long testTimeMs = 30 * 60 * 1000L;
    private int questionLimit = 0;
    private TextView title, meta, question, progress, result, explanation, timer, filterInfo;
    private RadioGroup options;
    private Button action, reviewButton;
    private String exam = "";
    private String subjectFilter = "All";
    private String typeFilter = "All";
    private String difficultyFilter = "All";
    private String chapterFilter = "All";
    private final Handler timerHandler = new Handler();
    private final Runnable timerRunnable = new Runnable() {
        @Override public void run() {
            if (current.isEmpty()) return;
            long remaining = testTimeMs - (System.currentTimeMillis() - startTime);
            if (remaining <= 0) {
                finishQuiz();
                return;
            }
            updateTimer(remaining);
            timerHandler.postDelayed(this, 1000);
        }
    };

    static class Question {
        String exam, subject, chapter, type, difficulty, text, solution;
        String[] options; int answer;
        Question(JSONObject o) throws Exception {
            exam=o.getString("exam"); subject=o.getString("subject"); chapter=o.getString("chapter");
            type=o.getString("type"); difficulty=o.optString("difficulty","Moderate");
            text=o.getString("q"); solution=o.optString("solution","");
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
        explanation=findViewById(R.id.explanation); options=findViewById(R.id.options);
        action=findViewById(R.id.action); timer=findViewById(R.id.timer); filterInfo=findViewById(R.id.filterInfo);
        loadQuestions();
        showHome();
    }

    private void loadQuestions() {
        try {
            InputStream in=getAssets().open("question_bank.json");
            byte[] bytes=new byte[in.available()]; in.read(bytes); in.close();
            JSONArray arr=new JSONObject(new String(bytes, StandardCharsets.UTF_8)).getJSONArray("questions");
            for(int i=0;i<arr.length();i++) all.add(new Question(arr.getJSONObject(i)));
        } catch(Exception e) {
            Toast.makeText(this,"Question bank load error",Toast.LENGTH_LONG).show();
        }
    }

    private void showHome() {
        timerHandler.removeCallbacks(timerRunnable);
        title.setText("CrackNEET");
        meta.setText("NEET + JEE Main • Smart practice");
        question.setText("Choose an exam to start practice");
        progress.setText(all.size()+" questions available");
        timer.setText("Time left: --:--:--");
        filterInfo.setText("All subjects • All chapters • All types");
        options.removeAllViews(); result.setText(""); explanation.setText("");
        action.setText("NEET Practice");
        action.setOnClickListener(v -> chooseTime("NEET"));
        if(reviewButton != null) { ((LinearLayout)reviewButton.getParent()).removeView(reviewButton); reviewButton=null; }
        Button jee=findViewById(R.id.secondary);
        jee.setVisibility(View.VISIBLE);
        jee.setText("JEE Main Practice");
        jee.setOnClickListener(v -> chooseTime("JEE Main"));
    }

    private void chooseTime(String e) {
        final String[] labels={"30 minutes","60 minutes","90 minutes","3 hours (180 questions)"};
        final long[] times={30L*60*1000,60L*60*1000,90L*60*1000,3L*60*60*1000};
        new AlertDialog.Builder(this).setTitle("Choose test duration")
            .setSingleChoiceItems(labels,0,(d,w)->{
                testTimeMs=times[w]; questionLimit=(w==3?180:0);
                d.dismiss(); chooseFilters(e);
            }).show();
    }

    private void chooseFilters(String e) {
        exam=e;
        final String[] subjects = {"All","Physics","Chemistry","Biology","Mathematics"};
        new AlertDialog.Builder(this).setTitle("Choose subject")
            .setSingleChoiceItems(subjects,0,(d,w)->{
                subjectFilter=subjects[w]; d.dismiss();
                ArrayList<String> chapters=new ArrayList<>(); chapters.add("All");
                for(Question q:all) if(q.exam.equals(e) &&
                    (subjectFilter.equals("All") || q.subject.equals(subjectFilter)) &&
                    !chapters.contains(q.chapter)) chapters.add(q.chapter);
                new AlertDialog.Builder(this).setTitle("Choose chapter")
                    .setSingleChoiceItems(chapters.toArray(new String[0]),0,(d2,x)->{
                        chapterFilter=chapters.get(x); d2.dismiss();
                        final String[] types={"All","MCQ","Statement","Assertion-Reason","Numerical"};
                        new AlertDialog.Builder(this).setTitle("Question type")
                            .setSingleChoiceItems(types,0,(d3,y)->{
                                typeFilter=types[y]; d3.dismiss();
                                final String[] levels={"All","Easy","Moderate","Hard"};
                                new AlertDialog.Builder(this).setTitle("Difficulty")
                                    .setSingleChoiceItems(levels,0,(d4,z)->{
                                        difficultyFilter=levels[z]; d4.dismiss(); start(exam);
                                    }).show();
                            }).show();
                    }).show();
            }).show();
    }

    private void start(String e) {
        exam=e; current=new ArrayList<>();
        for(Question q:all) if(q.exam.equals(e) &&
            (subjectFilter.equals("All") || q.subject.equals(subjectFilter)) &&
            (chapterFilter.equals("All") || q.chapter.equals(chapterFilter)) &&
            (typeFilter.equals("All") || q.type.equals(typeFilter)) &&
            (difficultyFilter.equals("All") || q.difficulty.equals(difficultyFilter))) current.add(q);

        if(current.isEmpty()) {
            Toast.makeText(this,"No questions match these filters. Try All.",Toast.LENGTH_LONG).show();
            showHome();
            return;
        }

        Collections.shuffle(current);
        if(questionLimit>0 && current.size()>questionLimit)
            current=new ArrayList<>(current.subList(0,questionLimit));

        startTime=System.currentTimeMillis();
        index=0; score=0; answered=0; attempted=0; selectedAnswers.clear();
        if(reviewButton != null) { ((LinearLayout)reviewButton.getParent()).removeView(reviewButton); reviewButton=null; }
        findViewById(R.id.secondary).setVisibility(View.GONE);
        filterInfo.setText(subjectFilter+" • "+chapterFilter+" • "+typeFilter+" • "+difficultyFilter);
        action.setOnClickListener(v -> next());
        render();
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.post(timerRunnable);
    }

    private void updateTimer(long remaining) {
        timer.setText(String.format(Locale.US,"Time left: %02d:%02d:%02d",
            remaining/3600000,(remaining/60000)%60,(remaining/1000)%60));
    }

    private void render() {
        if(index>=current.size()) { finishQuiz(); return; }
        long remaining=Math.max(0,testTimeMs-(System.currentTimeMillis()-startTime));
        if(remaining<=0) { finishQuiz(); return; }
        updateTimer(remaining);

        Question q=current.get(index);
        meta.setText(q.exam+" • "+q.subject+" • "+q.chapter+" • "+q.type);
        question.setText(q.text);
        progress.setText("Question "+(index+1)+" / "+current.size());
        result.setText(""); explanation.setText(""); options.removeAllViews();

        for(int i=0;i<q.options.length;i++){
            RadioButton r=new RadioButton(this);
            r.setText(q.options[i]); r.setTextSize(16); r.setPadding(4,12,4,12);
            options.addView(r);
        }
        action.setText(index==current.size()-1?"Finish":"Next");
    }

    private void next() {
        if(options.getCheckedRadioButtonId()==-1){
            Toast.makeText(this,"Please select an answer",Toast.LENGTH_SHORT).show();
            return;
        }
        int chosen=options.indexOfChild(findViewById(options.getCheckedRadioButtonId()));
        Question q=current.get(index);
        answered++; attempted++; selectedAnswers.add(chosen);
        if(chosen==q.answer) { score++; result.setText("✓ Correct"); }
        else result.setText("✗ Incorrect • Correct answer: "+q.options[q.answer]);
        explanation.setText(q.solution);
        index++;
        render();
    }

    private void showReview() {
        question.setText("Answer Review");
        meta.setText(exam+" • "+current.size()+" questions");
        options.removeAllViews(); result.setText("");
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<current.size();i++){
            Question q=current.get(i);
            String ans=(i<selectedAnswers.size()) ? q.options[selectedAnswers.get(i)] : "Not attempted";
            sb.append("Q").append(i+1).append(": ").append(q.text).append("\n");
            sb.append("Your answer: ").append(ans).append("\n");
            sb.append("Correct: ").append(q.options[q.answer]).append("\n");
            if(!q.solution.isEmpty()) sb.append(q.solution).append("\n");
            sb.append("\n");
        }
        explanation.setText(sb.toString());
        action.setText("Back to Result");
        action.setOnClickListener(v -> finishQuiz());
    }

    private void finishQuiz() {
        timerHandler.removeCallbacks(timerRunnable);
        question.setText("Test completed");
        meta.setText(exam+" Practice");
        progress.setText("Score: "+score+" / "+answered+" correct • "+current.size()+" questions");
        options.removeAllViews();
        result.setText("Accuracy: "+(answered==0?0:(score*100/answered))+"%");
        explanation.setText("Correct: "+score+"   Attempted: "+attempted+"   Total: "+current.size()+"\n\nReview your answers or start again.");

        if(reviewButton != null) ((LinearLayout)reviewButton.getParent()).removeView(reviewButton);
        reviewButton=new Button(this);
        reviewButton.setText("Review Answers");
        reviewButton.setOnClickListener(v -> showReview());
        ((LinearLayout)action.getParent()).addView(reviewButton);

        action.setText("Retry Test");
        action.setOnClickListener(v -> start(exam));
        findViewById(R.id.secondary).setVisibility(View.GONE);
    }

    @Override protected void onDestroy() {
        timerHandler.removeCallbacks(timerRunnable);
        super.onDestroy();
    }
}