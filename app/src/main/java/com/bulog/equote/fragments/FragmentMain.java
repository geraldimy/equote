package com.bulog.equote.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bulog.equote.AuthActivity;
import com.bulog.equote.R;
import com.bulog.equote.adapter.MainMenuTabAdapter;
import com.bulog.equote.databinding.FragmentMainBinding;
import com.bulog.equote.model.RPKMap;
import com.bulog.equote.model.smallproduct.DataSmallProduct;
import com.bulog.equote.model.smallproduct.SmallProduct;
import com.bulog.equote.model.UserModel;
import com.bulog.equote.utils.ApiCall;
import com.bulog.equote.utils.ApiService;
import com.bulog.equote.utils.GPSTracker;
import com.bulog.equote.utils.SPService;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.jama.carouselview.CarouselViewListener;
import com.kennyc.view.MultiStateView;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentMain.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentMain#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMain extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String MAIN_FRAGMENT_TAG = "MAIN_FRAGMENT";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentMainBinding binding;
    private SPService sharedPreferenceService;
    private GoogleMap rpkMap;

    private GPSTracker gpsTracker;
    private GPSTracker.LocationResult locationResult;

    private OnFragmentInteractionListener mListener;
    private Location location;

    //TODO: Remove this after retrieving data from API
    private ArrayList<DataSmallProduct> mockData = new ArrayList<>();

    public FragmentMain() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentMain.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentMain newInstance(String param1, String param2) {
        FragmentMain fragment = new FragmentMain();
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

        //Initialize mock data for viewpager
        //TODO: Remove this after retrieving product data from API
        ArrayList<SmallProduct> beras = new ArrayList<>();
        beras.add(new SmallProduct("1", "BerasKita", "BerasKita Beras komunis awkekwae", "#EA5E24", "http://www.bulog.co.id/images/produk_kita/beras_kita.png", "beras"));
        beras.add(new SmallProduct("2", "BerasKitaSachet", "BerasKita Beras komunis tapi kecil aweokwaek", "#EA5E24", "http://www.bulog.co.id/images/produk_kita/beceng.png", "beras"));

        ArrayList<SmallProduct> minyak = new ArrayList<>();
        minyak.add(new SmallProduct("4", "MinyakKita", "MinyakKita minyak komunis awkekwae", "#EA5E24", "http://www.bulog.co.id/images/produk_kita/beras_kita.png", "beras"));
        minyak.add(new SmallProduct("5", "MinyakSaya", "MinyakSaya minyak kapitalis awkekwae", "#EA5E24", "http://www.bulog.co.id/images/produk_kita/beras_kita.png", "beras"));

        mockData.add(new DataSmallProduct("beras", beras));
        mockData.add(new DataSmallProduct("minyak", minyak));

        Gson gson = new Gson();
        Log.i("data :", gson.toJson(mockData));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@NonNull ViewGroup container,@NonNull Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        sharedPreferenceService = new SPService(getContext());

        UserModel user = sharedPreferenceService.getUserFromSp();

        if(user == null){
            binding.userNameOrLoginButton.setText(R.string.login);
            binding.userNameOrLoginButton.setOnClickListener(v -> {
                Intent i = new Intent(getActivity(), AuthActivity.class);
                startActivity(i);
            });
        }else{
            binding.userNameOrLoginButton.setText(user.getFullname());
        }

        locationResult = new GPSTracker.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                binding.shimmerMap.hideShimmer();
                binding.shimmerMap.stopShimmer();
                binding.shimmerMap.clearAnimation();

                LatLng userPos = new LatLng(location.getLatitude(), location.getLongitude());
                Marker marker = rpkMap.addMarker(new MarkerOptions().position(userPos).icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_directions_walk_orange_24dp)));

                ApiService service = ApiCall.getClient().create(ApiService.class);
                Call<List<RPKMap>> call = service.searchNearestRPK(location.getLatitude(), location.getLongitude());
                call.enqueue(new Callback<List<RPKMap>>() {
                    @Override
                    public void onResponse(Call<List<RPKMap>> call, Response<List<RPKMap>> response) {
                        if(response.errorBody() != null) return;

                        BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_rpk_symetric);
                        Bitmap smallb = Bitmap.createScaledBitmap(bd.getBitmap(), 100, 100, false);
                        BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(smallb);

                        List<Marker> markers = new ArrayList<>();
                        LatLngBounds.Builder boundBuilder = new LatLngBounds.Builder();

                        List<RPKMap> nearestRPK = response.body();
                        for (RPKMap rpk : nearestRPK) {
                            LatLng pos = new LatLng(Double.parseDouble(rpk.getLatitude()), Double.parseDouble(rpk.getLongitude()));
                            Marker marker = rpkMap.addMarker(new MarkerOptions().title(rpk.getNamaRpk()).position(pos).icon(markerIcon));
                            boundBuilder.include(marker.getPosition());
                        }
                        LatLngBounds bounds = boundBuilder.build();
                        CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, 0);
                        rpkMap.animateCamera(camera);
                    }

                    @Override
                    public void onFailure(Call<List<RPKMap>> call, Throwable t) {
                        Toasty.error(getContext(), R.string.err_general_api_error, Toasty.LENGTH_LONG).show();
                    }
                });

                FragmentMain.this.location = location;
                LatLng coord = new LatLng(location.getLatitude(), location.getLongitude());
                rpkMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 15));
            }
        };

        gpsTracker = new GPSTracker();

        if(rpkMap == null){
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.rpk_main_menu_map);
            mapFragment.getMapAsync(FragmentMain.this::onMapReady);
        }

        //TODO: REMOVE THIS AFTER CALLING IMAGE FROM API
        int[] images = {R.drawable.promo_1, R.drawable.promo_2, R.drawable.promo_3};

        binding.promoCarouselview.setSize(images.length);
        binding.promoCarouselview.setResource(R.layout.promo_recyclerview_layout);
        binding.promoCarouselview.setCarouselViewListener(new CarouselViewListener() {
            @Override
            public void onBindView(View view, int i) {
                ImageView imageView = view.findViewById(R.id.promo_layout_imageview);
                //TODO: Change to setImageBitmap with corresponding promo image from API
                imageView.setImageDrawable(getResources().getDrawable(images[i]));
            }
        });
        binding.promoCarouselview.show();

        MainMenuTabAdapter pageAdapter = new MainMenuTabAdapter(getChildFragmentManager(), getLifecycle(), mockData);
        binding.productViewPagerMainmenu.setAdapter(pageAdapter);
        binding.productViewPagerMainmenu.setUserInputEnabled(false);
        binding.productViewPagerMainmenu.setOffscreenPageLimit(4);

        binding.productTabLayoutMainmenu.setTabMode(TabLayout.MODE_SCROLLABLE);
        TabLayoutMediator mediator = new TabLayoutMediator(binding.productTabLayoutMainmenu, binding.productViewPagerMainmenu, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int i) {
                tab.setText(mockData.get(i).getCategory());
            }
        });
        mediator.attach();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void checkForLocationPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                gpsTracker.getLocation(getActivity(), locationResult);
            }else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            }
        }else{
            gpsTracker.getLocation(getActivity(), locationResult);
        }
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 10){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                gpsTracker.getLocation(getActivity(), locationResult);
            }else{
                if(!ActivityCompat.shouldShowRequestPermissionRationale((Activity) getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                    dialogBuilder.setTitle(getString(R.string.permission_required));
                    dialogBuilder.setCancelable(false);
                    dialogBuilder.setMessage(getString(R.string.permission_required_message));
                    dialogBuilder.setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getContext().getPackageName(), null));
                            startActivityForResult(i, 1001);
                        }
                    });
                    dialogBuilder.create().show();
                }

                binding.mapMultistateview.setViewState(MultiStateView.ViewState.ERROR);
            }
        }
    }

    public void startActivityForResult(Intent intent, int requestCode){
        super.startActivityForResult(intent, requestCode);

        switch (requestCode){
            case 1001:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        gpsTracker.getLocation(getActivity(), locationResult);
                    }

                }else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},10);
                }
                break;
            default:
                break;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        rpkMap = googleMap;

        LatLng pos = new LatLng(0.7893, 113.9213);
        rpkMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 11));
        checkForLocationPermission();

        UiSettings configs = rpkMap.getUiSettings();
        configs.setMapToolbarEnabled(false);
        configs.setZoomControlsEnabled(false);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
