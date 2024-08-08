package com.PKG.rs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

public class QuestionPaperAdapter extends RecyclerView.Adapter<QuestionPaperAdapter.ViewHolder> {
    private List<QuestionPaper> questionPapers;
    private Context context;

    public QuestionPaperAdapter(List<QuestionPaper> questionPapers, Context context) {
        this.questionPapers = questionPapers;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_paper, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuestionPaper questionPaper = questionPapers.get(position);
        holder.bind(questionPaper, context);
    }

    @Override
    public int getItemCount() {
        return questionPapers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView id,name;
        private LinearLayout carproduct;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.id1);
            name=itemView.findViewById(R.id.name1);
            carproduct = itemView.findViewById(R.id.cardproducr);
        }

        public void bind(final QuestionPaper questionPaper, final Context context) {
            id.setText(questionPaper.getId());
            name.setText(questionPaper.getName());
            carproduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("QUESTION_PAPER_NAME", questionPaper.getId());
                    intent.putExtra("ID", questionPaper.getId());
                    intent.putExtra("NAME", questionPaper.getName());
                    context.startActivity(intent);
                }
            });
        }
    }
}
