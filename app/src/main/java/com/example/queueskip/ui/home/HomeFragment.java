package com.example.queueskip.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.queueskip.Database.DataSource.CartRepository;
import com.example.queueskip.Database.Local.CartDataSource;
import com.example.queueskip.Database.Local.CartDatabase;
import com.example.queueskip.Database.ModelDB.Cart;
import com.example.queueskip.Items;
import com.example.queueskip.LoginActivity;
import com.example.queueskip.R;
import com.example.queueskip.SignupActivity;
import com.example.queueskip.utliz.Common;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
/*
public class HomeFragment extends Fragment {
    private Button scan;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //removed comment
        scan = (Button)view.findViewById(R.id.scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ScanActivity.class));
            }
        });

        return view;

    }
}


 */


public class HomeFragment extends Fragment {

private HomeViewModel homeViewModel;
    SurfaceView cameraPreview;
    TextView txtResult;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    final int RequestCameraPermissionID=1001;
    private Button closeBtn;
    private TextView productNameTxt;
    private TextView productPriceTxt;
    private Button addBTn;
    private Context mContext;
    private ImageView productImg;
    DatabaseReference reff;
    DataSnapshot dataSnapshot; //?
    private String photo;

    String price="";
    String name="";

    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
       // Context context=getContext().getApplicationContext();

        iniDB();
        cameraPreview = (SurfaceView)view.findViewById(R.id.cameraPreview);
        txtResult= (TextView)view.findViewById(R.id.txtResult);

        reff= FirebaseDatabase.getInstance().getReference().child("items");
//        dataSnapshot.child("Items").getValue();


        barcodeDetector=new BarcodeDetector.Builder(mContext).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(getContext(),barcodeDetector).setRequestedPreviewSize(640,480).build();
//        homeViewModel =
//                ViewModelProviders.of(this).get(HomeViewModel.class);
       // final TextView textView = root.findViewById(R.id.text_home);
//        homeViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });



        //add event
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED){
                    //request permission
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},RequestCameraPermissionID);
                    return;

                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes=detections.getDetectedItems();
                if(qrcodes.size() != 0){
                    txtResult.post(new Runnable() {
                        @Override
                        public void run() {
                            cameraSource.stop();//check
                            //create vibrate
                            Vibrator vibrator=(Vibrator)getActivity().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(100);
                            //txtResult.setText(qrcodes.valueAt(0).displayValue);
                            //----------------
                            final Dialog dialog = new Dialog(mContext);
                            dialog.setContentView(R.layout.product_dialog);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                            closeBtn=dialog.findViewById(R.id.button_close);
                            productNameTxt=dialog.findViewById(R.id.product_name_dialog);
                            productPriceTxt=dialog.findViewById( R.id.product_price_dialog );
                            addBTn=dialog.findViewById( R.id.Add);
                            productImg=dialog.findViewById(R.id.product_img_dialog);

                            String qrtext=(qrcodes.valueAt(0).displayValue);

                           // productNameTxt=qrtext.

                          int   x=qrtext.indexOf( '-');
                          int y=qrtext.indexOf("-", x + 1);

                          try {

                              price = qrtext.substring(x + 1, y);
                              name = qrtext.substring(0, x);
                              productNameTxt.setText("Item: " + qrtext.substring(0, x));
                              productPriceTxt.setText("Price: " + qrtext.substring(x + 1, y) + " SR");
                          }catch (StringIndexOutOfBoundsException e){
                              productNameTxt.setText("This QR Code doesn't exist");
                              addBTn.setVisibility(View.INVISIBLE);
                          }


                           // String itemId = reff.push().getKey();
                          //  reff= FirebaseDatabase.getInstance().getReference().child("Items");///?!?!???!?!??!

                           // reff.child( id ).setValue( item );

                            reff.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Items item = snapshot.getValue(Items.class);
                                       // Toast.makeText(getActivity(),item.getName(),Toast.LENGTH_SHORT).show();

                                        if(item.getName().equals(name)){
                                             photo = item.getPhoto();


                                            Glide.with(mContext).load(photo).into(productImg);
                                        }

                                        //just for testing retrieving data

                                       // text.setText(item.getName()+" Item retrieved successfully :)");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

//                            final ElegantNumberButton quantity=dialog.findViewById(R.id.txt_amount_dialog);
//                              final Integer quant=Integer.parseInt(quantity.getNumber());

                            /*quantity.setOnClickListener(new ElegantNumberButton.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String num = quantity.getNumber();
                                }
                            });
                            */


                           // int s = productNameTxt.getText().toString().indexOf('P');
                           // int e = productNameTxt.getText().toString().indexOf('E');

                           // productPriceTxt.setText(productNameTxt.getText().toString().substring(s,e));


                            addBTn.setOnClickListener( new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Cart cart=new Cart();
                                    cart.setName(name);
                                    cart.setPrice(Integer.parseInt( (String ) price  ));
                                    cart.setAmount( 1 );
                                    cart.setLink(photo);

                                    Common.cartRepository.insertToCart(cart); //?



                                    Toast.makeText(getActivity(),"Item added successfully",Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();



                                   /* int items1 = CartDatabase.getInstance(getActivity().getApplicationContext()).cartDAO().countCartItems();
                                    String items = String.valueOf(items1);
                                    Toast.makeText(getActivity(),"Items:" + items,Toast.LENGTH_SHORT).show();*/



                                }
                            } );


                            closeBtn.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                dialog.cancel();
                                                                
                                                            }//end of onClick
                                                        }//end of OnClickListener
                            );


                            dialog.show();
                            //-----------------
                            /*
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

                            alertDialog.setMessage(qrcodes.valueAt(0).displayValue);


                            alertDialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }//end of onClick
                                    }//end of OnClickListener
                            );

                            alertDialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }//end of onClick
                                    }//end of OnClickListener
                            );
                            alertDialog.show();

                             */
                        }
                    });
                }
            }
        });
        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED){

                        return;

                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }

            break;
        }
    }
    private void iniDB(){
        Common.cartDatabase=CartDatabase.getInstance( mContext );
        Common.cartRepository= CartRepository.getInstance( CartDataSource.getInstance( Common.cartDatabase.cartDAO() ) );
    }
}




