package com.teamproject.gastroventure.menu.member;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.util.DialogSampleUtil;
import com.teamproject.gastroventure.util.LoginSharedPreference;
import com.teamproject.gastroventure.vo.UserInfo;

import java.lang.reflect.Member;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberFindIdPwd#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberFindIdPwd extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseDatabase member_db;
    private DatabaseReference db_ref;

    private View view;

    EditText et_find_id_name, et_find_id_tel, et_find_pwd_name, et_find_pwd_id, et_find_pwd_tel;
    Button btn_find_id, btn_find_pwd;

    EditText et_modify_pwd, et_modify_pwd_check;

    String find_id_name, find_id_tel, find_pwd_name, find_pwd_id, find_pwd_tel;

    String check_id_name, check_id_tel;
    String check_pwd_name, check_pwd_id, check_pwd_tel;

    LayoutInflater  inflater;

    MainActivity main;

    MemberLoginFormFragment loginFrag;

    private ArrayList<UserInfo> memberInfo = new ArrayList<UserInfo>();

    public MemberFindIdPwd() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberFindIdPwd.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberFindIdPwd newInstance(String param1, String param2) {
        MemberFindIdPwd fragment = new MemberFindIdPwd();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_member_find_id_pwd, container, false);

        main = (MainActivity)getActivity();
        loginFrag = new MemberLoginFormFragment();

        member_db = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        db_ref = member_db.getReference(); // DB 테이블 연결

        //아이디 찾기
        et_find_id_name = view.findViewById(R.id.et_find_id_name);
        et_find_id_tel =view.findViewById(R.id.et_find_id_tel);
        btn_find_id = view.findViewById(R.id.btn_find_id);

        //비밀번호찾기
        et_find_pwd_name = view.findViewById(R.id.et_find_pwd_name);
        et_find_pwd_id =view.findViewById(R.id.et_find_pwd_id);
        et_find_pwd_tel = view.findViewById(R.id.et_find_pwd_tel);
        btn_find_pwd = view.findViewById(R.id.btn_find_pwd);

        find_info();

        btn_find_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DB에서 이름과 전화번호 검색 후 일치하는 아이디 다이얼로그로 띄워주기

                //아이디 찾기 입력값
                find_id_name = et_find_id_name.getText().toString().trim();
                find_id_tel = et_find_id_tel.getText().toString().trim();

                if(find_id_name.isEmpty() || find_id_tel.isEmpty()){
                    DialogSampleUtil.showMessageDialog(getContext(),"","이름 또는 전화번호를 입력해주세요.");
                    return;
                }


                for( UserInfo check_id : memberInfo ){
                    check_id_name = check_id.getName();
                    check_id_tel = check_id.getTel();

                    if(check_id_name.equals(find_id_name) && check_id_tel.equals(find_id_tel)){
                        String answer_id = check_id.getId();
                        DialogSampleUtil.showMessageDialog(getContext(),"","회원님의 아이디는 ["+answer_id+"] 입니다.");
                        break;
                    }else {
                        DialogSampleUtil.showMessageDialog(getContext(),"","입력정보와 일치하는 계정이 없습니다.");
                        break;
                    }
                }

            }
        });

        btn_find_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DB에서 아이디, 이름, 전화번호 검색 후 일치하는 계정 비밀번호 재설정 진행

                //비밀번호 찾기 입력값
                find_pwd_name = et_find_pwd_name.getText().toString().trim();
                find_pwd_id = et_find_pwd_id.getText().toString().trim();
                find_pwd_tel = et_find_pwd_tel.getText().toString().trim();

                if(find_pwd_name.isEmpty() || find_pwd_id.isEmpty() || find_pwd_tel.isEmpty()){
                    DialogSampleUtil.showMessageDialog(getContext(),"","이름, ID, 전화번호를 입력해주세요");
                    return;
                }

                for(UserInfo check_pwd : memberInfo){
                    check_pwd_id  =  check_pwd.getId();
                    check_pwd_name = check_pwd.getName();
                    check_pwd_tel = check_pwd.getTel();

                    if(check_pwd_id.equals(find_pwd_id) && check_pwd_name.equals(find_pwd_name) && check_pwd_tel.equals(find_pwd_tel)){
                        modify_pwd(check_pwd);
                        et_find_pwd_name.setText("");
                        et_find_pwd_id.setText("");
                        et_find_pwd_tel.setText("");
                    }else {
                        DialogSampleUtil.showMessageDialog(getContext(),"","입력정보와 일치하는 계정이 없습니다.");
                        break;
                    }
                }

            }
        });

        // 프래그먼트가 옵션 메뉴를 가질수 있도록 설정
        setHasOptionsMenu(true);

        return view;
    }

    public void find_info(){
        db_ref.child("Member").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    UserInfo vo = dataSnapshot1.getValue(UserInfo.class);
                    vo.setUser_key(dataSnapshot1.getKey());

                    memberInfo.add(vo);

                    Log.d("LLLL", "아이디찾기:" + vo.getName() + "/" + vo.getTel() + "/" + vo.getUser_key());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void modify_pwd(final UserInfo vo){

        View view = inflater.inflate(R.layout.member_refactor_pwd,null);

        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());

        et_modify_pwd = view.findViewById(R.id.et_modify_pwd);
        et_modify_pwd_check = view.findViewById(R.id.et_modify_pwd_check);

        builder.setView(view).setPositiveButton("재설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String modify_pwd = et_modify_pwd.getText().toString().trim();
                String modify_pwd_check = et_modify_pwd_check.getText().toString().trim();

                if(modify_pwd.equals(modify_pwd_check)){
                    vo.setPwd(modify_pwd);

                    db_ref.child("Member").child(vo.getUser_key()).setValue(vo);

                    dialog.dismiss();
                }

            }
        })
        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();

    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.move_login_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_move_login:
                // 로그인 화면으로 이동
                main.replaceFragment(loginFrag);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
