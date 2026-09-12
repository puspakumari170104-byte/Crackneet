require("dotenv").config();
const express=require("express");
const cors=require("cors");
const helmet=require("helmet");
const crypto=require("crypto");
const bcrypt=require("bcryptjs");
const {Pool}=require("pg");
const app=express();
app.use(helmet());
app.use(cors({origin:process.env.CORS_ORIGIN||"*"}));
app.post("/api/payments/webhook", express.raw({type:"application/json"}), async (req,res)=>{
 try{
  const secret=process.env.RAZORPAY_WEBHOOK_SECRET;
  const signature=req.headers["x-razorpay-signature"];
  if(!secret||!signature) return res.status(400).json({error:"webhook_not_configured"});
  const expected=crypto.createHmac("sha256",secret).update(req.body).digest("hex");
  if(!crypto.timingSafeEqual(Buffer.from(expected),Buffer.from(String(signature)))) return res.status(401).json({error:"invalid_webhook_signature"});
  const event=JSON.parse(req.body.toString("utf8"));
  if(event.event==="payment.captured"||event.event==="order.paid"){
   const p=event.payload?.payment?.entity;
   const o=event.payload?.order?.entity;
   const orderId=p?.order_id||o?.id;
   if(orderId) await pool.query("UPDATE subscriptions SET status='paid',payment_id=COALESCE($1,payment_id) WHERE order_id=$2", [p?.id||null,orderId]);
  }
  res.json({ok:true});
 }catch(e){console.error("Webhook error",e);res.status(400).json({error:"webhook_failed"});}
});
app.use(express.json({limit:"2mb"}));
const pool=new Pool({connectionString:process.env.DATABASE_URL,max:Number(process.env.DB_POOL_MAX||10),idleTimeoutMillis:Number(process.env.DB_IDLE_TIMEOUT_MS||30000),connectionTimeoutMillis:Number(process.env.DB_CONNECTION_TIMEOUT_MS||5000),ssl:process.env.DATABASE_SSL==="true"?{rejectUnauthorized:process.env.DATABASE_SSL_REJECT_UNAUTHORIZED!=="false"}:undefined});
pool.on("error",(err)=>console.error("PostgreSQL pool error:",err));
async function ensureDatabase(){

 await pool.query(`CREATE TABLE IF NOT EXISTS users(id BIGSERIAL PRIMARY KEY,name TEXT NOT NULL,email TEXT UNIQUE NOT NULL,password_hash TEXT,created_at TIMESTAMPTZ NOT NULL DEFAULT NOW())`);
 await pool.query(`CREATE TABLE IF NOT EXISTS questions(id TEXT PRIMARY KEY,exam TEXT NOT NULL,subject TEXT NOT NULL,chapter TEXT,type TEXT NOT NULL,difficulty TEXT,question TEXT NOT NULL,options JSONB NOT NULL,answer_index INT NOT NULL,solution TEXT)`);
 await pool.query(`CREATE TABLE IF NOT EXISTS test_attempts(id BIGSERIAL PRIMARY KEY,user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,exam TEXT NOT NULL,score INT NOT NULL DEFAULT 0,total INT NOT NULL,correct INT NOT NULL DEFAULT 0,wrong INT NOT NULL DEFAULT 0,skipped INT NOT NULL DEFAULT 0,duration_seconds INT NOT NULL DEFAULT 0,answers JSONB,created_at TIMESTAMPTZ NOT NULL DEFAULT NOW())`);
 await pool.query(`CREATE TABLE IF NOT EXISTS sessions(id BIGSERIAL PRIMARY KEY,user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,token_hash TEXT UNIQUE NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),expires_at TIMESTAMPTZ NOT NULL)`);
 await pool.query(`CREATE TABLE IF NOT EXISTS subscriptions(id BIGSERIAL PRIMARY KEY,user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,plan TEXT NOT NULL,order_id TEXT UNIQUE NOT NULL,payment_id TEXT,amount_paise INT NOT NULL,status TEXT NOT NULL DEFAULT 'created',created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW())`);
 await pool.query("CREATE INDEX IF NOT EXISTS idx_subscriptions_user ON subscriptions(user_id,created_at DESC)");
 await pool.query("CREATE INDEX IF NOT EXISTS idx_questions_filters ON questions(exam,subject,type)");
 await pool.query("CREATE INDEX IF NOT EXISTS idx_attempts_user ON test_attempts(user_id,created_at DESC)");
 await pool.query("CREATE INDEX IF NOT EXISTS idx_sessions_token ON sessions(token_hash)");
 const count=await pool.query("SELECT COUNT(*)::int AS n FROM questions");
 if(count.rows[0].n===0){
  const bank=require("../../question_bank.json");
  for(const q of bank.questions||[]){
   const options=Array.isArray(q.options)?q.options:[];
   const answerIndex=Number.isInteger(q.answer_index)?q.answer_index:(Number.isInteger(q.answer)?q.answer:null);
   if(!q.exam||!q.subject||!q.type||!q.q||answerIndex===null) continue;
   const id=q.id||crypto.createHash("sha256").update(JSON.stringify(q)).digest("hex").slice(0,24);
   await pool.query("INSERT INTO questions(id,exam,subject,chapter,type,difficulty,question,options,answer_index,solution) VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9,$10) ON CONFLICT(id) DO NOTHING",[id,q.exam,q.subject,q.chapter||null,q.type,q.difficulty||null,q.q,JSON.stringify(options),answerIndex,q.solution||null]);
  }
 }
}

