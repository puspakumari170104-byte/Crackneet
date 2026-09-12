package com.crackneet.app;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.net.*;
import java.io.*;
import org.json.*;
import com.android.billingclient.api.*;

public class MainActivity extends Activity {
    static final String API_BASE="https://crackneet-api.onrender.com";
    static final int GREEN=Color.rgb(0,170,118), DARK=Color.rgb(15,38,48), TEXT=Color.rgb(35,52,62), MUTED=Color.rgb(102,119,126), BG=Color.rgb(245,248,247);
    FrameLayout root; LinearLayout content; String exam="NEET"; long userId=-1; String authToken=""; int qIndex=0,score=0; long duration=30; long remainingSeconds=0; CountDownTimer timer;
    ArrayList<Question> bank=new ArrayList<Question>(), test=new ArrayList<Question>(); ArrayList<Integer> answers=new ArrayList<Integer>(), marked=new ArrayList<Integer>();
    BillingClient billingClient; ProductDetails premiumProduct;

    static class Question {
        String id,exam,subject,chapter,type,difficulty,text,solution; String[] options; int answer;
        Question(String id,String exam,String subject,String chapter,String type,String difficulty,String text,String[] options,int answer,String solution){
            this.id=id;this.exam=exam;this.subject=subject;this.chapter=chapter;this.type=type;this.difficulty=difficulty;this.text=text;this.options=options;this.answer=answer;this.solution=solution;
        }
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b); setContentView(R.layout.activity_main); root=findViewById(R.id.root);
        loadSession(); initBilling(); showSplash(); bank=QuestionBank.generate40000();
    }
    String profileNameSafe(long id){ return pendingProfileName==null||pendingProfileName.length()==0?"Student":pendingProfileName; }
    String profileEmailSafe(long id){ return pendingProfileEmail==null?"":pendingProfileEmail; }
    String pendingProfileName="",pendingProfileEmail="";
    void loadSession(){ android.content.SharedPreferences p=getSharedPreferences("crackneet_session",MODE_PRIVATE); userId=p.getLong("userId",-1); authToken=p.getString("token",""); }
    void saveSession(long id,String token){ userId=id; authToken=token; getSharedPreferences("crackneet_session",MODE_PRIVATE).edit().putLong("userId",id).putString("token",token).putString("name",profileNameSafe(id)).putString("email",profileEmailSafe(id)).apply(); }
    void clearSession(){ userId=-1; authToken=""; getSharedPreferences("crackneet_session",MODE_PRIVATE).edit().clear().apply(); }
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
        Button login=btn("Login");login.setOnClickListener(v->login(email.getText().toString().trim(),pass.getText().toString()));l.addView(login,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView or=tv("──────────  OR  ──────────",12,MUTED,false);or.setGravity(Gravity.CENTER);l.addView(or,new LinearLayout.LayoutParams(-1,dp(44)));
        TextView signup=tv("Don't have an account?  Sign Up",12,GREEN,true);signup.setGravity(Gravity.CENTER);signup.setOnClickListener(v->showSignup());l.addView(signup);
        root.addView(l);
    }
    void login(final String email,final String password){
        if(email.length()==0||password.length()<8){Toast.makeText(this,"Enter a valid email and password (8+ characters).",Toast.LENGTH_SHORT).show();return;}
        new Thread(() -> {
            try{
                HttpURLConnection con=(HttpURLConnection)new URL(API_BASE+"/api/auth/login").openConnection();
                con.setRequestMethod("POST"); con.setDoOutput(true); con.setRequestProperty("Content-Type","application/json");
                JSONObject body=new JSONObject(); body.put("email",email); body.put("password",password);
                OutputStream os=con.getOutputStream();os.write(body.toString().getBytes("UTF-8"));os.close();
                int code=con.getResponseCode(); InputStream is=code>=400?con.getErrorStream():con.getInputStream();
                BufferedReader br=new BufferedReader(new InputStreamReader(is));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);br.close();
                JSONObject out=new JSONObject(sb.toString());
                if(code!=200)throw new Exception(out.optString("error","Login failed"));
                JSONObject u=out.getJSONObject("user"); pendingProfileName=u.optString("name","Student"); pendingProfileEmail=u.optString("email",""); saveSession(u.getLong("id"),out.getString("token"));
                runOnUiThread(()->showDashboard());
            }catch(Exception e){runOnUiThread(()->Toast.makeText(this,e.getMessage()==null?"Login failed":e.getMessage(),Toast.LENGTH_LONG).show());}
        }).start();
    }
    void showSignup(){
        final LinearLayout l=box(); l.setPadding(dp(26),dp(28),dp(26),dp(22)); l.setBackgroundResource(R.drawable.app_background);
        l.addView(tv("Create your CrackNEET account",22,DARK,true));
        final EditText name=new EditText(this);name.setHint("Name");l.addView(name);
        final EditText email=new EditText(this);email.setHint("Email");email.setInputType(33);l.addView(email);
        final EditText pass=new EditText(this);pass.setHint("Password (8+ characters)");pass.setInputType(129);l.addView(pass);
        Button create=btn("Create account");create.setOnClickListener(v->register(name.getText().toString().trim(),email.getText().toString().trim(),pass.getText().toString()));l.addView(create,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView back=tv("Back to Login",13,GREEN,true);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->showWelcome());l.addView(back);
        root.removeAllViews();root.addView(l);
    }
    void register(final String name,final String email,final String password){
        if(name.length()<2||email.length()<5||password.length()<8){Toast.makeText(this,"Enter name, email and an 8+ character password.",Toast.LENGTH_SHORT).show();return;}
        new Thread(() -> {
            try{
                HttpURLConnection con=(HttpURLConnection)new URL(API_BASE+"/api/auth/register").openConnection();
                con.setRequestMethod("POST");con.setDoOutput(true);con.setRequestProperty("Content-Type","application/json");
                JSONObject body=new JSONObject();body.put("name",name);body.put("email",email);body.put("password",password);
                OutputStream os=con.getOutputStream();os.write(body.toString().getBytes("UTF-8"));os.close();
                int code=con.getResponseCode();InputStream is=code>=400?con.getErrorStream():con.getInputStream();BufferedReader br=new BufferedReader(new InputStreamReader(is));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);br.close();
                JSONObject out=new JSONObject(sb.toString());if(code!=201)throw new Exception(out.optString("error","Registration failed"));
                JSONObject u=out.getJSONObject("user");pendingProfileName=u.optString("name","Student"); pendingProfileEmail=u.optString("email","");saveSession(u.getLong("id"),out.getString("token"));runOnUiThread(()->showDashboard());
            }catch(Exception e){runOnUiThread(()->Toast.makeText(this,e.getMessage()==null?"Registration failed":e.getMessage(),Toast.LENGTH_LONG).show());}
        }).start();
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
            nav.addView(b,new LinearLayout.LayoutParams(0,dp(56),1));} LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,dp(56)); np.setMargins(0,0,0,dp(8)); frame.addView(nav,np);
    }
    void card(String title,String sub,String action,View.OnClickListener click){
        LinearLayout c=box();c.setBackground(bg(Color.WHITE,18));c.setPadding(dp(18),dp(14),dp(18),dp(14));lift(c,7);
        TextView t=tv(title,17,DARK,true);c.addView(t);c.addView(tv(sub,13,MUTED,false));
        if(action.length()>0){Button b=btn(action);b.setTextSize(12);b.setOnClickListener(click);c.addView(b,new LinearLayout.LayoutParams(-2,dp(42)));}
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(118));p.setMargins(0,dp(7),0,dp(7));content.addView(c,p);
    }
    void miniCard(LinearLayout row,String icon,String title,String sub,View.OnClickListener click){
        Button b=btn(icon+"  "+title+"\n"+sub);b.setTextSize(12);b.setTextColor(DARK);b.setGravity(Gravity.CENTER);b.setBackground(bg(Color.WHITE,18));b.setOnClickListener(click);lift(b,6);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(82),1);p.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(b,p);
    }
    void showDashboard(){
        base("CrackNEET",false);
        TextView greet=tv("Good Morning 👋",25,DARK,true);content.addView(greet);content.addView(tv("Your preparation dashboard",13,MUTED,false));
        LinearLayout hero=box();hero.setPadding(dp(20),dp(18),dp(20),dp(18));hero.setBackgroundResource(R.drawable.hero_gradient);lift(hero,12);
        hero.addView(tv("CrackNEET",28,Color.WHITE,true));hero.addView(tv("NEET + JEE  •  Smart preparation",13,Color.rgb(215,240,233),false));
        LinearLayout stats=new LinearLayout(this);stats.setPadding(0,dp(12),0,0);
        statBoxDark(stats,"40K+","Questions");statBoxDark(stats,"180","Mock Tests");statBoxDark(stats,"24×7","Practice");hero.addView(stats);content.addView(hero,new LinearLayout.LayoutParams(-1,dp(170)));
        content.addView(tv("Continue Learning",18,DARK,true));
        card("Your Daily Target","3 / 10 chapters completed","Continue  →",v->showSubjects());
        content.addView(tv("Quick Practice",18,DARK,true));
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);miniCard(row,"✦","NEET PYQ","Chapter-wise",v->{exam="NEET";showSubjects();});miniCard(row,"⚡","JEE Main","PYQ Practice",v->{exam="JEE Main";showSubjects();});content.addView(row);
        LinearLayout row2=new LinearLayout(this);row2.setGravity(Gravity.CENTER);miniCard(row2,"🧠","Mixed Test","All patterns",v->startInstructions());miniCard(row2,"📊","Analytics","Your progress",v->showProgress());content.addView(row2);
        content.addView(tv("Recommended For You",18,DARK,true));
        card("Full Syllabus Mock","180 Questions  •  Timed Test","Start Test",v->startInstructions());
        card("Assertion + Statement Challenge","Mixed advanced practice","Practice",v->showSubjects());
        card("Match Column Mastery","Concept linking practice","Practice",v->showSubjects());
    }
    void statBoxDark(LinearLayout row,String value,String label){
        LinearLayout c=box();c.setGravity(Gravity.CENTER);c.setPadding(0,dp(5),0,0);c.addView(tv(value,17,Color.WHITE,true));c.addView(tv(label,10,Color.rgb(205,232,225),false));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(55),1);row.addView(c,p);
    }
