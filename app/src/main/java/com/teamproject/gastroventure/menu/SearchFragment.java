package com.teamproject.gastroventure.menu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.service.GPSTracker;
import com.teamproject.gastroventure.service.SearchUtil;
import com.teamproject.gastroventure.util.DialogSampleUtil;
import com.teamproject.gastroventure.vo.SearchVo;

import java.util.List;

public class SearchFragment extends Fragment {

    GPSTracker gps = null;
    double latitude = 0;
    double longitude = 0;
    public Handler mHandler;

    public final static int RENEW_GPS = 1;
    public final static int SEND_PRINT = 2;

    SearchView sv_search;
    ListView lv_local;
    TextView tv_pagecount;
    Button bt_prev;
    Button bt_next;

    String search_text = "";//검색어
    int page = 1;     //검색결과 페이지
    int size = 10;    //검색결과 조회갯수
    int radius = 3000;//검색반경
    int total_page = 0;//전체페이지
    ProgressDialog progressDialog;
    List<SearchVo> local_list = null;
    private View view;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_search, container, false);
        tv_pagecount = view.findViewById(R.id.tv_pagecount);


        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == RENEW_GPS) {
                    makeNewGpsService();
                }
                if (msg.what == SEND_PRINT) {
                    Toast.makeText(getContext(), (String) msg.obj, Toast.LENGTH_SHORT);
                }
            }
        };

        sv_search = view.findViewById(R.id.sv_search);
        lv_local = view.findViewById(R.id.lv_local);
        bt_next = view.findViewById(R.id.bt_next);
        bt_prev = view.findViewById(R.id.bt_prev);

        bt_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page--;
                if (page < 1) {
                    page = 1;
                    DialogSampleUtil.showMessageDialog(getContext(), "", "첫번째 페이지 입니다");
                    return;
                }
                search();

            }
        });
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page++;

                if (page > total_page) {
                    page = total_page;
                    DialogSampleUtil.showMessageDialog(getContext(), "", "마지막 페이지 입니다");
                    return;
                }

                search();
            }
        });

        //서치뷰 이벤트
        sv_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Log.d("MY", query);

                //초기화
                page = 1;
                total_page = 0;

                //검색시작
                search_text = query;
                search();

                return false;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return view;
    }

    public void makeNewGpsService() {
        if (gps == null) {
            gps = new GPSTracker(getContext(), mHandler);
        } else {
            gps.Update();
        }
    }

    public void search() {

        makeNewGpsService();
        /*if(gps == null) {
            gps = new GPSTracker(MainActivity.this,mHandler);
        }else{
            gps.Update();
        }*/
        // check if GPS enabled
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            Log.d("MY", "lat:" + latitude + " lon:" + longitude);
            // \n is for new line
            Toast.makeText(getContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        //여기서 검색
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Search...");
        progressDialog.setCancelable(false);
        progressDialog.show();


        //Kakao로부터 데이터를 가져오겠금 실행시킨다
        // ①
        new KakaoLocalAsyncTask().execute();//doInBackground() Call
    }

    //데이터 수신용 객체 선언
    class KakaoLocalAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {

            try {
                //여기서 데이터 서버로 부터 요청데이터를 가져오기
                // ②
                local_list = SearchUtil.getLocalListFromXml(search_text, latitude, longitude, radius, page, size);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            // ③

            //진행바 닫기
            progressDialog.dismiss();

            //검색결과 갯수
            Log.d("MY", "검색갯수:" + local_list.size());
            /*for(DaumLocalVo vo : local_list){
                Log.d("MY",vo.getPlace_name());
            }*/

            //키패드 내리기
            sv_search.clearFocus();//포커스제거=>키패드 안올라옴
            //MyUtil.hideKeypad(MainActivity.this,sv_search);

            //page계산후 출력

            total_page = SearchVo.pageable_count / size;
            if ((SearchVo.pageable_count % size) > 0)
                total_page++;

            String strPage = String.format("%d/%d(%d)", page, total_page, SearchVo.pageable_count);
            tv_pagecount.setText(strPage);


            //리스트뷰 배치
            lv_local.setAdapter(new LocalItemAdapter());


        }
    }

    class LocalItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {//ListView Item갯수 설정
            return local_list.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.search_item, null);
            }

            //여기서 LocalItem배치...
            //각각의 컨트롤의 참조값 구하기
            TextView tv_place_name = convertView.findViewById(R.id.tv_place_name);
            TextView tv_address = convertView.findViewById(R.id.tv_address);
            TextView tv_phone = convertView.findViewById(R.id.tv_phone);
            TextView tv_distance = convertView.findViewById(R.id.tv_distance);

            Button bt_url = convertView.findViewById(R.id.bt_url);
            Button bt_phone = convertView.findViewById(R.id.bt_phone);

            final SearchVo vo = local_list.get(position);
            tv_place_name.setText("상호:" + vo.getPlace_name());
            tv_address.setText("주소:" + vo.getAddress());
            tv_phone.setText("전화:" + vo.getPhone());
            tv_distance.setText(String.format("거리:%d(m)", vo.getDistance()));

            //바로가기
            bt_url.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //바로가기(묵시적 인텐트)
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(vo.getPlace_url()));
                    startActivity(intent);

                }
            });

            //전화걸기
            bt_phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //전화걸기(묵시적 인텐트)=>"tel:0101112345"

                    if (vo.getPhone().isEmpty()) {
                        Toast.makeText(getContext(), "전화번호가 없습니다", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    String tel_url = String.format("tel:%s", vo.getPhone());
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(tel_url));
                    startActivity(intent);
                }
            });

            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}