app.get("/api/health",async(req,res)=>{try{await pool.query("SELECT 1");res.json({ok:true,service:"crackneet-api",database:"connected"});}catch(e){res.status(503).json({ok:false,database:"unavailable"});}});

app.get("/privacy-policy",(req,res)=>{res.type("html").send(`<!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>CrackNEET Privacy Policy</title></head><body><h1>CrackNEET Privacy Policy</h1><p>Last updated: September 12, 2026</p><p>CrackNEET collects account information such as name and email when you create an account, stores a password securely as a hash, and stores test attempts and progress to provide the service. We do not sell personal information.</p><h2>Deletion</h2><p>You can request account deletion from the app profile/account area. Account data and associated test attempts are permanently deleted.</p><h2>Security</h2><p>Passwords are stored as one-way hashes and the production API is served over HTTPS.</p><h2>Contact</h2><p>Privacy questions can be submitted through the developer contact shown on Google Play.</p></body></html>`);});

function tokenHash(t){return crypto.createHash("sha256").update(t).digest("hex");}
async function auth(req,res,next){
 const h=req.headers.authorization||"";
 if(!h.startsWith("Bearer ")) return res.status(401).json({error:"authentication_required"});
 const token=h.slice(7), hash=tokenHash(token);
 const {rows}=await pool.query("SELECT u.id,u.name,u.email,s.token_hash FROM sessions s JOIN users u ON u.id=s.user_id WHERE s.token_hash=$1 AND s.expires_at>NOW()",[hash]);
 if(!rows.length) return res.status(401).json({error:"invalid_or_expired_token"});
 req.user=rows[0]; next();
}
function issueToken(){return crypto.randomBytes(32).toString("hex");}

app.post("/api/auth/register",async(req,res)=>{
 try{
  const {name,email,password}=req.body||{};
  if(!name||!email||!password||password.length<8) return res.status(400).json({error:"name_email_and_8_char_password_required"});
  const normalized=String(email).trim().toLowerCase();
  const exists=await pool.query("SELECT id FROM users WHERE email=$1",[normalized]);
  if(exists.rows.length) return res.status(409).json({error:"email_already_registered"});
  const hash=await bcrypt.hash(String(password),12);
  const u=await pool.query("INSERT INTO users(name,email,password_hash) VALUES($1,$2,$3) RETURNING id,name,email",[String(name).trim(),normalized,hash]);
  const token=issueToken();
  await pool.query("INSERT INTO sessions(user_id,token_hash,expires_at) VALUES($1,$2,NOW()+INTERVAL '30 days')",[u.rows[0].id,tokenHash(token)]);
  res.status(201).json({user:u.rows[0],token});
 }catch(e){res.status(500).json({error:"registration_failed"});}
});

app.post("/api/auth/login",async(req,res)=>{
 try{
  const {email,password}=req.body||{};
  if(!email||!password) return res.status(400).json({error:"email_and_password_required"});
  const {rows}=await pool.query("SELECT id,name,email,password_hash FROM users WHERE email=$1",[String(email).trim().toLowerCase()]);
  if(!rows.length||!rows[0].password_hash||!(await bcrypt.compare(String(password),rows[0].password_hash))) return res.status(401).json({error:"invalid_credentials"});
  const token=issueToken();
  await pool.query("INSERT INTO sessions(user_id,token_hash,expires_at) VALUES($1,$2,NOW()+INTERVAL '30 days')",[rows[0].id,tokenHash(token)]);
  res.json({user:{id:rows[0].id,name:rows[0].name,email:rows[0].email},token});
 }catch(e){res.status(500).json({error:"login_failed"});}
});

app.post("/api/auth/logout",auth,async(req,res)=>{try{await pool.query("DELETE FROM sessions WHERE token_hash=$1",[req.user.token_hash]);res.json({ok:true});}catch(e){res.status(500).json({error:"logout_failed"});}});
app.get("/api/users/me",auth,(req,res)=>res.json({user:{id:req.user.id,name:req.user.name,email:req.user.email}}));
const razorpay = process.env.RAZORPAY_KEY_ID && process.env.RAZORPAY_KEY_SECRET
 ? new (require("razorpay"))({key_id:process.env.RAZORPAY_KEY_ID,key_secret:process.env.RAZORPAY_KEY_SECRET}) : null;
