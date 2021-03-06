package com.example.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.item.ItemPhotos;
import com.example.util.Constant;
import com.example.util.DBHelper;
import com.example.util.JsonUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;
import com.viaviapp.hdwallpaper.R;
import com.viaviapp.hdwallpaper.RequestActivity;
import com.viaviapp.hdwallpaper.SlideImageActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


public class AdapterImage extends RecyclerView.Adapter{
    static final String REQ_TAG = "VACTIVITY";
    SharedPreferences sp;
    SharedPreferences.Editor spe;

    private DBHelper dbHelper;
    private ArrayList<ItemPhotos> list;
    private Context context;
    private InterstitialAd mInterstitial;
    private JsonUtils utils;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView_fav;
        private SimpleDraweeView imageView;
        private TextView textView_totviews;

        private MyViewHolder(View view) {
            super(view);
            imageView = (SimpleDraweeView) view.findViewById(R.id.item);
            textView_totviews = (TextView) view.findViewById(R.id.textView_totviews);
            imageView_fav = (ImageView) view.findViewById(R.id.imageView_favourite);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private static ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        }
    }

    public AdapterImage(Context context, ArrayList<ItemPhotos> list) {
        this.list = list;
        this.context = context;
        dbHelper = new DBHelper(context);
        loadInter();

        utils = new JsonUtils(context);

        Resources r = context.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,Constant.GRID_PADDING, r.getDisplayMetrics());
        Constant.columnWidth = (int) ((utils.getScreenWidth() - ((Constant.NUM_OF_COLUMNS + 1) * padding)) / Constant.NUM_OF_COLUMNS);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

//        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.latest_grid_item, parent, false);

            return new MyViewHolder(itemView);
//        }
//        else {
//            View v = LayoutInflater.from(parent.getContext()).inflate(
//                    R.layout.layout_progressbar, parent, false);
//
//            return new ProgressViewHolder(v);
//        }

    }
    RequestQueue requestQueue;

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof MyViewHolder) {
            FirstFav(position, ((MyViewHolder) holder).imageView_fav);

            ((MyViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ((MyViewHolder) holder).imageView.setLayoutParams(new RelativeLayout.LayoutParams(Constant.columnWidth, Constant.columnWidth));
            ((MyViewHolder) holder).textView_totviews.setText(list.get(position).getTotalViews());

//            Picasso.with(context)
//                    .load(list.get(position).getImageThumb().replace(" ", "%20"))
//                    .placeholder(R.mipmap.placeholder)
//                    .into(((MyViewHolder) holder).imageView);
            final String url = list.get(position).getImage().replace(" ", "%20");
//            if(url.substring(url.length()-3).equals("gif")){
//                url = url.replace("/categories/"+ list.get(position).getId(),"/images/animation");
//                DraweeController controller = Fresco.newDraweeControllerBuilder()
//                        .setUri(url)//list.get(position).getImage().replace(" ", "%20"))
//                        .setAutoPlayAnimations(true)
//                        .build();
//                ((MyViewHolder) holder).imageView.setController(controller);
//
//            } else {
//                DraweeController controller = Fresco.newDraweeControllerBuilder()
//                        .setUri(list.get(position).getImage().replace(" ", "%20"))
//                        .setAutoPlayAnimations(true)
//                        .build();
//                ((MyViewHolder) holder).imageView.setController(controller);
//            }

            String finalUrl = url;


            if(url.substring(url.length()-3).equals("gif")) {
                ///////////////////////
                    requestQueue = Volley.newRequestQueue(context);
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://panel.alivemessages.com//api.php?gif_list",
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    //	serverResp.setText("String Response : "+ response);
                                    try {
                                        //String url = Constant.arrayList.get(Integer.parseInt(s)).getImage()) //.replace(" ","%20"
                                        JSONObject reader = new JSONObject(response);
                                        JSONArray arrayOfObjs = reader.getJSONArray("HD_WALLPAPER");
                                        for (int i = 0; i < arrayOfObjs.length(); i++) {
                                            JSONObject c = arrayOfObjs.getJSONObject(i);
                                            String str = c.getString("gif_image");
                                            if (str.substring(str.lastIndexOf("_") + 1).equals(url.substring(url.lastIndexOf("_") + 1))) {
                                                DraweeController controller = Fresco.newDraweeControllerBuilder()
                                                        .setUri(str)//list.get(position).getImage().replace(" ", "%20"))
                                                        .setAutoPlayAnimations(true)
                                                        .build();
                                                ((MyViewHolder) holder).imageView.setController(controller);
                                            }
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //	serverResp.setText("Error getting response");
                        }
                    });

                    stringRequest.setTag(REQ_TAG);
                    requestQueue.add(stringRequest);
                ////
//						url = url.replace("/categories/" + Constant.arrayList.get((Integer.parseInt(s))).getId(), "/images/animation");
//						DraweeController controller = Fresco.newDraweeControllerBuilder()
//								.setUri(url)//list.get(position).getImage().replace(" ", "%20"))
//								.setAutoPlayAnimations(true)
//								.build();
//						imageView.setController(controller);
            }else {
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(url)
                        .setAutoPlayAnimations(true)
                        .build();
                ((MyViewHolder) holder).imageView.setController(controller);
            }

            ((MyViewHolder) holder).imageView_fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String image_id = list.get(position).getId();

                    ArrayList<ItemPhotos> pojolist = dbHelper.getFavRow(image_id, "fav");
                    if (pojolist.size() == 0) {
                        AddtoFav(position);//if size is zero i.e means that record not in database show add to favorite
                        ((MyViewHolder) holder).imageView_fav.setImageDrawable(context.getResources().getDrawable(R.mipmap.fav_ind_active));
                    } else {
                        if (pojolist.get(0).getId().equals(image_id)) {
                            RemoveFav(position);
                            ((MyViewHolder) holder).imageView_fav.setImageDrawable(context.getResources().getDrawable(R.mipmap.fav_ind));
                        }
                    }
                }
            });


            ((MyViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(readFromFile(context).equals("yay")) {
                        sp = context.getSharedPreferences("sp",MODE_PRIVATE);
                        spe=sp.edit();
                        spe.putString("wall",url);
                        spe.commit();

                        writeToFile2(Integer.toString(position), context,"wall_selected.txt");
                        RequestActivity requestActivity = new RequestActivity();
                        requestActivity.finish();
                        Intent iner = new Intent(context, RequestActivity.class);
                        context.startActivity(iner);
                    }
                    else {
                        showInter(position);
                    }
                }
            });
        }
