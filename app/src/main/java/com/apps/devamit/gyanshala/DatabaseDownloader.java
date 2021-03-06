package com.apps.devamit.gyanshala;

import android.app.Notification;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import br.com.goncalves.pugnotification.notification.PugNotification;

class DatabaseDownloader {
    static HashMap<String, ArrayList<String>> quesMetadata=new HashMap<>(),
            quesAnswers=new HashMap<>();
    static ArrayList<String> questionTitleList=new ArrayList<>(), questionDescriptionList=new ArrayList<>(),
                                myQuestionTitleList=new ArrayList<>(), myQuestionDescriptionList=new ArrayList<>();
    static ArrayList<QuestionChildren> questionsIAnswered=new ArrayList<>();
    static MainActivity obj;
    static void refresh() {
        final FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
        DatabaseReference refq=mDatabase.getReference("Questions");
        refq.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                quesMetadata.clear();
                quesAnswers.clear();
                questionTitleList.clear();
                questionDescriptionList.clear();
                myQuestionTitleList.clear();
                myQuestionDescriptionList.clear();
                questionsIAnswered.clear();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    final QuestionChildren question=snapshot.getValue(QuestionChildren.class);

                    if(question.user_id.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])) {
                        //need to generate nnotification
                        PugNotification.with(obj).load().title("Gyanshala")
                                .message("Your question has been answered")
                                .smallIcon(R.mipmap.ic_launcher)
                                .flags(Notification.DEFAULT_ALL)
                                .simple()
                                .build();
                    }

                    //Log.e("in question :", snapshot.getKey());
                    final String[] ques=new String[2];
                    ques[0]=question.q_title;
                    ques[1]=question.q_details;
                    questionTitleList.add(ques[0]);
                    questionDescriptionList.add(ques[1]);
                    if((question.user_id+"@gmail.com").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        //if(quesMetadata.get(ques[0]+" "+ques[1])==null||!quesMetadata.get(ques[0]+" "+ques[1]).contains(question.q_id)) {
                            //Log.e("adding to myques :", "key = "+ques[0]+" "+ques[1]+" qid = "+question.q_id);
                            myQuestionTitleList.add(ques[0]);
                            myQuestionDescriptionList.add(ques[1]);
                        //}
                    }
                    ArrayList<String> metadatas=new ArrayList<>();
                    metadatas.add(question.q_id);
                    metadatas.add(question.user_id);
                    metadatas.add(question.ans_id);
                    //Log.e("putting key :", "as "+ques[0]+" and "+ques[1]);
                    quesMetadata.put(ques[0]+" "+ques[1], metadatas);
                    //Log.e("reading databse :", "the read database has q_id = "+question.q_id);
                    if(!question.ans_id.equals("null")) {
                        for(String x: question.ans_id.split(" ")) {
                            //Log.e("Answer child :", x);
                            DatabaseReference refa=mDatabase.getReference("Answers").child(x);
                            //Log.e("looking for :", refa.getKey());
                            refa.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    AnswerChildren answer=dataSnapshot.getValue(AnswerChildren.class);
                                    if(answer!=null) {
                                        String ans = answer.answer;

                                        //if answer answered by the user, add it
                                        if(answer.user_id.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0])) {
                                            Log.e("wanna add :", question.q_title+" and "+question.q_details+" ans = "+ans+" contains = "+questionsIAnswered.contains(question));
                                            if(!questionsIAnswered.contains(question))
                                                questionsIAnswered.add(question);
                                        }

                                        ArrayList<String> ansList = quesAnswers.get(ques[0] + " " + ques[1]);
                                        if (ansList == null)
                                            ansList = new ArrayList<>();
                                        //ArrayList<String> ansList =new ArrayList<>();
                                        if(!ansList.contains(ans+"\n\n-"+answer.user_id))
                                            ansList.add(ans+"\n\n-"+answer.user_id);
                                        //Log.e("putting ans :", "with ansList size = "+ansList.size()+" and ques = "+ques[0]);
                                        quesAnswers.put(ques[0] + " " + ques[1], ansList);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                obj.refreshUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}