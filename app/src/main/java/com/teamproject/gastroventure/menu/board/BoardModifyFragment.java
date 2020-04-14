package com.teamproject.gastroventure.menu.board;

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
import com.teamproject.gastroventure.menu.member.MemberLoginFormFragment;
import com.teamproject.gastroventure.util.LoginSharedPreference;
import com.teamproject.gastroventure.vo.BoardVo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BoardModifyFragment extends Fragment {

    private static final String SELECT_KEY = "select_key";

    private View view;
    private MainActivity mainActivity;
    private EditText board_modify_title;
    private EditText board_modify_content;
    private Button board_modify_btn;
    private Button board_modify_cancle_btn;
    private FirebaseDatabase boardDatabase;
    private DatabaseReference databaseReference;

    // TODO: Rename and change types of parameters
    private String select_key;
    private int board_num_int = 0;

    public BoardModifyFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static BoardModifyFragment newInstance(String select_key) {
        BoardModifyFragment fragment = new BoardModifyFragment();
        Bundle args = new Bundle();
        args.putString(SELECT_KEY, select_key);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            select_key = getArguments().getString(SELECT_KEY);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_board_modify_form,container,false);
        mainActivity = (MainActivity) getActivity();

        board_modify_title = view.findViewById(R.id.board_modify_title);
        board_modify_content = view.findViewById(R.id.board_modify_content);
        board_modify_btn = view.findViewById(R.id.board_modify_btn);
        board_modify_cancle_btn = view.findViewById(R.id.board_modify_cancle_btn);

        boardDatabase  = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        databaseReference = boardDatabase.getReference(); // DB 테이블 연결

        board_modify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = board_modify_title.getText().toString();
                String content = board_modify_content.getText().toString();
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
                boardVo.setWrite_user(user_id);
                boardVo.setBoard_title(title);
                boardVo.setBoard_content(content);
                boardVo.setBoard_date(date);


                databaseReference.child("Board").child(select_key).setValue(boardVo);

                mainActivity.replaceFragment(new BoardFragment());
            }
        });
        board_modify_cancle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.main_frame, BoardDetailFragment.newInstance(select_key)).commit();
            }
        });

        dataRead();

        return view;
    }
    public void dataRead(){
        databaseReference.child("Board").child(select_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //FireBase Database의 데이터를 받아오는곳
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    BoardVo boardVo = dataSnapshot.getValue(BoardVo.class);
                    boardVo.setBoard_key(dataSnapshot.getKey());
                    board_num_int = boardVo.getBoard_num();

                    board_modify_title.setText(boardVo.getBoard_title());
                    board_modify_content.setText(boardVo.getBoard_content());

            }
            //DB를 가져오던중 에러 발생할경우
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", String.valueOf(databaseError.toException()));
            }
        });
    }
}
