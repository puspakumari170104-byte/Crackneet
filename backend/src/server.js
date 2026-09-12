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
app.use(express.json({limit:"2mb"}));
const pool=new Pool({connectionString:process.env.DATABASE_URL});
async function ensureSessionTable(){
 await pool.query("CREATE TABLE IF NOT EXISTS sessions(id BIGSERIAL PRIMARY KEY,user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,token_hash TEXT UNIQUE NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),expires_at TIMESTAMPTZ NOT NULL)");
 await pool.query("CREATE INDEX IF NOT EXISTS idx_sessions_token ON sessions(token_hash)");
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

app.delete("/api/auth/account",auth,async(req,res)=>{
 try{await pool.query("BEGIN");await pool.query("DELETE FROM test_attempts WHERE user_id=$1",[req.user.id]);await pool.query("DELETE FROM sessions WHERE user_id=$1",[req.user.id]);await pool.query("DELETE FROM users WHERE id=$1",[req.user.id]);await pool.query("COMMIT");res.json({ok:true});}catch(e){await pool.query("ROLLBACK");res.status(500).json({error:"account_deletion_failed"});}
});

app.get("/api/questions",async(req,res)=>{try{const {exam,subject,type,limit}=req.query;const n=Math.min(Math.max(parseInt(limit||20),1),100);const params=[];const where=[];if(exam){params.push(exam);where.push("exam=$"+params.length)}if(subject){params.push(subject);where.push("subject=$"+params.length)}if(type){params.push(type);where.push("type=$"+params.length)}params.push(n);const q="SELECT id,exam,subject,chapter,type,difficulty,question,options,answer_index,solution FROM questions "+(where.length?"WHERE "+where.join(" AND "):"")+" ORDER BY RANDOM() LIMIT $"+params.length;const {rows}=await pool.query(q,params);res.json({count:rows.length,questions:rows});}catch(e){res.status(500).json({error:"question_fetch_failed"});}});

app.post("/api/tests/submit",auth,async(req,res)=>{
 try{const {exam,score,total,correct,wrong,skipped,durationSeconds,answers}=req.body;if(!exam||total==null) return res.status(400).json({error:"invalid_submission"});const r=await pool.query("INSERT INTO test_attempts(user_id,exam,score,total,correct,wrong,skipped,duration_seconds,answers) VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9) RETURNING id,created_at",[req.user.id,exam,score||0,total,correct||0,wrong||0,skipped||0,durationSeconds||0,JSON.stringify(answers||[])]);res.status(201).json(r.rows[0]);}
 catch(e){res.status(500).json({error:"submission_failed"});}
});

app.get("/api/users/me/progress",auth,async(req,res)=>{try{const {rows}=await pool.query("SELECT COUNT(*) attempts,COALESCE(SUM(correct),0) correct,COALESCE(SUM(wrong),0) wrong,COALESCE(SUM(skipped),0) skipped,COALESCE(AVG(CASE WHEN total>0 THEN score::numeric*100/total END),0) accuracy FROM test_attempts WHERE user_id=$1",[req.user.id]);res.json({user:req.user,progress:rows[0]});}catch(e){res.status(500).json({error:"progress_failed"});}});

const PORT=process.env.PORT||8080;
ensureSessionTable().then(()=>app.listen(PORT,()=>console.log("CrackNEET API listening on "+PORT))).catch(e=>{console.error("Database initialization failed",e);process.exit(1);});
