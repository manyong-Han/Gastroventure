package com.teamproject.gastroventure.menu.member;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
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
import com.teamproject.gastroventure.vo.UserInfo;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberModifyFormFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberModifyFormFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER_KEY = "user_key";
    String user_key;

    String TAG = "LLLL";
    private FirebaseDatabase member_db;
    private DatabaseReference db_ref;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View view;
    EditText et_id_email,et_pwd, et_pwd_check, et_name, et_nickname, et_tel;
    Button btn_modify, btn_cancel;

    MainActivity main;

    LoginUserInfoFragment login_user_info_Frag;

    String id,pwd,pwd_check,name,nickname,tel;

    private ArrayList<UserInfo> memberInfo = new ArrayList<UserInfo>();

    public MemberModifyFormFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MemberModifyFormFragment newInstance(String user_key) {
        MemberModifyFormFragment fragment = new MemberModifyFormFragment();
        Bundle args = new Bundle();
        args.putString(USER_KEY, user_key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user_key = getArguments().getString(USER_KEY);
            Log.d("dasda","수정폼 유저키 : " + user_key);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_member_modify_form, container, false);

        main = (MainActivity)getActivity();

        member_db = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        db_ref = member_db.getReference(); // DB 테이블 연결

        login_user_info_Frag = new LoginUserInfoFragment();

        et_id_email = view.findViewById(R.id.et_id_email);
        et_pwd = view.findViewById(R.id.et_pwd);
        et_pwd_check  = view.findViewById(R.id.et_pwd_check);
        et_name = view.findViewById(R.id.et_name);
        et_nickname = view.findViewById(R.id.et_nickname);
        et_tel = view.findViewById(R.id.et_tel);

        btn_modify = view.findViewById(R.id.btn_modify);
        btn_cancel = view.findViewById(R.id.btn_cancel);

        member_data();

        db_ref.child("Member").child(user_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                UserInfo vo = dataSnapshot.getValue(UserInfo.class);
                id = vo.getId();
                et_id_email.setText(id);
                et_name.setText(vo.getName());
                et_nickname.setText(vo.getNickname());
                et_tel.setText(vo.getTel());

                //정보값에 null값이 들어옴
                Log.d("LLLL", "수정폼에서 유저키값 : "+user_key+"/"+"정보값 :"+vo.getId()+"/"+vo.getName()+"/"+vo.getNickname()+"/"+vo.getTel());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btn_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "수정버튼 누름");
                pwd = et_pwd.getText().toString().trim();
                pwd_check  = et_pwd_check.getText().toString().trim();
                name = et_name.getText().toString().trim();
                nickname = et_nickname.getText().toString().trim();
                tel = et_tel.getText().toString().trim();

                Log.d(TAG, "각 값을 스트링변수에 입력");

                Log.d(TAG, "공백값 확인");
                if(pwd.isEmpty()){
                    DialogSampleUtil.showMessageDialog(getContext(),"","비밀번호를 입력해주세요.");
                    return;
                }
                if(pwd_check.isEmpty()){
                    DialogSampleUtil.showMessageDialog(getContext(),"","비밀번호를 재입력해주세요.");
                    return;
                }
                if(!pwd_check.equals(pwd)){
                    DialogSampleUtil.showMessageDialog(getContext(),"","비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
                    return;
                }
                if(name.isEmpty()){
                    DialogSampleUtil.showMessageDialog(getContext(),"","이름을 입력해주세요.");
                    return;
                }
                if(nickname.isEmpty()){
                    DialogSampleUtil.showMessageDialog(getContext(),"","닉네임을 입력해주세요.");
                    return;
                }
                if(tel.isEmpty()){
                    DialogSampleUtil.showMessageDialog(getContext(),"","전화번호를 입력해주세요.");
                    return;
                }

                if(isValidtel(tel)==false){
                    Toast.makeText(getContext(),"유효하지 않은 형식의 전화번호입니다.",Toast.LENGTH_SHORT).show();
                    et_tel.requestFocus();
                    return;
                }

                Log.d(TAG, "디비조회");

                boolean checkB =false;
                for( UserInfo vo : memberInfo ){
                    String check_user_key = vo.getUser_key();
                    String check_tel = vo.getTel();
                    if(check_tel.equals(tel) && !check_user_key.equals(user_key)){
                        checkB= true;
                        et_tel.requestFocus();
                        break;
                    }

                    //DB에 정보 업데이트
                    vo.setId(id);
                    vo.setPwd(pwd);
                    vo.setName(name);
                    vo.setNickname(nickname);
                    vo.setTel(tel);
                    vo.setUser_key(user_key);

                    db_ref.child("Member").child(user_key).setValue(vo);
                    Log.d(TAG, "멤버 테이블에 수정");
                }
                if(checkB) {
                    DialogSampleUtil.showMessageDialog(getContext(),"","이미 사용중인 전화번호 입니다.");
                    return;
                } else {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.main_frame, login_user_info_Frag.newInstance(user_key)).commit();
                    Log.d(TAG, "기본정보화면으로 이동");

                    Log.d("LLLL", "키값 :" + user_key);
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.replaceFragment(login_user_info_Frag);
            }
        });


        return view;
    }


    public void member_data() {
        //DB에서 id, pwd 일치하는 경우 로그인 페이지로 이동, 일치하지 않을 경우 다이얼로그 이용해서 알려주기
        db_ref.child("Member").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    UserInfo vo = dataSnapshot1.getValue(UserInfo.class);
                    vo.setUser_key(dataSnapshot1.getKey());

                    memberInfo.add(vo);

                    Log.d("LLLL", "입력값:" + vo.getId() + "/" + vo.getPwd() + "/" + vo.getName() + "/" +vo.getNickname() +"/"+ vo.getTel());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //전화번호 유효성 정규식
    public boolean isValidtel(String tel){
        return tel.matches("(01[016789])(\\d{3,4})(\\d{4})");
    }
}