const PLANS={basic:{amount:1900,questions:10000},moderate:{amount:4900,questions:20000},advance:{amount:9900,questions:null}};
app.post("/api/payments/create-order",auth,async(req,res)=>{
 try{
  if(!razorpay) return res.status(503).json({error:"razorpay_not_configured"});
  const plan=String(req.body?.plan||"").toLowerCase();
  if(!PLANS[plan]) return res.status(400).json({error:"invalid_plan"});
  const p=PLANS[plan];
  const order=await razorpay.orders.create({amount:p.amount,currency:"INR",receipt:"cn_"+req.user.id+"_"+Date.now(),notes:{user_id:String(req.user.id),plan}});
  await pool.query("INSERT INTO subscriptions(user_id,plan,order_id,amount_paise,status) VALUES($1,$2,$3,$4,'created') ON CONFLICT(order_id) DO NOTHING",[req.user.id,plan,order.id,p.amount]);
  res.json({orderId:order.id,amount:p.amount,currency:"INR",plan,questions:p.questions,keyId:process.env.RAZORPAY_KEY_ID});
 }catch(e){console.error("Create order error",e);res.status(500).json({error:"order_creation_failed"});}
});
app.post("/api/payments/verify",auth,async(req,res)=>{
 try{
  const {razorpay_order_id,razorpay_payment_id,razorpay_signature}=req.body||{};
  if(!razorpay_order_id||!razorpay_payment_id||!razorpay_signature||!process.env.RAZORPAY_KEY_SECRET) return res.status(400).json({error:"invalid_payment_verification"});
  const expected=crypto.createHmac("sha256",process.env.RAZORPAY_KEY_SECRET).update(razorpay_order_id+"|"+razorpay_payment_id).digest("hex");
  if(expected!==razorpay_signature) return res.status(400).json({error:"invalid_payment_signature"});
  const r=await pool.query("UPDATE subscriptions SET status='paid',payment_id=$1,updated_at=NOW() WHERE order_id=$2 AND user_id=$3 RETURNING id,plan,amount_paise,status,created_at",[razorpay_payment_id,razorpay_order_id,req.user.id]);
  if(!r.rows.length) return res.status(404).json({error:"order_not_found"});
  res.json({ok:true,subscription:r.rows[0]});
 }catch(e){res.status(500).json({error:"payment_verification_failed"});}
});
app.get("/api/users/me/subscription",auth,async(req,res)=>{
 try{
  const {rows}=await pool.query("SELECT id,plan,amount_paise,status,payment_id,created_at FROM subscriptions WHERE user_id=$1 ORDER BY created_at DESC LIMIT 1",[req.user.id]);
  res.json({subscription:rows[0]||null});
 }catch(e){res.status(500).json({error:"subscription_fetch_failed"});}
});


app.delete("/api/auth/account",auth,async(req,res)=>{
 const client=await pool.connect();
 try{await client.query("BEGIN");await client.query("DELETE FROM test_attempts WHERE user_id=$1",[req.user.id]);await client.query("DELETE FROM sessions WHERE user_id=$1",[req.user.id]);await client.query("DELETE FROM users WHERE id=$1",[req.user.id]);await client.query("COMMIT");res.json({ok:true});}
 catch(e){await client.query("ROLLBACK").catch(()=>{});res.status(500).json({error:"account_deletion_failed"});}
 finally{client.release();}
});

app.get("/api/questions",async(req,res)=>{try{const {exam,subject,type,limit}=req.query;const n=Math.min(Math.max(parseInt(limit||20),1),100);const params=[];const where=[];if(exam){params.push(exam);where.push("exam=$"+params.length)}if(subject){params.push(subject);where.push("subject=$"+params.length)}if(type){params.push(type);where.push("type=$"+params.length)}params.push(n);const q="SELECT id,exam,subject,chapter,type,difficulty,question,options,answer_index,solution FROM questions "+(where.length?"WHERE "+where.join(" AND "):"")+" ORDER BY RANDOM() LIMIT $"+params.length;const {rows}=await pool.query(q,params);res.json({count:rows.length,questions:rows});}catch(e){res.status(500).json({error:"question_fetch_failed"});}});

app.post("/api/tests/submit",auth,async(req,res)=>{
 try{const {exam,score,total,correct,wrong,skipped,durationSeconds,answers}=req.body;if(!exam||total==null) return res.status(400).json({error:"invalid_submission"});const r=await pool.query("INSERT INTO test_attempts(user_id,exam,score,total,correct,wrong,skipped,duration_seconds,answers) VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9) RETURNING id,created_at",[req.user.id,exam,score||0,total,correct||0,wrong||0,skipped||0,durationSeconds||0,JSON.stringify(answers||[])]);res.status(201).json(r.rows[0]);}
 catch(e){res.status(500).json({error:"submission_failed"});}
});

app.get("/api/users/me/progress",auth,async(req,res)=>{try{const {rows}=await pool.query("SELECT COUNT(*) attempts,COALESCE(SUM(correct),0) correct,COALESCE(SUM(wrong),0) wrong,COALESCE(SUM(skipped),0) skipped,COALESCE(AVG(CASE WHEN total>0 THEN score::numeric*100/total END),0) accuracy FROM test_attempts WHERE user_id=$1",[req.user.id]);res.json({user:req.user,progress:rows[0]});}catch(e){res.status(500).json({error:"progress_failed"});}});

const PORT=process.env.PORT||8080;
ensureDatabase().then(()=>app.listen(PORT,()=>console.log("CrackNEET API listening on "+PORT))).catch(e=>{console.error("Database initialization failed",e);process.exit(1);});