void showSubjects(){
        base(exam+" Tests",true);content.addView(tv("Choose your subject",22,DARK,true));
        String[][] d=exam.equals("JEE Main")?new String[][]{{"Physics","PYQ + Practice"},{"Chemistry","PYQ + Practice"},{"Mathematics","PYQ + Practice"}}:new String[][]{{"Physics","12 Chapters"},{"Chemistry","14 Chapters"},{"Biology","16 Chapters"}};
        for(String[] x:d){final String s=x[0];card(s,x[1]+" • Tests • Notes","Open",v->showChapters(s));}
    }
    void showChapters(String subject){
        base(subject,true);content.addView(tv("Chapter-wise Practice",22,DARK,true));
        String[] ch;
        if(subject.equals("Biology")) ch=new String[]{"The Living World","Biological Classification","Plant Kingdom","Animal Kingdom","Morphology of Flowering Plants","Anatomy of Flowering Plants","Structural Organisation in Animals","Cell: The Unit of Life","Biomolecules","Cell Cycle and Cell Division","Transport in Plants","Mineral Nutrition","Photosynthesis in Plants","Respiration in Plants","Plant Growth and Development","Digestion and Absorption","Breathing and Exchange of Gases","Body Fluids and Circulation","Excretory Products and Elimination","Locomotion and Movement","Neural Control and Coordination","Chemical Coordination and Integration","Sexual Reproduction in Flowering Plants","Human Reproduction","Reproductive Health","Principles of Inheritance and Variation","Molecular Basis of Inheritance","Evolution","Human Health and Disease","Strategies for Enhancement in Food Production","Microbes in Human Welfare","Biotechnology: Principles and Processes","Biotechnology and its Applications","Organisms and Populations","Ecosystem","Biodiversity and Conservation","Environmental Issues"};
        else if(subject.equals("Physics")) ch=new String[]{"Units and Measurements","Motion in a Straight Line","Motion in a Plane","Laws of Motion","Work, Energy and Power","System of Particles and Rotational Motion","Gravitation","Properties of Bulk Matter","Thermodynamics","Kinetic Theory","Oscillations","Waves","Electric Charges and Fields","Electrostatic Potential and Capacitance","Current Electricity","Moving Charges and Magnetism","Magnetism and Matter","Electromagnetic Induction","Alternating Current","Electromagnetic Waves","Ray Optics and Optical Instruments","Wave Optics","Dual Nature of Radiation and Matter","Atoms","Nuclei","Semiconductor Electronics";
        else if(subject.equals("Chemistry")) ch=new String[]{"Some Basic Concepts of Chemistry","Structure of Atom","Classification of Elements and Periodicity","Chemical Bonding and Molecular Structure","Thermodynamics","Equilibrium","Redox Reactions","Organic Chemistry: Basic Principles","Hydrocarbons","Solutions","Electrochemistry","Chemical Kinetics","p-Block Elements","d- and f-Block Elements","Coordination Compounds","Haloalkanes and Haloarenes","Alcohols, Phenols and Ethers","Aldehydes, Ketones and Carboxylic Acids","Amines","Biomolecules","Principles Related to Practical Chemistry"};
        else ch=new String[]{"Sets and Functions","Complex Numbers","Quadratic Equations","Matrices","Determinants","Permutations and Combinations","Binomial Theorem","Sequences and Series","Limits","Continuity and Differentiability","Integral Calculus","Differential Equations","Coordinate Geometry","Three Dimensional Geometry","Vector Algebra","Statistics","Probability","Trigonometry","Mathematical Reasoning"};
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
        final String selectedExam=exam;
        new Thread(() -> {
            ArrayList<Question> remote=fetchRemoteQuestions(selectedExam,45);
            runOnUiThread(() -> {
                test.clear();
                if(remote.size()>0) test.addAll(remote);
                else for(Question q:bank) if(q.exam.equals(selectedExam)) test.add(q);
                Collections.shuffle(test); if(test.size()>45) test=new ArrayList<Question>(test.subList(0,45));
                qIndex=0;score=0;answers.clear();marked.clear();showQuestion();
            });
        }).start();
    }

    ArrayList<Question> fetchRemoteQuestions(String selectedExam,int limit){
        ArrayList<Question> out=new ArrayList<Question>(); HttpURLConnection con=null;
        try{
            String url=API_BASE+"/api/questions?exam="+URLEncoder.encode(selectedExam,"UTF-8")+"&limit="+limit;
            con=(HttpURLConnection)new URL(url).openConnection(); con.setRequestMethod("GET"); con.setConnectTimeout(8000); con.setReadTimeout(10000);
            if(con.getResponseCode()!=200)return out;
            BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream())); StringBuilder sb=new StringBuilder(); String line;
            while((line=br.readLine())!=null)sb.append(line); br.close();
            JSONArray arr=new JSONObject(sb.toString()).optJSONArray("questions"); if(arr==null)return out;
            for(int i=0;i<arr.length();i++){
                JSONObject o=arr.getJSONObject(i); JSONArray opts=o.optJSONArray("options"); String[] options=new String[opts==null?0:opts.length()];
                for(int j=0;j<options.length;j++)options[j]=opts.optString(j);
                if(options.length==0)continue;
                out.add(new Question(o.optString("id"),o.optString("exam"),o.optString("subject"),o.optString("chapter"),o.optString("type"),o.optString("difficulty"),o.optString("question"),options,o.optInt("answer_index",0),o.optString("solution")));
            }
        }catch(Exception ignored){} finally{if(con!=null)con.disconnect();}
        return out;
    }

    void submitAttemptToServer(){
        final int finalScore=calculateScore(); final int total=test.size(); int wrong=0,skipped=0;
        for(int i=0;i<total;i++){int a=answers.size()>i?answers.get(i):-1;if(a<0)skipped++;else if(a!=test.get(i).answer)wrong++;}
        final int finalWrong=wrong,finalSkipped=skipped; final ArrayList<Integer> finalAnswers=new ArrayList<Integer>(answers);
        new Thread(() -> {
            HttpURLConnection con=null;
            try{
                con=(HttpURLConnection)new URL(API_BASE+"/api/tests/submit").openConnection(); con.setRequestMethod("POST"); con.setDoOutput(true); con.setRequestProperty("Content-Type","application/json"); if(authToken.length()>0) con.setRequestProperty("Authorization","Bearer "+authToken); con.setConnectTimeout(8000); con.setReadTimeout(10000);
                JSONObject body=new JSONObject(); body.put("userId",userId); body.put("exam",exam); body.put("score",finalScore); body.put("total",total); body.put("correct",finalScore); body.put("wrong",finalWrong); body.put("skipped",finalSkipped); body.put("durationSeconds",Math.max(0,(int)(duration*60-remainingSeconds)));
                JSONArray ans=new JSONArray(); for(Integer a:finalAnswers)ans.put(a==null?-1:a); body.put("answers",ans);
                OutputStream os=con.getOutputStream(); os.write(body.toString().getBytes("UTF-8")); os.close(); con.getResponseCode();
            }catch(Exception ignored){} finally{if(con!=null)con.disconnect();}
        }).start();
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
        if(timer!=null){timer.cancel();timer=null;}
        submitAttemptToServer();
        base("Test Result",true);score=calculateScore();int total=test.size(),wrong=0,unattempted=0;for(int i=0;i<total;i++){int a=answers.size()>i?answers.get(i):-1;if(a<0)unattempted++;else if(a!=test.get(i).answer)wrong++;}
        int accuracy=total==0?0:score*100/total;
        LinearLayout hero=box();hero.setGravity(Gravity.CENTER);hero.setBackgroundResource(R.drawable.hero_gradient);lift(hero,12);
        hero.addView(tv("🎉  TEST COMPLETED",12,Color.WHITE,true));hero.addView(tv(accuracy+"%",42,Color.WHITE,true));hero.addView(tv("Accuracy • "+(total-unattempted)+"/"+total+" attempted",14,Color.WHITE,false));content.addView(hero,new LinearLayout.LayoutParams(-1,dp(165)));
        content.addView(tv("Performance Snapshot",18,DARK,true));
        LinearLayout stats=new LinearLayout(this);statBox(stats,"✓","Correct",String.valueOf(score),GREEN);statBox(stats,"×","Wrong",String.valueOf(wrong),Color.rgb(220,80,70));statBox(stats,"○","Skipped",String.valueOf(unattempted),MUTED);content.addView(stats);
        LinearLayout stats2=new LinearLayout(this);statBox(stats2,"🎯","Score",score+" / "+total,GREEN);statBox(stats2,"⏱","Time",String.format(Locale.US,"%02d:%02d",remainingSeconds/60,remainingSeconds%60),DARK);statBox(stats2,"⭐","Accuracy",accuracy+"%",GREEN);content.addView(stats2);
        content.addView(tv("What would you like to review?",17,DARK,true));
        card("📖 Solutions","Every question with your answer, correct answer and explanation","Open Solutions",v->showSolutions());
        card("📊 Test Analysis","Subject-wise performance, accuracy and weak areas","Detailed Analysis",v->showAnalysis());
        card("🔢 Question Review","Jump to correct, wrong, skipped and marked questions","Open Palette",v->showPalette());
        Button retake=btn("↻  Retake Test");retake.setOnClickListener(v->startTest());LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(54));rp.setMargins(0,dp(10),0,dp(10));content.addView(retake,rp);
    }
