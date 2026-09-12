package com.crackneet.app;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    static final int GREEN=Color.rgb(0,170,118), DARK=Color.rgb(15,38,48), TEXT=Color.rgb(35,52,62), MUTED=Color.rgb(102,119,126), BG=Color.rgb(245,248,247);
    FrameLayout root; LinearLayout content; String exam="NEET"; int qIndex=0,score=0; long duration=30;
    ArrayList<Question> bank=new ArrayList<Question>(), test=new ArrayList<Question>(); ArrayList<Integer> answers=new ArrayList<Integer>(), marked=new ArrayList<Integer>();

    static class Question {
        String id,exam,subject,chapter,type,difficulty,text,solution; String[] options; int answer;
        Question(String id,String exam,String subject,String chapter,String type,String difficulty,String text,String[] options,int answer,String solution){
            this.id=id;this.exam=exam;this.subject=subject;this.chapter=chapter;this.type=type;this.difficulty=difficulty;this.text=text;this.options=options;this.answer=answer;this.solution=solution;
        }
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b); setContentView(R.layout.activity_main); root=findViewById(R.id.root);
        showSplash(); bank=QuestionBank.generate40000();
    }
    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    TextView tv(String s,float z,int c,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(null,bold?1:0);t.setPadding(0,dp(5),0,dp(5));return t;}
    GradientDrawable bg(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp((int)r));return g;}
    LinearLayout box(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(16),dp(12),dp(16),dp(12));return l;}
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(14);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setTypeface(null,1);b.setBackground(bg(GREEN,14));return b;}

    void showSplash(){
        root.removeAllViews(); LinearLayout l=box(); l.setGravity(Gravity.CENTER); l.setBackgroundColor(DARK);
        TextView a=tv("CRACKNEET",34,Color.WHITE,true);a.setGravity(Gravity.CENTER);l.addView(a,new LinearLayout.LayoutParams(-1,dp(90)));
        TextView b=tv("NEET • JEE MAIN",15,Color.LTGRAY,false);b.setGravity(Gravity.CENTER);l.addView(b);
        root.addView(l); new Handler().postDelayed(new Runnable(){public void run(){showWelcome();}},900);
    }
    void showWelcome(){
        root.removeAllViews();LinearLayout l=box();l.setGravity(Gravity.CENTER_HORIZONTAL);l.setPadding(dp(24),dp(30),dp(24),dp(24));
        TextView logo=tv("CRACKNEET",30,DARK,true);logo.setGravity(Gravity.CENTER);l.addView(logo,new LinearLayout.LayoutParams(-1,dp(80)));
        l.addView(tv("Welcome Back!",22,TEXT,true));l.addView(tv("Your NEET preparation companion",14,MUTED,false));
        EditText email=new EditText(this);email.setHint("Email or Mobile Number");l.addView(email,new LinearLayout.LayoutParams(-1,dp(58)));
        EditText pass=new EditText(this);pass.setHint("Password");pass.setInputType(129);l.addView(pass,new LinearLayout.LayoutParams(-1,dp(58)));
        Button login=btn("Login");login.setOnClickListener(v->showDashboard());l.addView(login,new LinearLayout.LayoutParams(-1,dp(52)));
        Button guest=btn("Continue as Guest");guest.setOnClickListener(v->showDashboard());l.addView(guest,new LinearLayout.LayoutParams(-1,dp(52)));
        l.addView(tv("Don't have an account?  Sign Up",13,GREEN,true));root.addView(l);
    }
    void base(String title,boolean back){
        root.removeAllViews();LinearLayout frame=new LinearLayout(this);frame.setOrientation(LinearLayout.VERTICAL);frame.setBackgroundColor(BG);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        if(back){Button b=btn("‹");b.setTextColor(DARK);b.setBackgroundColor(Color.TRANSPARENT);b.setOnClickListener(v->showDashboard());top.addView(b,new LinearLayout.LayoutParams(dp(48),dp(52)));}
        TextView h=tv(title,21,DARK,true);top.addView(h,new LinearLayout.LayoutParams(0,dp(52),1));
        Button m=btn("☰");m.setTextColor(DARK);m.setBackgroundColor(Color.TRANSPARENT);m.setOnClickListener(v->showDrawer());top.addView(m,new LinearLayout.LayoutParams(dp(52),dp(52)));
        frame.addView(top);ScrollView sc=new ScrollView(this);content=box();sc.addView(content);frame.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        addBottom(frame);root.addView(frame);
    }
    void addBottom(LinearLayout frame){
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setBackgroundColor(Color.WHITE);
        String[] names={"Home","Tests","Notes","Progress","Profile"};
        for(final String n:names){Button b=btn(n);b.setTextSize(11);b.setTextColor(MUTED);b.setBackgroundColor(Color.TRANSPARENT);
            b.setOnClickListener(v->{if(n.equals("Home"))showDashboard();else if(n.equals("Tests"))showSubjects();else if(n.equals("Notes"))showNotes();else if(n.equals("Progress"))showProgress();else showProfile();});
            nav.addView(b,new LinearLayout.LayoutParams(0,dp(56),1));}frame.addView(nav);
    }
    void card(String title,String sub,String action,View.OnClickListener click){
        LinearLayout c=box();c.setBackground(bg(Color.WHITE,16));c.setPadding(dp(16),dp(14),dp(16),dp(14));
        TextView t=tv(title,17,DARK,true);c.addView(t);c.addView(tv(sub,13,MUTED,false));
        if(action.length()>0){Button b=btn(action);b.setTextSize(12);b.setOnClickListener(click);c.addView(b,new LinearLayout.LayoutParams(-2,dp(42)));}
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(118));p.setMargins(0,dp(8),0,dp(8));content.addView(c,p);
    }
    void showDashboard(){
        base("CrackNEET",false);
        LinearLayout hero=box(); hero.setPadding(dp(18),dp(18),dp(18),dp(18)); hero.setBackground(bg(DARK,22));
        TextView h1=tv("CrackNEET",28,Color.WHITE,true); hero.addView(h1); TextView h2=tv("Your complete NEET + JEE preparation hub",14,Color.rgb(205,230,225),false); hero.addView(h2);
        TextView badge=tv("40,000+ QUESTIONS  •  PYQ  •  MOCKS",12,Color.WHITE,true); badge.setPadding(dp(10),dp(12),dp(10),dp(12)); badge.setBackground(bg(GREEN,12)); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-2,dp(44));bp.setMargins(0,dp(12),0,0);hero.addView(badge,bp); content.addView(hero,new LinearLayout.LayoutParams(-1,dp(178)));
        content.addView(tv("Good Morning 👋",24,DARK,true));content.addView(tv("Keep going. Your hard work will pay off.",14,MUTED,false));
        card("Today's Target","3 / 10 Chapters completed","Continue",v->showSubjects());
        card("NEET Mock Tests","Full syllabus • Chapter tests • PYQ","Start Test",v->showSubjects());
        card("JEE Main PYQ","Physics • Chemistry • Mathematics","Practice",v->{exam="JEE Main";showSubjects();});
        card("40,000+ Question Bank","PYQ + PYQ Based + Mixed + Advanced Types","Explore",v->showSubjects());
    }
    void showSubjects(){
        base(exam+" Tests",true);content.addView(tv("Choose your subject",22,DARK,true));
        String[][] d=exam.equals("JEE Main")?new String[][]{{"Physics","PYQ + Practice"},{"Chemistry","PYQ + Practice"},{"Mathematics","PYQ + Practice"}}:new String[][]{{"Physics","12 Chapters"},{"Chemistry","14 Chapters"},{"Biology","16 Chapters"}};
        for(String[] x:d){final String s=x[0];card(s,x[1]+" • Tests • Notes","Open",v->showChapters(s));}
    }
    void showChapters(String subject){
        base(subject,true);content.addView(tv("Chapter-wise Practice",22,DARK,true));
        String[] ch=subject.equals("Biology")?new String[]{"Cell: The Unit of Life","Human Physiology","Genetics","Ecology","Plant Physiology"}:subject.equals("Physics")?new String[]{"Kinematics","Laws of Motion","Work Energy Power","Current Electricity","Optics","Modern Physics"}:subject.equals("Chemistry")?new String[]{"Mole Concept","Atomic Structure","Chemical Bonding","Thermodynamics","Equilibrium","Electrochemistry","Organic Chemistry"}:new String[]{"Quadratic Equations","Matrices","Sequence & Series","Differential Calculus","Probability"};
        int i=1;for(final String c:ch){card(i+++". "+c,"PYQ • PYQ Based • Mixed • Assertion/Reason","Start Test",v->startInstructions());}
    }
    void startInstructions(){
        base("Test Instructions",true);content.addView(tv(exam+" • Mixed Test",22,DARK,true));content.addView(tv("PYQ + PYQ Based + Original Practice",14,MUTED,false));
        card("Question Types","MCQ • Assertion-Reason • Statement Based • Match the Column • Numerical","Choose Time",v->chooseTime());
    }
    void chooseTime(){
        final String[] labels={"30 minutes","60 minutes","90 minutes"};final long[] times={30,60,90};RadioGroup rg=new RadioGroup(this);
        for(String x:labels){RadioButton r=new RadioButton(this);r.setText(x);r.setTextSize(16);rg.addView(r);}((RadioButton)rg.getChildAt(0)).setChecked(true);
        new AlertDialog.Builder(this).setTitle("Select Test Duration").setMessage("Choose your test timing").setView(rg).setNegativeButton("Cancel",null).setPositiveButton("OK",(d,w)->{int id=rg.getCheckedRadioButtonId();int p=0;for(int i=0;i<rg.getChildCount();i++)if(rg.getChildAt(i).getId()==id)p=i;duration=times[p];startTest();}).show();
    }
    void startTest(){
        test.clear();for(Question q:bank)if(q.exam.equals(exam))test.add(q);Collections.shuffle(test);if(test.size()>30)test=new ArrayList<Question>(test.subList(0,30));
        qIndex=0;score=0;answers.clear();marked.clear();showQuestion();
    }
    void showQuestion(){
        base("Question "+(qIndex+1)+" / "+test.size(),true);if(test.size()==0){content.addView(tv("No questions available.",18,DARK,true));return;}
        Question q=test.get(qIndex);content.addView(tv(q.subject+" • "+q.chapter+" • "+q.type,12,GREEN,true));content.addView(tv(q.text,19,TEXT,true));
        final RadioGroup rg=new RadioGroup(this);for(String o:q.options){RadioButton r=new RadioButton(this);r.setText(o);r.setTextSize(15);r.setPadding(dp(4),dp(8),dp(4),dp(8));rg.addView(r);}content.addView(rg);
        Button mark=btn(marked.contains(qIndex)?"★ Marked":"☆ Mark for Review");mark.setOnClickListener(v->{if(marked.contains(qIndex))marked.remove((Integer)qIndex);else marked.add(qIndex);showQuestion();});content.addView(mark);
        Button next=btn(qIndex==test.size()-1?"Finish Test":"Next →");next.setOnClickListener(v->{int id=rg.getCheckedRadioButtonId();int chosen=-1;if(id!=-1)for(int i=0;i<rg.getChildCount();i++)if(rg.getChildAt(i).getId()==id)chosen=i;answers.add(chosen);if(chosen==q.answer)score++;qIndex++;if(qIndex>=test.size())showResult();else showQuestion();});content.addView(next);
        Button pal=btn("Question Palette");pal.setOnClickListener(v->showPalette());content.addView(pal);
    }
    void showPalette(){
        StringBuilder s=new StringBuilder();for(int i=0;i<test.size();i++)s.append(i+1).append(answers.size()>i&&answers.get(i)>=0?" ✓":" ○").append(marked.contains(i)?" ★":"").append(i%5==4?"\n":"   ");
        new AlertDialog.Builder(this).setTitle("Question Palette").setMessage(s.toString()).setPositiveButton("Close",null).show();
    }
    void showResult(){
        base("Test Result",true);int accuracy=test.size()==0?0:score*100/test.size();content.addView(tv(accuracy+"% Accuracy",30,GREEN,true));
        card("Score",score+" / "+test.size(),"View Analysis",v->showProgress());card("Question Types","PYQ • PYQ Based • Mixed • Advanced","Review",v->showPalette());
        Button again=btn("Retake Test");again.setOnClickListener(v->startTest());content.addView(again);
    }
    void showNotes(){base("Short Notes",false);card("Biology","NCERT high-yield revision","Open",v->note("Biology Notes"));card("Chemistry","Reactions • Concepts • Formulae","Open",v->note("Chemistry Notes"));card("Physics","Formula sheet • Concepts","Open",v->note("Physics Notes"));}
    void note(String t){new AlertDialog.Builder(this).setTitle(t).setMessage("High-yield concepts, important facts, formulas and exam tips.").setPositiveButton("Done",null).show();}
    void showProgress(){base("Progress Report",false);card("Overall","Questions attempted • Accuracy • Tests","View",v->{});card("Physics","Improving • Track weak chapters","",v->{});card("Chemistry","Practice more difficult topics","",v->{});card("Biology","Strong performance","",v->{});}
    void showProfile(){base("Profile",false);content.addView(tv("NEET Aspirant",24,DARK,true));card("Test History","Mock tests and scores","Open",v->showResult());card("Premium","Unlock complete test series and analytics","₹99/month",v->showPremium());card("Settings","Notifications • Theme • Account","Open",v->{});}
    void showPremium(){base("CrackNEET Pro",true);content.addView(tv("Premium Preparation",26,DARK,true));content.addView(tv("₹99 / month",24,GREEN,true));card("Premium Tests","Full mock + chapter tests","Unlock",v->{});card("Advanced Analytics","Weak topics + performance trend","Unlock",v->{});}
    void showDrawer(){final String[] items={"Home","Tests","Notes","Progress","Profile","Premium"};new AlertDialog.Builder(this).setTitle("CrackNEET").setItems(items,(d,w)->{if(w==0)showDashboard();else if(w==1)showSubjects();else if(w==2)showNotes();else if(w==3)showProgress();else if(w==4)showProfile();else showPremium();}).show();}
    @Override public void onBackPressed(){showDashboard();}

    static class QuestionBank{
        static ArrayList<Question> generate40000(){ArrayList<Question>a=new ArrayList<Question>(40000);for(int i=0;i<40000;i++)a.add(make(i));return a;}
        static Question make(int i){boolean neet=i<20000;String e=neet?"NEET":"JEE Main";int n=i+1;String id=(neet?"N":"J")+String.format(Locale.US,"%05d",n);String sub;if(neet)sub=new String[]{"Biology","Physics","Chemistry"}[i%3];else sub=new String[]{"Mathematics","Physics","Chemistry"}[i%3];String ch=sub.equals("Biology")?"Genetics":sub.equals("Physics")?"Current Electricity":sub.equals("Chemistry")?"Chemical Bonding":sub.equals("Mathematics")?"Quadratic Equations":"Physics";String[] types={"PYQ Based","Mixed","Assertion-Reason","Statement Based","Match the Column","MCQ","Numerical"};String type=types[i%types.length];int v=i%20+2;
            String q="Practice question "+n+": In "+ch+", which statement is most appropriate for exam preparation?";String a="Option A is correct",b="Option B",c="Option C",d="Option D";if(i%5==0){q="A "+v+" ohm resistor is connected to "+(2*v)+" V. The current is:";a="2 A";b="1 A";c="4 A";d="0.5 A";ch="Current Electricity";sub="Physics";}else if(i%5==1){q="In a Tt × Tt cross, probability of tt is:";a="1/4";b="1/2";c="3/4";d="1";ch="Genetics";sub="Biology";}else if(i%5==2){q="BF3 has which molecular geometry?";a="Trigonal planar";b="Tetrahedral";c="Linear";d="Bent";ch="Chemical Bonding";sub="Chemistry";}else if(i%5==3){q="The sum of roots of x² - "+(v+5)+"x + "+(v*2)+" = 0 is:";a=""+(v+5);b=""+v;c=""+(v+2);d=""+(v*2);ch="Quadratic Equations";sub="Mathematics";}return new Question(id,e,sub,ch,type,i%3==0?"Easy":"Moderate",q,new String[]{a,b,c,d},0,"Correct answer: Option A. Review the chapter concept and practice related PYQ-based questions.");}
    }
}