//        else {
//            if(getItemCount()==1) {
//                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
//                ((ProgressViewHolder) holder).progressBar.setVisibility(View.GONE);
//            }
//        }
    }
    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("pos.txt", MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private void writeToFile2(String data,Context context,String name) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(name, MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("isStWall.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private String readFromFile2(Context context,String name) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(name);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
    @Override
    public int getItemCount() {
//        if(Constant.isFav) {
            return list.size();
//        } else {
//            return list.size() + 1;
//        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void FirstFav(int pos, ImageView imageView)
    {
        ArrayList<ItemPhotos> pojolist = dbHelper.getFavRow(list.get(pos).getId(),"fav");
        if(pojolist.size()==0) {
            imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.fav_ind));
        }
        else {
            imageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.fav_ind_active));

        }
    }

    public void RemoveFav(int position)
    {
//		Image_Url=Constant.arrayList.get(position).getImage();
        dbHelper.removeFav(list.get(position).getId());
        Toast.makeText(context, context.getResources().getString(R.string.removed_fav), Toast.LENGTH_SHORT).show();
    }

    public void AddtoFav(int position)
    {
        dbHelper.addtoFavorite(list.get(position),"fav");
        Toast.makeText(context, context.getResources().getString(R.string.added_fav), Toast.LENGTH_SHORT).show();
    }

    private void loadInter() {
        mInterstitial = new InterstitialAd(context);
        mInterstitial.setAdUnitId(context.getString(R.string.admob_intertestial_id));
        mInterstitial.loadAd(new AdRequest.Builder().build());
    }

    private void showInter(final int pos) {
        Constant.adCount = Constant.adCount + 1;
        if(Constant.adCount % Constant.adShow == 0) {
            if (mInterstitial.isLoaded()) {
                mInterstitial.show();
                mInterstitial.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        loadInter();
                        slideIntent(pos);
                        super.onAdClosed();
                    }
                });
            } else {
                slideIntent(pos);
            }
        } else {
            slideIntent(pos);
        }

    }

    private void slideIntent(int pos) {
        Constant.arrayList.clear();
        Constant.arrayList.addAll(list);
        Intent intslider=new Intent(context,SlideImageActivity.class);
        intslider.putExtra("POSITION_ID", pos);
        context.startActivity(intslider);
    }

    private void passId(int pos){
        Intent intslider=new Intent(context,RequestActivity.class);
        intslider.putExtra("POSITION_ID", pos);

    }
    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    public boolean isHeader(int position) {
        return position == list.size();
    }

    @Override
    public int getItemViewType(int position) {
//        return isHeader(position) ? VIEW_PROG : VIEW_ITEM;
        return VIEW_ITEM;
    }
}