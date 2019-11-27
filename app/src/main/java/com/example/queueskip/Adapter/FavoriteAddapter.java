package com.example.queueskip.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.queueskip.Database.ModelDB.Cart;
import com.example.queueskip.EditItem;
import com.example.queueskip.Items;
import com.example.queueskip.R;
import com.example.queueskip.utliz.Common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FavoriteAddapter extends RecyclerView.Adapter<FavoriteAddapter.MyViewHolder> {

    private FavoriteAddapter.OnItemClickListener mListener;
    private List<Items> itemList;
    private ArrayList<Items> arrayList;
    private Context mContext;
    private Button moveBtn;
    private String qrId;
    DatabaseReference reff;
    boolean enter=false;


    public void setOnItemClickListener(FavoriteAddapter.OnItemClickListener listener)
    {
        mListener=listener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView product_name,product_price,product_expire;
        public ImageView product_img;

        public  MyViewHolder(View view , final FavoriteAddapter.OnItemClickListener listener){
            super(view);

            product_name=(TextView) view.findViewById( R.id.product_name1 );
            product_price=(TextView) view.findViewById( R.id.product_price1);
            product_expire=(TextView) view.findViewById(R.id.product_expire1);
            product_img=(ImageView) view.findViewById(R.id.product_img1);



            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if(listener!=null){
                        int position =getAdapterPosition();
                        if(position !=RecyclerView.NO_POSITION){
                            listener.onItemClick( position );
                        }
                    }

                }
            });
        }
    }

    public FavoriteAddapter(List<Items> itemList, Context context){
        this.itemList=itemList;
        this.arrayList=new ArrayList<Items>(  );
        this.arrayList.addAll(itemList);
        this.itemList=itemList;
        mContext=context;
        //Log.d(  "TTest", String.valueOf( itemList.get( 0 ) ) );
    }
    @Override
    public FavoriteAddapter.MyViewHolder onCreateViewHolder(ViewGroup parent , int position){
        View itemView= LayoutInflater.from( parent.getContext() ).inflate(R.layout.favorite_row,parent,false);

        moveBtn=itemView.findViewById(R.id.move_btn);
        return  new FavoriteAddapter.MyViewHolder(itemView, (FavoriteAddapter.OnItemClickListener) mListener );

//        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FavoriteAddapter.MyViewHolder holder, final int position){
        Log.d(  "TTest", String.valueOf( position ) );
        final Items product=itemList.get(position);
        qrId=product.getId();
//put extra

        holder.product_name.setText(product.getName());
        // holder.product_price.setText(product.getPrice()+" SR");
        holder.product_price.setText(product.getPrice());
        holder.product_expire.setText( product.getExpire() );
        Glide.with(mContext).load(product.getPhoto()).into(holder.product_img);
        reff = FirebaseDatabase.getInstance().getReference().child( "items");
        moveBtn.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             int amount1 = 0;
                                             if (isItemExist( qrId )) {
                                                 amount1 = Common.cartRepository.getamountItemByID( qrId )+1;
                                                 Common.cartRepository.updateAmount( amount1, qrId );



                                             }else {
                                                 reff.addValueEventListener(new ValueEventListener() {
                                                     @Override
                                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                         for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                             Items item = snapshot.getValue(Items.class);
                                                             // Toast.makeText(getActivity(),item.getName(),Toast.LENGTH_SHORT).show();

                                                             if(item.getId().equals(qrId)){
                                                                 Cart cart = new Cart();
                                                                 cart.setName( product.getName() );
                                                                 cart.setId( qrId );
                                                                 cart.setPrice( Integer.parseInt( (String) product.getPrice()) );
                                                                 cart.setAmount( 1 );
                                                                 cart.setLink( product.getPhoto() );

                                                                 Common.cartRepository.insertToCart( cart );
                                                                 enter=true;
                                                             }

                                                             //just for testing retrieving data

                                                             // text.setText(item.getName()+" Item retrieved successfully :)");
                                                         }
                                                         if(!enter){
                                                             //Toast.makeText(this, "Item is not exist", Toast.LENGTH_SHORT ).show();
                                                         }

                                                     }

                                                     @Override
                                                     public void onCancelled(@NonNull DatabaseError databaseError) {

                                                     }
                                                 });
                                             //?



                                             }


                                   /* int items1 = CartDatabase.getInstance(getActivity().getApplicationContext()).cartDAO().countCartItems();
                                    String items = String.valueOf(items1);
                                    Toast.makeText(getActivity(),"Items:" + items,Toast.LENGTH_SHORT).show();*/



                                         }
        } );

                                         }










private boolean isItemExist(String Id){
        boolean flag=false;
        List<Cart> cartList=Common.cartRepository.getCartItemss();
        for(int i=0;i< Common.cartRepository.countCartItems();i++){
        if(cartList.get( i ).id.equals(Id))
        flag=true;



        }
        return flag;
        }
    @Override
    public int getItemCount(){
        return itemList.size();
    }
    public void removeItems(int position){
        itemList.remove( position );
        notifyItemRemoved( position );
    }
    public void restoreIteme(Items item,int position){
        itemList.add(position,item);
        notifyItemInserted( position );

    }
    public List<Items> getItemList(){
        return itemList;
    }
}
