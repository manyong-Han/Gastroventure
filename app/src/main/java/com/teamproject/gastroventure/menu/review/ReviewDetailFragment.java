package com.teamproject.gastroventure.menu.review;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.adapter.ReviewDetailImgAdapter;
import com.teamproject.gastroventure.menu.member.MemberLoginFormFragment;
import com.teamproject.gastroventure.util.DialogSampleUtil;
import com.teamproject.gastroventure.util.LoginSharedPreference;
import com.teamproject.gastroventure.vo.ReviewImgVo;
import com.teamproject.gastroventure.vo.ReviewVo;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReviewDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReviewDetailFragment extends Fragment {
    private static final String SELECT_KEY = "select_key";
    private final String CHILE_NAME_REVIEW = "Review";
    private final String CHILE_NAME_REVIEW_IMAGE = "Review_Image";
    private final String TAG = "ReviewDetailFrag";

    private MainActivity main;
    private View view;

    private TextView detail_store_name;
    private TextView detail_menu;
    private TextView detail_content;

    private RatingBar detail_review_rating;

    private Button detail_modify_btn;
    private Button detail_cancel_btn;

    private FirebaseDatabase reviewDatabase;
    private DatabaseReference databaseReference;

    private RecyclerView detail_rcv_view;
    private RecyclerView.Adapter detailAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ReviewImgVo> reviewImageList = new ArrayList<ReviewImgVo>();

    private ReviewVo reviewVo;
    private ReviewImgVo reviewImgVo;

    private String select_key;
    private String user_id;

    public ReviewDetailFragment() {
    }

    public static ReviewDetailFragment newInstance(String select_key) {
        ReviewDetailFragment fragment = new ReviewDetailFragment();
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
        main = (MainActivity) getActivity();

        view = inflater.inflate(R.layout.fragment_review_detail, container, false);

        detail_store_name = view.findViewById(R.id.review_detail_store_name);
        detail_menu = view.findViewById(R.id.review_detail_menu);
        detail_content = view.findViewById(R.id.review_detail_content);

        detail_review_rating = view.findViewById(R.id.review_detail_rating);

        detail_modify_btn = view.findViewById(R.id.review_detail_modify);
        detail_cancel_btn = view.findViewById(R.id.review_detail_cancel);

        reviewDatabase = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        databaseReference = reviewDatabase.getReference(); // DB 테이블 연결

        detail_rcv_view = view.findViewById(R.id.review_detail_rcv_image); // 아디 연결
        detail_rcv_view.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new GridLayoutManager(getContext(), 3);
        detail_rcv_view.setLayoutManager(layoutManager);

        detail_modify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user_id.equals(LoginSharedPreference.getAttribute(getContext(), MemberLoginFormFragment.LOGIN_ID))) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.main_frame, ReviewModifyFragment.newInstance(select_key)).commit();
                } else {
                    DialogSampleUtil.showMessageDialog(getContext(), "", "작성자만 수정 가능합니다.");
                }
            }
        });

        detail_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.replaceFragment(new ReviewFragment());
            }
        });

        dataRead();
        imageDataRead();

        return view;
    }

    public void dataRead() {
        databaseReference.child(CHILE_NAME_REVIEW).child(select_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviewVo = dataSnapshot.getValue(ReviewVo.class); // 만들어뒀던 User 객체에 데이터를 담는다.
                reviewVo.setReview_key(dataSnapshot.getKey());

                detail_store_name.setText(reviewVo.getStore_name());
                detail_menu.setText(reviewVo.getMenu());
                detail_content.setText(reviewVo.getReview_content());
                user_id = reviewVo.getWrite_user();

                detail_review_rating.setRating((float) reviewVo.getRating_num());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 디비를 가져오던중 에러 발생 시
                Log.e("ReviewDetailFragment", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });
    }

    public void imageDataRead() {
        databaseReference.child(CHILE_NAME_REVIEW_IMAGE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 파이어베이스 데이터베이스의 데이터를 받아오는 곳
                reviewImageList.clear(); // 기존 배열리스트가 존재하지않게 초기화
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 반복문으로 데이터 List를 추출해냄
                    reviewImgVo = snapshot.getValue(ReviewImgVo.class); // 만들어뒀던 User 객체에 데이터를 담는다.
                    reviewImgVo.setReview_img_key(snapshot.getKey());

                    if (reviewImgVo.getReview_key().equals(reviewVo.getReview_key())) {
                        reviewImageList.add(reviewImgVo); // 담은 데이터들을 배열리스트에 넣고 리사이클러뷰로 보낼 준비
                        Log.d(TAG, " 리뷰 키 !! " + reviewImgVo.getReview_img_key());
                        Log.d(TAG, " 사진이름 !! " + reviewImgVo.getMenu_image());
                    }
                }
                detailAdapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 디비를 가져오던중 에러 발생 시
                Log.e("ReviewDetailFragment", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });
        detailAdapter = new ReviewDetailImgAdapter(getContext(), reviewImageList);
        detail_rcv_view.setAdapter(detailAdapter); // 리사이클러뷰에 어댑터 연결
    }
}
