package com.example.eduquiz.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduquiz.R;
import com.example.eduquiz.models.Section;

import java.util.ArrayList;
import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {

    public interface OnSectionClickListener {
        void onSectionClick(Section section);
    }

    private final List<Section> sections = new ArrayList<>();
    private final OnSectionClickListener listener;

    public SectionAdapter(OnSectionClickListener listener) {
        this.listener = listener;
    }

    public void setSections(List<Section> newSections) {
        sections.clear();
        if (newSections != null) {
            sections.addAll(newSections);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Section section = sections.get(position);
        holder.nameText.setText(section.getName());
        holder.descText.setText(section.getDescription());
        holder.itemView.setOnClickListener(v -> listener.onSectionClick(section));
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView descText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.section_name);
            descText = itemView.findViewById(R.id.section_desc);
        }
    }
}
