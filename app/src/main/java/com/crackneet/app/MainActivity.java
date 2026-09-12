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
    void lift(View v,float z){v.setElevation(dp((int)z));}
    LinearLayout box(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(16),dp(12),dp(16),dp(12));return l;}
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(14);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setTypeface(null,1);b.setBackground(bg(GREEN,14));return b;}

    void showSplash(){
        root.removeAllViews(); LinearLayout l=box(); l.setGravity(Gravity.CENTER); l.setBackgroundResource(R.drawable.hero_gradient);
        ImageView cap=new ImageView(this);cap.setImageResource(R.drawable.ic_crackneet);cap.setScaleType(ImageView.ScaleType.CENTER_INSIDE);l.addView(cap,new LinearLayout.LayoutParams(-1,dp(92)));
        TextView a=tv("Crack",34,Color.WHITE,true);a.setGravity(Gravity.CENTER);TextView n=tv("NEET",34,GREEN,true);n.setGravity(Gravity.CENTER);
        LinearLayout brand=new LinearLayout(this);brand.setGravity(Gravity.CENTER);brand.addView(a);brand.addView(n);l.addView(brand,new LinearLayout.LayoutParams(-1,dp(60)));
        TextView sub=tv("Your NEET + JEE Preparation Companion",14,Color.rgb(200,220,220),false);sub.setGravity(Gravity.CENTER);l.addView(sub);
        TextView tag=tv("PREPARE   •   PRACTICE   •   IMPROVE",11,GREEN,true);tag.setGravity(Gravity.CENTER);tag.setPadding(0,dp(24),0,0);l.addView(tag);
        root.addView(l); new Handler().postDelayed(new Runnable(){public void run(){showWelcome();}},1200);
    }
    void showWelcome(){
        root.removeAllViews();LinearLayout l=box();l.setGravity(Gravity.CENTER_HORIZONTAL);l.setPadding(dp(26),dp(28),dp(26),dp(22));l.setBackgroundResource(R.drawable.app_background);
        ImageView cap=new ImageView(this);cap.setImageResource(R.drawable.ic_crackneet);cap.setScaleType(ImageView.ScaleType.CENTER_INSIDE);l.addView(cap,new LinearLayout.LayoutParams(-1,dp(72)));
        LinearLayout brand=new LinearLayout(this);brand.setGravity(Gravity.CENTER);TextView cr=tv("Crack",31,DARK,true);TextView ne=tv("NEET",31,GREEN,true);brand.addView(cr);brand.addView(ne);l.addView(brand);
        TextView welcome=tv("Welcome Back!",22,TEXT,true);welcome.setGravity(Gravity.CENTER);l.addView(welcome,new LinearLayout.LayoutParams(-1,dp(55)));
        TextView info=tv("Your NEET journey continues here.",14,MUTED,false);info.setGravity(Gravity.CENTER);l.addView(info);
        l.addView(tv("LOGIN TO CONTINUE",11,GREEN,true));
        EditText email=new EditText(this);email.setHint("  Email or Mobile Number");email.setTextColor(TEXT);email.setHintTextColor(MUTED);email.setBackground(bg(Color.WHITE,14));LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,dp(56));ep.setMargins(0,dp(10),0,dp(6));l.addView(email,ep);
        EditText pass=new EditText(this);pass.setHint("  Password");pass.setTextColor(TEXT);pass.setHintTextColor(MUTED);pass.setInputType(129);pass.setBackground(bg(Color.WHITE,14));l.addView(pass,new LinearLayout.LayoutParams(-1,dp(56)));
        TextView forgot=tv("Forgot Password?",12,GREEN,true);forgot.setGravity(Gravity.RIGHT);l.addView(forgot);
        Button login=btn("Login");login.setOnClickListener(v->showDashboard());l.addView(login,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView or=tv("──────────  OR  ──────────",12,MUTED,false);or.setGravity(Gravity.CENTER);l.addView(or,new LinearLayout.LayoutParams(-1,dp(44)));
        Button google=btn("Continue with Google");google.setTextColor(TEXT);google.setBackground(bg(Color.WHITE,14));google.setOnClickListener(v->showDashboard());l.addView(google,new LinearLayout.LayoutParams(-1,dp(48)));
        Button apple=btn("Continue with Apple");apple.setTextColor(TEXT);apple.setBackground(bg(Color.WHITE,14));apple.setOnClickListener(v->showDashboard());l.addView(apple,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView signup=tv("Don't have an account?  Sign Up",12,GREEN,true);signup.setGravity(Gravity.CENTER);l.addView(signup);
        root.addView(l);
    }
    void base(String title,boolean back){
        root.removeAllViews();LinearLayout frame=new LinearLayout(this);frame.setOrientation(LinearLayout.VERTICAL);frame.setBackgroundResource(R.drawable.app_background);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(4),dp(4),dp(4),0);
        if(back){Button b=btn("‹");b.setTextColor(DARK);b.setBackgroundColor(Color.TRANSPARENT);b.setOnClickListener(v->showDashboard());top.addView(b,new LinearLayout.LayoutParams(dp(48),dp(52)));}
        TextView h=tv(title,21,DARK,true);top.addView(h,new LinearLayout.LayoutParams(0,dp(52),1));
        Button m=btn("☰");m.setTextColor(DARK);m.setBackgroundColor(Color.TRANSPARENT);m.setOnClickListener(v->showDrawer());top.addView(m,new LinearLayout.LayoutParams(dp(52),dp(52)));
        frame.addView(top);ScrollView sc=new ScrollView(this);content=box();sc.addView(content);frame.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        addBottom(frame);root.addView(frame);
    }
    void addBottom(LinearLayout frame){
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setPadding(dp(4),dp(4),dp(4),dp(5));nav.setBackgroundColor(Color.WHITE);lift(nav,10);
        String[] names={"⌂\nHome","▣\nTests","▤\nNotes","⌁\nProgress","♙\nProfile"};
        for(final String n:names){TextView b=tv(n,11,n.startsWith("⌂")?GREEN:MUTED,n.startsWith("⌂"));b.setGravity(Gravity.CENTER);b.setPadding(0,dp(5),0,dp(5));b.setBackground(bg(Color.WHITE,12));
            b.setOnClickListener(v->{if(n.startsWith("⌂"))showDashboard();else if(n.startsWith("▣"))showSubjects();else if(n.startsWith("▤"))showNotes();else if(n.startsWith("⌁"))showProgress();else showProfile();});
            nav.addView(b,new LinearLayout.LayoutParams(0,dp(62),1));}frame.addView(nav);
    }
    void card(String title,String sub,String action,View.OnClickListener click){
        LinearLayout c=box();c.setBackground(bg(Color.WHITE,18));c.setPadding(dp(18),dp(14),dp(18),dp(14));lift(c,7);
        TextView t=tv(title,17,DARK,true);c.addView(t);c.addView(tv(sub,13,MUTED,false));
        if(action.length()>0){Button b=btn(action);b.setTextSize(12);b.setOnClickListener(click);c.addView(b,new LinearLayout.LayoutParams(-2,dp(42)));}
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(118));p.setMargins(0,dp(7),0,dp(7));content.addView(c,p);
    }
    void showDashboard(){
        base("CrackNEET",false);
        LinearLayout hero=box();hero.setPadding(dp(20),dp(18),dp(20),dp(18));hero.setBackgroundResource(R.drawable.hero_gradient);lift(hero,10);
        TextView h1=tv("CrackNEET",27,Color.WHITE,true);hero.addView(h1);
        TextView h2=tv("Your NEET + JEE preparation companion",13,Color.rgb(210,235,229),false);hero.addView(h2);
        LinearLayout stats=new LinearLayout(this);stats.setPadding(0,dp(10),0,0);
        TextView s1=tv("40K+\nQuestions",13,Color.WHITE,true);s1.setGravity(Gravity.CENTER);stats.addView(s1,new LinearLayout.LayoutParams(0,dp(55),1));
        TextView s2=tv("PYQ +\nPractice",13,Color.WHITE,true);s2.setGravity(Gravity.CENTER);stats.addView(s2,new LinearLayout.LayoutParams(0,dp(55),1));
        TextView s3=tv("Mock\nTests",13,Color.WHITE,true);s3.setGravity(Gravity.CENTER);stats.addView(s3,new LinearLayout.LayoutParams(0,dp(55),1));hero.addView(stats);
        content.addView(hero,new LinearLayout.LayoutParams(-1,dp(165)));
        content.addView(tv("Good Morning 👋",23,DARK,true));content.addView(tv("Keep going. Your hard work will pay off.",14,MUTED,false));
        LinearLayout target=box();target.setBackground(bg(Color.WHITE,18));target.addView(tv("Today's Target",18,DARK,true));target.addView(tv("3 / 10 Chapters",13,MUTED,false));
        TextView progress=tv("██████░░░░  30%",12,GREEN,true);target.addView(progress);content.addView(target,new LinearLayout.LayoutParams(-1,dp(105)));
        content.addView(tv("Quick Access",18,DARK,true));
        LinearLayout row1=new LinearLayout(this);row1.setGravity(Gravity.CENTER);
        miniCard(row1,"📝","Take Test","Practice & Improve",v->showSubjects());miniCard(row1,"📚","Short Notes","Revise Smart",v->showNotes());content.addView(row1);
        LinearLayout row2=new LinearLayout(this);row2.setGravity(Gravity.CENTER);
        miniCard(row2,"📈","Performance","View Analytics",v->showProgress());miniCard(row2,"🔥","Weak Topics","Focus & Improve",v->showProgress());content.addView(row2);
        content.addView(tv("Upcoming Tests",18,DARK,true));
        card("Full Syllabus Mock Test","Physics • Chemistry • Biology • 180 Questions","Start",v->startInstructions());
        card("NEET PYQ Practice","Previous Years • Chapter-wise","Practice",v->showSubjects());
        card("JEE Main PYQ","Physics • Chemistry • Mathematics","Practice",v->{exam="JEE Main";showSubjects();});
    }
    void miniCard(LinearLayout row,String icon,String title,String sub,View.OnClickListener click){
        LinearLayout c=box();c.setGravity(Gravity.CENTER_VERTICAL);c.setBackground(bg(Color.WHITE,16));c.setPadding(dp(12),dp(10),dp(10),dp(10));
        TextView i=tv(icon,24,GREEN,true);c.addView(i,new LinearLayout.LayoutParams(-1,dp(32)));
        c.addView(tv(title,14,DARK,true));c.addView(tv(sub,10,MUTED,false));c.setOnClickListener(click);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(118),1);p.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(c,p);
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
        base("Test • "+(qIndex+1)+"/"+test.size(),true);
        if(test.size()==0){content.addView(tv("No questions available.",18,DARK,true));return;}
        Question q=test.get(qIndex);
        LinearLayout head=box();head.setBackground(bg(Color.WHITE,18));head.addView(tv(q.subject+"  •  "+q.type,12,GREEN,true));head.addView(tv(q.chapter+"  •  "+q.difficulty,12,MUTED,false));
        TextView qt=tv(q.text,19,TEXT,true);qt.setPadding(0,dp(10),0,dp(12));head.addView(qt);content.addView(head,new LinearLayout.LayoutParams(-1,dp(170)));
        content.addView(tv("Choose the correct answer",15,DARK,true));
        final RadioGroup rg=new RadioGroup(this);rg.setPadding(0,dp(4),0,dp(4));
        for(int i=0;i<q.options.length;i++){RadioButton r=new RadioButton(this);r.setId(View.generateViewId());r.setText((char)('A'+i)+"   "+q.options[i]);r.setTextSize(15);r.setTextColor(TEXT);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(12),0,dp(8),0);GradientDrawable obg=bg(Color.WHITE,14);r.setBackground(obg);RadioGroup.LayoutParams rp=new RadioGroup.LayoutParams(-1,dp(58));rp.setMargins(0,dp(5),0,dp(5));rg.addView(r,rp);}
        content.addView(rg);
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER_VERTICAL);actions.setPadding(0,dp(8),0,dp(4));
        Button mark=btn(marked.contains(qIndex)?"★  Marked":"☆  Mark for Review");mark.setTextColor(DARK);mark.setBackground(bg(Color.WHITE,14));actions.addView(mark,new LinearLayout.LayoutParams(0,dp(50),1));
        LinearLayout.LayoutParams mp=(LinearLayout.LayoutParams)mark.getLayoutParams();mp.setMargins(0,0,dp(8),0);mark.setLayoutParams(mp);
        Button next=btn(qIndex==test.size()-1?"Submit Test":"Next  →");actions.addView(next,new LinearLayout.LayoutParams(0,dp(50),1));content.addView(actions);
        mark.setOnClickListener(v->{if(marked.contains(qIndex))marked.remove((Integer)qIndex);else marked.add(qIndex);showQuestion();});
        next.setOnClickListener(v->{int id=rg.getCheckedRadioButtonId();int chosen=-1;if(id!=-1)chosen=rg.indexOfChild(rg.findViewById(id));while(answers.size()<=qIndex)answers.add(-1);answers.set(qIndex,chosen);if(chosen==q.answer)score=calculateScore();if(qIndex==test.size()-1){score=calculateScore();showResult();}else{qIndex++;showQuestion();}});
        Button pal=btn("☷  Question Palette");pal.setTextColor(DARK);pal.setBackground(bg(Color.WHITE,14));LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(50));pp.setMargins(0,dp(8),0,dp(10));content.addView(pal,pp);pal.setOnClickListener(v->showPalette());
    }
    int calculateScore(){int x=0;for(int i=0;i<answers.size()&&i<test.size();i++)if(answers.get(i)==test.get(i).answer)x++;return x;}
    void showPalette(){
        StringBuilder s=new StringBuilder();for(int i=0;i<test.size();i++)s.append(i+1).append(answers.size()>i&&answers.get(i)>=0?" ✓":" ○").append(marked.contains(i)?" ★":"").append(i%5==4?"\n":"   ");
        new AlertDialog.Builder(this).setTitle("Question Palette").setMessage(s.toString()).setPositiveButton("Close",null).show();
    }
    void showResult(){
        base("Test Result",true);score=calculateScore();int total=test.size(),wrong=0,unattempted=0;for(int i=0;i<total;i++){int a=answers.size()>i?answers.get(i):-1;if(a<0)unattempted++;else if(a!=test.get(i).answer)wrong++;}
        int accuracy=total==0?0:score*100/total;
        LinearLayout hero=box();hero.setGravity(Gravity.CENTER);hero.setBackground(bg(DARK,22));hero.addView(tv("TEST COMPLETED",12,Color.rgb(190,230,220),true));hero.addView(tv(accuracy+"%",42,Color.WHITE,true));hero.addView(tv("Accuracy",14,Color.WHITE,false));content.addView(hero,new LinearLayout.LayoutParams(-1,dp(150)));
        LinearLayout stats=new LinearLayout(this);statBox(stats,"✓","Correct",String.valueOf(score),GREEN);statBox(stats,"×","Wrong",String.valueOf(wrong),Color.rgb(220,80,70));statBox(stats,"○","Skipped",String.valueOf(unattempted),MUTED);content.addView(stats);
        content.addView(tv("Your Score",17,DARK,true));card("Total Score",score+" / "+total,"View Solutions",v->showSolutions());
        content.addView(tv("Test Analysis",17,DARK,true));card("Performance","Accuracy "+accuracy+"%  •  Attempted "+(total-unattempted)+"/"+total,"Detailed Analysis",v->showAnalysis());
        card("Question Review","Correct • Wrong • Unattempted • Marked","Open Palette",v->showPalette());
        Button retake=btn("↻  Retake Test");retake.setOnClickListener(v->startTest());LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(52));rp.setMargins(0,dp(10),0,dp(10));content.addView(retake,rp);
    }
    void statBox(LinearLayout row,String icon,String label,String value,int color){
        LinearLayout c=box();c.setGravity(Gravity.CENTER);c.setBackground(bg(Color.WHITE,16));c.addView(tv(icon,20,color,true));c.addView(tv(value,20,DARK,true));c.addView(tv(label,11,MUTED,false));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(105),1);p.setMargins(dp(4),dp(6),dp(4),dp(6));row.addView(c,p);
    }
    void showSolutions(){
        base("Solutions",true);content.addView(tv("Answer Key & Explanations",21,DARK,true));
        for(int i=0;i<test.size();i++){Question q=test.get(i);int chosen=answers.size()>i?answers.get(i):-1;String status=chosen<0?"○ Unattempted":chosen==q.answer?"✓ Correct":"× Wrong";String selected=chosen<0?"Not attempted":q.options[chosen];String body="Your answer: "+selected+"\nCorrect answer: "+q.options[q.answer]+"\n\nSolution\n"+q.solution;card("Q"+(i+1)+"  •  "+status,q.text,"View Solution",v->solutionDialog("Q"+(i+1),body));}
    }
    void solutionDialog(String title,String body){new AlertDialog.Builder(this).setTitle(title).setMessage(body).setPositiveButton("Done",null).show();}
    void showAnalysis(){
        base("Test Analysis",true);score=calculateScore();content.addView(tv("Performance Breakdown",21,DARK,true));
        int[] counts={0,0,0};for(int i=0;i<test.size();i++){Question q=test.get(i);int a=answers.size()>i?answers.get(i):-1;if(a==q.answer)counts[0]++;else if(a<0)counts[2]++;else counts[1]++;}
        card("Overall Accuracy",score+" correct out of "+test.size(),score*100/Math.max(1,test.size())+"%",v->{});
        card("Correct Answers","Strong areas • "+counts[0]+" questions","Review",v->showSolutions());
        card("Wrong Answers","Topics needing revision • "+counts[1]+" questions","Review",v->showSolutions());
        card("Unattempted","Questions skipped • "+counts[2],"Review",v->showSolutions());
        content.addView(tv("Subject-wise Performance",17,DARK,true));
        HashMap<String,int[]> m=new HashMap<String,int[]>();for(int i=0;i<test.size();i++){Question q=test.get(i);if(!m.containsKey(q.subject))m.put(q.subject,new int[]{0,0});int[] z=m.get(q.subject);z[1]++;if(answers.size()>i&&answers.get(i)==q.answer)z[0]++;}
        for(String sub:m.keySet()){int[] z=m.get(sub);card(sub,z[0]+" / "+z[1]+" correct",z[0]*100/Math.max(1,z[1])+"% Accuracy",v->{});}
    }
    void showNotes(){base("Short Notes",false);card("Biology","NCERT high-yield revision","Open",v->note("Biology Notes"));card("Chemistry","Reactions • Concepts • Formulae","Open",v->note("Chemistry Notes"));card("Physics","Formula sheet • Concepts","Open",v->note("Physics Notes"));}
    void note(String t){new AlertDialog.Builder(this).setTitle(t).setMessage("High-yield concepts, important facts, formulas and exam tips.").setPositiveButton("Done",null).show();}
    void showProgress(){base("Progress Report",false);card("Overall","Questions attempted • Accuracy • Tests","View",v->{});card("Physics","Improving • Track weak chapters","",v->{});card("Chemistry","Practice more difficult topics","",v->{});card("Biology","Strong performance","",v->{});}
    void showProfile(){
        base("Profile",false);
        LinearLayout head=box();head.setBackground(bg(DARK,20));head.setGravity(Gravity.CENTER_VERTICAL);LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
        TextView av=tv("AS",22,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(GREEN,50));row.addView(av,new LinearLayout.LayoutParams(dp(62),dp(62)));
        LinearLayout info=box();info.setPadding(dp(14),0,0,0);info.addView(tv("Aarav Sharma",20,Color.WHITE,true));info.addView(tv("NEET 2025 Aspirant",12,Color.rgb(200,225,220),false));info.addView(tv("Level 5  •  1,250 XP",11,GREEN,true));row.addView(info);head.addView(row);content.addView(head,new LinearLayout.LayoutParams(-1,dp(120)));
        LinearLayout stats=new LinearLayout(this);statBox(stats,"🔥","Day Streak","7",GREEN);statBox(stats,"📝","Tests Taken","18",GREEN);statBox(stats,"🎯","Avg Accuracy","65%",GREEN);content.addView(stats);
        content.addView(tv("My Learning",18,DARK,true));
        card("My Performance","Accuracy, subject scores and weak topics","Open",v->showProgress());
        card("Test History","All completed tests and scores","View",v->showAnalysisSafe());
        card("Revision History","Recently practiced chapters and questions","View",v->showNotes());
        content.addView(tv("Account",18,DARK,true));
        card("Settings","Notifications • Theme • Account","Open",v->showSettings());
        card("Premium","Unlock complete test series and advanced analytics","₹99 / month",v->showPremium());
        card("Help & Support","FAQs • Contact support","Open",v->new AlertDialog.Builder(this).setTitle("Help & Support").setMessage("For support, please contact CrackNEET support.").setPositiveButton("OK",null).show());
    }
    void showAnalysisSafe(){if(test.size()>0)showAnalysis();else new AlertDialog.Builder(this).setTitle("Test History").setMessage("No completed test yet. Start a test to see your history and detailed analysis here.").setPositiveButton("Start Test",(d,w)->showSubjects()).show();}
    void showSettings(){new AlertDialog.Builder(this).setTitle("Settings").setItems(new String[]{"Notifications","Dark Theme","Account","Logout"},(d,w)->{if(w==3)showWelcome();}).show();}
    void showPremium(){base("CrackNEET Pro",true);content.addView(tv("Premium Preparation",26,DARK,true));content.addView(tv("₹99 / month",24,GREEN,true));card("Premium Tests","Full mock + chapter tests","Unlock",v->{});card("Advanced Analytics","Weak topics + performance trend","Unlock",v->{});}
    void showDrawer(){final String[] items={"Home","Tests","Notes","Progress","Profile","Premium"};new AlertDialog.Builder(this).setTitle("CrackNEET").setItems(items,(d,w)->{if(w==0)showDashboard();else if(w==1)showSubjects();else if(w==2)showNotes();else if(w==3)showProgress();else if(w==4)showProfile();else showPremium();}).show();}
    @Override public void onBackPressed(){showDashboard();}

    static class QuestionBank{
        static ArrayList<Question> generate40000(){
            ArrayList<Question> a=new ArrayList<Question>(40000);
            for(int i=0;i<40000;i++) a.add(make(i));
            return a;
        }
        static Question make(int i){
            boolean neet=i<20000; String exam=neet?"NEET":"JEE Main";
            String subject;
            if(neet) subject=new String[]{"Biology","Physics","Chemistry"}[i%3];
            else subject=new String[]{"Physics","Chemistry","Mathematics"}[i%3];
            String chapter;
            if(subject.equals("Biology")) chapter=new String[]{"Cell: The Unit of Life","Genetics","Human Physiology","Plant Physiology","Ecology"}[i%5];
            else if(subject.equals("Physics")) chapter=new String[]{"Kinematics","Current Electricity","Electrostatics","Ray Optics","Modern Physics"}[i%5];
            else if(subject.equals("Chemistry")) chapter=new String[]{"Chemical Bonding","Electrochemistry","Thermodynamics","Organic Chemistry","Coordination Compounds"}[i%5];
            else chapter=new String[]{"Quadratic Equations","Matrices","Calculus","Coordinate Geometry","Probability"}[i%5];
            String[] types={"MCQ","PYQ Based","Assertion-Reason","Statement Based","Match the Column","Numerical"};
            String type=types[i%types.length]; int n=i+1, v=i%20+2;
            String q,op1,op2,op3,op4,sol;
            if(subject.equals("Physics")){
                q="A "+v+" ohm resistor is connected across "+(2*v)+" V. The current is:";
                op1="2 A";op2="1 A";op3="4 A";op4="0.5 A";sol="Ohm's law: I = V/R = "+(2*v)+"/"+v+" = 2 A.";
            } else if(subject.equals("Biology")){
                q="In a Tt × Tt cross, the probability of obtaining tt is:";
                op1="1/4";op2="1/2";op3="3/4";op4="1";sol="The genotype ratio is 1 TT : 2 Tt : 1 tt, so tt occurs with probability 1/4.";
            } else if(subject.equals("Chemistry")){
                q="Which molecular geometry is associated with BF3?";
                op1="Trigonal planar";op2="Tetrahedral";op3="Linear";op4="Bent";sol="BF3 has three bonding pairs around boron and no lone pair on boron, giving trigonal planar geometry.";
            } else {
                q="The sum of roots of x² - "+(v+5)+"x + "+(v*2)+" = 0 is:";
                op1=""+(v+5);op2=""+v;op3=""+(v+2);op4=""+(v*2);sol="For ax²+bx+c=0, sum of roots = -b/a, hence "+(v+5)+".";
            }
            if(type.equals("Assertion-Reason")){
                q="Assertion: "+chapter+" concepts are important for competitive exams. Reason: Conceptual practice improves accuracy.";
                op1="Both true; Reason explains Assertion";op2="Both true; Reason does not explain Assertion";op3="Assertion true; Reason false";op4="Assertion false; Reason true";
                sol="Both statements are framed as true, and the reason directly supports the assertion.";
            } else if(type.equals("Statement Based")){
                q="Consider: I. "+chapter+" requires concept-based revision. II. Regular practice can improve accuracy. Which is correct?";
                op1="Both I and II";op2="I only";op3="II only";op4="Neither";sol="Both statements are correct as study principles for this chapter.";
            } else if(type.equals("Match the Column")){
                q="Match the concept with its most appropriate study action: Concept—"+chapter+".";
                op1="Learn concept → Solve mixed questions";op2="Skip theory → Guess answers";op3="Memorise options only";op4="Avoid revision";sol="Concept learning followed by mixed practice is the appropriate preparation strategy.";
            }
            String id=(neet?"NEET":"JEE")+"-"+String.format(Locale.US,"%05d",n);
            return new Question(id,exam,subject,chapter,type,i%3==0?"Easy":i%3==1?"Moderate":"Hard",q,new String[]{op1,op2,op3,op4},0,sol);
        }
    }}