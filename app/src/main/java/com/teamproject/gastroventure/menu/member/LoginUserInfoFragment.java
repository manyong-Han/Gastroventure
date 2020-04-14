package com.teamproject.gastroventure.menu.member;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.util.DialogSampleUtil;
import com.teamproject.gastroventure.util.LoginSharedPreference;
import com.teamproject.gastroventure.vo.ReviewVo;
import com.teamproject.gastroventure.vo.UserInfo;

public class LoginUserInfoFragment extends Fragment {

    private static final String USER_KEY = "user_key";
    private static String user_key;

    private FirebaseDatabase member_db;
    private DatabaseReference db_ref;

    public static LoginUserInfoFragment newInstance(String user_key){
        LoginUserInfoFragment fragment = new LoginUserInfoFragment();
        Bundle args = new Bundle();
        args.putString(USER_KEY, user_key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user_key = getArguments().getString(USER_KEY);
        }
    }

    private View view;

    ImageView civ_rank;
    TextView tv_show_nickname, tv_grade, tv_writen_review;
    Button btn_user_info, btn_dodge;

    MainActivity main;

    MemberModifyFormFragment modifyFrag;
    LogoutUserInfoFragment logoutFrag;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_login_user_info, container, false);

        main = (MainActivity)getActivity();

        member_db = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        db_ref = member_db.getReference(); // DB 테이블 연결

        modifyFrag = new MemberModifyFormFragment();
        logoutFrag = new LogoutUserInfoFragment();


        //등급별 이미지뷰
        civ_rank = view.findViewById(R.id.civ_rank);
        //닉네임, 등급, 작성게시물수 텍스트뷰
        tv_show_nickname = view.findViewById(R.id.tv_show_nickname);
        tv_grade = view.findViewById(R.id.tv_grade);
        tv_writen_review = view.findViewById(R.id.tv_writen_review);
        // 정보수정, 회원탈퇴 버튼
        btn_user_info = view.findViewById(R.id.btn_user_info);
        btn_dodge = view.findViewById(R.id.btn_dodge);

        btn_user_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Log.d("LLLL", "회원정보수정버튼 눌렀을때 유저키 :"+user_key);
                ft.replace(R.id.main_frame, modifyFrag.newInstance(user_key)).commit();
            }
        });

        btn_dodge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Handler handler = new Handler(){

                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        if (msg.what == 1) {//Yes
                            try {
                                db_ref.child("Member").child(user_key).removeValue();
                                Log.d("LLLL", "회원삭제 완료");
                                LoginSharedPreference.removeAttribute(getContext(), MemberLoginFormFragment.LOGIN_ID);
                                LoginSharedPreference.removeAttribute(getContext(), MemberLoginFormFragment.AUTO_ID);
                                LoginSharedPreference.removeAttribute(getContext(), MemberLoginFormFragment.USER_KEY);
                                LoginSharedPreference.removeAttribute(getContext(), MemberLoginFormFragment.SAVE_ID);
                                LoginSharedPreference.removeAttribute(getContext(), MemberLoginFormFragment.IS_CHECKED);

                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                FragmentTransaction ft = fm.beginTransaction();
                                ft.replace(R.id.main_frame, new LogoutUserInfoFragment()).commit();
                            } catch (Exception e){
                                Log.d("LLLL", e.getMessage());
                            }
                        }
                    }
                };
                //다이얼로그 이용해서 정말 삭제할건지 확인 후 삭제, 취소 선택
                DialogSampleUtil.showConfirmDialog(getContext(),"","정말 회원탈퇴 하시겠습니까?", handler);

           }
        });

        setting_review_info();

        // 프래그먼트가 옵션 메뉴를 가질수 있도록 설정
        setHasOptionsMenu(true);

        return view;
    }

    // 등급별 이미지, 등급, 닉네임
    public void setting_review_info(){
        db_ref.child("Member").child(user_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInfo vo = dataSnapshot.getValue(UserInfo.class);
                //닉네임 세팅
                tv_show_nickname.setText(vo.getNickname());

                String review_id = vo.getId();
                Log.d("LLLL", "리뷰쓴 아이디 : "+review_id);

                db_ref.child("Review").orderByChild("write_user").equalTo(review_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int count = (int) dataSnapshot.getChildrenCount();
                        Log.d("LLLL", "카운트 :"+count);

                        //리뷰 수 표시
                        tv_writen_review.setText("작성한 리뷰 ("+count+")");

                        //이미지, 등급 표시
                        if(count==0){
                            tv_grade.setText("언랭크");
                            civ_rank.setImageResource(R.drawable.rank_unrank);
                        }else if(count>0 && count<=5){
                            tv_grade.setText("브론즈");
                            civ_rank.setImageResource(R.drawable.rank_bronze);
                        }else if(count>5 && count<=15){
                            tv_grade.setText("실버");
                            civ_rank.setImageResource(R.drawable.rank_silver);
                        }else if(count>15 && count<=50){
                            tv_grade.setText("골드");
                            civ_rank.setImageResource(R.drawable.rank_gold);
                        }else if(count>50){
                            tv_grade.setText("VIP");
                            civ_rank.setImageResource(R.drawable.rank_vip);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.logout_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_logout:
                // 자동 로그인 체크, 로그인 정보 삭제
                LoginSharedPreference.removeAttribute(getContext(), MemberLoginFormFragment.LOGIN_ID);
                LoginSharedPreference.removeAttribute(getContext(), MemberLoginFormFragment.AUTO_ID);
                LoginSharedPreference.removeAttribute(getContext(),MemberLoginFormFragment.USER_KEY);

                main.replaceFragment(logoutFrag);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
