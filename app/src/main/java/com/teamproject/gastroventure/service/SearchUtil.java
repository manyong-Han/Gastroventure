package com.teamproject.gastroventure.service;



import com.teamproject.gastroventure.vo.SearchVo;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.teamproject.gastroventure.vo.SearchVo;

public class SearchUtil {

    public static List<SearchVo>  getLocalListFromJson(
            //String  keyword,      //검색어
            double latitude,      //위도
            double longitude,     //경도
            int radius,
            int page,
            int size) throws Exception{
        List<SearchVo> list = new ArrayList<SearchVo>();

        //        3ebe1c7e586491e37bc04090f8d133b0
        //String  kakaoAK = "KakaoAK 3ebe1c7e586491e37bc04090f8d133b0";
        //keyword = URLEncoder.encode(keyword, "utf-8");
        String  kakaoAK = MyOpenAPIKey.Daum.KAKAOAK;
        String urlStr = String.format("https://dapi.kakao.com/v2/local/search/cagegory.json?category_group_code=%s&query=x=%f&y=%f&radius=%d&page=%d&size=%d",
                "FD6",longitude,latitude,radius,page,size
        );

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        //발급받은 key
        connection.setRequestProperty("Authorization", kakaoAK);
        connection.setRequestProperty("Content-Type", "application/plain");
        connection.connect();

        InputStreamReader isr = new InputStreamReader(connection.getInputStream(),"utf-8");
        BufferedReader br = new BufferedReader(isr);
        StringBuffer sb = new StringBuffer();

        while(true) {
            String readData = br.readLine();
            if(readData==null)break;
            sb.append(readData);
        }


        JSONObject json = new JSONObject(sb.toString());

        //검색결과정보
        int pageable_count = json.getJSONObject("meta")
                .getInt("total_count");

        //전체조회건수
        SearchVo.pageable_count = pageable_count;

        //검색목록
        JSONArray local_array = json.getJSONArray("documents");
        for(int i=0;i<local_array.length();i++) {

            JSONObject local = local_array.getJSONObject(i);
            SearchVo vo = new SearchVo();
            vo.setPlace_name(local.getString("place_name"));
            vo.setPlace_url(local.getString("place_url"));
            vo.setAddress(local.getString("address_name"));
            vo.setRoad_address(local.getString("road_address_name"));
            vo.setCategory_group_name(local.getString("category_group_name"));
            vo.setCategory_name(local.getString("category_name"));
            vo.setPhone(local.getString("phone"));
            vo.setCategory_group_code("category_group_code");

            int distance=0;
            double local_longitude=0;
            double local_latitude=0;

            try {
                local_longitude = Double.parseDouble(local.getString("y"));
                vo.setLongitude(local_longitude);
            } catch (Exception e) {
                // TODO: handle exception
            }


            try {
                local_latitude = Double.parseDouble(local.getString("x"));
                vo.setLatitude(local_latitude);
            } catch (Exception e) {
                // TODO: handle exception
            }

            try {
                distance = Integer.parseInt(local.getString("distance"));
                vo.setDistance(distance);
            } catch (Exception e) {
                // TODO: handle exception
            }

            list.add(vo);

        }

        //거리순(가까운 순서로 정렬)
        Collections.sort(list, new SearchVoComp());



        return list;
    }


    //------------------------------------------------------------------------------------
    public static List<SearchVo>  getLocalListFromXml(
            String  keyword,      //검색어
            double latitude,      //위도
            double longitude,     //경도
            int radius,
            int page,
            int size) throws Exception{

        List<SearchVo> list = new ArrayList<SearchVo>();
        //        3ebe1c7e586491e37bc04090f8d133b0
        //String  kakaoAK = "KakaoAK 3ebe1c7e586491e37bc04090f8d133b0";
        keyword = URLEncoder.encode(keyword, "utf-8");
        String  kakaoAK = MyOpenAPIKey.Daum.KAKAOAK;
        String urlStr = String.format("https://dapi.kakao.com/v2/local/search/keyword.xml?category.xml?category_group_code=%s&query=%s&x=%f&y=%f&radius=%d&page=%d&size=%d",
                "FD6",keyword,longitude,latitude,radius,page,size
        );

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        //발급받은 key
        connection.setRequestProperty("Authorization", kakaoAK);
        connection.setRequestProperty("Content-Type", "application/plain");
        connection.connect();

        SAXBuilder sb = new SAXBuilder();
        Document doc = sb.build(connection.getInputStream());
        Element root = doc.getRootElement();// <result>

        //검색결과 정보
        Element meta = root.getChild("meta");
        //검색리스트
        /*
            <result>
               <documents></documents>
               <documents></documents>
               <documents></documents>
               <documents></documents>
               <meta>
                  <pageable_count>45</pageable_count>
                  <total_count>22145</total_count>
               </meta>
            </result>

         */
        int pageable_count = 0;

        try {

            pageable_count = Integer.parseInt(

                    root.getChild("meta")
                            .getChildText("pageable_count")

            );

        }catch (Exception e){

        }

        //전체조회건수
        SearchVo.pageable_count = pageable_count;


        List<Element> localList = root.getChildren("documents");
        for(Element local : localList) {
            SearchVo vo = new SearchVo();

            vo.setPlace_name(local.getChildText("place_name"));
            vo.setPlace_url(local.getChildText("place_url"));
            vo.setCategory_group_name(local.getChildText("category_group_name"));
            vo.setCategory_name(local.getChildText("category_name"));
            vo.setAddress(local.getChildText("address_name"));
            vo.setRoad_address(local.getChildText("road_address_name"));
            vo.setPhone(local.getChildText("phone"));


            int distance=0;
            double local_longitude=0;
            double local_latitude=0;

            try {
                local_longitude = Double.parseDouble(local.getChildText("y"));
                vo.setLongitude(local_longitude);
            } catch (Exception e) {
                // TODO: handle exception
            }


            try {
                local_latitude = Double.parseDouble(local.getChildText("x"));
                vo.setLatitude(local_latitude);
            } catch (Exception e) {
                // TODO: handle exception
            }

            try {
                distance = Integer.parseInt(local.getChildText("distance"));
                vo.setDistance(distance);
            } catch (Exception e) {
                // TODO: handle exception
            }

            list.add(vo);
        }

        //거리순(가까운 순서로 정렬)
        Collections.sort(list, new SearchVoComp());

        return list;
    }


    //ArrayList 정렬기준 객체
    static class SearchVoComp implements Comparator<SearchVo>{

        public int compare(SearchVo lhs, SearchVo rhs) {
            // TODO Auto-generated method stub
            int ret=0;
            if(lhs.getDistance()>rhs.getDistance())ret=1;
            else if(lhs.getDistance()<rhs.getDistance())ret=-1;

            return ret;
        }
    }

}