void statBox(LinearLayout row,String icon,String label,String value,int color){
        LinearLayout c=box();c.setGravity(Gravity.CENTER);c.setBackground(bg(Color.WHITE,16));c.addView(tv(icon,20,color,true));c.addView(tv(value,20,DARK,true));c.addView(tv(label,11,MUTED,false));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(105),1);p.setMargins(dp(4),dp(6),dp(4),dp(6));row.addView(c,p);
    }
    void showSolutions(){
        base("Solutions",true);content.addView(tv("Answer Key & Explanations",21,DARK,true));
        for(int i=0;i<test.size();i++){Question q=test.get(i);int chosen=answers.size()>i?answers.get(i):-1;String status=chosen<0?"○ Unattempted":chosen==q.answer?"✓ Correct":"× Wrong";String selected=chosen<0?"Not attempted":q.options[chosen];String body="Your answer: "+selected+"\nCorrect answer: "+q.options[q.answer]+"\n\nSolution\n"+q.solution;final String title="Q"+(i+1)+"  •  "+status;final String details=body;card(title,q.text,"View Solution",v->solutionDialog(title,details));}
    }
    void solutionDialog(String title,String body){new AlertDialog.Builder(this).setTitle(title).setMessage(body).setPositiveButton("Done",null).show();}
    void showAnalysis(){
        base("Test Analysis",true);score=calculateScore();content.addView(tv("Performance Breakdown",21,DARK,true));
        int[] counts={0,0,0};for(int i=0;i<test.size();i++){Question q=test.get(i);int a=answers.size()>i?answers.get(i):-1;if(a==q.answer)counts[0]++;else if(a<0)counts[2]++;else counts[1]++;}
        int total=test.size(),attempted=total-counts[2],accuracy=attempted==0?0:score*100/attempted; card("Overall Accuracy",score+" correct • "+attempted+" attempted",accuracy+"%",v->{});
        card("Accuracy Meter","Correct "+score+"  •  Wrong "+counts[1]+"  •  Skipped "+counts[2],accuracy+"% Accuracy",v->{});
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
        String profileName="Student", profileEmail="";
        android.content.SharedPreferences sp=getSharedPreferences("crackneet_session",MODE_PRIVATE);
        profileName=sp.getString("name","Student"); profileEmail=sp.getString("email","");
        String initials=profileName.trim().length()>0?profileName.trim().substring(0,Math.min(2,profileName.trim().length())).toUpperCase():"ST";
        TextView av=tv(initials,22,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(GREEN,50));row.addView(av,new LinearLayout.LayoutParams(dp(62),dp(62)));
        LinearLayout info=box();info.setPadding(dp(14),0,0,0);info.addView(tv(profileName,20,Color.WHITE,true));info.addView(tv(profileEmail.length()>0?profileEmail:"NEET Aspirant",12,Color.rgb(200,225,220),false));info.addView(tv("Your CrackNEET account",11,GREEN,true));row.addView(info);head.addView(row);content.addView(head,new LinearLayout.LayoutParams(-1,dp(120)));
        LinearLayout stats=new LinearLayout(this);statBox(stats,"🔥","Day Streak","7",GREEN);statBox(stats,"📝","Tests Taken","18",GREEN);statBox(stats,"🎯","Avg Accuracy","65%",GREEN);content.addView(stats);
        content.addView(tv("My Learning",18,DARK,true));
        card("My Performance","Accuracy, subject scores and weak topics","Open",v->showProgress());
        card("Test History","All completed tests and scores","View",v->showAnalysisSafe());
        card("Revision History","Recently practiced chapters and questions","View",v->showNotes());
        content.addView(tv("Account",18,DARK,true));
        card("Settings","Notifications • Theme • Account","Open",v->showSettings());
        card("Delete Account","Permanently delete your CrackNEET account and progress","Delete",v->confirmDeleteAccount());
        card("Premium","Unlock complete test series and advanced analytics","₹99 / month",v->showPremium());
        card("Help & Support","FAQs • Contact support","Open",v->new AlertDialog.Builder(this).setTitle("Help & Support").setMessage("For support, please contact CrackNEET support.").setPositiveButton("OK",null).show());
    }
    void showAnalysisSafe(){if(test.size()>0)showAnalysis();else new AlertDialog.Builder(this).setTitle("Test History").setMessage("No completed test yet. Start a test to see your history and detailed analysis here.").setPositiveButton("Start Test",(d,w)->showSubjects()).show();}
    void showSettings(){
        base("Settings",true);
        content.addView(tv("Preferences",20,DARK,true));
        card("🔔 Notifications","Daily practice reminders and test alerts","Manage",v->new AlertDialog.Builder(this).setTitle("Notifications").setMessage("Daily reminders are enabled for this build.").setPositiveButton("OK",null).show());
        card("🌙 Appearance","Light, dark and system interface options","Choose",v->new AlertDialog.Builder(this).setTitle("Appearance").setItems(new String[]{"Light","Dark","System Default"},null).show());
        card("👤 Account","Profile and learning preferences","Open Profile",v->showProfile());
        card("🔒 Privacy","Practice data and app preferences","View",v->new AlertDialog.Builder(this).setTitle("Privacy").setMessage("Practice progress is stored locally in this prototype.").setPositiveButton("OK",null).show());
        card("↪ Logout","Return to the welcome screen","Logout",v->{clearSession();showWelcome();});
    }
    void confirmDeleteAccount(){
        new AlertDialog.Builder(this).setTitle("Delete account?").setMessage("This permanently deletes your account, test attempts and progress. This cannot be undone.")
        .setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->deleteAccount()).show();
    }
    void deleteAccount(){
        if(authToken.length()==0){clearSession();showWelcome();return;}
        new Thread(() -> {
            try{
                HttpURLConnection con=(HttpURLConnection)new URL(API_BASE+"/api/auth/account").openConnection();
                con.setRequestMethod("DELETE");con.setRequestProperty("Authorization","Bearer "+authToken);con.setConnectTimeout(8000);con.setReadTimeout(10000);
                int code=con.getResponseCode(); if(code!=200) throw new Exception("Account deletion failed");
                clearSession();runOnUiThread(()->{Toast.makeText(this,"Account deleted.",Toast.LENGTH_LONG).show();showWelcome();});
            }catch(Exception e){runOnUiThread(()->Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show());}
        }).start();
    }

    void showQuestionBank(){
        base("Question Bank",true);
        content.addView(tv("40,000+ Practice Questions",24,DARK,true));
        content.addView(tv("NEET + JEE Main • PYQ Pattern • Mixed Practice",13,MUTED,false));
        String[] types={"All Types","MCQ","PYQ Based","Assertion-Reason","Statement Based","Match the Column","Numerical"};
        for(String type:types){
            int count=0; for(Question q:bank) if(type.equals("All Types")||q.type.equals(type)) count++;
            card(type.equals("All Types")?"🔥 All Questions":"📝 "+type,
                    count+" questions • "+(type.equals("All Types")?"NEET + JEE Main":"Mixed chapters"),
                    "Practice",v->{exam=exam.equals("NEET")?"JEE Main":"NEET";startTest();});
        }
    }

    void showStudyPlanner(){
        base("Study Planner",true);
        content.addView(tv("📅 Your Preparation Plan",24,DARK,true));
        content.addView(tv("Plan daily practice across Physics, Chemistry and Biology",13,MUTED,false));
        card("🔥 Today's Target","40 questions • 60 minutes • Mixed practice","Start",v->startTest());
        card("🧬 Biology","NCERT + NEET PYQ practice","Practice",v->startTest());
        card("⚗ Chemistry","Physical + Organic + Inorganic","Practice",v->startTest());
        card("⚡ Physics","Concept + numerical + PYQ","Practice",v->startTest());
        card("📈 Progress","Track attempts, accuracy and test performance","View Analysis",v->showAnalysis());
        card("🎯 7-Day Challenge","Daily streak • 7 tests • 700 questions","Start Challenge",v->startTest());
        card("🏆 Leaderboard","Compare your mock-test performance","View",v->new AlertDialog.Builder(this).setTitle("Leaderboard").setMessage("Leaderboard will populate as test attempts are completed.").setPositiveButton("OK",null).show());
    }

    void showPremium(){
        base("CrackNEET Pro",true);
        LinearLayout hero=box();hero.setBackgroundResource(R.drawable.hero_gradient);hero.setPadding(dp(20),dp(20),dp(20),dp(20));
        hero.addView(tv("✦  CRACKNEET PRO",13,Color.WHITE,true));hero.addView(tv("Serious preparation. Smarter practice.",22,Color.WHITE,true));hero.addView(tv("₹99 / month  •  Cancel anytime",12,Color.rgb(220,240,235),false));content.addView(hero);
        content.addView(tv("Everything you need",19,DARK,true));
        card("💳 Pro Membership","₹99/month • Google Play payment • UPI where available","Buy Pro",v->buyPremium());
        card("🚀 Full Mock Tests","Complete NEET + JEE Main timed tests","Start Mock",v->startTest());
        card("🧠 Advanced Question Bank","40,000+ practice questions across the syllabus","Unlock Pro",v->premiumDialog("Advanced Question Bank"));
        card("📊 Advanced Analytics","Weak chapters, accuracy and performance trends","Unlock Pro",v->premiumDialog("Advanced Analytics"));
        card("⭐ Smart Revision","Revision from attempted questions","Unlock Pro",v->premiumDialog("Smart Revision"));
    }
    void premiumDialog(String feature){new AlertDialog.Builder(this).setTitle("CrackNEET Pro").setMessage(feature+" is included in Pro.").setPositiveButton("Continue",null).setNegativeButton("Later",null).show();}
    void initBilling(){
        billingClient=BillingClient.newBuilder(this).setListener((result,purchases)->{
            if(result.getResponseCode()==BillingClient.BillingResponseCode.OK && purchases!=null){
                for(Purchase p:purchases) if(p.getPurchaseState()==Purchase.PurchaseState.PURCHASED){
                    if(!p.isAcknowledged()) billingClient.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.getPurchaseToken()).build(),br->{});
                    Toast.makeText(this,"CrackNEET Pro activated.",Toast.LENGTH_LONG).show();
                }
            }
        }).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build();
        billingClient.startConnection(new BillingClientStateListener(){
            public void onBillingSetupFinished(BillingResult r){if(r.getResponseCode()==BillingClient.BillingResponseCode.OK) queryPremium();}
            public void onBillingServiceDisconnected(){}
        });
    }
    void queryPremium(){
        QueryProductDetailsParams.Product p=QueryProductDetailsParams.Product.newBuilder().setProductId("crackneet_pro_monthly").setProductType(BillingClient.ProductType.SUBS).build();
        billingClient.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(Collections.singletonList(p)).build(),(r,res)->{
            if(r.getResponseCode()==BillingClient.BillingResponseCode.OK && res.getProductDetailsList()!=null && !res.getProductDetailsList().isEmpty()) premiumProduct=res.getProductDetailsList().get(0);
        });
    }
    void buyPremium(){
        if(billingClient==null||!billingClient.isReady()){Toast.makeText(this,"Google Play payment is connecting. Please try again.",Toast.LENGTH_SHORT).show();return;}
        if(premiumProduct==null){Toast.makeText(this,"Premium plan is not published in Google Play yet.",Toast.LENGTH_LONG).show();return;}
        List<ProductDetails.SubscriptionOfferDetails> offers=premiumProduct.getSubscriptionOfferDetails();
        if(offers==null||offers.isEmpty()){Toast.makeText(this,"Premium offer is unavailable.",Toast.LENGTH_LONG).show();return;}
        String token=offers.get(0).getOfferToken();
        BillingFlowParams.ProductDetailsParams pp=BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(premiumProduct).setOfferToken(token).build();
        BillingFlowParams flow=BillingFlowParams.newBuilder().setProductDetailsParamsList(Collections.singletonList(pp)).build();
        billingClient.launchBillingFlow(this,flow);
    }

    void showDrawer(){
        final String[] items={"⌂  Home","📝  Tests","📚  Notes","📊  Progress","👤  Profile","⭐  Premium"};
        final Dialog dialog=new Dialog(this); LinearLayout panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(18),dp(22),dp(18),dp(18));
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(10,55,55),Color.rgb(18,30,48)}); bg.setCornerRadius(dp(24)); panel.setBackground(bg);
        TextView title=tv("CRACKNEET",20,Color.WHITE,true); title.setGravity(Gravity.CENTER); panel.addView(title,new LinearLayout.LayoutParams(-1,dp(54)));
        TextView sub=tv("Your preparation hub",12,Color.rgb(205,225,220),false); sub.setGravity(Gravity.CENTER); panel.addView(sub,new LinearLayout.LayoutParams(-1,dp(30)));
        for(int k=0;k<items.length;k++){ final int w=k; Button b=btn(items[k]); b.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT); b.setTextSize(15); b.setTextColor(Color.WHITE); b.setBackground(bg(Color.argb(55,255,255,255),16)); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(50)); bp.setMargins(0,dp(5),0,dp(5)); panel.addView(b,bp); b.setOnClickListener(v->{dialog.dismiss();if(w==0)showDashboard();else if(w==1)showSubjects();else if(w==2)showNotes();else if(w==3)showProgress();else if(w==4)showProfile();else showPremium();});}
        dialog.setContentView(panel); Window win=dialog.getWindow(); if(win!=null){win.setBackgroundDrawableResource(android.R.color.transparent);win.setLayout((int)(getResources().getDisplayMetrics().widthPixels*0.86),-2);} dialog.show();
    }
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
            if(subject.equals("Biology")) {String[] c={"The Living World","Biological Classification","Plant Kingdom","Animal Kingdom","Morphology of Flowering Plants","Anatomy of Flowering Plants","Structural Organisation in Animals","Cell: The Unit of Life","Biomolecules","Cell Cycle and Cell Division","Transport in Plants","Mineral Nutrition","Photosynthesis in Plants","Respiration in Plants","Plant Growth and Development","Digestion and Absorption","Breathing and Exchange of Gases","Body Fluids and Circulation","Excretory Products and Elimination","Locomotion and Movement","Neural Control and Coordination","Chemical Coordination and Integration","Sexual Reproduction in Flowering Plants","Human Reproduction","Reproductive Health","Principles of Inheritance and Variation","Molecular Basis of Inheritance","Evolution","Human Health and Disease","Strategies for Enhancement in Food Production","Microbes in Human Welfare","Biotechnology: Principles and Processes","Biotechnology and its Applications","Organisms and Populations","Ecosystem","Biodiversity and Conservation","Environmental Issues"};chapter=c[i%c.length];}
            else if(subject.equals("Physics")) {String[] c={"Units and Measurements","Motion in a Straight Line","Motion in a Plane","Laws of Motion","Work, Energy and Power","System of Particles and Rotational Motion","Gravitation","Properties of Bulk Matter","Thermodynamics","Kinetic Theory","Oscillations","Waves","Electric Charges and Fields","Electrostatic Potential and Capacitance","Current Electricity","Moving Charges and Magnetism","Magnetism and Matter","Electromagnetic Induction","Alternating Current","Electromagnetic Waves","Ray Optics and Optical Instruments","Wave Optics","Dual Nature of Radiation and Matter","Atoms","Nuclei","Semiconductor Electronics"};chapter=c[i%c.length];}
            else if(subject.equals("Chemistry")) {String[] c={"Some Basic Concepts of Chemistry","Structure of Atom","Classification of Elements and Periodicity","Chemical Bonding and Molecular Structure","Thermodynamics","Equilibrium","Redox Reactions","Organic Chemistry: Basic Principles","Hydrocarbons","Solutions","Electrochemistry","Chemical Kinetics","p-Block Elements","d- and f-Block Elements","Coordination Compounds","Haloalkanes and Haloarenes","Alcohols, Phenols and Ethers","Aldehydes, Ketones and Carboxylic Acids","Amines","Biomolecules","Principles Related to Practical Chemistry"};chapter=c[i%c.length];}
            else {String[] c={"Sets and Functions","Complex Numbers","Quadratic Equations","Matrices","Determinants","Permutations and Combinations","Binomial Theorem","Sequences and Series","Limits","Continuity and Differentiability","Integral Calculus","Differential Equations","Coordinate Geometry","Three Dimensional Geometry","Vector Algebra","Statistics","Probability","Trigonometry","Mathematical Reasoning"};chapter=c[i%c.length];}
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
            String[] opts=new String[]{op1,op2,op3,op4}; int ans=i%4; String tmp=opts[0]; opts[0]=opts[ans]; opts[ans]=tmp;
            return new Question(id,exam,subject,chapter,type,i%3==0?"Easy":i%3==1?"Moderate":"Hard",q,opts,ans,sol);
        }
    }}