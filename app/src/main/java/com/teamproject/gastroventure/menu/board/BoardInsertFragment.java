package com.teamproject.gastroventure.menu.board;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.menu.member.MemberLoginFormFragment;
import com.teamproject.gastroventure.util.LoginSharedPreference;
import com.teamproject.gastroventure.vo.BoardVo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class BoardInsertFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    private View view;
    private MainActivity mainActivity;

    private EditText board_insert_title;
    private EditText board_insert_content;
    private Button board_insert_btn;
    private Button board_cancle_btn;
    private FirebaseDatabase boardDatabase;
    private DatabaseReference databaseReference;


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int board_num_int = 0;

    public BoardInsertFragment() {

    }


    // TODO: Rename and change types and number of parameters
    public static BoardInsertFragment newInstance(String param1, String param2) {
        BoardInsertFragment fragment = new BoardInsertFragment();
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
        view = inflater.inflate(R.layout.fragment_board_insert_form,container,false);
        mainActivity = (MainActivity) getActivity();

        board_insert_title = view.findViewById(R.id.board_insert_title);
        board_insert_content = view.findViewById(R.id.board_insert_content);
        board_insert_btn = view.findViewById(R.id.board_insert_btn);
        board_cancle_btn = view.findViewById(R.id.board_cancle_btn);
        boardDatabase  = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        databaseReference = boardDatabase.getReference(); // DB 테이블 연결

        dataRead();

        board_insert_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = board_insert_title.getText().toString();
                String content = board_insert_content.getText().toString();

                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("yyyy년 MM월 dd일");
                String date = df.format(c);

                if (title.isEmpty()){
                    Toast.makeText(getContext(),"제목을 입력해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (content.isEmpty()){
                    Toast.makeText(getContext(),"내용을 입력해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                BoardVo boardVo = new BoardVo();

                if(board_num_int!=0) {
                    boardVo.setBoard_num(board_num_int + 1);
                } else {
                    boardVo.setBoard_num(1);
                }
                String user_id = LoginSharedPreference.getAttribute(getContext(), MemberLoginFormFragment.LOGIN_ID);
                boardVo.setBoard_title(title);
                boardVo.setBoard_content(content);
                boardVo.setBoard_date(date);
                boardVo.setWrite_user(user_id);


                databaseReference.child("Board").push().setValue(boardVo);

                mainActivity.replaceFragment(new BoardFragment());
            }
        });
        board_cancle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.replaceFragment(new BoardFragment());
            }
        });

        return view;
    }

    public void dataRead(){
        databaseReference.child("Board").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //FireBase Database의 데이터를 받아오는곳
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    BoardVo boardVo = snapshot.getValue(BoardVo.class);
                    board_num_int = boardVo.getBoard_num();
                }
            }
            //DB를 가져오던중 에러 발생할경우
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", String.valueOf(databaseError.toException()));
            }
        });
    }
}