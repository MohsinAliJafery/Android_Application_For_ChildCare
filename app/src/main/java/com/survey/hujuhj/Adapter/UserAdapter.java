package com.survey.hujuhj.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.survey.hujuhj.MessageActivity;
import com.survey.hujuhj.Model.Chat;
import com.survey.hujuhj.Model.User;
import com.survey.hujuhj.MessageActivityForChild;
import com.survey.hujuhj.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUser;
    private boolean isChat;

    private String TheLastMessage;

    public UserAdapter(Context mContext, List<User> mUser, boolean isChat) {
        this.mContext = mContext;
        this.mUser = mUser;
        this.isChat = isChat;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {

        final User user = mUser.get(position);
        holder.Username.setText(user.getUsername());
        if(user.getImageUrl().equals("default")){
            holder.ProfileImage.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(mContext).load(user.getImageUrl()).into(holder.ProfileImage);
        }

        if(isChat){
            lastMessage(user.getID(), holder.LastText);
        }else{
            holder.LastText.setVisibility(View.GONE);
        }

        if(isChat){
          if(user.getStatus().equals("online")){
                holder.ImgOn.setVisibility(View.VISIBLE);
                holder.ImgOff.setVisibility(View.VISIBLE);
         }else{
               holder.ImgOff.setVisibility(View.VISIBLE);
               holder.ImgOn.setVisibility(View.GONE);
           }
       }else{
               holder.ImgOff.setVisibility(View.GONE);
              holder.ImgOn.setVisibility(View.GONE);
       }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sharedpreferences = mContext.getSharedPreferences("ChildApp", Context.MODE_PRIVATE);
                boolean mParent = sharedpreferences.getBoolean("areYouParent", false);

                Intent mIntent;
                if(!mParent){
                   mIntent  = new Intent(mContext, MessageActivity.class);
                }else{
                   mIntent = new Intent(mContext, MessageActivityForChild.class);
                }


                mIntent.putExtra("UserId", user.getID());
                mContext.startActivity(mIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView Username;
        public CircleImageView ProfileImage;
        private CircleImageView ImgOn, ImgOff;
        private TextView LastText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Username = itemView.findViewById(R.id.Username);
            ProfileImage = itemView.findViewById(R.id.ProfileImage);
            ImgOn = itemView.findViewById(R.id.img_on);
            ImgOff = itemView.findViewById(R.id.img_off);
            LastText = itemView.findViewById(R.id.last_text);
        }

    }

    private void lastMessage(final String userId, final TextView last_message){
        TheLastMessage = "default";

        final FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("Chats");

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot mDataSnapshot: snapshot.getChildren()){
                    Chat mChat = mDataSnapshot.getValue(Chat.class);
                    if(mChat.getReceiver().equals(mFirebaseUser.getUid()) && mChat.getSender().equals(userId) ||
                            mChat.getReceiver().equals(userId) && mChat.getSender().equals(mFirebaseUser.getUid())){
                            TheLastMessage = mChat.getMessage();
                    }
                }

                switch(TheLastMessage){
                    case "default":
                        last_message.setText("No Message");
                        break;

                     default:
                        last_message.setText(TheLastMessage);
                        break;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
