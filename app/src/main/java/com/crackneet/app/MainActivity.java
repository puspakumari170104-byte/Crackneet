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
    private int index=0, score=0, answered=0, attempted=0;
    private final ArrayList<Integer> selectedAnswers=new ArrayList<>();
    private long startTime, testTimeMs=30*60*1000L;
    private TextView title,meta,question,progress,result,explanation,timer,filterInfo;
    private RadioGroup options;
    private Button action,reviewButton;
    private String exam="",subjectFilter="All",typeFilter="All",difficultyFilter="All",chapterFilter="All";
    private final Handler timerHandler=new Handler();
    private final Runnable timerRunnable=new Runnable(){public void run(){
        if(current.isEmpty()) return;
        long remaining=testTimeMs-(System.currentTimeMillis()-startTime);
        if(remaining<=0){finishQuiz();return;}
        updateTimer(remaining); timerHandler.postDelayed(this,1000);
    }};

    static class Question {
        String exam,subject,chapter,type,difficulty,text,solution; String[] options; int answer;
        Question(JSONObject o)throws Exception{
            exam=o.getString("exam");subject=o.getString("subject");chapter=o.getString("chapter");
            type=o.getString("type");difficulty=o.optString("difficulty","Moderate");
            text=o.getString("q");solution=o.optString("solution","");
            JSONArray a=o.getJSONArray("options");options=new String[a.length()];
            for(int i=0;i<a.length();i++)options[i]=a.getString(i);answer=o.getInt("answer");
        }
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);setContentView(R.layout.activity_main);
        title=findViewById(R.id.title);meta=findViewById(R.id.meta);question=findViewById(R.id.question);
        progress=findViewById(R.id.progress);result=findViewById(R.id.result);explanation=findViewById(R.id.explanation);
        options=findViewById(R.id.options);action=findViewById(R.id.action);timer=findViewById(R.id.timer);
        filterInfo=findViewById(R.id.filterInfo);loadQuestions();showHome();
    }

    private void loadQuestions(){
        try{
            InputStream in=getAssets().open("question_bank.json");
            byte[] bytes=new byte[in.available()];int read=0,n;
            while((n=in.read(bytes,read,bytes.length-read))>0)read+=n;
            in.close();
            JSONArray arr=new JSONObject(new String(bytes,0,read,StandardCharsets.UTF_8)).getJSONArray("questions");
            for(int i=0;i<arr.length();i++)all.add(new Question(arr.getJSONObject(i)));
            if(all.isEmpty())throw new Exception("Empty question bank");
        }catch(Exception e){
            Toast.makeText(this,"Question bank unavailable",Toast.LENGTH_LONG).show();
        }
    }

    private void showHome(){
        timerHandler.removeCallbacks(timerRunnable);
        title.setText("CrackNEET");meta.setText("NEET + JEE Main • Smart practice");
        question.setText("Select an exam to begin");progress.setText(all.size()+" questions available");
        timer.setText("Time left: --:--:--");filterInfo.setText("Quick Practice • All subjects");
        options.removeAllViews();result.setText("");explanation.setText("");
        subjectFilter="All";chapterFilter="All";typeFilter="All";difficultyFilter="All";
        action.setText("Start NEET Practice");action.setOnClickListener(v->chooseTime("NEET"));
        if(reviewButton!=null){((LinearLayout)reviewButton.getParent()).removeView(reviewButton);reviewButton=null;}
        Button jee=findViewById(R.id.secondary);jee.setVisibility(View.VISIBLE);
        jee.setText("Start JEE Main Practice");jee.setOnClickListener(v->chooseTime("JEE Main"));
    }

    private void chooseTime(String e){
        final String[] labels={"30 minutes","60 minutes","90 minutes"};
        final long[] times={1800000L,3600000L,5400000L};
        new AlertDialog.Builder(this).setTitle("Choose test duration")
            .setSingleChoiceItems(labels,0,(d,w)->{testTimeMs=times[w];d.dismiss();start(e);}).show();
    }

    private boolean matchesType(Question q){
        if(typeFilter.equals("All"))return true;
        if(typeFilter.equals("Statement"))return q.type.equalsIgnoreCase("Statement")||q.type.equalsIgnoreCase("Statement Based");
        if(typeFilter.equals("Assertion-Reason"))return q.type.replace("–","-").equalsIgnoreCase("Assertion-Reason");
        return q.type.equalsIgnoreCase(typeFilter);
    }

    private void start(String e){
        exam=e;current=new ArrayList<>();
        for(Question q:all)if(q.exam.equals(e)&&(subjectFilter.equals("All")||q.subject.equals(subjectFilter))&&
            (chapterFilter.equals("All")||q.chapter.equals(chapterFilter))&&matchesType(q)&&
            (difficultyFilter.equals("All")||q.difficulty.equalsIgnoreCase(difficultyFilter)))current.add(q);
        if(current.isEmpty()){
            Toast.makeText(this,"No questions found for "+e,Toast.LENGTH_LONG).show();showHome();return;
        }
        Collections.shuffle(current);startTime=System.currentTimeMillis();index=0;score=0;answered=0;attempted=0;selectedAnswers.clear();
        if(reviewButton!=null){((LinearLayout)reviewButton.getParent()).removeView(reviewButton);reviewButton=null;}
        findViewById(R.id.secondary).setVisibility(View.GONE);filterInfo.setText(e+" • All subjects • Mixed practice");
        action.setOnClickListener(v->next());render();timerHandler.removeCallbacks(timerRunnable);timerHandler.post(timerRunnable);
    }

    private void updateTimer(long r){timer.setText(String.format(Locale.US,"Time left: %02d:%02d:%02d",r/3600000,(r/60000)%60,(r/1000)%60));}

    private void render(){
        if(index>=current.size()){finishQuiz();return;}
        Question q=current.get(index);question.setText(q.text);meta.setText(q.exam+" • "+q.subject+" • "+q.chapter);
        progress.setText("Question "+(index+1)+" / "+current.size());result.setText("");explanation.setText("");options.removeAllViews();
        for(String s:q.options){RadioButton r=new RadioButton(this);r.setText(s);r.setTextSize(16);r.setPadding(4,12,4,12);options.addView(r);}
        action.setText(index==current.size()-1?"Finish":"Next");
    }

    private void next(){
        int id=options.getCheckedRadioButtonId();
        if(id==-1){Toast.makeText(this,"Select an answer first",Toast.LENGTH_SHORT).show();return;}
        int chosen=options.indexOfChild(findViewById(id));Question q=current.get(index);
        attempted++;answered++;selectedAnswers.add(chosen);if(chosen==q.answer)score++;
        index++;render();
    }

    private void showReview(){
        question.setText("Answer Review");meta.setText(exam+" • "+current.size()+" questions");options.removeAllViews();result.setText("");
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<current.size();i++){Question q=current.get(i);String ans=i<selectedAnswers.size()?q.options[selectedAnswers.get(i)]:"Not attempted";
            sb.append("Q").append(i+1).append(": ").append(q.text).append("\nYour answer: ").append(ans)
              .append("\nCorrect: ").append(q.options[q.answer]).append("\n").append(q.solution).append("\n\n");}
        explanation.setText(sb.toString());action.setText("Back to Result");action.setOnClickListener(v->finishQuiz());
    }

    private void finishQuiz(){
        timerHandler.removeCallbacks(timerRunnable);question.setText("Test completed");meta.setText(exam+" Practice");
        progress.setText("Score: "+score+" / "+current.size());result.setText("Accuracy: "+(current.isEmpty()?0:score*100/current.size())+"%");
        options.removeAllViews();explanation.setText("Attempted: "+attempted+"   Correct: "+score+"   Total: "+current.size());
        if(reviewButton!=null)((LinearLayout)reviewButton.getParent()).removeView(reviewButton);
        reviewButton=new Button(this);reviewButton.setText("Review Answers");reviewButton.setOnClickListener(v->showReview());
        ((LinearLayout)action.getParent()).addView(reviewButton);action.setText("Retry Test");action.setOnClickListener(v->start(exam));
        findViewById(R.id.secondary).setVisibility(View.GONE);
    }

    @Override protected void onDestroy(){timerHandler.removeCallbacks(timerRunnable);super.onDestroy();}
}