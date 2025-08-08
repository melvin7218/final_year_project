package com.example.finalyearproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private List<String> memberNames = new ArrayList<>();
    private List<Integer> memberIds = new ArrayList<>();
    private OnMemberClickListener listener;

    public void setMemberNames(List<String> names) {
        this.memberNames = names;
        System.out.println("MemberAdapter: Setting " + names.size() + " member names");
        for (String name : names) {
            System.out.println("MemberAdapter: " + name);
        }
        notifyDataSetChanged();
    }

    public void setMembers(List<String> names, List<Integer> ids) {
        this.memberNames = names;
        this.memberIds = ids;
        System.out.println("MemberAdapter: Setting " + names.size() + " members");
        notifyDataSetChanged();
    }

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.listener = listener;
    }

    public interface OnMemberClickListener {
        void onMemberClick(int memberId, String memberName);
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_card, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String memberName = memberNames.get(position);
        System.out.println("MemberAdapter: Binding member " + position + " with name: " + memberName);
        holder.memberNameTextView.setText(memberName);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position < memberIds.size()) {
                listener.onMemberClick(memberIds.get(position), memberName);
            }
        });
    }

    @Override
    public int getItemCount() {
        System.out.println("MemberAdapter: getItemCount called, returning " + memberNames.size());
        return memberNames.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView memberNameTextView;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberNameTextView = itemView.findViewById(R.id.memberNameTextView);
        }
    }
} 